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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class OrderLineEditDialog extends DialogFragment {

    EditText qtyText;
    Decimal qty;
    OrderLine orderLine;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onStart() {
        super.onStart();

        getDialog().getWindow().setLayout(250, WindowManager.LayoutParams.WRAP_CONTENT);

    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        qty = this.orderLine.getQuantity();

        final View view = (inflater.inflate(R.layout.order_line_edit_dialog, null));
        builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                qty = Decimal.make(qtyText.getText().toString());
                orderLine.setQuantity(qty);

                if (orderLine.getQuantity().isEqual(Decimal.ZERO)) {
                    Cart.INSTANCE.removeSelectedOrderLine();
                }

                MainActivity.getInstance().getCartFragment().refreshCart();
            }
        });

        ImageButton subtractButton = (ImageButton) view.findViewById(R.id.qtySubtractButton);
        subtractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("vilde", "clicked subtract");
                if (qty.isGreater(Decimal.make(0.99)))
                    qty = qty.subtract(Decimal.make(1.00));

                qtyText.setText(qty.toString());
            }
        });

        ImageButton addButton = (ImageButton) view.findViewById(R.id.qtyAddButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("vilde", "clicked add");
                qty = qty.add(Decimal.ONE);
                Log.i("vilde", "qty is: " + qty);
                qtyText.setText(qty.toString());
            }
        });

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
                // TODO Auto-generated method stub

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

        });

        return builder.create();
    }

    public void setOrderLine(OrderLine orderLine) {
        this.orderLine = orderLine;

    }

}
