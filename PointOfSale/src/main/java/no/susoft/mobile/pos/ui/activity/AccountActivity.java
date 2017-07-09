package no.susoft.mobile.pos.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import jp.co.casio.vx.framework.device.IButton;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.ui.activity.util.DallasKey;
import no.susoft.mobile.pos.ui.activity.util.IDallasKey;
import no.susoft.mobile.pos.ui.activity.util.SecondaryDisplay;

/**
 * Activity for the user to input account credentials.
 *
 * @author Yesod
 */
public final class AccountActivity extends Activity implements OnClickListener, AccountManager.AccountManagerListener, IButton.StatCallback, IDallasKey {

    private AccountViewManager manager;
    private boolean isDismissing = true;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.account_dialog);
    }

    DallasKey dallasKey;
    private void startDallasKey() {
        if(dallasKey == null) {
            dallasKey = new DallasKey();
        }
        dallasKey.startIButton(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(SecondaryDisplay.getInstance() == null) {
            new SecondaryDisplay();
        }
        SecondaryDisplay.getInstance().initializeSecondaryDisplay(this);
        startDallasKey();
        setDismissOurself();
        setAccountViewManager();
        setAccountActivityContext();
        addAccountManagerListener();
		if (MainActivity.getInstance() != null && MainActivity.getInstance().isNeedToUpdate()) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setVisualStateForUpdate();
				}
			});
		}

        //authenticateUserWithIButton(this, "00001854287F", "cornelia"); // to simulate login by key, green (IDA)
        //		authenticateUserWithIButton(this, "000018540097", "cornelia"); // to simulate login by admin key
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshManager();
        startDallasKey();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeAccountManagerListener();

        dallasKey.removeReceiverIfExists(this);

    }

    @Override
    public void onPause() {
        super.onPause();

        dallasKey.removeReceiverIfExists(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.account_action_authenticate: {
                authenticateUser();
                break;
            }
            case R.id.account_action_update: {
				manager.progress.setVisibility(View.VISIBLE);
                MainActivity.getInstance().checkForUpdate();
                break;
            }
            case R.id.account_action_masking: {
                toggleMaskPassword();
                break;
            }
            case R.id.account_action_remember: {
                toggleRemember();
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        doubleClickBackToExit();
    }

    private void doubleClickBackToExit() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            MainActivity.getInstance().finish();
            this.finish();
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public void onChangeIbutton(final boolean b, final byte[] data) {
        if (data != null) {
            dallasKey.onChangeIButton(this, "AccountActivity", b, data);
        }
    }

    private void setDismissOurself() {
        try {
            this.isDismissing = this.getIntent().getExtras().getBoolean("DISMISS");
        } catch (Exception x) {
            //else what?
        }
    }

    private void setAccountViewManager() {
        this.manager = new AccountViewManager(this);
    }

    private void setAccountActivityContext() {
		if (MainActivity.getInstance() != null) {
			MainActivity.getInstance().setAccountActivityContext(this);
		} else {
            this.finish();
		}
    }

    public void refreshThisAuthorizingState(String key, String license) {
        if (AccountManager.INSTANCE.onRequestAccountAuthorization(key, license)) {
            this.manager.refreshThisAuthorizingState();
        }
    }

    private void authenticateUser() {
        if (!AccountManager.INSTANCE.isAuthorizing()) {
            String u = this.manager.getUser();
            String p = this.manager.getPassword();
            String l = this.manager.getLicense();

            if (!verifyInputs(u, p, l)) {
                return;
            }
            notifyAccountManager(u, p, l);
        }
    }

	public void setVisualStateForUpdate() {
		manager.setVisualStateForUpdate();
	}

    public String getLicenseForKeyAuthentication() {
        if (manager.getLicense().length() > 0) {
            return manager.getLicense();
        } else {
            Toast.makeText(this, R.string.write_license_before_using_key, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void toggleMaskPassword() {
        this.manager.onRequestToggleMaskPassword();
    }

    private void toggleRemember() {
        this.manager.onRequestToggleRemember();
    }

    private void notifyAccountManager(String u, String p, String l) {
        if (AccountManager.INSTANCE.onRequestAccountAuthorization(u, p, l)) {
            this.manager.refreshThisAuthorizingState();
        }
    }

    private boolean verifyInputs(String u, String p, String l) {

        // Verify that there was an input.
        if (u == null || u.isEmpty()) {
            Toast.makeText(this, R.string.account_dialog_form_missing_username, Toast.LENGTH_LONG).show();
            return false;
        }
        // Verify that there was an input.
        if (p == null || p.isEmpty()) {
            Toast.makeText(this, R.string.account_dialog_form_missing_password, Toast.LENGTH_LONG).show();
            return false;
        }
        // Verify that there was an input.
        if (l == null || l.isEmpty()) {
            Toast.makeText(this, R.string.account_dialog_form_missing_license, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void addAccountManagerListener() {
        AccountManager.INSTANCE.addListener(this);
    }

    @Override
    public void onAccountAuthorizationChange() {
		if (AccountManager.INSTANCE.isActiveAccountAuthorized()) {
            removeAccountManagerListener();
            finishAuthenticationAndReturnToMain();
        } else {
            refreshManager();
        }
    }

    private void removeAccountManagerListener() {
        AccountManager.INSTANCE.removeListener(this);
        refreshManager();
    }

    private void refreshManager() {
        this.manager.refreshThisAuthorizingState();
    }

    private void finishAuthenticationAndReturnToMain() {
        dallasKey.removeReceiverIfExists(this);

        if (this.isDismissing) {
            this.setResult(RESULT_OK);
            MainActivity.getInstance().setAccountActivityContext(null);
            this.finish();
        } else {
            this.startActivity(new Intent(this, MainActivity.class));
        }
    }

    /**
     * Initialization
     * Ask the user to supply account credentials.
     */
    public static void start(Activity activity, boolean can_dismiss_self) {
        activity.startActivity(new Intent(activity, AccountActivity.class).putExtra("DISMISS", can_dismiss_self));
    }


    @Override
    public void registerDallasKeyReceiver(BroadcastReceiver intentReceiver, IntentFilter intentFilter) {
        this.registerReceiver(intentReceiver, intentFilter);
    }

    /**
     * Helper class to manage this view.
     */
    private static class AccountViewManager {

        private final AccountActivity host;
        // The account user name field.
        private final EditText form_username;
        // The account license field.
        private final EditText form_license;
        // The account password field.
        private final EditText form_password;
        // The account password mask button.
        private final ImageView button_eye;
        // The account progress indicator.
        private final View progress;
        // The account authenticate button.
        private final View button_authenticate;
        // Update button.
        private final View button_update;
        // The account remember check-box.
        private final CheckBox remember;
        // Whether password is being masked.
        boolean isMaskingPassword = true;
        private final TextView form_license_label;

        /**
         * @param v
         */
        public AccountViewManager(final AccountActivity v) {
            this.host = v;

            this.form_username = (EditText) this.host.findViewById(R.id.account_dialog_form_username);
            this.form_password = (EditText) this.host.findViewById(R.id.account_dialog_form_password);
            this.form_license_label = (TextView) this.host.findViewById(R.id.account_dialog_form_label);
            this.form_license = (EditText) this.host.findViewById(R.id.account_dialog_form_license);

            this.button_eye = (ImageView) this.host.findViewById(R.id.account_action_masking);
            this.button_eye.getDrawable().setLevel(0);
            this.button_eye.setId(R.id.account_action_masking);
            this.button_eye.setOnClickListener(this.host);

            this.remember = (CheckBox) this.host.findViewById(R.id.account_action_remember);
            this.remember.setId(R.id.account_action_remember);
            this.remember.setOnClickListener(this.host);
            this.remember.setChecked(AccountManager.INSTANCE.isRememberingAccountCredentials());

            this.button_authenticate = this.host.findViewById(R.id.account_action_authenticate);
            this.button_authenticate.setId(R.id.account_action_authenticate);
            this.button_authenticate.setOnClickListener(this.host);

            this.button_update = this.host.findViewById(R.id.account_action_update);
            this.button_update.setId(R.id.account_action_update);
            this.button_update.setOnClickListener(this.host);

            setPreparedFields();

            this.progress = this.host.findViewById(R.id.account_dialog_progress);

            final TextView version = (TextView) this.host.findViewById(R.id.app_version);
            version.setText(String.format("Beta %1$s", SusoftPOSApplication.getVersionName()));
        }

        private void setPreparedFields() {
            this.preparedField(this.form_password, null);
            this.preparedField(this.form_username, AccountManager.INSTANCE.getSavedUsername());
            this.preparedField(this.form_license, AccountManager.INSTANCE.getSavedLicense());
            checkLicenseFieldToHideIfHasSavedValue();
        }

        private void checkLicenseFieldToHideIfHasSavedValue() {
            if (AccountManager.INSTANCE.getSavedLicense() != null && !AccountManager.INSTANCE.getSavedLicense().isEmpty()) {
                this.form_license.setVisibility(View.GONE);
                this.form_license_label.setVisibility(View.GONE);
            }
        }

        private void preparedField(final EditText field, final String value) {
            field.setText(value);

            if (value == null || value.trim().length() == 0) {
                field.requestFocus();
            }
        }

        /**
         * Toggle the visibility of the password mask.
         */
        public void onRequestToggleMaskPassword() {
            int s = this.form_password.getSelectionStart();
            int e = this.form_password.getSelectionEnd();

            if (this.isMaskingPassword) {
                unmaskPassword();
            } else {
                maskPassword();
            }
            // Place the cursor just after the input field text, if any.
            this.form_password.setSelection(s, e);
        }

        private void unmaskPassword() {
            this.form_password.setTransformationMethod(null);
            this.button_eye.getDrawable().setLevel(1);
            this.isMaskingPassword = false;
        }

        private void maskPassword() {
            this.form_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            this.button_eye.getDrawable().setLevel(0);
            this.isMaskingPassword = true;
        }

        /**
         * Save the state of the remember check box.
         */
        public void onRequestToggleRemember() {
            AccountManager.INSTANCE.setRememberingAccountCredentials(this.remember.isChecked());
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
         * Get the license.
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
			if (MainActivity.getInstance() != null && !MainActivity.getInstance().isNeedToUpdate()) {
				if (isAuthorizing) {
					this.progress.setVisibility(View.VISIBLE);
					setVisualEnabledState(false);
				} else {
					this.progress.setVisibility(View.INVISIBLE);
					setVisualEnabledState(true);
				}
			}
        }

        private void setVisualEnabledState(boolean bool) {
            this.button_authenticate.setEnabled(bool);
            this.form_username.setEnabled(bool);
            this.form_license.setEnabled(bool);
            this.form_password.setEnabled(bool);
            this.button_eye.setEnabled(bool);
            this.remember.setEnabled(bool);
        }

        private void setVisualStateForUpdate() {
            this.button_update.setVisibility(View.VISIBLE);
            this.button_authenticate.setVisibility(View.GONE);
            this.form_username.setEnabled(false);
            this.form_license.setEnabled(false);
            this.form_password.setEnabled(false);
            this.button_eye.setEnabled(false);
            this.remember.setEnabled(false);
        }

        public void refreshThisAuthorizingState() {
            this.setAuthorizing(AccountManager.INSTANCE.isAuthorizing());
        }
    }

}