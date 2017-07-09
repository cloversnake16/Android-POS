package no.susoft.mobile.pos.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;

/**
 * Created by Vilde on 11.12.2015.
 */
public class AdminSettingsFragment extends Fragment {

    @InjectView(R.id.chainName)
    TextView chainName;
    @InjectView(R.id.isRestaurant)
    TextView isRestaurant;
    @InjectView(R.id.isUsingDallasKey)
    TextView isUsingDallasKey;
    @InjectView(R.id.resetSettingsButton)
    Button resetSettingsButton;
    @InjectView(R.id.pos)
    TextView pos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.admin_settings_fragment, container, false);
        ButterKnife.inject(this, rootView);

        refreshTextViews();
        setButtonListener();
        return rootView;
    }

    private void refreshTextViews() {
        try {
            if (AccountManager.INSTANCE.getSavedChain() == null || AccountManager.INSTANCE.getSavedChain().isEmpty()) {
                chainName.setText(R.string.null_text);
            } else {
                chainName.setText(AccountManager.INSTANCE.getSavedChain());
            }
            isRestaurant.setText(String.valueOf(AccountManager.INSTANCE.getSavedRestaurant()));
            isUsingDallasKey.setText((String.valueOf(AccountManager.INSTANCE.getSavedDallasKey())));
            if (AccountManager.INSTANCE.getSavedPos() != null) {
                pos.setText(AccountManager.INSTANCE.getSavedPos().getId() + " " + AccountManager.INSTANCE.getSavedPos().getName());
            } else {
                pos.setText(getString(R.string.null_text));
            }
        } catch (Exception ex) {
            Log.i("vilde", "error refreshing saved details text views");
            ex.printStackTrace();
        }
    }

    private void setButtonListener() {
        resetSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.reset_settings);
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        clearSettings();
                        refreshTextViews();
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void clearSettings() {
        AccountManager.INSTANCE.clearShopSettings();
        AccountManager.INSTANCE.clearAccountCredentials();
        AppConfig.getState().setUsingDallasKey(false);
        AppConfig.getState().setRestaurant(false);
        AppConfig.getState().setWorkshop(false);
    }

}
