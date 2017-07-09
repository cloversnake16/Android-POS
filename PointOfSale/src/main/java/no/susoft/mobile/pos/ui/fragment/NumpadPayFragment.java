package no.susoft.mobile.pos.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import jp.co.casio.vx.framework.device.Drawer;
import jp.co.casio.vx.framework.device.Up400DrawerPort;
import jp.co.casio.vx.framework.device.lineprintertools.LinePrinterDeviceBase;
import jp.co.casio.vx.framework.device.lineprintertools.SerialUp400;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.data.Payment.PaymentType;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.ConnectionManager;
import no.susoft.mobile.pos.hardware.combi.Star_mPOP;
import no.susoft.mobile.pos.hardware.combi.mPOPPrintOrderSlim;
import no.susoft.mobile.pos.hardware.combi.mPOPPrintPrepaidSlim;
import no.susoft.mobile.pos.hardware.combi.mPOPPrintReturnsSlim;
import no.susoft.mobile.pos.hardware.printer.*;
import no.susoft.mobile.pos.hardware.terminal.CardTerminal;
import no.susoft.mobile.pos.hardware.terminal.CardTerminalFactory;
import no.susoft.mobile.pos.hardware.terminal.TerminalRequest;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalException;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalIllegalAmountException;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalInBankModeException;
import no.susoft.mobile.pos.hardware.terminal.exception.CardTerminalNotConnectedException;
import no.susoft.mobile.pos.network.Protocol.Message;
import no.susoft.mobile.pos.server.DbAPI;
import no.susoft.mobile.pos.server.GiftCardLoadAsync;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;
import no.susoft.mobile.pos.ui.activity.util.MainTopBarMenu;
import no.susoft.mobile.pos.ui.adapter.PaymentListAdapter;
import no.susoft.mobile.pos.ui.dialog.PayGiftCardDialog;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;
import no.susoft.mobile.pos.ui.fragment.utils.PaymentList;
import no.susoft.mobile.pos.ui.fragment.utils.PaymentNumpadLogic;
import no.susoft.mobile.pos.ui.fragment.utils.PaymentValues;
import no.susoft.mobile.pos.ui.utils.SimpleInfoAlertDialog;

public class NumpadPayFragment extends NumpadFragment {

    @InjectView(R.id.button0)
    Button btn0;
    @InjectView(R.id.button1)
    Button btn1;
    @InjectView(R.id.button5)
    Button btn2;
    @InjectView(R.id.button10)
    Button btn3;
    @InjectView(R.id.button20)
    Button btn4;
    @InjectView(R.id.button50)
    Button btn5;
    @InjectView(R.id.button100)
    Button btn6;
    @InjectView(R.id.button200)
    Button btn7;
    @InjectView(R.id.button500)
    Button btn8;
    @InjectView(R.id.button1000)
    Button btn9;

    @InjectView(R.id.buttonBack)
    ImageView btnBack;
    @InjectView(R.id.buttonEnter)
    ImageView btnEnter;
    @InjectView(R.id.buttonC)
    Button btnC;
    @InjectView(R.id.buttonDot)
    Button btnDot;
    @InjectView(R.id.payment_done)
    Button paymentDone;
    @InjectView(R.id.buttonOption0)
    ToggleButton btnNumValToggle;
    @InjectView(R.id.btnCash)
    ToggleButton btnCash;
    @InjectView(R.id.btnOption3)
    ToggleButton btnCard;
    @InjectView(R.id.btnGiftCard)
    ToggleButton btnGiftCard;
    @InjectView(R.id.btnInvoice)
    ToggleButton btnInvoice;

    @InjectView(R.id.numpad_edit_text)
    EditText inputField;
    @InjectView(R.id.changeText)
    TextView changeText;
    @InjectView(R.id.to_pay_text)
    TextView toPayOrChange;
    @InjectView(R.id.payments_list)
    ListView paymentListview;
    @InjectView(R.id.cashRoundAmountText)
    TextView cashRoundAmountText;

    private Dialog paymentsDialog;
    private ToggleButton toggledButton;
    private PaymentListAdapter paymentListAdapter;
    private PaymentValues paymentValues = new PaymentValues();
    private PaymentList paymentList = new PaymentList(paymentValues);
    private Order paymentOrderDoNotUseForLogic;
    private PaymentNumpadLogic numpad;
    private int giftCardNumberLength = 5;
    private boolean isReturnState = false;
    private OrderLine roundingLine = new OrderLine();

    public NumpadPayFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.getInstance().setNumpadPayFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.numpad_pay_fragment, container, false);
        ButterKnife.inject(this, rootView);

        numpad = new PaymentNumpadLogic(btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btnNumValToggle, btnEnter, btnBack, btnC, btnDot, inputField);
        setToggleButtonPaymentTypeTags();
        setListeners();
        setToggledPaymentType(Payment.PaymentType.CASH);
        setupChangeText();
        if (!MainActivity.getInstance().isConnected()) {
            setOfflineMode();
        }

        return rootView;
    }

    private void setToggleButtonPaymentTypeTags() {
        btnCash.setTag(Payment.PaymentType.CASH);
        btnCard.setTag(Payment.PaymentType.CARD);
        btnGiftCard.setTag(Payment.PaymentType.GIFT_CARD);
        btnInvoice.setTag(Payment.PaymentType.INVOICE);
    }

    public void setOnlineMode() {
        if (btnGiftCard != null) {
            btnGiftCard.setEnabled(true);
            btnInvoice.setEnabled(true);
        }
    }

    public void setOfflineMode() {
        if (btnGiftCard != null) {
            btnGiftCard.setEnabled(false);
            btnInvoice.setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().getServerCallMethods().loadCartRoundingProduct(Order.roundId, Decimal.ZERO, "pay");
        refreshView();
    }

    public void setToggledButton(ToggleButton toggledButton) {
        this.toggledButton = toggledButton;
    }

    // VIEW ============================================================================================================

    private void resumeToggledPaymentTypeButton() {
        if (toggledButton == null) {
            setToggledPaymentType(Payment.PaymentType.CASH);
        } else {
            setToggledPaymentType((Payment.PaymentType) toggledButton.getTag());
        }
    }

    private void checkForReturnState() {
        if (Cart.INSTANCE.hasActiveOrder()) {
            if ((Cart.INSTANCE.getOrder().hasReturnLines() && paymentValues.orderAmountIsNegative())
                    || (paymentList.hasGiftCard() && (getChange().isZero() || getChange().isNegative()))) {
                toggleDefaultReturnPaymentType();
                //btnCard.setEnabled(false);
                btnInvoice.setEnabled(false);
                btnGiftCard.setTextOn(getString(R.string.credit_voucher));
                btnGiftCard.setTextOff(getString(R.string.credit_voucher));
                isReturnState = true;
            } else {
                btnCard.setEnabled(true);
                btnInvoice.setEnabled(true);
                btnGiftCard.setTextOn(getText(R.string.gift_card));
                btnGiftCard.setTextOff(getText(R.string.gift_card));
                isReturnState = false;
            }
        } else {
            MainTopBarMenu.getInstance().toggleLastUsedView();
        }
    }

    private void toggleDefaultReturnPaymentType() {
        checkPaymentTypeToggleButton(btnGiftCard);
    }

    public void refreshView() {
        refreshPayments();
        MainActivity.getInstance().getCartFragment().getCartButtons().refreshCartButtonStates();
        if (MainActivity.getInstance().isConnected()) {
            setOnlineMode();
        } else {
            setOfflineMode();
        }
        checkForReturnState();
        resumeToggledPaymentTypeButton();
        numpad.focusOnInputField();
        checkForEnterButtonFinalizingOrder();
    }

    //LISTENERS ========================================================================================================

    private void setListeners() {
        setPaymentTypeListeners();
        setBtnNumValToggleListener();
        setPaymentDoneListeners();
        setInputFieldChangeListener();
    }

    private void setPaymentTypeListeners() {

        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                paymentTypeChanged(compoundButton, b);
            }
        };

        btnCash.setOnCheckedChangeListener(listener);
        btnCard.setOnCheckedChangeListener(listener);
        btnGiftCard.setOnCheckedChangeListener(listener);
        btnInvoice.setOnCheckedChangeListener(listener);
    }

    private void setBtnNumValToggleListener() {
        btnNumValToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean toggled) {
                doNumpadTypeChanged(toggled);
            }
        });
    }

    private void setPaymentDoneListeners() {
        paymentDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalisePaymentOrder();
            }
        });
    }

    private void setInputFieldChangeListener() {
        inputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkForEnterButtonFinalizingOrder();
            }
        });
    }

    //LISTENER clicks ==================================================================================================

    private void doNumpadTypeChanged(boolean toggled) {
        if (toggled) {
            numpad.setNumpadToPresetValues();
        } else {
            numpad.setNumpadToNumbers();
        }
    }

    public void deletePayment(Payment item) {
        paymentList.getPaymentList().remove(item);
        MainActivity.getInstance().getNumpadPayFragment().refreshPayments();
        MainActivity.getInstance().getCartFragment().getCartButtons().refreshCartButtonStates();
    }

    public void doEnterClickInMainActivity() {
        try {
            numpad.doEnterClickFromScanner();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkForEnterButtonFinalizingOrder() {
        try {

            if (isReturnState) {
                setBtnEnterGreen();
                return;
            }

            if (inputFieldHasText() && !inputField.getText().toString().substring(0,1).equals("%")) {
                if ((getRemainingPositiveAmountToPay().subtract(Decimal.make(inputField.getText().toString())).isZero()
                        || getRemainingPositiveAmountToPay().subtract(Decimal.make(inputField.getText().toString())).isNegative())
                        && !paymentList.hasPaymentOfType(getToggledButtonPaymentType())
                        ) {

                    setBtnEnterGreen();

                } else {
                    setBtnEnterNormal();
                }
            } else if (!btnGiftCard.isChecked()) {
                setBtnEnterGreen();
            } else if (getChange().isZero() || getChange().isNegative()) {
                setBtnEnterGreen();
            } else {
                setBtnEnterNormal();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showCashRoundPayment(boolean show) {
        if(show) {
            cashRoundAmountText.setVisibility(View.VISIBLE);
        } else {
            cashRoundAmountText.setVisibility(View.INVISIBLE);
        }
    }

    private void roundCashPayment() {
		Decimal cartAmount = Cart.INSTANCE.getOrder().getAmount(true);
		if (!getRoundedOrderAmount().isEqual(cartAmount)) {
		
			roundingLine.setPrice(getRoundedOrderAmount().subtract(cartAmount));
			roundingLine.setQuantity(Decimal.ONE);

			if (!Cart.INSTANCE.getOrder().getAmount(true).isEqual(Cart.INSTANCE.getOrder().getAmountWithoutRoundingLine(true)) || Cart.INSTANCE.getOrder().getLineWithProductId(Order.roundId) != null) {
			
				OrderLine existingRoundingLine = Cart.INSTANCE.getOrder().getLineWithProductId(Order.roundId);
				Decimal newRoundingAmount = existingRoundingLine.getPrice().add(roundingLine.getPrice());
			
				existingRoundingLine.getProduct().setPrice(newRoundingAmount);
				existingRoundingLine.setPrice(newRoundingAmount);
				existingRoundingLine.setQuantity(Decimal.ONE);
			
			} else {
				Cart.INSTANCE.addOrderLine(roundingLine.getProduct());
				Cart.INSTANCE.getOrder().getLineWithProduct(roundingLine.getProduct()).setPrice(roundingLine.getPrice());
				Cart.INSTANCE.getOrder().getLineWithProduct(roundingLine.getProduct()).getProduct().setPrice(roundingLine.getPrice());
			}
		
			MainActivity.getInstance().getCartFragment().refreshCart();
			paymentValues.refreshPaymentValues(paymentList.getPaymentList());
		}
	}


    private boolean inputFieldHasText() {
        return inputField.getText().toString().length() > 0;
    }

    private void setBtnEnterNormal() {
        btnEnter.setBackgroundColor(Color.parseColor("#CCCCCC"));
    }

    private void setBtnEnterGreen() {
        btnEnter.setBackgroundColor(getResources().getColor(R.color.green_pay_color));
    }

    public Payment.PaymentType getToggledButtonPaymentType() {
        if(getToggledButton() != null)
            return (Payment.PaymentType)getToggledButton().getTag();

        return null;
    }

    public boolean hasPayments() {
        return (paymentList.getPaymentList().size() > 0);
    }

    public void focusOnInputField() {
        numpad.focusOnInputField();
    }

    public void dispatchKeyToInputField(KeyEvent event) {
        inputField.dispatchKeyEvent(event);
    }

    public void setRoundingProduct(Product product) {
        roundingLine = new OrderLine();
        roundingLine.setOrderId(getId());
        roundingLine.setShopId(Cart.INSTANCE.getOrder().getShopId());
        roundingLine.setProduct(product);
        roundingLine.setText(product.getName());
        roundingLine.setPrice(Cart.INSTANCE.getProductPrice(product));
        roundingLine.setQuantity(Decimal.ONE);
    }

    public void hideDoneButton() {
        setPaymentDoneButtonState(false);
    }

    //TOGGLE BUTTONS ===================================================================================================

    private enum NumpadType {
        PRESET_VALUES, NUMBERS
    }

    private ToggleButton getToggledButton() {
        return toggledButton;
    }

    private void paymentTypeChanged(CompoundButton button, Boolean b) {
        if (b) {
            setToggledPaymentType((Payment.PaymentType) button.getTag());
            button.setClickable(false);
        } else {
            button.setChecked(false);
            button.setClickable(true);
        }
    }

    private void setToggledPaymentType(Payment.PaymentType paymentType) {
        ToggleButton button;
        switch (paymentType) {
            case CASH:
                button = btnCash;
                showCashRoundPayment(true);
                //roundCashPayment();
                break;
            case CARD:
                button = btnCard;
                showCashRoundPayment(false);
                break;
            case GIFT_CARD:
                button = btnGiftCard;
                showCashRoundPayment(false);
                break;
            case INVOICE:
                button = btnInvoice;
                showCashRoundPayment(false);
                showMorePaymentsDialog();
                break;
            default:
                return;
        }
        checkPaymentTypeToggleButton(button);
        updateNumpadType(button);
    }

    private void checkPaymentTypeToggleButton(ToggleButton button) {
        uncheckAllPaymentTypeButtons();
        setToggledButton(button);
        button.setChecked(true);
        checkForEnterButtonFinalizingOrder();
    }

    private void uncheckAllPaymentTypeButtons() {
        btnCash.setChecked(false);
        btnCard.setChecked(false);
        btnGiftCard.setChecked(false);
        btnInvoice.setChecked(false);
        setToggledButton(null);
    }
	
	protected void showMorePaymentsDialog() {

	}

    //NUMPAD ===========================================================================================================
    private void updateNumpadType(CompoundButton button) {
        if (button == btnCash) {
            setNumpadToType(NumpadType.PRESET_VALUES);
        } else {
            setNumpadToType(NumpadType.NUMBERS);
        }
    }

    private void setNumpadToType(NumpadType type) {
        switch (type) {
            case PRESET_VALUES:
                btnNumValToggle.setChecked(true);
                break;
            case NUMBERS:
                btnNumValToggle.setChecked(false);
                break;
        }
    }

    //VISUALS ==========================================================================================================
    public void clearPayments() {
        if (paymentList != null) {
            paymentList.clearPaymentList();
        }
        refreshPaymentAdapter();

        if (getView() != null) {
            getView().invalidate();
        }
    }

    public void refreshPayments() {
		paymentList.removeNullAmountPayments();
		paymentValues.refreshPaymentValues(paymentList.getPaymentList());
		refreshPaymentAdapter();
	
		if (Cart.INSTANCE.getOrder() != null && Cart.INSTANCE.getOrder().hasLines()) {
			checkForFullPayment();
		}
	}

    public void resetPayments() {
        paymentValues.setDecimalsToZero();
        setPaymentDoneButtonState(false);
        setupChangeText();
        paymentList.clearPaymentList();
        clearPaymentAdapter();
    }

    private void refreshPaymentAdapter() {
        if (paymentListview != null) {
            if (paymentListview.getAdapter() == null || paymentListAdapter == null) {
                paymentListAdapter = new PaymentListAdapter(MainActivity.getInstance(), 0, paymentList.getPaymentList());
                paymentListview.setAdapter(paymentListAdapter);
            } else if (paymentListview.getAdapter() != null && paymentListAdapter != null) {
                paymentListAdapter.notifyDataSetChanged();
            }
            paymentListview.smoothScrollToPosition(paymentListAdapter.getCount() - 1);
        }
    }

    private void clearPaymentAdapter() {
        paymentListAdapter.clear();
        paymentListAdapter.notifyDataSetChanged();
    }

    public void setChangeVisualsToEnoughPaid(boolean enoughPaid) {
        updateChangeStatus(enoughPaid);
        setPaymentDoneButtonState(enoughPaid);
    }

    private void setupChangeText() {
        paymentValues.setChangeValueToOrderAmountWithDiscount();
        paymentValues.refreshChange();
        changeText.setText(paymentValues.getChange().toString());
        cashRoundAmountText.setText(getString(R.string.cash) + ": " + String.valueOf(Math.round(Float.valueOf(paymentValues.getChange().toString()))));
    }

    private void updateChangeStatus(boolean isPaid) {
        changeText.setText(paymentValues.getChange().toString());
        cashRoundAmountText.setText(getString(R.string.cash) + ": " +String.valueOf(Math.round(Float.valueOf(paymentValues.getChange().toString()))));
        checkForReturnState();
        if (Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().hasReturnLines()) {
            if (isPaid) {
                toPayOrChange.setText(getString(R.string.return_amount));
                changeText.setTextColor(getResources().getColor(R.color.btn_important_normal));
            } else {
                toPayOrChange.setText(getString(R.string.left_to_pay));
                changeText.setTextColor(Color.RED);
            }
        } else {
            if (isPaid) {
                toPayOrChange.setText(getString(R.string.change));
                changeText.setTextColor(getResources().getColor(R.color.btn_important_normal));
            } else {
                toPayOrChange.setText(getString(R.string.left_to_pay));
                changeText.setTextColor(Color.RED);
            }
        }
    }

    private void setPaymentDoneButtonState(boolean show) {
        if (paymentDone != null) {
            if (show) {
                paymentDone.setEnabled(true);
                paymentDone.setVisibility(View.VISIBLE);
            } else {
                paymentDone.setEnabled(false);
                paymentDone.setVisibility(View.GONE);
            }
        }
    }

    //PAYMENT ==========================================================================================================

    public Decimal getChange() {
        return paymentValues.getChange();
    }

	public void handlePaymentOnEnter(boolean isFromNumpad) {
		if (Cart.INSTANCE.getOrder() != null && Cart.INSTANCE.getOrder().getLines() != null && Cart.INSTANCE.getOrder().getLines().size() > 0) {
            if (isReturnState && paymentValues.getTendered().isGreater(Cart.INSTANCE.getOrder().getAmount(true)) || paymentValues.getTendered().isEqual(Cart.INSTANCE.getOrder().getAmount(true))) {
                if (toggledButton.equals(btnCard)) {
                    processReturnOfGoodsOnCard(paymentValues.getChange());
                } else {
                    paymentDone.callOnClick();
                }
				return;
			}

			if (toggledButton.equals(btnGiftCard) || !isFromNumpad) {
                String giftCardNumber = numpad.getInputFieldText();
				if (checkForGiftCardInInput() != null) {
					giftCardNumber = checkForGiftCardInInput();
				}

				if (giftCardNumber != null && giftCardNumber.length() > 0) {
					String shopId = AccountManager.INSTANCE.getAccount().getShop().getID();
					if (giftCardNumber.length() > 3) {
						shopId = giftCardNumber.substring(0, 3);
					}
					if (giftCardNumber.length() <= giftCardNumberLength) {
						try {
							int number = Integer.valueOf(giftCardNumber);
							String formatted = String.format("%0" + giftCardNumberLength + "d", number);
							giftCardNumber = shopId + formatted;
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					payWithGiftCard(giftCardNumber);
				} else {
					makeInvalidGiftCardToast();
				}

			} else {
                String giftCardNumber = checkForGiftCardInInput();
				if (giftCardNumber != null) {
					payWithGiftCard(giftCardNumber);
				} else {
                    Decimal amount = getPaymentAmount();
					if (amount != null && getToggledButtonPaymentType() != null) {
						addNumberToPayment(amount);
					}
				}
			}
			
            refreshPayments();
		} else {
            ErrorReporter.INSTANCE.filelog("handlePaymentOnEnter", "no lines in cart.");
		}
	}

    private Decimal getRoundedOrderAmount() {
        return Decimal.make(Math.round(Float.valueOf(Cart.INSTANCE.getOrder().getAmount(true).toString())));
    }

    private void checkForFullPayment() {
        checkForRoundCashPayment();
		if (!isReturnState && paymentValues.getTendered().isGreater(Cart.INSTANCE.getOrder().getAmount(true)) || paymentValues.getTendered().isEqual(Cart.INSTANCE.getOrder().getAmount(true))) {
            paymentDone.callOnClick();
        }
    }
	
	private void checkForRoundCashPayment() {
		if (paymentList.hasOnlyPaymentOfType(Payment.PaymentType.CASH)
				&& !getRoundedOrderAmount().isEqual(Cart.INSTANCE.getOrder().getAmount(true))
				&& getRoundedOrderAmount().isEqual(paymentValues.getTendered())) {
			roundCashPayment();
		} else if (paymentList.hasPaymentOfType(Payment.PaymentType.CASH)
				&& (getRoundedOrderAmount().isEqual(paymentValues.getTendered()) || getRoundedOrderAmount().isLess(paymentValues.getTendered()))) {
			roundCashPayment();
		}
	}

    private void payWithGiftCard(String giftCardNumber) {
        if (hasAlreadyUsedGiftCard(giftCardNumber)) {
            makeGiftCardAlreadyAddedToast();

        } else if (verifyGiftCardNumber()) {
            new GiftCardLoadAsync().execute(giftCardNumber);
        } else {
            makeInvalidGiftCardToast();
        }
    }

    private String checkForGiftCardInInput() {
        String giftCardNumber = numpad.getInputFieldText();
        if (giftCardNumber.length() >= 3 && (giftCardNumber.substring(0, 3).equals("%G%") || giftCardNumber.substring(0, 3).equals("1G1"))) {
            return giftCardNumber.substring(3, giftCardNumber.length());
        } else {
            return null;
        }
    }

    private boolean hasAlreadyUsedGiftCard(String giftCardNumber) {
        for (Payment p : paymentList.getPaymentList()) {
            if (p.getNumber().equals(giftCardNumber)) {
                return true;
            }
        }
        return false;
    }

    private boolean verifyGiftCardNumber() {
        //TODO
        return true;
    }

    public Decimal getRemainingPositiveAmountToPay() {
        if (paymentValues.getRemainingPositiveAmountToPay() != null) {
            return paymentValues.getRemainingPositiveAmountToPay();
        } else {
            return Decimal.ZERO;
        }
    }

    private Decimal getPaymentAmount() {
        if (numpad.inputFieldHasText()) {
            try {
                return Decimal.make(numpad.getInputFieldText());
            } catch (NumberFormatException nfex) {
                nfex.printStackTrace();
                makeInvalidInputToast();
            }
        } else if (paymentValues.getRemainingPositiveAmountToPay() != null) {
            return paymentValues.getRemainingPositiveAmountToPay();
        }
        return null;
    }

    public void handlePaymentOfAmount(Decimal amount) {
        addNumberToPayment(amount);
        checkForEnterButtonFinalizingOrder();
    }

    private void processReturnOfGoodsOnCard(Decimal amount) {

        ErrorReporter.INSTANCE.filelog("processReturnOfGoodsOnCard", "amount=" + amount.toString());

        try {

            final CardTerminal cardTerminal = CardTerminalFactory.getInstance().getCardTerminal();
            final Decimal remaining = getRemainingPositiveAmountToPay();

            if (cardTerminal != null) {

                ErrorReporter.INSTANCE.filelog("processReturnOfGoodsOnCard", "showBankTerminalDialog \"Connecting to card terminal\"");
                MainActivity.getInstance().showBankTerminalDialog(); // if it is not already

                final TerminalRequest terminalRequest;
                ErrorReporter.INSTANCE.filelog("processReturnOfGoodsOnCard", "isReturnState = " + isReturnState);

                // region prepare terminal req
                // return of goods
                terminalRequest = new TerminalRequest().setOperID("0000") // todo operatorId ?
                        .setTransferType(TerminalRequest.TRANSFER_TYPE_RETURN_OF_GOODS)
                        .setTotalAmount(amount.abs().multiply(Decimal.HUNDRED).toInteger()) // total amount to charge from card
                        .setTotalPurchaseAmount(0) // amount of purchase
                        .setVatAmount(0);

                ErrorReporter.INSTANCE.filelog("processReturnOfGoodsOnCard", "TerminalRequest = " + terminalRequest.toString());

                // endregion

                // region terminal request thread
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            int count = 0;
                            while (MainActivity.getInstance().getBankTerminalDialog() == null && count < 10) {
                                sleep(500);
                                count++;
                            }

                            if (MainActivity.getInstance().getBankTerminalDialog() != null) {
                                MainActivity.getInstance().getBankTerminalDialog().setPaymentAmount(remaining);
                                MainActivity.getInstance().addLineToBankTerminalDialog("CONNECTING TO CARD TERMINAL");

                                // region Checking terminal is connected
                                ErrorReporter.INSTANCE.filelog("addNumberToPayment", "Checking terminal is connected");
                                count = 0;
                                while (!cardTerminal.isConnected() && count < 3) {
                                    try {
                                        MainActivity.getInstance().addLineToBankTerminalDialog("CONNECTING...");
                                        ErrorReporter.INSTANCE.filelog("addNumberToPayment", "Checking terminal is connected... reconnecting attempt");

                                        cardTerminal.testConnectionAndReconnectIfLost("from addNumberToPayment()");
                                    } catch (CardTerminalException e) {
                                        e.printStackTrace();
                                    }

                                    sleep(500);
                                    count++;
                                }
                                // endregion

                                // region Checking terminal is not in bank mode
                                ErrorReporter.INSTANCE.filelog("addNumberToPayment", "Checking terminal is not in bank mode");

                                count = 0;
                                while (cardTerminal.isInBankMode() && count < 3) {
                                    MainActivity.getInstance().addLineToBankTerminalDialog("WAITING FOR TERMINAL GO BACK FROM BANK MODE...");
                                    ErrorReporter.INSTANCE.filelog("addNumberToPayment", "Waiting for terminal go back from Bank mode...");
                                    sleep(500);
                                    count++;
                                }
                                // endregion

                                if (!cardTerminal.isConnected()) {
                                    MainActivity.getInstance().addLineToBankTerminalDialog("CARD TERMINAL NOT CONNECTED.");

                                    // todo can be manual card ?
                                } else if (cardTerminal.isInBankMode()) {
                                    MainActivity.getInstance().addLineToBankTerminalDialog("CARD TERMINAL IS IN BANK MODE.");

                                } else {
                                    ErrorReporter.INSTANCE.filelog("processReturnOfGoodsOnCard", "Running returnOfGoods");

                                    try {
                                        cardTerminal.returnOfGoods(terminalRequest);
                                    } catch (CardTerminalInBankModeException e) {
                                        MainActivity.getInstance().addLineToBankTerminalDialog("CARD TERMINAL IS IN BANK MODE.");
                                        ErrorReporter.INSTANCE.filelog("processReturnOfGoodsOnCard", "exception ", e);

                                    } catch (CardTerminalNotConnectedException e) {
                                        MainActivity.getInstance().addLineToBankTerminalDialog("CARD TERMINAL NOT CONNECTED.");
                                        ErrorReporter.INSTANCE.filelog("processReturnOfGoodsOnCard", "exception ", e);

                                    } catch (CardTerminalIllegalAmountException e) {
                                        MainActivity.getInstance().addLineToBankTerminalDialog("ILLEGAL AMOUNT.");
                                        ErrorReporter.INSTANCE.filelog("processReturnOfGoodsOnCard", "exception ", e);

                                    } catch (CardTerminalException e) {
                                        MainActivity.getInstance().addLineToBankTerminalDialog("ERROR PROCESSING PAYMENT: " + e.getLocalizedMessage());
                                        ErrorReporter.INSTANCE.filelog("processReturnOfGoodsOnCard", "exception ", e);
                                    }
                                }
                            }
                        } catch (InterruptedException e) {
                            ErrorReporter.INSTANCE.filelog("processReturnOfGoodsOnCard", "exception ", e);
                        }
                    }
                }.start();

                // endregion
            } else {
                paymentList.addCardPayment("", amount, 4, getContext().getString(R.string.card), 0);
            }
        } catch (Exception ex) {
            ErrorReporter.INSTANCE.filelog("processReturnOfGoodsOnCard", "No connection to card terminal", ex);
        }
    }

    public boolean isReturnState() {
        return isReturnState;
    }

    private void addNumberToPayment(final Decimal amount) {
        try {
            Payment.PaymentType paymentType = getPaymentTypeFromToggleButton();
            if (paymentType != null) {
                switch (paymentType) {
                    case CARD:
                        try {
							final CardTerminal cardTerminal = CardTerminalFactory.getInstance().getCardTerminal();
							final Decimal remaining = getRemainingPositiveAmountToPay();

							if (cardTerminal != null) {

								ErrorReporter.INSTANCE.filelog("addNumberToPayment", "showBankTerminalDialog \"Connecting to card terminal\"");
								MainActivity.getInstance().showBankTerminalDialog(); // if it is not already

								final TerminalRequest terminalRequest;
                                ErrorReporter.INSTANCE.filelog("addNumberToPayment", "isReturnState = " + isReturnState);

								// region prepare terminal req
								// if charging more than remaining
								if (amount.isGreater(remaining)) {
                                    // with cashback
                                    terminalRequest = new TerminalRequest().setOperID("0000") // todo operatorId ?
                                            .setTransferType(TerminalRequest.TRANSFER_TYPE_PURCHASE_WITH_CASH_BACK).setTotalAmount(amount.multiply(Decimal.HUNDRED).toInteger()) // total amount to charge from card
                                            .setTotalPurchaseAmount(remaining.multiply(Decimal.HUNDRED).toInteger()) // amount of purchase
                                            .setVatAmount(0);

                                } else {
									terminalRequest = new TerminalRequest().setOperID("0000") // todo operatorId ?
														  .setTransferType(TerminalRequest.TRANSFER_TYPE_PURCHASE).setTotalAmount(amount.multiply(Decimal.HUNDRED).toInteger()).setVatAmount(0);
								}
								// endregion

								// region terminal request thread
								new Thread() {
									@Override
									public void run() {
										try {
											int count = 0;
											while (MainActivity.getInstance().getBankTerminalDialog() == null && count < 10) {
												sleep(500);
												count++;
											}

											if (MainActivity.getInstance().getBankTerminalDialog() != null) {
												MainActivity.getInstance().getBankTerminalDialog().setPaymentAmount(remaining);
												MainActivity.getInstance().addLineToBankTerminalDialog("CONNECTING TO CARD TERMINAL");

												// region Checking terminal is connected
												ErrorReporter.INSTANCE.filelog("addNumberToPayment", "Checking terminal is connected");
												count = 0;
												while (!cardTerminal.isConnected() && count < 3) {
													try {
														MainActivity.getInstance().addLineToBankTerminalDialog("CONNECTING...");
														ErrorReporter.INSTANCE.filelog("addNumberToPayment", "Checking terminal is connected... reconnecting attempt");

														cardTerminal.testConnectionAndReconnectIfLost("from addNumberToPayment()");
													} catch (CardTerminalException e) {
														e.printStackTrace();
													}

													sleep(500);
													count++;
												}
												// endregion

												// region Checking terminal is not in bank mode
												ErrorReporter.INSTANCE.filelog("addNumberToPayment", "Checking terminal is not in bank mode");

												count = 0;
												while (cardTerminal.isInBankMode() && count < 3) {
													MainActivity.getInstance().addLineToBankTerminalDialog("WAITING FOR TERMINAL GO BACK FROM BANK MODE...");
													ErrorReporter.INSTANCE.filelog("addNumberToPayment", "Waiting for terminal go back from Bank mode...");
													sleep(500);
													count++;
												}
												// endregion

												if (!cardTerminal.isConnected()) {
													MainActivity.getInstance().addLineToBankTerminalDialog("CARD TERMINAL NOT CONNECTED.");

													// todo can be manual card ?
												} else if (cardTerminal.isInBankMode()) {
													MainActivity.getInstance().addLineToBankTerminalDialog("CARD TERMINAL IS IN BANK MODE.");

												} else {
													ErrorReporter.INSTANCE.filelog("addNumberToPayment", "Running purchase");

													try {

                                                        cardTerminal.purchase(terminalRequest);
													} catch (CardTerminalInBankModeException e) {
														MainActivity.getInstance().addLineToBankTerminalDialog("CARD TERMINAL IS IN BANK MODE.");
														ErrorReporter.INSTANCE.filelog("addNumberToPayment", "exception ", e);

													} catch (CardTerminalNotConnectedException e) {
														MainActivity.getInstance().addLineToBankTerminalDialog("CARD TERMINAL NOT CONNECTED.");
														ErrorReporter.INSTANCE.filelog("addNumberToPayment", "exception ", e);

													} catch (CardTerminalIllegalAmountException e) {
														MainActivity.getInstance().addLineToBankTerminalDialog("ILLEGAL AMOUNT.");
														ErrorReporter.INSTANCE.filelog("addNumberToPayment", "exception ", e);

													} catch (CardTerminalException e) {
														MainActivity.getInstance().addLineToBankTerminalDialog("ERROR PROCESSING PAYMENT: " + e.getLocalizedMessage());
														ErrorReporter.INSTANCE.filelog("addNumberToPayment", "exception ", e);
													}
												}
											}
										} catch (InterruptedException e) {
											ErrorReporter.INSTANCE.filelog("addNumberToPayment", "exception ", e);
										}
									}
								}.start();

								// endregion
							} else {
								paymentList.addCardPayment("", amount, 4, getContext().getString(R.string.card), 0);
							}
						} catch (Exception ex) {
							ErrorReporter.INSTANCE.filelog("addCardPayment", "No connection to card terminal", ex);
						}
						break;

					default: {
						paymentList.addPayment(paymentType, "", amount);
						break;
                    }
                }
            }

            refreshPayments();

        } catch (Exception ex) {
            ErrorReporter.INSTANCE.filelog("addNumberToPayment", "Error!", ex);
            makeInvalidInputToast();
        }
    }

    public PaymentList getPaymentList() {
        return paymentList;
    }

    private Payment.PaymentType getPaymentTypeFromToggleButton() {
        if (getToggledButton().equals(btnCash)) {
            return Payment.PaymentType.CASH;
        } else if (getToggledButton().equals(btnCard)) {
            return Payment.PaymentType.CARD;
        } else if (getToggledButton().equals(btnGiftCard)) {
            return Payment.PaymentType.GIFT_CARD;
        } else if (getToggledButton().equals(btnInvoice)) {
            return Payment.PaymentType.INVOICE;
        }
        return null;
    }

    public void showPayWithGiftCardDialog(Prepaid p) {
        if (p != null) {
            PayGiftCardDialog giftCardDialog = new PayGiftCardDialog();
            giftCardDialog.setPrepaid(p);
            giftCardDialog.show(MainActivity.getInstance().getSupportFragmentManager(), "PayGiftCardDialog");
        } else {
            Toast.makeText(MainActivity.getInstance(), R.string.foundNoMatches, Toast.LENGTH_SHORT).show();
        }
    }

    public void addGiftCardPayment(String number, Decimal amount) {
        Log.i("vilde", "gift card number " + number);
        paymentList.addPayment(Payment.PaymentType.GIFT_CARD, number, amount);
        refreshPayments();
    }

    //PAYMENT SERVER ===================================================================================================
    ProgressDialog loadingDialog;

    public void paymentPostExecute(OrderPaymentResponse result) {
        dismissLoadingDialog();
        if (result != null && result.getMessage() == Message.OK) {
			try {
				Order order = result.getOrder();
				DbAPI.markOrderAsSent(order.getShopId(), order.getId());
				order.setId(order.getRemoteId());
			} catch (Exception e) {
				ErrorReporter.INSTANCE.filelog(e);
			}
		}
        postOrderProcess(result);
		isSent = false;
    }

    private void dismissLoadingDialog() {
        try {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static boolean isSent = false;
	
	public void finalisePaymentOrder() {
		if (!isSent) {
			isSent = true;
			loadingDialog = ProgressDialog.show(MainActivity.getInstance(), "", MainActivity.getInstance().getString(R.string.sending_order_to_server), true);
			
			checkForRoundCashPayment();
			Order order = Cart.INSTANCE.getOrder();
			
			if (order != null && order.hasLines()) {
				try {
					if (order.getPosNo() == null) {
						order.setPosNo(AccountManager.INSTANCE.getSavedPos().getId());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
	
				ArrayList<Payment> payments = handleChangeCreation(order);
				paymentListAdapter.notifyDataSetChanged();
				order.setPayments(payments);
				try {
					paymentOrderDoNotUseForLogic = order;
					sendPaymentOrderToServer();
				} catch (Exception ex) {
					ErrorReporter.INSTANCE.filelog(ex);
				}
			}
		}
    }

    private void sendPaymentOrderToServer() {
        if (paymentOrderDoNotUseForLogic.getPayments() != null && paymentOrderDoNotUseForLogic.getPayments().size() > 0) {
            paymentDone.setEnabled(false);
            MainActivity.getInstance().getServerCallMethods().sendPaymentOrderToServer(paymentOrderDoNotUseForLogic);
        } else if (paymentOrderDoNotUseForLogic.getPayments() != null && paymentOrderDoNotUseForLogic.getPayments().size() == 0 && paymentOrderDoNotUseForLogic.getAmount(true).isZero()) {
            paymentDone.setEnabled(false);
            MainActivity.getInstance().getServerCallMethods().sendPaymentOrderToServer(paymentOrderDoNotUseForLogic);
        } else {
            makeGenericPaymentErrorToast();
            dismissLoadingDialog();
        }
    }

    private ArrayList<Payment> handleChangeCreation(Order order) {
        ArrayList<Payment> result = new ArrayList<>();
        for (Payment payment : paymentList.getPaymentList()) {
            result.add(new Payment(payment));
        }

        if (!paymentValues.getChange().isZero()) {
            if (btnCash.isChecked()) {
                result = paymentList.handleCashChange(order);
            } else if (btnGiftCard.isChecked()) {
                paymentList.handleCreditVoucherChange(order);
                result = paymentList.getPaymentList();
            }
        }
        refreshPaymentAdapter();
        return result;
    }

    private void postOrderProcess(final OrderPaymentResponse response) {
        try {
            setPaymentDoneButtonState(false);

            //This needs to be before the payment list is cleared in the postexec class - aka before any async stuff
            ArrayList<Payment> newList = new ArrayList<>();
            for(Payment p : paymentList.getPaymentList()) {
                newList.add(new Payment(p));
            }
            response.getOrder().setPayments(newList);
            Cart.persistingOrders.setPreviousOrder(response.getOrder());

            final Handler handler = new Handler();
            final CartFragment cart = MainActivity.getInstance().getCartFragment();
            AlertDialog changeDialog = null;

            if (!paymentValues.getChange().isZero()) {
                changeDialog = displayChangeDialog(cart, handler);
            }

            openDrawer(response);
            doPrints(changeDialog, handler, response, cart);
            paymentList.clearPaymentList();

        } catch (Exception ex) {
            ErrorReporter.INSTANCE.filelog(ex);
        }
    }

    private void openDrawer(final OrderPaymentResponse response) {
		if (listHasPaymentOfType(response.getOrder().getPayments(), Payment.PaymentType.CASH) || !paymentValues.getChange().isZero()) {
			try {
				SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(PeripheralDevice.class.toString(), Context.MODE_PRIVATE);
				int cashdrawerProvider = preferences.getInt("CASHDRAWER_PROVIDER", PeripheralProvider.NONE.ordinal());
				if (cashdrawerProvider == PeripheralProvider.STAR.ordinal()) {
					ConnectionManager.getInstance().execute(new Runnable() {
						@Override
						public void run() {
							ErrorReporter.INSTANCE.filelog("mPOP openDrawer started..");
							Star_mPOP.openDrawer(MainActivity.getInstance());
							ErrorReporter.INSTANCE.filelog("mPOP openDrawer end");
						}
					});
				} else if (cashdrawerProvider == PeripheralProvider.CASIO.ordinal()) {
					if (CasioPrint.hasPrinterConnected()) {
						ConnectionManager.getInstance().execute(new Runnable() {
							@Override
							public void run() {
								int result = 0;
								int counter = 0;
								do {
									result = doCasioOpenDrawer();
									counter++;
								} while (result != 0 && counter < 20);
								if (counter == 20 && result != 0) {
									MainActivity.getInstance().showPrinterNotConnectedToast();
								}
							}
						});
					} else {
						doDirectOpenDrawer();
					}
				} else if (cashdrawerProvider == PeripheralProvider.BIXOLON.ordinal() && AppConfig.getState().getPrinterIp() != null && AppConfig.getState().getPrinterIp().length() > 0) {
					doBixolonCashdrawerIPOpen();
				} else if (cashdrawerProvider == PeripheralProvider.BIXOLON.ordinal()) {
					ConnectionManager.getInstance().execute(new Runnable() {
						@Override
						public void run() {
							doBixolonCashdrawerBluetoothOpen();
						}
					});
				}
			} catch (Exception ex) {
				ErrorReporter.INSTANCE.filelog("openCashDrawer", "ERROR Open Drawer ", ex);
			}
		}
	}

    private int doBixolonCashdrawerBluetoothOpen() {
        BluetoothPrintOrderWide bp = new BluetoothPrintOrderWide();
        return bp.openCashDrawer();
    }

    private void doBixolonCashdrawerIPOpen() {
		ConnectionManager.getInstance().execute(new BixolonPrinterJob() {
			@Override
			public int print() {
				IPPrintOrderWide ip = new IPPrintOrderWide();
				return ip.openCashDrawer();
			}
		});
    }

    private boolean listHasPaymentOfType(List<Payment> list, Payment.PaymentType type) {
        for(Payment p : list) {
            if(p.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    private void showNoPrinterDialog(AlertDialog changeDialog, final Handler handler) {
        final AlertDialog errorDialog = new SimpleInfoAlertDialog(MainActivity.getInstance().getResources().getString(R.string.no_printer_connected)).getAlertDialog();

        if (changeDialog != null) {
            final AlertDialog finalChangeDialog = changeDialog;
            errorDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeChangeDialogInMiliSeconds(handler, finalChangeDialog, 3000);
                    errorDialog.cancel();
                }
            });
        }
    }

    private void doDirectOpenDrawer() {
        try {
            //RegIODrawerPort drawerPort = new RegIODrawerPort(Drawer.OPENMODE_COMMON, Drawer.DEVICE_HOST_LOCALHOST);
            Drawer drawer = new Drawer();
            int ret = drawer.open(Drawer.OPENMODE_COMMON, Drawer.DEVICE_HOST_LOCALHOST);
        } catch (Exception ex) {
            ErrorReporter.INSTANCE.filelog("Direct drawer exception -> ", "" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private int doCasioOpenDrawer() {
        SerialUp400 printerDevice;
		int ret = 0;
        try {
			CasioPrint.setBusy(true);
            printerDevice = new SerialUp400(
                    SerialUp400.Port.PORT_COM1,
                    SerialUp400.BaudRate.BAUDRATE_9600,
                    SerialUp400.BitLen.BITLEN_8,
                    SerialUp400.ParityBit.PARITYBIT_NON,
                    SerialUp400.StopBit.STOPBIT_1,
                    SerialUp400.FlowCntl.FLOWCNTL_NON);

            printerDevice.setMulticharMode(LinePrinterDeviceBase.MULTICHARMODE_JIS);

            Up400DrawerPort drawerPort = new Up400DrawerPort(printerDevice);
            Drawer drawer = new Drawer();
            ret = drawer.open(drawerPort);
            ret = drawer.open(Drawer.OPENMODE_COMMON, Drawer.DEVICE_HOST_LOCALHOST);
            ret = drawer.setOpen(Drawer.DEVICE_DRAWER_NO1);
            drawer.close();
			return ret;
        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("NavigationDrawerOptions", "Printer drawer error ", e);
        }
		return ret;
    }

    private void doPrints(final AlertDialog changeDialog, final Handler handler, final OrderPaymentResponse response, final CartFragment cart) {
		try {
			if (changeDialog != null) {
				closeChangeDialogInMiliSeconds(handler, changeDialog, 5000);
			}

			boolean isInvoice = false;
			if (response.getOrder() != null && response.getOrder().getPayments() != null && response.getOrder().getPayments().size() > 0) {
				for (Payment payment : response.getOrder().getPayments()) {
					if (payment.getType() == PaymentType.INVOICE) {
						isInvoice = true;
					}
				}
			}

			final boolean orderIsAnInvoice = isInvoice;

			int printerProvider = AppConfig.getState().getPrinterProviderOrdinal();
			if (printerProvider == PeripheralProvider.CASIO.ordinal()) {
				// region CASIO
				if (CasioPrint.hasPrinterConnected()) {
					ConnectionManager.getInstance().execute(new Runnable() {
						@Override
						public void run() {
							int result = 0;
							int counter = 0;
							do {
								CasioPrintOrder po = new CasioPrintOrder();
								result = po.print(response.getOrder(), ReceiptPrintType.ORIGINAL);
								counter++;
							} while (result != 0 && counter < 20);
							if (counter == 20 && result != 0) {
								MainActivity.getInstance().showPrinterNotConnectedToast();
							}
						}
					});
					if (isInvoice) {
						ConnectionManager.getInstance().execute(new Runnable() {
							@Override
							public void run() {
								int result = 0;
								int counter = 0;
								do {
									CasioPrintOrder po = new CasioPrintOrder();
									result = po.print(response.getOrder(), ReceiptPrintType.ORIGINAL);
									counter++;
								} while (result != 0 && counter < 20);
								if (counter == 20 && result != 0) {
									MainActivity.getInstance().showPrinterNotConnectedToast();
								}
							}
						});
					}
					if (response.getOrder().hasReturnLines()) {
						ConnectionManager.getInstance().execute(new Runnable() {
							@Override
							public void run() {
								int result = 0;
								int counter = 0;
								do {
									CasioPrintReturns po = new CasioPrintReturns(response.getOrder());
									result = po.print(response.getOrder(), ReceiptPrintType.ORIGINAL);
									counter++;
								} while (result != 0 && counter < 20);
								if (counter == 20 && result != 0) {
									MainActivity.getInstance().showPrinterNotConnectedToast();
								}
							}
						});
					}
					if (response.getGiftCards() != null && response.getGiftCards().size() > 0) {
						ConnectionManager.getInstance().execute(new Runnable() {
							@Override
							public void run() {
								int result = 0;
								int counter = 0;
								do {
									CasioPrintPrepaid pp = new CasioPrintPrepaid(response.getGiftCards());
									result = pp.print();
									counter++;
								} while (result != 0 && counter < 20);
								if (counter == 20 && result != 0) {
									MainActivity.getInstance().showPrinterNotConnectedToast();
								}
							}
						});
					}
				} else {
					showNoPrinterDialog(changeDialog, handler);
				}
				// endregion
			} else if (printerProvider == PeripheralProvider.BIXOLON.ordinal()) {
				// region BIXOLON
				if (AppConfig.getState().getPrinterIp().isEmpty()) {
					ConnectionManager.getInstance().execute(new Runnable() {
						@Override
						public void run() {
							BluetoothPrintOrderWide bp = new BluetoothPrintOrderWide(response, false);
							bp.print(bp.makeReceipt(response.getOrder()));
						}
					});
					if (isInvoice) {
						ConnectionManager.getInstance().execute(new Runnable() {
							@Override
							public void run() {
								BluetoothPrintOrderWide bp = new BluetoothPrintOrderWide(response, true);
								bp.print(bp.makeReceipt(response.getOrder()));
							}
						});
					}
					if (response.getGiftCards() != null && response.getGiftCards().size() > 0) {
						ConnectionManager.getInstance().execute(new Runnable() {
							@Override
							public void run() {
								for (Prepaid p : response.getGiftCards()) {
									new BluetoothPrintPrepaidWide(p);
								}
							}
						});
					}
				} else {
					ConnectionManager.getInstance().execute(new BixolonPrinterJob() {
						@Override
						public int print() {
							IPPrintOrderWide po = new IPPrintOrderWide(response, false);
							return po.printIP(response.getOrder(), AppConfig.getState().getPrinterName(), AppConfig.getState().getPrinterIp());
						}
					});
					if (orderIsAnInvoice) {
						ConnectionManager.getInstance().execute(new BixolonPrinterJob() {
							@Override
							public int print() {
								IPPrintOrderWide po = new IPPrintOrderWide(response, true);
								return po.printIP(response.getOrder(), AppConfig.getState().getPrinterName(), AppConfig.getState().getPrinterIp());
							}
						});
					}
					if (response.getOrder().hasReturnLines()) {
						ConnectionManager.getInstance().execute(new BixolonPrinterJob() {
							@Override
							public int print() {
								IPPrintReturns po = new IPPrintReturns(response.getOrder());
								return po.printIP(response.getOrder(), AppConfig.getState().getPrinterName(), AppConfig.getState().getPrinterIp());
							}
						});
					}
					if (response.getGiftCards() != null && response.getGiftCards().size() > 0) {
						for (final Prepaid p : response.getGiftCards()) {
							ConnectionManager.getInstance().execute(new BixolonPrinterJob() {
								@Override
								public int print() {
									IPPrintPrepaid po = new IPPrintPrepaid(p);
									return po.printIP(response.getOrder(), AppConfig.getState().getPrinterName(), AppConfig.getState().getPrinterIp());
								}
							});
						}
					}
				}
				// endregion
			} else if (printerProvider == PeripheralProvider.STAR.ordinal()) {
				// region STAR
				ConnectionManager.getInstance().execute(new Runnable() {
					@Override
					public void run() {
						new mPOPPrintOrderSlim(response.getOrder());
					}
				});
				if (isInvoice) {
					ConnectionManager.getInstance().execute(new Runnable() {
						@Override
						public void run() {
							new mPOPPrintOrderSlim(response.getOrder());
						}
					});
				}
				if(response.getOrder().hasReturnLines()) {
					ConnectionManager.getInstance().execute(new Runnable() {
						@Override
						public void run() {
							new mPOPPrintReturnsSlim(response.getOrder());
						}
					});
				}
				if(response.getGiftCards() != null && response.getGiftCards().size() > 0) {
					ConnectionManager.getInstance().execute(new Runnable() {
						@Override
						public void run() {
							new mPOPPrintPrepaidSlim(response.getGiftCards());
						}
					});
				}
				// endregion
			} else if (printerProvider == PeripheralProvider.VERIFONE.ordinal()) {
				//region VERIFONE
				Printer printer = PrinterFactory.getInstance().getPrinter();
				if (printer != null) {
					if (response.getGiftCards() != null && response.getGiftCards().size() > 0) {
						// joined print for receipt and giftcard
						printer.printPrepaidJoined(response.getOrder(), response.getGiftCards());
					} else {
						printer.printOrder(response.getOrder(), ReceiptPrintType.ORIGINAL);
						if (isInvoice) {
							printer.printOrder(response.getOrder(), ReceiptPrintType.ORIGINAL);
						}
						if (response.getOrder().hasReturnLines()) {
							printer.printReturn(response.getOrder());
						}
					}
				} else {
					showNoPrinterDialog(changeDialog, handler);
				}
				//endregion
			} else {
				showNoPrinterDialog(changeDialog, handler);
			}

			if (AppConfig.getState().isRestaurant()) {
				printOrderOnKitchen(response.getOrder());
			}

			if (changeDialog == null) {
				orderPaymentsCompleteTidyUp(cart);
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}
	}
	
    public void printOrderOnKitchen(final Order order) {
        if (order != null && !order.getLines().isEmpty()) {
			boolean kitchenPrint = false;
			boolean barPrint = false;
			for (OrderLine line : order.getLines()) {
				if (line.getProduct().getAbcCode().equals("K") && !line.getDeliveredQty().equals(line.getQuantity())) {
					kitchenPrint = true;
				}
				if (line.getProduct().getAbcCode().equals("B") && !line.getDeliveredQty().equals(line.getQuantity())) {
					barPrint = true;
				}
			}

			if (kitchenPrint && !AppConfig.getState().getKitchenPrinterIp().isEmpty()) {
				ConnectionManager.getInstance().execute(new BixolonPrinterJob(BixolonPrinterJob.KITCHEN_PRINTER) {
					@Override
					public int print() {
						IPPrintKitchen pk = new IPPrintKitchen();
						return pk.print(order, IPPrintKitchen.DESTINATION_KITCHEN);
					}
				});
			}

			if (barPrint && !AppConfig.getState().getBarPrinterIp().isEmpty()) {
				ConnectionManager.getInstance().execute(new BixolonPrinterJob(BixolonPrinterJob.BAR_PRINTER) {
					@Override
					public int print() {
						IPPrintKitchen pk = new IPPrintKitchen();
						return pk.print(order, IPPrintKitchen.DESTINATION_BAR);
					}
				});
			}

			if ((kitchenPrint && AppConfig.getState().getKitchenPrinterIp().isEmpty()) || barPrint && AppConfig.getState().getBarPrinterIp().isEmpty()) {
				MainActivity.getInstance().showPrinterNotConnectedToast();
			}
        }
    }

    private void paymentReturnedFailed() {

        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }

        paymentValues.refreshChange();
        paymentDone.setEnabled(true);
        refreshPayments();
        paymentListAdapter.notifyDataSetChanged();

        new AlertDialog.Builder(MainActivity.getInstance()).setTitle("Error").setMessage("A error has occured.\nRetry, or cancel and return to the previous screen.").setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                sendPaymentOrderToServer();
                dialog.dismiss();
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    private void closeChangeDialogInMiliSeconds(final Handler handler, final AlertDialog changeDialog, int i) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (changeDialog != null && changeDialog.isShowing()) {
                    changeDialog.getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();
                }
            }
        }, i);
    }

    private AlertDialog displayChangeDialog(final CartFragment cart, final Handler handler) {

        final AlertDialog dialog = new Builder(MainActivity.getInstance())
                .setTitle(getString(R.string.change))
                .setMessage(getString(R.string.change_to_pay) + " " + paymentValues.getChange().toString()).setPositiveButton(android.R.string.ok, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        handler.removeCallbacksAndMessages(null);

                        try {
                            if(Cart.INSTANCE.getSplitOrders() != null) {
                                Cart.INSTANCE.getSplitOrders().remove(Cart.INSTANCE.getOrder());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
	
						System.out.println("displayChangeDialog..");
	
						orderPaymentsCompleteTidyUp(cart);

                    }
                }).setIcon(android.R.drawable.ic_dialog_info).setCancelable(false).show();

        return dialog;
    }

    private void orderPaymentsCompleteTidyUp(CartFragment cart) {
        try {
			Cart.INSTANCE.closeOrder();
            resetPayments();
            cart.resetTakeAway();
            MainTopBarMenu.getInstance().toggleLastUsedView();
            cart.doPayStuff();
            dismissLoadingDialog();
        } catch (Exception ex) {
            ex.printStackTrace();
			ErrorReporter.INSTANCE.filelog(ex);
        }
    }


    //TOASTS ===========================================================================================================
    public void makeInvalidInputToast() {
        Toast.makeText(getActivity(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
    }

    public void makeInvalidGiftCardToast() {
        Toast.makeText(getActivity(), R.string.invalid_giftcard_number, Toast.LENGTH_SHORT).show();
    }

    private void makeNullPaymentResultToast() {
        Toast.makeText(MainActivity.getInstance(), R.string.error_payment_result_null, Toast.LENGTH_LONG).show();
    }

    private void makeGenericPaymentErrorToast() {
        Toast.makeText(MainActivity.getInstance(), R.string.error_payment, Toast.LENGTH_LONG).show();
    }

    private void makeGiftCardAlreadyAddedToast() {
        Toast.makeText(getActivity(), R.string.gift_card_already_added, Toast.LENGTH_SHORT).show();
    }
	
}
