package no.susoft.mobile.pos.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnLongClickListener;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.MainTopBarMenu;
import no.susoft.mobile.pos.ui.adapter.DiscountReasonSpinnerAdapter;
import no.susoft.mobile.pos.ui.dialog.OrderAddNoteDialog;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class NumpadScanFragment extends NumpadFragment {

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
    @InjectView(R.id.Layout)
    LinearLayout layout;
    @InjectView(R.id.panel_welcome)
    LinearLayout panelWelcome;
    @InjectView(R.id.panel_product_discount)
    LinearLayout panelProductDiscount;
    @InjectView(R.id.discount_amount_edittext)
    EditText etDiscountAmount;
    @InjectView(R.id.btnDiscountCancel)
    Button btnDiscountCancel;
    @InjectView(R.id.btnDiscountDone)
    Button btnDiscountDone;
    @InjectView(R.id.btnOption3)
    Button btnOrderNote;
    //endregion

    private Spinner discountReasonSpinner;
    public DiscountReasonSpinnerAdapter discountReasonAdapter;

    private String numberQueue = "";
    private ToggleButton toggledButton;
    CartFragment cart;

    public NumpadScanFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.getInstance().setNumpadScanFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.numpad_scan_fragment, container, false);
        ButterKnife.inject(this, rootView);

        setNumberOnClickListeners(btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0, inputField);

        final Button buttonC = (Button) rootView.findViewById(R.id.buttonC);
        buttonC.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                inputField.setText("");
            }
        });

        setListeners(rootView);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }

        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            showWelcomeView();
            inputFieldRequestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showWelcomeView() {
        panelProductDiscount.setVisibility(View.GONE);
        panelWelcome.setVisibility(View.VISIBLE);
        resetToggleButtons();
        //refreshNoteButton();
    }

    public void refreshNoteButton() {
        try {
            if(Cart.INSTANCE.getOrder() != null && Cart.INSTANCE.getOrder().hasNote()) {
                btnOrderNote.setBackground(getContext().getResources().getDrawable(R.drawable.numpad_orange_button));
            } else {
                btnOrderNote.setBackground(getContext().getResources().getDrawable(R.drawable.numpad_blue_selector));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void getSwipedFragment(String side) {
        if (side.equalsIgnoreCase("left")) {
            MainTopBarMenu.getInstance().toggleEditView();
        } else if (side.equalsIgnoreCase("right")) {
            MainTopBarMenu.getInstance().toggleBrowseView();
        }
    }

    //==================================================================================================================

    private void setListeners(View rootView) {
        setBackButtonListeners();
        setEnterButtonListener();
        setDotButtonListener();
        setMinusButtonListener();
        setPlusButtonListener();
        setUpcEanOnChangeListener();
        setQtyOnChangeListener();
        setPriceOnChangeListener();
        setDiscountOnChangeListener();
        setPercentButtonListener();
        //setBtnOrderNoteListener();
        setInputFieldLongListener();
        setEtDiscountAmountListener();
        setDiscountButtonCancelListener();
        setDiscountButtonListener();
        discountReasonSpinner = (Spinner) rootView.findViewById(R.id.discount_reason_spinner);
        setupDiscountReasonSpinner(discountReasonSpinner, discountReasonAdapter, etDiscountAmount);
    }

    private void setBtnOrderNoteListener() {
        btnOrderNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderAddNoteDialog oadDialog = new OrderAddNoteDialog();
                if(Cart.INSTANCE.getOrder() != null) {
                    oadDialog.show(MainActivity.getInstance().getSupportFragmentManager(), "addordernote");
                }
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

    private void setBackButtonListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_BACK, inputField);
            }
        });

        btnBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                inputField.selectAll();
                return true;
            }
        });

    }

    private void setEnterButtonListener() {
        btnEnter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_ENTER, inputField);
            }
        });
    }

    private void setDotButtonListener() {
        btnDot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(ButtonPress.DOT.getValue(), inputField);
            }
        });
    }

    private void setMinusButtonListener() {
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                            resetToggleButtons();
                            inputField.setText("");
                        }
                    }

                } catch (Exception ex) {
                    Log.getStackTraceString(ex);
                }
            }
        });
    }

    private void setPlusButtonListener() {
        btnPlus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
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

                            resetToggleButtons();
                            inputField.setText("");
                        }
                    }

                } catch (Exception ex) {
                    Log.getStackTraceString(ex);
                }
            }
        });
    }

    private void setUpcEanOnChangeListener() {
        btnUpcEan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                try {

                    if (isChecked) {
                        compoundButton.setChecked(true);
                        if (btnQty.isChecked() || btnPrice.isChecked() || btnDisc.isChecked()) {
                            if (btnDisc.isChecked()) {
                                showWelcomeView();
                            }
                            inputField.setText("");
                        }
                        setToggledButton(btnUpcEan);
                    } else {
                        setToggledButton(null);
                    }

                    inputFieldRequestFocus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setQtyOnChangeListener() {
        btnQty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                try {
                    cart = MainActivity.getInstance().getCartFragment();
                    if (isChecked) {
                        compoundButton.setChecked(true);
                        if (btnUpcEan.isChecked() || btnPrice.isChecked() || btnDisc.isChecked()) {
                            if (btnDisc.isChecked()) {
                                showWelcomeView();
                            }

                            toggleThisButton(btnQty);
                            inputField.setText("");
                        } else {
                            if (Cart.INSTANCE.hasSelectedLine()) {
                                if (inputFieldHasText()) {
                                    handleQtyInput();
                                } else {
                                    setToggledButton(btnQty);
                                }
                            }
                        }
                    } else {
                        if (Cart.INSTANCE.hasSelectedLine() && inputFieldHasText()) {
                            handleQtyInput();
                        }
                        setToggledButton(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void performNumpadNumberClick(int number, EditText inputField) {

        switch (number) {
            case KeyEvent.KEYCODE_BACK: {
                inputField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                break;
            }
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER: {
                doEnterClick();
                hideKeyboard();
                break;
            }
            case KeyEvent.KEYCODE_PERIOD: {
                inputField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PERIOD));
                break;
            }
            case KeyEvent.KEYCODE_PLUS: {
                inputField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PLUS));
                break;
            }
            case KeyEvent.KEYCODE_MINUS: {
                inputField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MINUS));
                break;
            }
            case -90: {
                inputField.append(".");
                break;
            }
            case -100: {
                inputField.append("%");
                break;
            }
            default: {
                inputField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, number));
                break;
            }
        }
    }

    private void setInputFieldLongListener() {
        inputField.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
    }

    private void setDiscountOnChangeListener() {
        btnDisc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    toggleThisButton(btnDisc);
                    btnPercent.setEnabled(true);
                    inputField.setText("");

                    switchToProductDiscountView();
                } else {
                    setToggledButton(null);
                    showWelcomeView();
                }
            }
        });
    }

    private void switchToProductDiscountView() {
        if (Cart.selectedLine != null) {
            loadOrderLineDiscount(Cart.selectedLine);
        }
    }

    private void setPercentButtonListener() {
        btnPercent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performNumpadNumberClick(ButtonPress.PERCENT.getValue(), inputField);
            }
        });
    }

    private void setDiscountButtonCancelListener() {
        btnDiscountCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btnDisc.setChecked(false);
                setToggledButton(null);
                inputField.setText("");
                showWelcomeView();
            }
        });
    }

    private void setDiscountButtonListener() {
        btnDiscountDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    setDiscountText();

                    if (etDiscountAmount.getText().length() > 0) {
                        String discountStr = etDiscountAmount.getText().toString();
                        discountStr = discountStr.replaceAll("%", "").trim();

                        Decimal value = Decimal.make(discountStr);
                        addDiscountToLineIfPositive(value, discountReasonSpinner);
                    } else {
                        Cart.INSTANCE.addDiscount(null);
                    }
                } catch (Exception e) {
                }

                btnDisc.setChecked(false);
                setToggledButton(null);
                inputField.setText("");
                showWelcomeView();
            }
        });
    }

    private String getInputFieldText() {
        return inputField.getText().toString();
    }

    private void setPriceOnChangeListener() {
        btnPrice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                try {
                    cart = MainActivity.getInstance().getCartFragment();
                    if (isChecked) {
                        compoundButton.setChecked(true);
                        if (btnQty.isChecked() || btnDisc.isChecked()) {
                            if (btnDisc.isChecked()) {
                                showWelcomeView();
                            }
                            toggleThisButton(btnPrice);
                            inputField.setText("");
                        } else {
                            if (Cart.INSTANCE.hasSelectedLine()) {
                                if (inputFieldHasText()) {
                                    doPriceUpdate();
                                } else {
                                    setToggledButton(btnPrice);
                                }
                            }
                        }
                    } else {
                        if (Cart.INSTANCE.hasSelectedLine()) {
                            if (inputFieldHasText()) {
                                try {
                                    doPriceUpdate();
                                } catch (NumberFormatException nfex) {
                                    nfex.printStackTrace();
                                }
                            }
                        }
                        setToggledButton(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void doPriceUpdate() {
        Decimal price = Decimal.make(getInputFieldText());
        updateOrderLinePrice(price);
        inputField.setText("");
        btnPrice.setChecked(false);
        setToggledButton(null);
    }

    //==================================================================================================================

    public void doEnterClick() {
        cart = MainActivity.getInstance().getCartFragment();
        ToggleButton btn = getToggledButton();

        Log.i("vilde", "Searching for: " + inputField.getText().toString());
        if (inputField.getText().length() > 0) {
            if (checkForReceiptInInput() != null) {
				MainActivity.getInstance().getServerCallMethods().loadCompleteOrderByReceipt(checkForReceiptInInput());
			} else if (checkForOrderingReceiptInInput() != null) {
                MainActivity.getInstance().getServerCallMethods().loadOrderByID(checkForOrderingReceiptInInput());
			} else if (checkForAlternativeIdReceiptInInput() != null) {
                MainActivity.getInstance().getServerCallMethods().loadCompleteOrderByAlternativeID(checkForAlternativeIdReceiptInInput());
//			} else if (checkForAlternativeOrderingReceiptInInput() != null) {
//                MainActivity.getInstance().getServerCallMethods().loadOrderByAlternativeID(checkForAlternativeOrderingReceiptInInput());
            } else if (btnUpcEan.isChecked() || btn == null) {
                MainActivity.getInstance().getServerCallMethods().loadProductByBarcode(inputField.getText().toString());
            } else {

                if (Cart.selectedLine != null) {

                    switch (btn.getId()) {
                        case R.id.buttonPrice: {
                            doPriceEnter();
                            break;
                        }
                        case R.id.buttonDiscount: {
                            doDiscEnter();
                            break;
                        }
                        case R.id.buttonQuantity: {
                            doQtyEnter();
                            break;
                        }
                    }
                }
            }
        }
        inputField.setText("");
        inputFieldRequestFocus();
        hideKeyboard();
    }

    private void doPriceEnter() {
        Cart.selectedLine.setPrice(Decimal.make(getInputFieldText()));
        cart.refreshCart();
        btnPrice.setChecked(false);
        setToggledButton(null);
    }

    private void doDiscEnter() {
        try {
            setDiscountText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doQtyEnter() {
        Cart.selectedLine.setQuantity(Decimal.make(getInputFieldText()));
        cart.refreshCart();
        btnQty.setChecked(false);
        setToggledButton(null);
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

    //==================================================================================================================

    private void handleQtyInput() {
        try {
            Decimal qty = Decimal.make(getInputFieldText());
            if (qty.isNegative()) {
                Cart.INSTANCE.removeSelectedOrderLine();
            } else {
                Cart.selectedLine.setQuantity(qty);
                cart.refreshCart();
            }
            inputField.setText("");
            btnQty.setChecked(false);
            setToggledButton(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean inputFieldHasText() {
        return !getInputFieldText().isEmpty();
    }

    private void toggleThisButton(ToggleButton button) {
        setChecked(btnPrice, btnPrice == button);
        setChecked(btnDisc, btnDisc == button);
        setChecked(btnQty, btnQty == button);
        setChecked(btnUpcEan, btnUpcEan == button);
        setToggledButton(button);
    }

    private void setChecked(ToggleButton button, boolean b) {
        button.setChecked(b);
    }


    private void setDiscountText() {
        if (inputField.getText().length() > 0) {
            String discountStr = inputField.getText().toString();
            if (discountStr.contains("%")) {
                etDiscountAmount.setText(discountStr.replaceAll("%", " %").trim());
            } else {
                setDiscountAmountTextIfPositive(discountStr, etDiscountAmount);
            }
        }
    }

    private void resetToggleButtons() {
        btnQty.setChecked(false);
        btnPrice.setChecked(false);
        btnDisc.setChecked(false);
        btnPercent.setEnabled(false);
        setToggledButton(null);
    }

    public void loadOrderLineDiscount(OrderLine line) {
        panelWelcome.setVisibility(View.GONE);
        panelProductDiscount.setVisibility(View.VISIBLE);

        discountReasonSpinner = (Spinner) MainActivity.getInstance().findViewById(R.id.discount_reason_spinner);
        setDiscountSelection(line, discountReasonSpinner, etDiscountAmount);
    }

    private void setToggledButton(ToggleButton btn) {
        toggledButton = btn;
    }

    private ToggleButton getToggledButton() {
        return toggledButton;
    }

    public void resetNumberQueue() {
        numberQueue = "";
    }

    private boolean inputFieldHasFocus() {
        return inputField.hasFocus();
    }

    public void inputFieldRequestFocus() {
        MainActivity.getInstance().findViewById(R.id.root).requestFocusFromTouch();
        inputField.requestFocusFromTouch();
    }

    public EditText getInputField() {
        return inputField;
    }

    public void setInputFieldToString(String text) {
        inputField.requestFocus();
        inputField.setText(text);
    }
}
