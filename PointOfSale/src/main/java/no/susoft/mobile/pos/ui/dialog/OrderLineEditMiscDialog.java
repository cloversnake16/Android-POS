package no.susoft.mobile.pos.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class OrderLineEditMiscDialog extends DialogFragment {

    private EditText qtyText;
    private EditText productName;
    private EditText productPrice;
    private Decimal qty;
    private Product product;
    private OrderLine ol;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(400, WindowManager.LayoutParams.WRAP_CONTENT);
    }

	@Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = (inflater.inflate(R.layout.order_line_edit_misc_dialog, null));
        builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//                if (newOrderLine) {
//                    Cart.INSTANCE.removeSelectedOrderLine();
//                    MainActivity.getInstance().getCartFragment().refreshCart();
//                    getDialog().dismiss();
//                }
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (ol != null && ol.getProduct().isMiscellaneous()) {
                    if (productName.getText().length() > 0)
                        ol.getProduct().setName(productName.getText().toString().trim());

                    if (productPrice.getText().length() > 0) {
                        if(ol.getProduct().getType().equalsIgnoreCase("6")) {
                            ol.getProduct().setPrice(Decimal.make(productPrice.getText().toString().trim()));
                        }
                        ol.setPrice(Decimal.make(productPrice.getText().toString().trim()));
                    }

                    if (qtyText.getText().length() > 0) {
                        qty = Decimal.make(qtyText.getText().toString());
                        ol.setQuantity(qty);
                    }

                    MainActivity.getInstance().getCartFragment().refreshCart();
                    MainActivity.getInstance().getCartFragment().selectLastRow();
                    getDialog().dismiss();
                } else {

                    qty = Decimal.make(qtyText.getText().toString());

                    if (!qty.isEqual(Decimal.ZERO)) {
                        ol.getProduct().setName(productName.getText().toString());
                        ol.getProduct().setPrice(Decimal.make(productPrice.getText().toString()));

						MainActivity.getInstance().getCartFragment().refreshCart();
                        MainActivity.getInstance().getCartFragment().selectLastRow();
                        getDialog().dismiss();
                    }
                }
            }
        });

        ImageButton subtractButton = (ImageButton) view.findViewById(R.id.qtySubtractButton);
        subtractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qty = qty.subtract(Decimal.make(1.00));
                qtyText.setText(qty.toString());
            }
        });

        ImageButton addButton = (ImageButton) view.findViewById(R.id.qtyAddButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qty = qty.add(Decimal.ONE);
                qtyText.setText(qty.toString());
            }
        });

        productName = (EditText) view.findViewById(R.id.product_name);
        productPrice = (EditText) view.findViewById(R.id.product_price);

        qtyText = (EditText) view.findViewById(R.id.qtyText);
        qtyText.setText(qty.toString());

        qtyText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    qty = Decimal.make(qtyText.getText().toString());
                } catch (Exception ex) {
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });

        try {
            productName.setText(product.getName());
            productPrice.requestFocusFromTouch();
            productName.setSelection(productName.getText().length());
        } catch (Exception e) {
            e.printStackTrace();
        }

		Dialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        return dialog;
    }


    public void setOrderLine(OrderLine ol) {
        this.ol = ol;
        if (this.ol != null) {
            product = Cart.selectedLine.getProduct();
            qty = Cart.selectedLine.getQuantity();
        } else {
            qty = Decimal.ONE;
        }
    }
}
