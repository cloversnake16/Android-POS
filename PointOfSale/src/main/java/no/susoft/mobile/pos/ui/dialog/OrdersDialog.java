package no.susoft.mobile.pos.ui.dialog;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderPointer;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.OrderListAdapter;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class OrdersDialog extends DialogFragment {

    ProgressBar progressBar;
    TextView noOrderText;
    LinearLayout listHeader;
    ListView list;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = (inflater.inflate(R.layout.orders_dialog, null));
        builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        progressBar = (ProgressBar) getDialog().findViewById(R.id.progress_bar_orders);
        progressBar.setVisibility(View.VISIBLE);
        noOrderText = (TextView) getDialog().findViewById(R.id.no_orders);
        noOrderText.setVisibility(View.GONE);
        listHeader = (LinearLayout) getDialog().findViewById(R.id.order_list_header);
        listHeader.setVisibility(View.GONE);
        list = (ListView) getDialog().findViewById(R.id.order_list);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapterView.setSelection(i);
                Order o = (Order) adapterView.getItemAtPosition(i);
                if (o != null && MainActivity.getInstance().getCartFragment() != null && MainActivity.getInstance().getNumpadScanFragment() != null) {
                    Cart.INSTANCE.setOrder(o);
                    getDialog().dismiss();
                }
            }
        });

    }

    public void refreshAdapter(ArrayList<OrderPointer> orders) {
        progressBar.setVisibility(View.GONE);

        if (orders.size() > 0) {
            noOrderText.setVisibility(View.GONE);
            listHeader.setVisibility(View.VISIBLE);
            OrderListAdapter adapter = new OrderListAdapter(MainActivity.getInstance(), 0, orders);
            ListView orderList = (ListView) getDialog().findViewById(R.id.order_list);
            orderList.setAdapter(adapter);
        } else {
            noOrderText.setVisibility(View.VISIBLE);
            listHeader.setVisibility(View.GONE);
        }

    }

}
