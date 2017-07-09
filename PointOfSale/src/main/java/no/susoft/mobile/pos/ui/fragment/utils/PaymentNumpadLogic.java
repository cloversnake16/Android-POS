package no.susoft.mobile.pos.ui.fragment.utils;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.NumpadPayFragment;

/**
 * Created by Vilde on 23.01.2016.
 * <p/>
 * This controls the logic associated with the number buttons, input field, as well as buttons such as enter and back,
 * but does not include the payment type toggle buttons
 */
public class PaymentNumpadLogic {


    private final Button btn0;
    private final Button btn1;
    private final Button btn2;
    private final Button btn3;
    private final Button btn4;
    private final Button btn5;
    private final Button btn6;
    private final Button btn7;
    private final Button btn8;
    private final Button btn9;
    private final ToggleButton tBtnValuesOrNumbers;
    private final ImageView btnEnter;
    private final ImageView btnBack;
    private final Button btnC;
    private final Button btnDot;
    private final NumpadPayFragment fragment;
    private final EditText inputField;

    public PaymentNumpadLogic(Button btn0, Button btn1, Button btn2, Button btn3, Button btn4, Button btn5, Button btn6, Button btn7, Button btn8, Button btn9, ToggleButton valuesOrNumbersTBtn, ImageView btnEnter, ImageView btnBack, Button btnC, Button btnDot, EditText inputField) {
        this.btn0 = btn0;
        this.btn1 = btn1;
        this.btn2 = btn2;
        this.btn3 = btn3;
        this.btn4 = btn4;
        this.btn5 = btn5;
        this.btn6 = btn6;
        this.btn7 = btn7;
        this.btn8 = btn8;
        this.btn9 = btn9;
        this.tBtnValuesOrNumbers = valuesOrNumbersTBtn;
        this.btnEnter = btnEnter;
        this.btnBack = btnBack;
        this.btnC = btnC;
        this.btnDot = btnDot;
        this.fragment = MainActivity.getInstance().getNumpadPayFragment();
        this.inputField = inputField;

        setListeners();
    }

    //LISTENERS ========================================================================================================
    private void setListeners() {
        setNumberButtonListeners();
        setBtnBackListeners();
        setBtnEnterListeners();
        setBtnCListeners();
        setBtnDotListeners();
    }

    private void setNumberButtonListeners() {
        btn0.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doNumpadNumberClick(0, KeyEvent.KEYCODE_0);
            }
        });
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doNumpadNumberClick(1, KeyEvent.KEYCODE_1);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doNumpadNumberClick(5, KeyEvent.KEYCODE_2);
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doNumpadNumberClick(10, KeyEvent.KEYCODE_3);
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doNumpadNumberClick(20, KeyEvent.KEYCODE_4);
            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doNumpadNumberClick(50, KeyEvent.KEYCODE_5);
            }
        });
        btn6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doNumpadNumberClick(100, KeyEvent.KEYCODE_6);
            }
        });
        btn7.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doNumpadNumberClick(200, KeyEvent.KEYCODE_7);
            }
        });
        btn8.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doNumpadNumberClick(500, KeyEvent.KEYCODE_8);
            }
        });
        btn9.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doNumpadNumberClick(1000, KeyEvent.KEYCODE_9);
            }
        });
    }

    private void setBtnBackListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doBackClick();
            }
        });

        btnBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                doBackLongClick();
                return true;
            }
        });
    }

    private void setBtnEnterListeners() {
        btnEnter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doEnterClickFromNumpad();
            }
        });
    }

    private void setBtnCListeners() {
        btnC.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doButtonCClick();
            }
        });
    }

    private void setBtnDotListeners() {
        btnDot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!inputField.getText().toString().contains("."))
                    inputField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PERIOD));
            }
        });
    }

    //CLICKS ===========================================================================================================
    private void doNumpadNumberClick(int number, int keycode) {
        if (isValuesKeyboard())
            valueClick(number);
        else
            numberClick(keycode);
    }

    private void valueClick(int number) {
        try {
            fragment.handlePaymentOfAmount(Decimal.make(number));
        } catch (NumberFormatException ex) {
            fragment.makeInvalidInputToast();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void numberClick(int number) {
        if (number >= 0) {
            dispatchInputFieldKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, number));
        }
    }

    public void doEnterClickFromScanner() {
        try {
            //inputField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            fragment.handlePaymentOnEnter(false);
            clearInputField();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            fragment.makeInvalidInputToast();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doEnterClickFromNumpad() {
        try {
            //inputField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            fragment.handlePaymentOnEnter(true);
            clearInputField();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            fragment.makeInvalidInputToast();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doBackClick() {
        dispatchInputFieldKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
    }

    private void doButtonCClick() {
        clearInputField();
    }

    private void doBackLongClick() {
        inputField.selectAll();
    }

    private void dispatchInputFieldKeyEvent(KeyEvent ke) {
        inputField.dispatchKeyEvent(ke);
    }

    //UTILS ============================================================================================================
    private boolean isValuesKeyboard() {
        return tBtnValuesOrNumbers.isChecked();
    }

    public void focusOnInputField() {
        try {
            if (inputField != null) {
                inputField.requestFocusFromTouch();
            }
        } catch (Exception e) {

        }
    }

    public boolean inputFieldHasText() {
        return (inputField.getText().toString().length() > 0);
    }

    private void clearInputField() {
        inputField.setText("");
    }

    public String getInputFieldText() {
        return inputField.getText().toString().trim();
    }

    //NUMPAD STATE =====================================================================================================
    public void setNumpadToPresetValues() {
        btn1.setText(fragment.getString(R.string.button1));
        btn2.setText(fragment.getString(R.string.button5));
        btn3.setText(fragment.getString(R.string.button10));
        btn4.setText(fragment.getString(R.string.button20));
        btn5.setText(fragment.getString(R.string.button50));
        btn6.setText(fragment.getString(R.string.button100));
        btn7.setText(fragment.getString(R.string.button200));
        btn8.setText(fragment.getString(R.string.button500));
        btn9.setText(fragment.getString(R.string.button1000));
    }

    public void setNumpadToNumbers() {
        btn1.setText(fragment.getString(R.string.button1));
        btn2.setText(fragment.getString(R.string.button2));
        btn3.setText(fragment.getString(R.string.button3));
        btn4.setText(fragment.getString(R.string.button4));
        btn5.setText(fragment.getString(R.string.button5));
        btn6.setText(fragment.getString(R.string.button6));
        btn7.setText(fragment.getString(R.string.button7));
        btn8.setText(fragment.getString(R.string.button8));
        btn9.setText(fragment.getString(R.string.button9));
        inputField.requestFocus();
    }

}
