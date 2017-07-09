package no.susoft.mobile.pos.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Account;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.WebViewActivity;
import no.susoft.mobile.pos.ui.activity.util.MainTopBarMenu;
import no.susoft.mobile.pos.ui.adapter.DiscountReasonSpinnerAdapter;
import no.susoft.mobile.pos.ui.dialog.OrderLineAddNoteDialog;
import no.susoft.mobile.pos.ui.dialog.OrderLineEditBundleDialog;
import no.susoft.mobile.pos.ui.dialog.SalesPersonsDialog;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class NumpadEditFragment extends NumpadFragment {

    //region InjectView
    @InjectView(R.id.button0)
    Button btn0;
    @InjectView(R.id.button1)
    Button btn1;
    @InjectView(R.id.button2)
    Button btn2;
    @InjectView(R.id.button3)
    Button btn3;
    @InjectView(R.id.button4)
    Button btn4;
    @InjectView(R.id.button5)
    Button btn5;
    @InjectView(R.id.button6)
    Button btn6;
    @InjectView(R.id.button7)
    Button btn7;
    @InjectView(R.id.button8)
    Button btn8;
    @InjectView(R.id.button9)
    Button btn9;
    @InjectView(R.id.buttonMinus)
    Button btnMinus;
    @InjectView(R.id.buttonPlus)
    Button btnPlus;
    @InjectView(R.id.buttonBack)
    ImageView btnBack;
    @InjectView(R.id.buttonEnter)
    ImageView btnEnter;
    @InjectView(R.id.buttonDot)
    Button btnDot;
    @InjectView(R.id.buttonC)
    Button buttonC;
    @InjectView(R.id.buttonDiscount)
    ToggleButton btnDisc;
    @InjectView(R.id.buttonQuantity)
    ToggleButton btnQty;
    @InjectView(R.id.buttonPrice)
    ToggleButton btnPrice;
    @InjectView(R.id.buttonUpcEan)
    ToggleButton btnUpcEan;
    @InjectView(R.id.buttonSlash)
    Button btnSlash;
    @InjectView(R.id.buttonPercent)
    Button btnPercent;
    @InjectView(R.id.buttonStar)
    Button btnStar;
    @InjectView(R.id.numpad_edit_text)
    EditText inputField;
    @InjectView(R.id.top_panel)
    LinearLayout topPanel;
    @InjectView(R.id.panel_product_details)
    RelativeLayout panelProductDetails;
    @InjectView(R.id.panel_product_discount)
    LinearLayout panelProductDiscount;
    @InjectView(R.id.edit_details_product_image)
    ImageView productImage;
    @InjectView(R.id.tvProductName)
    TextView tvProductName;
    @InjectView(R.id.tvProductDescription)
    TextView tvProductDescription;
    @InjectView(R.id.tvProductCostLabel)
    TextView tvProductCostLabel;
    @InjectView(R.id.tvProductCost)
    TextView tvProductCost;
    @InjectView(R.id.tvProductStockLabel)
    TextView tvProductStockLabel;
    @InjectView(R.id.tvProductStock)
    TextView tvProductStock;
    @InjectView(R.id.ivProductStockOtherShops)
    ImageView ivProductStockOtherShops;
    @InjectView(R.id.tvProductPriceLabel)
    TextView tvProductPriceLabel;
    @InjectView(R.id.tvProductPrice)
    TextView tvProductPrice;
    @InjectView(R.id.discount_amount_edittext)
    EditText etDiscountAmount;
    @InjectView(R.id.btnDiscountCancel)
    Button btnDiscountCancel;
    @InjectView(R.id.btnDiscountDone)
    Button btnApplyDiscount;
    @InjectView(R.id.btnReturn)
    Button btnReturns;
    @InjectView(R.id.btnOption3)
    Button btnNote;
    @InjectView(R.id.btnSalesPerson)
    Button btnSalesPerson;
    @InjectView(R.id.btnBundle)
    Button btnBundle;
    //endregion

    public DiscountReasonSpinnerAdapter discountReasonAdapter;
    private Spinner discountReasonSpinner;
    private Product product;
    private ToggleButton toggledButton;
    private CartFragment cart;
    private boolean infoViewShowing = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.getInstance().setNumpadEditFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.numpad_edit_fragment, container, false);
        ButterKnife.inject(this, rootView);
        setButtonListeners();
        setCartFragmentReference();
        loadOrderLineInfo(Cart.selectedLine);

        discountReasonSpinner = (Spinner) rootView.findViewById(R.id.discount_reason_spinner);
        setupDiscountReasonSpinner(discountReasonSpinner, discountReasonAdapter, etDiscountAmount);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }

        });

        return rootView;
    }

    @Override
    protected void getSwipedFragment(String side) {
        if (side.equalsIgnoreCase("left")) {
            MainTopBarMenu.getInstance().toggleSearchView();
        } else if (side.equalsIgnoreCase("right")) {
            MainTopBarMenu.getInstance().toggleScanView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        doOnResume();
    }

    public void refreshView() {
        doOnResume();
    }

    private void doOnResume() {
        setCartFragmentReference();
        inputFieldRequestFocus();
        try {
            if (Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.hasSelectedLine()) {
                loadOrderLineInfo(Cart.selectedLine);
                btnReturns.setEnabled(true);
                updateOptionLineButtons();
            } else {
                Cart.INSTANCE.setSelectedLine(null);
                btnReturns.setEnabled(false);
                hideProductInfo();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCartFragmentReference() {
        cart = MainActivity.getInstance().getCartFragment();
    }

    private void setToggledButton(ToggleButton btn) {
        toggledButton = btn;
    }

    private ToggleButton getToggledButton() {
        return toggledButton;
    }

    private String getInputFieldText() {
        return inputField.getText().toString();
    }

    private void clearInputField() {
        inputField.setText("");
    }

    private boolean inputFieldHasText() {
        return (inputField.getText().length() > 0);
    }

    //LISTENERS  =======================================================================================================

    private void setButtonListeners() {
        setNumberOnClickListeners(btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0, inputField);
        setBtnCListener();
        setBtnBackListeners();
        setBtnEnterListener();
        setBtnDotListener();
        setBtnMinusListener();
        setBtnPlusListener();
        setBtnUpcEanListener();
        setBtnQuantityListener();
        setBtnPriceListener();
        setBtnDiscListener();
        setBtnPercentListener();
        setInputFieldListener();
        setEtDiscountAmountListener();
        setBtnDiscountCancelListener();
        setBtnApplyDiscountListener();
        setBtnReturnsListener();
        setBtnNoteListener();

		btnSalesPerson.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Cart.INSTANCE.getOrder() != null && Cart.selectedLine != null) {
					SalesPersonsDialog dialog = new SalesPersonsDialog();
					dialog.show(MainActivity.getInstance().getFragmentManager(), "addsalesperson");
				}
			}
		});

		btnBundle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Cart.INSTANCE.getOrder() != null && Cart.selectedLine != null && Cart.selectedLine.getProduct() != null && Cart.selectedLine.getProduct().isBundle() && Cart.selectedLine.getProduct().getComponents() != null) {
					OrderLineEditBundleDialog dialog = new OrderLineEditBundleDialog();
					dialog.setOrderLine(Cart.selectedLine);
					dialog.show(MainActivity.getInstance().getFragmentManager(), "bundle");
				}
			}
		});
    }

    private void setBtnNoteListener() {
        btnNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBtnNoteClick();
            }
        });

    }

    private void setBtnBackListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_BACK, inputField);
            }
        });

        btnBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                doLongBackClick();
                return true;
            }
        });
    }

    private void setBtnCListener() {
        buttonC.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                inputField.setText("");
            }
        });
    }

    private void setBtnEnterListener() {
        btnEnter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doEnterClick();
            }
        });
    }

    private void setBtnReturnsListener() {
        btnReturns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doReturnsClick();
            }
        });
    }

    private void setBtnDotListener() {
        btnDot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(ButtonPress.DOT.getValue(), inputField);
            }
        });

    }

    private void setBtnMinusListener() {
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doMinusClick();
            }
        });
    }

    private void setBtnPlusListener() {
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPlusClick();
            }
        });
    }

    private void setBtnDiscountCancelListener() {
        btnDiscountCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetNumpad();
            }
        });
    }

    private void setEtDiscountAmountListener() {
        etDiscountAmount.setFocusable(false);
        etDiscountAmount.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
    }

    private void setInputFieldListener() {
        inputField.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
    }

    private void setBtnPercentListener() {
        btnPercent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performNumpadNumberClick(ButtonPress.PERCENT.getValue(), inputField);
            }
        });
    }

    private void setBtnApplyDiscountListener() {
        btnApplyDiscount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doApplyDiscountClick();
            }
        });
    }

    private void setBtnQuantityListener() {
        btnQty.setOnCheckedChangeListener(checkedChangeListener);
    }

    private void setBtnPriceListener() {
        btnPrice.setOnCheckedChangeListener(checkedChangeListener);
    }

    private void setBtnDiscListener() {
        btnDisc.setOnCheckedChangeListener(checkedChangeListener);
    }

    private void setBtnUpcEanListener() {
        btnUpcEan.setOnCheckedChangeListener(checkedChangeListener);
    }

	private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
			doCheckedChanged((ToggleButton) compoundButton, isChecked);
			inputFieldRequestFocus();
		}
	};

    //LISTENER clicks ==================================================================================================

    private void doLongBackClick() {
        inputField.selectAll();
    }

    public void doEnterClick() {
        setCartFragmentReference();

        if (inputFieldHasText()) {
            if (checkForReceiptInInput() != null) {
				MainActivity.getInstance().getServerCallMethods().loadCompleteOrderByReceipt(checkForReceiptInInput());
			} else if (checkForOrderingReceiptInInput() != null) {
                MainActivity.getInstance().getServerCallMethods().loadOrderByID(checkForOrderingReceiptInInput());
			} else if (checkForAlternativeIdReceiptInInput() != null) {
                MainActivity.getInstance().getServerCallMethods().loadCompleteOrderByAlternativeID(checkForAlternativeIdReceiptInInput());
//			} else if (checkForAlternativeOrderingReceiptInInput() != null) {
//                MainActivity.getInstance().getServerCallMethods().loadOrderByAlternativeID(checkForAlternativeOrderingReceiptInInput());
            } else if (btnUpcEan.isChecked() || getToggledButton() == null) {
                loadProductFromServer();
            } else {
                handleEnterButtonClick(getToggledButton());
            }
        }

        clearInputField();
        inputFieldRequestFocus();
        hideKeyboard();
    }

    private String checkForReceiptInInput() {
        String orderNumber = getInputFieldText().trim();
        if (orderNumber.length() > 3 && (orderNumber.substring(0, 3).equalsIgnoreCase("%O%") || orderNumber.substring(0, 3).equalsIgnoreCase("1O1"))) {
            return orderNumber.substring(3, orderNumber.length());
        } else {
            return null;
        }
    }

    private String checkForOrderingReceiptInInput() {
        String orderNumber = getInputFieldText().trim();
        if (orderNumber.length() > 6 && (orderNumber.substring(0, 3).equalsIgnoreCase("1T1"))) {
            return orderNumber.substring(6, orderNumber.length());
        } else {
            return null;
        }
    }

    private String checkForAlternativeIdReceiptInInput() {
        String orderNumber = getInputFieldText().trim();
        if (orderNumber.length() > 6 && (orderNumber.substring(0, 3).equalsIgnoreCase("1A1"))) {
            return orderNumber.substring(3, orderNumber.length());
        } else {
            return null;
        }
    }
    
    private String checkForAlternativeOrderingReceiptInInput() {
        String orderNumber = getInputFieldText().trim();
        if (orderNumber.length() > 6 && (orderNumber.substring(0, 3).equalsIgnoreCase("1Q1"))) {
            return orderNumber.substring(3, orderNumber.length());
        } else {
            return null;
        }
    }

    private void doReturnsClick() {
        if (Cart.INSTANCE.hasSelectedLine()) {
            try {
                updateOrderLineQuantity(Cart.selectedLine.getQuantity().multiply(Decimal.NEGATIVE_ONE));
                cart.lvOrderLines.setSelection(Cart.INSTANCE.getOrder().getLines().indexOf(Cart.selectedLine));
                updateOptionLineButtons();
            } catch (Exception ex) {

            }
        }
    }

    private void doMinusClick() {
        try {
            cart = MainActivity.getInstance().getCartFragment();
            if (btnUpcEan.isChecked()) {
                performNumpadNumberClick(KeyEvent.KEYCODE_MINUS, inputField);
            } else {
                if (Cart.INSTANCE.hasSelectedLine()) {
                    Decimal num = Decimal.ONE;
                    if (inputFieldHasText()) {
                        num = Decimal.make(inputField.getText().toString());
                    }
                    Decimal qty = Cart.selectedLine.getQuantity().subtract(num);
                    handleQuantityChanged(cart, qty);
                    uncheckAllToggleButtons();
                    inputField.setText("");
                }
            }
        } catch (Exception ex) {
            Log.getStackTraceString(ex);
        }
    }

    private void doPlusClick() {
        try {
            cart = MainActivity.getInstance().getCartFragment();
            if (btnUpcEan.isChecked()) {
                performNumpadNumberClick(KeyEvent.KEYCODE_PLUS, inputField);
            } else {
                if (Cart.INSTANCE.hasSelectedLine()) {
                    Decimal num = Decimal.ONE;
                    if (inputFieldHasText()) {
                        num = Decimal.make(inputField.getText().toString());
                    }
                    Decimal qty = Cart.selectedLine.getQuantity().add(num);
                    handleQuantityChanged(cart, qty);
                    uncheckAllToggleButtons();
                    inputField.setText("");
                }
            }
        } catch (Exception ex) {
            Log.getStackTraceString(ex);
        }
    }

    private void doBtnNoteClick() {
        try {
            OrderLineAddNoteDialog oadDialog = new OrderLineAddNoteDialog();
            if (Cart.INSTANCE.getOrder() != null && Cart.selectedLine != null) {
                oadDialog.setOrderLine(Cart.selectedLine);
                oadDialog.show(MainActivity.getInstance().getFragmentManager(), "addordernote");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doCheckedChanged(ToggleButton button, boolean isChecked) {
        try {
            if (isChecked) {
                doCheckedAction(button);
            } else {
                doUncheckedAction(button);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doCheckedAction(ToggleButton button) {
        if (Cart.INSTANCE.hasSelectedLine()) {

            if (button.equals(btnDisc)) {
                checkButton(btnDisc);
                btnPercent.setEnabled(true);
                clearInputField();

                if (infoViewShowing) {
                    switchToProductDiscountView();
                }
            } else {

                checkButton(button);
                if (!infoViewShowing) {
                    switchToProductInfoView();
                }

                if (inputFieldHasText()) {
                    changeLineValue(button, NumpadAction.ENTER);
                    clearInputField();
                    uncheckAllToggleButtons();
                }
            }
        }
    }

    private void doUncheckedAction(ToggleButton button) {
        if (button.equals(btnDisc)) {
            switchToProductInfoView();
        } else {
            changeLineValue(button, NumpadAction.UNCHECKED);
        }
        uncheckAllToggleButtons();
    }

    private void doApplyDiscountClick() {
        try {
            addDiscountToProduct();
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.error_adding_discount_reason, Toast.LENGTH_SHORT).show();
        }
        resetNumpad();
    }

    public EditText getInputField() {
        return inputField;
    }

    public void inputFieldRequestFocus() {
        MainActivity.getInstance().findViewById(R.id.root).requestFocusFromTouch();
        inputField.requestFocusFromTouch();
    }

    public void setInputFieldToString(String text) {
        inputField.requestFocus();
        inputField.setText(text);
    }

    //==================================================================================================================
    // CHANGING ORDER LINE VALUES
    private enum NumpadAction {
        ADD, SUBTRACT, UNCHECKED, ENTER
    }

    private void handleEnterButtonClick(ToggleButton button) {
        if (Cart.INSTANCE.hasSelectedLine()) {
            if (button.equals(btnDisc)) {
//                setDiscountAmountTextToInputFieldText();
				doApplyDiscountClick();
            } else {
                changeLineValue(button, NumpadAction.ENTER);
                cart.refreshCart();
                uncheckButton(button);
            }
        }
    }

    private void changeLineValue(ToggleButton button, NumpadAction action) {
        if (Cart.INSTANCE.hasSelectedLine()) {

            Decimal num = getDecimalFromInputField_defaultONE();
            Decimal newNumber = null;
            switch (action) {
                case ADD:
                    newNumber = Cart.selectedLine.getQuantity().add(num);
                    break;
                case SUBTRACT:
                    newNumber = Cart.selectedLine.getQuantity().subtract(num);
                    break;
                case UNCHECKED:
                    if (inputFieldHasText())
                        newNumber = Decimal.make(getInputFieldText());
                    break;
                case ENTER:
                    if (inputFieldHasText())
                        newNumber = Decimal.make(getInputFieldText());
                    break;
            }

            if (newNumber != null) {
                if (button.equals(btnQty)) {
                    updateOrderLineQuantity(newNumber);
                } else if (button.equals(btnPrice)) {
                    updateOrderLinePrice(newNumber);
                }
            }
            loadOrderLineInfo(Cart.selectedLine);
        }
    }

    private void updateOrderLineQuantity(Decimal qty) {
        try {
            Cart.selectedLine.setQuantity(qty);
			if (Cart.selectedLine.getComponents() != null) {
				for (OrderLine line : Cart.selectedLine.getComponents()) {
					line.setQuantity(qty);
				}
			}

            cart.refreshCart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Decimal getDecimalFromInputField_defaultONE() {
        if (inputFieldHasText()) {
            return Decimal.make(getInputFieldText());
        } else {
            return Decimal.ONE;
        }
    }

    //DISCOUNT =========================================================================================================
    private boolean etDiscountAmountHasText() {
        return (etDiscountAmount.getText().length() > 0);
    }

    private String getEtDiscountText() {
        return (etDiscountAmount.getText().toString());
    }

    private void addDiscountToProduct() {
        setDiscountAmountTextToInputFieldText();
        if (etDiscountAmountHasText()) {
            setProductDiscountAmountFromDiscountAmountText();
        } else {
            Cart.INSTANCE.addDiscount(null);
        }
    }

    private void setDiscountAmountTextToInputFieldText() {
        try {
            if (inputFieldHasText()) {
                String discountStr = getInputFieldText();
                if (discountStr.contains("%")) {
                    etDiscountAmount.setText(discountStr.replaceAll("%", " %").trim());
                } else {
                    setDiscountAmountTextIfPositive(discountStr, etDiscountAmount);
                }
            }
        } catch (Exception e) {
        }
    }

    private void setProductDiscountAmountFromDiscountAmountText() {
        String discountStr = getEtDiscountText().replaceAll("%", "").trim();
        Decimal value = Decimal.make(discountStr);
        addDiscountToLineIfPositive(value, discountReasonSpinner);
    }

    //TOGGLE BUTTONS ====================================================================================================
    private void checkButton(ToggleButton button) {
        uncheckAllToggleButtons();
        button.setChecked(true);
        setToggledButton(button);
    }

    private void uncheckButton(ToggleButton button) {
        button.setChecked(false);
        setToggledButton(null);
    }

    private void uncheckAllToggleButtons() {
        setToggledButton(null);
        btnDisc.setChecked(false);
        btnQty.setChecked(false);
        btnPrice.setChecked(false);
        btnUpcEan.setChecked(false);
    }

    private void resetNumpad() {
        uncheckAllToggleButtons();
        clearInputField();
        loadOrderLineInfo(Cart.selectedLine);
    }

    //VISUALS ==========================================================================================================
    private void switchToProductDiscountView() {
        if (Cart.INSTANCE.hasSelectedLine()) {
            loadOrderLineDiscount(Cart.selectedLine);
        }
    }

    private void switchToProductInfoView() {
        loadOrderLineInfo(Cart.selectedLine);
        clearInputField();
    }

    public void updateOptionLineButtons() {
        if (cart != null && Cart.INSTANCE.hasSelectedLine()) {
            if (Cart.selectedLine.getQuantity().isNegative()) {
                btnReturns.setBackgroundResource(R.drawable.numpad_orange_button);
            } else {
                btnReturns.setBackgroundResource(R.drawable.numpad_blue_button);
            }
			if (Cart.selectedLine.hasNote()) {
                btnNote.setBackgroundResource(R.drawable.numpad_orange_button);
            } else {
                btnNote.setBackgroundResource(R.drawable.numpad_blue_selector);
            }
			if (Cart.selectedLine.getSalesPersonId() != null && !Cart.selectedLine.getSalesPersonId().isEmpty()) {
                btnSalesPerson.setBackgroundResource(R.drawable.numpad_orange_button);
            } else {
                btnSalesPerson.setBackgroundResource(R.drawable.numpad_blue_selector);
            }
			if (Cart.selectedLine.getProduct() != null && Cart.selectedLine.getProduct().isBundle()) {
				btnBundle.setEnabled(true);
				btnBundle.setBackgroundResource(R.drawable.numpad_blue_selector);
			} else {
				btnBundle.setEnabled(false);
				btnBundle.setBackgroundResource(R.drawable.numpad_gray_button);
			}
			btnReturns.setEnabled(true);
			btnNote.setEnabled(true);
			btnSalesPerson.setEnabled(true);
		} else {
			btnReturns.setEnabled(false);
			btnNote.setEnabled(false);
			btnSalesPerson.setEnabled(false);
			btnBundle.setEnabled(false);
			btnBundle.setBackgroundResource(R.drawable.numpad_gray_button);
		}
    }

    public void loadOrderLineInfo(final OrderLine line) {
        infoViewShowing = true;
        panelProductDiscount.setVisibility(View.GONE);
        panelProductDetails.setVisibility(View.VISIBLE);

        uncheckAllToggleButtons();
        btnPercent.setEnabled(false);

        if (line != null && line.getProduct() != null) {
            showProductInfo(line);
			ivProductStockOtherShops.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(MainActivity.getInstance().getApplicationContext(), WebViewActivity.class);
					Account account = AccountManager.INSTANCE.getAccount();
					i.putExtra("url", "http://" + Server.authority + "/" + Server.path1 + "/no.susoft.sucom.servlets.AndroidAccessServlet?chain=" + account.getChain().getID() + "&user=" + account.getLogin() + "&pwd=" + account.getPassword() + "&exec=265&android=1&shop_id=" + account.getShop().getID() + "&sktab=2&artid=" + line.getProduct().getId());
					MainActivity.getInstance().startActivity(i);
				}
			});
        } else {
            hideProductInfo();
        }
        updateOptionLineButtons();
    }

    private void showProductInfo(OrderLine line) {
        tvProductName.setText(line.getProduct().getName());
        tvProductDescription.setText(line.getProduct().getDescription());
        tvProductCost.setText(line.getProduct().getCost().toString());
        tvProductStock.setText(String.valueOf(line.getProduct().getStockQty().toInteger()));
        tvProductPrice.setText(line.getPrice().toString());
        tvProductCostLabel.setVisibility(View.VISIBLE);
        tvProductStockLabel.setVisibility(View.VISIBLE);
        ivProductStockOtherShops.setVisibility(View.VISIBLE);
        tvProductPriceLabel.setVisibility(View.VISIBLE);
        this.product = line.getProduct();
        loadImage();
    }

    private void hideProductInfo() {
        tvProductName.setText("");
        tvProductDescription.setText("");
        tvProductCost.setText("");
        tvProductStock.setText("");
        tvProductPrice.setText("");
        tvProductCostLabel.setVisibility(View.GONE);
        tvProductStockLabel.setVisibility(View.GONE);
        ivProductStockOtherShops.setVisibility(View.GONE);
        tvProductPriceLabel.setVisibility(View.GONE);
        productImage.invalidate();
        productImage.setImageDrawable(null);
    }

    //SERVER CALLS =====================================================================================================
    public void loadOrderLineDiscount(OrderLine line) {
        infoViewShowing = false;
        panelProductDetails.setVisibility(View.GONE);
        panelProductDiscount.setVisibility(View.VISIBLE);

        discountReasonSpinner = (Spinner) MainActivity.getInstance().findViewById(R.id.discount_reason_spinner);
        setDiscountSelection(line, discountReasonSpinner, etDiscountAmount);
    }

    private void loadImage() {
        try {
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getToken());
            request.appendOperation(Protocol.OperationCode.REQUEST_IMAGE);
            request.appendParameter(Protocol.Parameters.TYPE, Protocol.ImageType.PRODUCT.ordinal());
            request.appendParameter(Protocol.Parameters.QUALITY, Protocol.Quality.DEFAULT.ordinal());
            request.appendParameter(Protocol.Parameters.ID, product.getId());

            Picasso.with(MainActivity.getInstance()).load(request.get()).into(productImage, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {
                }
            });
            productImage.invalidate();

        } catch (Exception ex) {
            Log.getStackTraceString(ex);
        }
    }

    private void loadProductFromServer() {
        MainActivity.getInstance().getServerCallMethods().loadProductByBarcode(getInputFieldText());
    }
}
