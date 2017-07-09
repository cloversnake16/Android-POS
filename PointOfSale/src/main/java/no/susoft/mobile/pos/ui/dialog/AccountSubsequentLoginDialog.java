package no.susoft.mobile.pos.ui.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import jp.co.casio.vx.framework.device.IButton;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.account.AccountManager.Preferences;
import no.susoft.mobile.pos.data.Account;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AccountButtons;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;
import no.susoft.mobile.pos.ui.utils.DallasKeyAuthenticationUtils;

/**
 * This class will present the user with an interface to enter necessary account credentials.
 * <p/>
 * Input will be verified by the AccountManager.class.
 *
 * @author Yesod
 */
public final class AccountSubsequentLoginDialog extends DialogFragment implements DialogInterface.OnClickListener, DialogInterface.OnShowListener, OnClickListener, AccountManager.AccountManagerListener {

    //implements , IButton.StatCallback

    private ViewManager manager;
    private IntentFilter intentFilter = new IntentFilter("jp.co.casio.vx.regdevice.comm.POWER_RECOVERY");
    private BroadcastReceiver intentReceiver;
    private Handler iButtonHandler;
    private Thread callback;
    private IButton ibutton;
    private boolean receiverIsRegistered;

    @Override
    public void onStart() {
        super.onStart();
        //startIButton();
    }

//    private void startIButton() {
//        int ret;
//
//        ibutton = new IButton();
//        ret = ibutton.open(IButton.OPENMODE_COMMON, IButton.DEVICE_HOST_LOCALHOST);
//        if (ret < 0) {
//            // new SimpleInfoAlertDialog("IButton open error.(" + errMessage(ret) + ")"); //DEBUG
//            return;
//        }
//        iButtonHandler = new Handler();
//        ibutton.setCallback((IButton.StatCallback) this);
//    }

    private String getKeyString(byte[] data) {
        return DallasKeyAuthenticationUtils.getDallasKeyStringFromBytes(data);
    }
//
//    private void registerReceiverIfNotDoneAlready() {
//        if (!receiverIsRegistered) {
//            MainActivity.getInstance().registerReceiver(intentReceiver, intentFilter);
//            receiverIsRegistered = true;
//        }
//    }

//    @Override
//    public void onChangeIbutton(final boolean stat, final byte[] data) {
//        Log.i("vilde", "on change in sublogin");
//        if (data != null) {
//            Log.i("vilde", "IButtonChange");
//            iButtonHandler.post(new Runnable() {
//                @TargetApi(Build.VERSION_CODES.KITKAT)
//                @Override
//                public void run() {
//                    if (stat) {
//                        try {
//                            Log.i("vilde", "IButtonChange is true");
//                            //removeReceiver();
//                            MainActivity.getInstance().dallasKey.performIButtonExistsAction(data);
//                        } catch (Exception ex) {
//                        }
//                    } else {
//                        MainActivity.getInstance().dallasKey.performMainIButtonRemovedAction();
//                    }
//                    registerReceiverIfNotDoneAlready();
//                }
//            });
//        }
//    }

    /**
     * Helper class to manage this view.
     */
    private class ViewManager {

        private final EditText form_username;
        private final EditText form_password;
		private final EditText form_license;
        private final TextView form_license_label;
        private final View firewall;
        private final ImageView button_eye;
        private Button button_verify;
        private CheckBox remember;
        private final SharedPreferences preferences;
        boolean isMaskingPassword = true;

        /**
         * @param view
         */
        public ViewManager(View view, OnClickListener listener) {
            // Load up the preferences.
            this.preferences = SusoftPOSApplication.getContext().getSharedPreferences(Account.class.toString(), Context.MODE_PRIVATE);

            // Find form fields.
            this.form_username = (EditText) view.findViewById(R.id.account_dialog_form_username);
            this.form_password = (EditText) view.findViewById(R.id.account_dialog_form_password);
			this.form_license = (EditText) view.findViewById(R.id.account_dialog_form_license);
            this.form_license_label = (TextView) view.findViewById(R.id.account_dialog_form_licenseLabel);

            // Find the eye.
            this.button_eye = (ImageView) view.findViewById(R.id.account_action_masking);
            this.button_eye.getDrawable().setLevel(0);
            this.button_eye.setId(R.id.account_action_masking);
            this.button_eye.setOnClickListener(listener);

            // Find the "REMEMBER' checkbox,
            this.remember = (CheckBox) view.findViewById(R.id.account_action_remember);
            this.remember.setId(R.id.account_action_remember);
            this.remember.setOnClickListener(listener);
            this.remember.setChecked(preferences.getBoolean(Preferences.REMEMBER.toString(), false));

            // Preset last successful login details and determine which field has default focus.
            this.preparedField(this.form_password, null);
            this.preparedField(this.form_username, preferences.getString(Preferences.USERNAME.toString(), null));
			this.preparedField(this.form_license, preferences.getString(Preferences.LICENSE.toString(), null));
            checkLicenseFieldToHideIfHasSavedValue();

            // Find the "firewall"
            this.firewall = view.findViewById(R.id.account_dialog_progress);
        }

        private void checkLicenseFieldToHideIfHasSavedValue() {
            if (AccountManager.INSTANCE.getSavedLicense() != null && !AccountManager.INSTANCE.getSavedLicense().isEmpty()) {
                this.form_license.setVisibility(View.GONE);
                this.form_license_label.setVisibility(View.GONE);
            }
        }

        /**
         * Set 'value' as text for 'field'. If 'value' is null or empty, 'field' is given focus.
         */
        private void preparedField(EditText field, String value) {
            field.setText(value);
            // If there is no value, give this field focus/
            if (value == null || value.trim().length() == 0) {
                field.requestFocus();
            }
        }

        //TODO: make this obsolete by manually implementing a button on the layout.
        public void onShow(AlertDialog alertdialog, OnClickListener listener) {
            // Since we want to validate the user input before closing the dialog, it became necessary to
            // bind onClick listeners to the button(s) /after/ the invoke of the show() routine contained
            // by the dialog. This way, we can keep the dialog from automatically dismissing.

            //
            this.button_verify = alertdialog.getButton(AlertDialog.BUTTON_POSITIVE);
            this.button_verify.setId(AlertDialog.BUTTON_POSITIVE);
            this.button_verify.setOnClickListener(listener);

            // Is the account manager by chance already authorizing?
            if (AccountManager.INSTANCE.isAuthorizing()) {
                this.setAuthorizing(true);
            } else {
                this.setAuthorizing(false);
            }
        }

        /**
         * Invoked when the given 'account' is authoried.
         *
         * @param account
         */
        public void onAccountAuthoriziation(Account account) {
            // Are we saving account credentials?
            if (this.remember.isChecked()) {
                // Get the preferences editor:
                final SharedPreferences.Editor editor = this.preferences.edit();
                editor.putString(Preferences.USERNAME.toString(), account.getLogin());
                editor.putString(Preferences.CHAIN.toString(), account.getChainName());
                editor.putString(Preferences.SHOP.toString(), account.getShop().getName());
                editor.putString(Preferences.SHOP_ORG_NO.toString(), account.getShop().getOrgNo());
                editor.putString(Preferences.SHOP_PHONE.toString(), account.getShop().getPhone());
                editor.putString(Preferences.SHOP_ADDRESS.toString(), account.getShop().getAddress());
                editor.putString(Preferences.SHOP_ZIP.toString(), account.getShop().getZip());
                editor.putString(Preferences.SHOP_CITY.toString(), account.getShop().getCity());
                editor.commit();
            }
        }

        /**
         * Save the state of the remember checkbox.
         */
        public void onRequestToggleRemember() {
            // Get the preferences editor:
            final SharedPreferences.Editor editor = this.preferences.edit();
            // Save the REMEMBER state.
            editor.putBoolean(Preferences.REMEMBER.toString(), this.remember.isChecked());
            // Clear account credentials.
            editor.remove(Preferences.USERNAME.toString());
            editor.remove(Preferences.CHAIN.toString());
            editor.remove(Preferences.SHOP.toString());
            editor.remove(Preferences.SHOP_ORG_NO.toString());
            editor.remove(Preferences.SHOP_PHONE.toString());
            editor.remove(Preferences.SHOP_ADDRESS.toString());
            editor.remove(Preferences.SHOP_ZIP.toString());
            editor.remove(Preferences.SHOP_CITY.toString());
            editor.commit();
        }

        /**
         * Toggle the visibility of the password mask.
         */
        public void onRequestToggleMaskPassword() {
            int s = this.form_password.getSelectionStart();
            int e = this.form_password.getSelectionEnd();

            if (this.isMaskingPassword) {
                this.form_password.setTransformationMethod(null);
                this.button_eye.getDrawable().setLevel(1);
                this.isMaskingPassword = false;
            } else {
                this.form_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                this.button_eye.getDrawable().setLevel(0);
                this.isMaskingPassword = true;
            }
            // Place the cursor just after the input field text, if any.
            this.form_password.setSelection(s, e);
        }

        /**
         * Get the user.
         *
         * @return
         */
        public String getUser() {
            return this.form_username.getText().toString().replaceAll("\\s", "");//.trim();
        }

        /**
         * Get the chain.
         *
         * @return
         */
        public String getLicense() {
            return this.form_license.getText().toString().replaceAll("\\s", "");//.trim();
        }

        /**
         * Get the password.
         *
         * @return
         */
        public String getPassword() {
            return this.form_password.getText().toString().replaceAll("\\s", "");//.trim();
        }

        /**
         * Display to the user that the AccountManager is authorizing their submitted credentials.
         */
        public void setAuthorizing(boolean isAuthorizing) {
            if (isAuthorizing) {
                // Disable the verify button.
                this.button_verify.setEnabled(false);
                // Block user input.
                this.firewall.setVisibility(View.VISIBLE);
                this.form_username.setEnabled(false);
                this.form_password.setEnabled(false);
				this.form_license.setEnabled(false);
                this.button_eye.setEnabled(false);
                this.remember.setEnabled(false);
            } else {
                // Enable the verify button.
                this.button_verify.setEnabled(true);
                this.form_username.setEnabled(true);
                this.form_password.setEnabled(true);
				this.form_license.setEnabled(true);
                this.button_eye.setEnabled(true);
                this.remember.setEnabled(true);
                // Allow user input.
                this.firewall.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Called to build a custom dialog container.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get Builder.
        Builder builder = new AlertDialog.Builder(this.getActivity());
        // Set positive button. TODO: Deprecate this in favor of a button that is contained /inside/ the layout.
        builder.setPositiveButton(R.string.account_dialog_button_positive, this);

        if (!AccountManager.INSTANCE.getLoggedInAccounts().isEmpty()) {
            builder.setNegativeButton(R.string.account_dialog_button_negative, this);
        }

        // Inflate the custom view:
        View view = this.getActivity().getLayoutInflater().inflate(R.layout.account_dialog_for_dialog, null);

        // Set our custom View.
        builder.setView(view);
        // Manager
        this.manager = new ViewManager(view, this);

        AlertDialog dialog = builder.create();
        // To prevent dismiss on outside touch.
        dialog.setCanceledOnTouchOutside(false);
        // To prevent dismiss on back key pressed.
        dialog.setCancelable(false);
        // To prevent dismiss on button press.
        dialog.setOnShowListener(this);
        // Done.
        if (AppConfig.getState().isUsingDallasKey()) {
            this.setCancelable(false);
        }

        // TO DIM THE BACKGROUND IF USED AS MAIN LOGIN DIALOG
        //if(AccountManager.INSTANCE.getLoggedInAccounts() == null || AccountManager.INSTANCE.getLoggedInAccounts().size() <=1) {

        //dialog.getWindow().setDimAmount(1.0f);
        //}

        return dialog;
    }

    /**
     * Called when the dialog will be shown.
     */
    @Override
    public void onShow(DialogInterface dialog) {
        this.manager.onShow((AlertDialog) dialog, this);
        AccountManager.INSTANCE.addListener(this);
    }

    /**
     * (non-Javadoc)
     *
     * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case AlertDialog.BUTTON_POSITIVE: {
                //Does not appear to be in use
            }
        }
    }

    private boolean validateAccess(String userName) {
        try {
            if (AppConfig.getState().isUsingDallasKey()) {
                if (userName.substring(0, 3).equals("998")) {
                    return true;
                } else {
                    Toast.makeText(MainActivity.getInstance(), R.string.please_use_dallas_key, Toast.LENGTH_LONG).show();
                    return false;
                }
            } else {
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        // Only the verify button should invoke this method.
        switch (v.getId()) {
            case AlertDialog.BUTTON_POSITIVE: {

                AppConfig.getState().setAdminLoggingInWithCredentials(true);

                if (!AccountManager.INSTANCE.isAuthorizing()) {
                    String u = this.manager.getUser();
                    String p = this.manager.getPassword();
					String l = this.manager.getLicense();

                    // Verify that there was an input.
                    if (u == null || u.isEmpty()) {
                        Toast.makeText(this.getActivity(), R.string.account_dialog_form_missing_username, Toast.LENGTH_LONG).show();
                        return;
                    }
                    // Verify that there was an input.
                    if (p == null || p.isEmpty()) {
                        Toast.makeText(this.getActivity(), R.string.account_dialog_form_missing_password, Toast.LENGTH_LONG).show();
                        return;
                    }
                    // Verify that there was an input.
                    if (l == null || l.isEmpty()) {
                        Toast.makeText(this.getActivity(), R.string.account_dialog_form_missing_license, Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (validateAccess(u)) {

                        // Block further user input to this dialog.
                        this.manager.setAuthorizing(true);

                        // Notify the account manager.
                        AccountManager.INSTANCE.onRequestAccountAuthorization(u, p, l);
                    }

                }
                break;
            }
            case AlertDialog.BUTTON_NEGATIVE: {
                System.out.println("CANCEL CLICK !!!!");
                this.onDismiss();
            }
            case R.id.account_action_masking: {
                this.manager.onRequestToggleMaskPassword();
                break;
            }
            case R.id.account_action_remember: {
                this.manager.onRequestToggleRemember();
                break;
            }
        }
    }

    /**
     * Indicate that there has been a response.
     */
    @Override
    public void onAccountAuthorizationChange() {
        // Was the authorization successful?
        if (AccountManager.INSTANCE.isActiveAccountAuthorized()) {

            // Notify -- this will save account credentials given that the user wishes.
            this.manager.onAccountAuthoriziation(AccountManager.INSTANCE.getAccount());
            // Done.
            this.onDismiss();
        } else {
            this.manager.setAuthorizing(false);
            if (MainActivity.getInstance().isConnected()) {
                Toast.makeText(SusoftPOSApplication.getContext(), "Authorization Failed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SusoftPOSApplication.getContext(), "Network Unavailable", Toast.LENGTH_LONG).show();
            }

        }
    }

    public void removeReceiverIfExists() {
        try {
            receiverIsRegistered = (DallasKeyAuthenticationUtils.unregisterIButton(MainActivity.getInstance(), ibutton, intentReceiver));
        } catch (Exception ex) {
            Log.i("vilde", "tried remove ibutton receiver that didnt exits");
        }
    }

    /**
     * Dismiss this dialog.
     */
    private void onDismiss() {
        // Is there a dialog?

        if (this.getDialog() != null) {

            if (AccountManager.INSTANCE.getLoggedInAccounts().size() == 1) {
                MainActivity.getInstance().getMainShell().setupUIElements();
            }
            AccountButtons.getInstance().updateButtons();
            //Remove listener
            AccountManager.INSTANCE.removeListener(this);
            if (this.getDialog().isShowing()) {
                MainActivity.getInstance().initializePos();
                this.dismiss();
            }
        }
    }

}