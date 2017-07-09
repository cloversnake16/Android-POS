package no.susoft.mobile.pos.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class CustomerSearchDialog extends DialogFragment {

    private View searchButton;
    private View progressBar;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = (inflater.inflate(R.layout.customer_search_dialog, null));
        builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        }).setPositiveButton(R.string.no_customer, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Cart.INSTANCE.setOrderCustomer(null);
            }
        });

		if (MainActivity.getInstance().isConnected()) {
			builder.setNeutralButton(R.string.new_customer, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					AddCustomerDialog dialog2 = new AddCustomerDialog();
					dialog2.show(MainActivity.getInstance().getSupportFragmentManager(), "addcustomer");
				}
			});
		}

        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBarCustomerSearch);
        final EditText searchText = (EditText) view.findViewById(R.id.customer_search_text);
        final Button searchButton = (Button) view.findViewById(R.id.customer_search_button);
        final LinearLayout listHeader = (LinearLayout) view.findViewById(R.id.customer_list_header);

        listHeader.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                MainActivity.getInstance().getServerCallMethods().searchCustomer(searchText.getText().toString().trim());

                //InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
            }
        });
        // Create the AlertDialog object and return it

        searchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            searchButton.callOnClick();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        return builder.create();
    }

}
