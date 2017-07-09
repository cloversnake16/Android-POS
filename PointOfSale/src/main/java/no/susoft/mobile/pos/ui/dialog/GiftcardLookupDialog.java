package no.susoft.mobile.pos.ui.dialog;

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
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.server.GetPrepaidsAsync;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.GiftcardListAdapter;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static no.susoft.mobile.pos.SusoftPOSApplication.getContext;
import static no.susoft.mobile.pos.ui.dialog._DialogUtils.getDateErrorMessage;
import static no.susoft.mobile.pos.ui.dialog._DialogUtils.getDateFromDatePicker;
import static no.susoft.mobile.pos.ui.dialog._DialogUtils.setVisibility;

/**
 * Created by Vilde on 15.02.2016.
 */
public class GiftcardLookupDialog extends DialogFragment {
    ProgressBar progressBar;
    LinearLayout listHeader;
    ListView list;
    private Button searchButton;
    private DatePicker fromDate;
    private DatePicker toDate;


    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    private TextView emptyListText;


    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = (inflater.inflate(R.layout.giftcard_lookup_dialog, null));
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
        MainActivity.getInstance().setGiftcardLookupDialog(this);

        progressBar = (ProgressBar) getDialog().findViewById(R.id.progress_bar_giftcard);
        progressBar.setVisibility(View.GONE);

        fromDate = (DatePicker) getDialog().findViewById(R.id.giftcard_date_from);
        toDate = (DatePicker) getDialog().findViewById(R.id.giftcard_date_to);

        searchButton = (Button) getDialog().findViewById(R.id.giftcard_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                new GetPrepaidsAsync().execute(df.format(getDateFromDatePicker(fromDate)), df.format(getDateFromDatePicker(toDate)));
            }
        });

        emptyListText = (TextView) getDialog().findViewById(R.id.no_giftcards);

        listHeader = (LinearLayout) getDialog().findViewById(R.id.giftcard_list_header);
        listHeader.setVisibility(View.GONE);
        list = (ListView) getDialog().findViewById(R.id.giftcard_lookup_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.hasOrdersWithLines()) {
                    try {
                        Prepaid p = (Prepaid) list.getItemAtPosition(i);
                        if(!p.getAmount().isZero()) {
                            MainActivity.getInstance().getCartFragment().getCartButtons().togglePayView();
                            MainActivity.getInstance().getNumpadPayFragment().showPayWithGiftCardDialog(p);
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), getContext().getString(R.string.giftcard_has_zero_balance), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.cannot_add_giftcard_to_empty_cart), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void updateNewResults(List<Prepaid> prepaids) {
        progressBar.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);

        if (prepaids.size() > 0) {
            emptyListText.setVisibility(View.GONE);
            setVisibility(View.VISIBLE, list, listHeader);

            GiftcardListAdapter adapter = new GiftcardListAdapter(MainActivity.getInstance(), 0, prepaids);
            ListView giftcardList = (ListView) getDialog().findViewById(R.id.giftcard_lookup_list);
            giftcardList.setAdapter(adapter);
        } else {
            setVisibility(View.GONE, list, listHeader);
            emptyListText.setText(getDateErrorMessage(getDateFromDatePicker(fromDate), getDateFromDatePicker(toDate)));
            emptyListText.setVisibility(View.VISIBLE);
        }

    }

}
