package no.susoft.mobile.pos.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TabHost;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class OrderLineEditAllDialog extends DialogFragment {

    EditText qtyText;
    EditText productName;
    EditText productPrice;
    Decimal qty;
    OrderLine orderLine;
    Product product;
    TabHost tabs;

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

        if (orderLine != null) {
            product = orderLine.getProduct();
            qty = this.orderLine.getQuantity();
        } else {
            qty = Decimal.ONE;
        }

        final View view = (inflater.inflate(R.layout.order_line_edit_tabbed_dialog, null));
        builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (orderLine != null) {
                    if (productName.getText().length() > 0)
                        orderLine.getProduct().setName(productName.getText().toString().trim());

                    if (productPrice.getText().length() > 0) {
                        orderLine.getProduct().setPrice(Decimal.make(productPrice.getText().toString().trim()));
                        orderLine.setPrice(Decimal.make(productPrice.getText().toString().trim()));
                    }

                    if (qtyText.getText().length() > 0) {
                        qty = Decimal.make(qtyText.getText().toString());
                        orderLine.setQuantity(qty);

                        if (orderLine.getQuantity().isEqual(Decimal.ZERO)) {
                            Cart.INSTANCE.removeSelectedOrderLine();
                        }
                    }

                    MainActivity.getInstance().getCartFragment().refreshCart();
                } else {

                    qty = Decimal.make(qtyText.getText().toString());

                    if (!qty.isEqual(Decimal.ZERO)) {
                        product.setName(productName.getText().toString());
                        product.setPrice(Decimal.make(productPrice.getText().toString()));

                        Cart.INSTANCE.addOrderLineFromDialog(product, qty);
                    }
                }
            }
        });

        // Add tabs
        tabs = (TabHost) view.findViewById(R.id.tabHost);

        tabs.setup();

        TabHost.TabSpec tabpage1 = tabs.newTabSpec("quantity");
        tabpage1.setContent(R.layout.product_qty);
        tabpage1.setIndicator("Product quantity");

        TabHost.TabSpec tabpage2 = tabs.newTabSpec("discount");
        tabpage2.setContent(R.layout.product_discount);
        tabpage2.setIndicator("Product discount");

        tabs.addTab(tabpage1);
        tabs.addTab(tabpage2);

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
            productName.setSelection(productName.getText().length());
            productPrice.setText(Cart.INSTANCE.getProductPrice(product).toString());

        } catch (Exception e) {
            Log.getStackTraceString(e);
        }

        return builder.create();
    }

    public void setOrderLine(OrderLine orderLine) {
        this.orderLine = orderLine;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

}
