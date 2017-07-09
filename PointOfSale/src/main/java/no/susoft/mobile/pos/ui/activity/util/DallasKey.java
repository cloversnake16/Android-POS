package no.susoft.mobile.pos.ui.activity.util;

import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import jp.co.casio.vx.framework.device.IButton;
import jp.co.casio.vx.framework.device.IButton.StatCallback;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.ui.activity.AccountActivity;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.AdminDallasKeyFragment;
import no.susoft.mobile.pos.ui.utils.DallasKeyAuthenticationUtils;

import static no.susoft.mobile.pos.ui.utils.DallasKeyAuthenticationUtils.getDallasKeyStringFromBytes;
import static no.susoft.mobile.pos.ui.utils.DallasKeyAuthenticationUtils.unregisterIButton;

public class DallasKey {

    private boolean receiverIsRegistered;

    private IButton ibutton;
    private Handler iButtonHandler;
    private BroadcastReceiver iButtonIntentReceiver;


    public IButton getIButton() {
        return this.ibutton;
    }
    public void setIButton(IButton ibutton) {
        this.ibutton = ibutton;
    }
    public Handler getIButtonHandler() {
        return this.iButtonHandler;
    }
    public void setIButtonHandler(Handler iButtonHandler) {
        this.iButtonHandler = iButtonHandler;
    }
    public BroadcastReceiver getIntentReceiver() {
        return this.iButtonIntentReceiver;
    }


    public <T extends IDallasKey & StatCallback> void startIButton(T activity) {

        setIButton(new IButton());
        int ret = getIButton().open(IButton.OPENMODE_COMMON, IButton.DEVICE_HOST_LOCALHOST);
        if (ret < 0) {
            return; //error
        }

        setIButtonHandler(new Handler());
        getIButton().setCallback((StatCallback) activity);
    }

    public <T extends IDallasKey & StatCallback> void onChangeIButton(final T context, final String activityString, final boolean keyOn, final byte[] data) {
        if (data != null) {
            Log.i("vilde", "IButtonChange");
            getIButtonHandler().post(new Runnable() {
                @TargetApi(Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    if (!AppConfig.getState().isAdminLoggingInWithCredentials()) {
                        try {
                            performIButtonExistsAction(activityString, keyOn, data);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        registerReceiverIfNotDoneAlready(context);
                    }
                }
            });
        }
    }


    public <T extends IDallasKey, StatCallback> void registerReceiverIfNotDoneAlready(T activity) {
        if (!receiverIsRegistered) {
            activity.registerDallasKeyReceiver(getIntentReceiver(), IDallasKey.getIntentFilter);
            Log.i("vilde", "registered button");
            receiverIsRegistered = true;
        }
    }

    public <T extends IDallasKey & StatCallback> void removeReceiverIfExists(T activity) {
        try {
            receiverIsRegistered = (DallasKeyAuthenticationUtils.unregisterIButton((Context) activity, getIButton(), getIntentReceiver()));
        } catch (Exception ex) {
            Log.i("vilde", "tried remove ibutton receiver that didnt exits");
        }
    }

    private void performIButtonExistsAction(String activity, boolean keyOn, byte[] data) {
        Log.i("vilde", "it does button");
        switch(activity) {
            case "MainActivity": {
                performMainActivity(keyOn, data);
                break;
            }
            case "AccountActivity": {
                performAccountActivity(keyOn, data);
                break;
            }
            case "AdminDallasKeyFragment": {
                performAdminDallasKey(keyOn, data);
                break;
            }
        }
    }

    private void performAdminDallasKey(final boolean keyOn, final byte[] data) {
        final AdminDallasKeyFragment fragment = MainActivity.getInstance().getAdminDallasKeyFragment();

        final String previousKey = fragment.getLastKey();
        Log.i("vilde", "IButtonChange admin dallas");

        getIButtonHandler().post(new Runnable() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    if (containsOnlyZero(getDallasKeyStringFromBytes(data))) {
                        if (previousKey.isEmpty()) {
                            fragment.setLastKey(AccountManager.INSTANCE.getAccount().getSecurityCode());
                        }
                    } else {
                        fragment.setLastKey(getDallasKeyStringFromBytes(data));
                    }

                    if (!AppConfig.getState().isAdminLoggingInWithCredentials() && !fragment.isWaitingForKeyInput) {
//                        if (!keyOn && previousKey.equals(AccountManager.INSTANCE.getAccount().getSecurityCode())) {
//                            unregisterIButton(fragment.getActivity(), getIButton(), getIntentReceiver());
//                            fragment.getActivity().onBackPressed();
//                        }
                    } else if (fragment.isWaitingForKeyInput) {
                        Log.i("vilde", "waiting on key for input");
                        if (keyOn) {
                            if (MainActivity.getInstance().getAdminDallasKeyFragment().isWaitingForKeyInput) {
                                try {
                                    fragment.setInputText(getDallasKeyStringFromBytes(data));
                                } catch (Exception ex) {
                                }
                                return;
                            }
                        }
                    }
                } catch (Exception ex) {
                }
            }
        });

    }

    private boolean containsOnlyZero(String key) {
        if (Pattern.matches("[0]+", key)) {
            return true;
        } else {
            return false;
        }
    }

    private void performAccountActivity(boolean keyOn, byte[] data) {
        try {
            if(keyOn) {
                AccountActivity acc = MainActivity.getInstance().getAccountActivity();
                if (data != null) {
                    String str = DallasKeyAuthenticationUtils.getDallasKeyStringFromBytes(data);
                    String license = acc.getLicenseForKeyAuthentication();
                    if (license == null) {
                        registerReceiverIfNotDoneAlready(acc);
                        Toast.makeText(acc, R.string.write_license_before_using_key, Toast.LENGTH_LONG).show();
                        Log.i("vilde", "could not find the license key");
                        return;
                    }

                    receiverIsRegistered = DallasKeyAuthenticationUtils.unregisterIButton(acc, getIButton(), getIntentReceiver());
                    DallasKeyAuthenticationUtils.authenticateUserWithIButton(acc, str, license);
                }
            }

        } catch (Exception ex) {
            //new SimpleInfoAlertDialog(ex.getMessage());
        }
    }

    private void performMainActivity(boolean keyOn, byte[] data) {
        if(keyOn) {
            if (AccountManager.INSTANCE.getLoggedInAccounts().isEmpty()) {
                if (getLicenseFromDialog() != null) {
                    DallasKeyAuthenticationUtils.authenticateUserWithIButton(MainActivity.getInstance(), getKeyString(data), getLicenseFromDialog());
                } else {
                    Toast.makeText(MainActivity.getInstance(), R.string.write_license_before_using_key, Toast.LENGTH_LONG).show();
                    return;
                }

            } else {
                DallasKeyAuthenticationUtils.authenticateUserWithIButton(MainActivity.getInstance(), getKeyString(data), getLicenseFromExistingUser());
            }

            MainActivity.getInstance().getMainShell().setupUIElements();
            Log.i("vilde", "set up ui");
        } else {
            performMainIButtonRemovedAction();
        }
    }

    private void performMainIButtonRemovedAction() {
        AccountManager.INSTANCE.logoutAccount();
        if (MainActivity.getInstance().getPosChooserDialog() != null && MainActivity.getInstance().getPosChooserDialog().getDialog() != null) {
            MainActivity.getInstance().getPosChooserDialog().dismissAllowingStateLoss();
        }
    }

    private String getKeyString(byte[] data) {
        return DallasKeyAuthenticationUtils.getDallasKeyStringFromBytes(data);
    }

    private static String getLicenseFromExistingUser() {
        return AccountManager.INSTANCE.getAccount().getLicense();
    }

    public String getLicenseFromDialog() {
        if (MainActivity.getInstance().getLoginDialog() != null) {
            EditText licenseText = (EditText) MainActivity.getInstance().getLoginDialog().getDialog().getWindow().findViewById(R.id.account_dialog_form_license);
            if (!licenseText.getText().toString().isEmpty()) {
                return licenseText.getText().toString().trim();
            }
        }
        return null;
    }
}
