package no.susoft.mobile.pos.ui.dialog;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.PointOfSale;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;
import no.susoft.mobile.pos.ui.adapter.PosListAdapter;

/**
 * Created by Vilde on 11.12.2015.
 */
public class PosChooserDialog extends DialogFragment {

    ListView list;
    String defaultPosCode = "100";
    String defaultPosName = "";

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

        final View view = (inflater.inflate(R.layout.pos_chooser_fragment, null));
        builder.setView(view);

        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.setCancelable(false);
        list = (ListView) getDialog().findViewById(R.id.posList);
        if (result != null) {
            setupPosList(result);
        }
    }

    ArrayList<PointOfSale> result;

    public void setCallResult(ArrayList<PointOfSale> result) {
        this.result = result;
    }

    public void setupPosList(ArrayList<PointOfSale> result) {
        final PointOfSale newPos;
        if (result != null && result.isEmpty()) {
            newPos = new PointOfSale(defaultPosCode, defaultPosName);

            AccountManager.INSTANCE.savePos(newPos);
            AppConfig.getState().setPos(newPos);
            MainActivity.getInstance().setPosChooserDialog(null);
            getDialog().dismiss();
        } else if (result != null) {
            if (list == null) {
                Log.i("vilde", "list is null");
            }
            list.setAdapter(new PosListAdapter(MainActivity.getInstance(), 0, result));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        PointOfSale pos = (PointOfSale) view.getTag();
                        AccountManager.INSTANCE.savePos(pos);
                        AppConfig.getState().setPos(pos);
                        MainActivity.getInstance().setPosChooserDialog(null);
                        getDialog().dismiss();
                    } catch (Exception ex) {
                        //handle
                    }
                }
            });
        } else {
            //you are screwed
            Log.i("vilde", "oops.");
        }
    }

}
