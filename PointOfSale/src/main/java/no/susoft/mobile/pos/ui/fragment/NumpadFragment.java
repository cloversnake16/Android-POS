package no.susoft.mobile.pos.ui.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Discount;
import no.susoft.mobile.pos.data.DiscountReason;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.DiscountReasonSpinnerAdapter;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public abstract class NumpadFragment extends Fragment {

    public enum ButtonPress {
        UPCEAN(-50), QTY(-60), PRICE(-70), DISC(-80), DOT(-90), PERCENT(-100);
        protected final int value;

        public int getValue() {
            return value;
        }

        ButtonPress(int value) {
            this.value = value;
        }
    }

    public void hideKeyboard() {
        View currentView = MainActivity.getInstance().getCurrentFocus();
        if (currentView != null) {
            InputMethodManager imm = (InputMethodManager) MainActivity.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentView.getWindowToken(), 0);
        }
    }

    public void performNumpadNumberClick(int number, EditText inputField) {
        switch (number) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_DEL: {
                inputField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                break;
            }
            case KeyEvent.KEYCODE_ENTER: {
                inputField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
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
            case KeyEvent.KEYCODE_NUMPAD_DOT: {
                inputField.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_NUMPAD_DOT));
                break;
            }
            case -90: {
                inputField.append(".");
                Log.i("vilde", "pressed dot");
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

    final protected GestureDetector gesture = new GestureDetector(getActivity(),
            new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                       float velocityY) {
                    final int SWIPE_MIN_DISTANCE = 120;
                    final int SWIPE_MAX_OFF_PATH = 250;
                    final int SWIPE_THRESHOLD_VELOCITY = 200;
                    try {
                        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                            return false;
                        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            // Log.i(Constants.APP_TAG, "Right to Left");
                            getSwipedFragment("right");
                        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            //Log.i(Constants.APP_TAG, "Left to Right");
                            getSwipedFragment("left");
                        }
                    } catch (Exception e) {
                        // nothing
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });

    protected void getSwipedFragment(String side) {

    }

    protected boolean cartSelectedLineProductIsGiftCard() {
        return Cart.selectedLine.getProduct().getType().equalsIgnoreCase("6");
    }


    protected void setNumberOnClickListeners(Button btn1, Button btn2, Button btn3, Button btn4, Button btn5, Button btn6, Button btn7, Button btn8, Button btn9, Button btn0, final EditText inputField) {
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_1, inputField);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_2, inputField);
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_3, inputField);
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_4, inputField);
            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_5, inputField);
            }
        });
        btn6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_6, inputField);
            }
        });
        btn7.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_7, inputField);
            }
        });
        btn8.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_8, inputField);
            }
        });
        btn9.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_9, inputField);
            }
        });
        btn0.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performNumpadNumberClick(KeyEvent.KEYCODE_0, inputField);
            }
        });
    }

    protected void setDiscountAmountTextIfPositive(String discountStr, EditText view) {
        Decimal price = Cart.selectedLine.getPrice();
        if (price.isPositive()) {
            Decimal discountAmount = Decimal.make(discountStr);
            Decimal discountPercent = discountAmount.multiply(Decimal.HUNDRED).divide(price);
            view.setText(discountPercent.toString() + " %");
        }
    }

    protected void addDiscountToLineIfPositive(Decimal value, Spinner discountReasonSpinner) {
        if (value.isPositive()) {
            Discount discount = new Discount(value, (DiscountReason) discountReasonSpinner.getSelectedItem());
            Cart.INSTANCE.addDiscount(discount);
        } else {
            Cart.INSTANCE.addDiscount(null);
        }
    }

    protected void setDiscountSelection(OrderLine line, Spinner discountReasonSpinner, EditText etDiscountAmount) {
        if (line != null && line.getDiscount() != null && line.getDiscount().getReason() != null) {
            int selectedPosition = 0;
            for (DiscountReason reason : MainActivity.getInstance().getDiscountReasons()) {
                if (reason.getID() == line.getDiscount().getReason().getID()) {
                    discountReasonSpinner.setSelection(selectedPosition);
                    break;
                }
                selectedPosition++;
            }
            etDiscountAmount.setText(line.getDiscount().getPercent().toString() + " %");
        } else {
            etDiscountAmount.setText("");
        }
    }

    protected void setDiscountReasonSpinnerListenerIfNotExists(final Spinner discountReasonSpinner, final EditText etDiscountAmount) {
        if (discountReasonSpinner.getOnItemSelectedListener() == null) {
            discountReasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (Cart.selectedLine != null) {
                        DiscountReason reason = (DiscountReason) discountReasonSpinner.getSelectedItem();
                        if (reason.getPercent() > 0) {
                            etDiscountAmount.setText(reason.getPercent() + " %");
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }

            });
        }
    }

    protected void setupDiscountReasonSpinner(Spinner discountReasonSpinner, DiscountReasonSpinnerAdapter discountReasonAdapter, EditText etDiscountAmount) {
        discountReasonAdapter = new DiscountReasonSpinnerAdapter(MainActivity.getInstance(), 0, MainActivity.getInstance().getDiscountReasons());
        discountReasonAdapter.setDropDownViewResource(R.layout.discount_reason_spinner);
        discountReasonSpinner.setAdapter(discountReasonAdapter);

        setDiscountReasonSpinnerListenerIfNotExists(discountReasonSpinner, etDiscountAmount);
    }

    protected void handleQuantityChanged(CartFragment cart, Decimal qty) {
        if (qty.isPositive()) {
            Cart.selectedLine.setQuantity(qty);
            cart.refreshCart();
        } else {
            ///TODO ASSUMPTION that line should be removed
            Cart.INSTANCE.removeSelectedOrderLine();
        }
    }


    protected void updateOrderLinePrice(Decimal price) {
        CartFragment cart = MainActivity.getInstance().getCartFragment();
        try {
            if (Cart.INSTANCE.hasSelectedLine()) {

                if (Cart.selectedLine.getProduct().getType().equalsIgnoreCase("6")) {
                    Cart.selectedLine.getProduct().setPrice(price);
                }

                if (price.isNegative()) {
                    Cart.INSTANCE.removeSelectedOrderLine();
                    Log.i("vilde", "Removed line because price was negative");
                } else {
                    Cart.selectedLine.getProduct().setPrice(price);
                    Cart.selectedLine.setPrice(price);
                }
            }

            cart.refreshCart();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
