package no.susoft.mobile.pos.ui.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import jp.co.casio.vx.framework.device.IButton;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Account;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AccountButtons;

public class DallasKeyAuthenticationUtils {

    private static String formatDallasKeyString(String str) {
        //Might need to extend to end at 18 instead to include "fa"? Not sure. This returns the number that is printed on the key
        str = str.substring(4, 16);
        return str;
    }

    public static String getDallasKeyStringFromBytes(byte[] bytes) {
        return formatDallasKeyString(BytesToHex.bytesToHex(bytes));
    }


    public static boolean unregisterIButton(Context context, IButton ibutton, BroadcastReceiver intentReceiver) {
        if (ibutton != null) {
            ibutton.setCallback(null);
            ibutton.close();
        }
        try {
            context.unregisterReceiver(intentReceiver);  //destroy
        } catch (Exception ex) {

        }
        return false;
    }

    public static void authenticateUserWithIButton(Context context, String key, String license) {
        if (thereAreAccountsLoggedIn()) {
            if (!keyBelongsToCurrentlyActiveUser(key)) {
                if (!keyBelongsToInactiveLoggedInUser(context, key)) {
                    loginNewUser(context, key, license);
                }
            } //TODO should probably log out all users and then log in new user if there is anyone logged in
        } else {
            loginNewUser(context, key, license);
        }
    }

    private static void loginNewUser(Context context, String key, String license) {
        AccountManager.INSTANCE.logInWithDallasKey(context, key, license);
    }

    private static boolean thereAreAccountsLoggedIn() {
        return AccountManager.INSTANCE.getLoggedInAccounts() != null && AccountManager.INSTANCE.getLoggedInAccounts().size() > 0;
    }

    private static boolean keyBelongsToCurrentlyActiveUser(String key) {
        return AccountManager.INSTANCE.getAccount().getSecurityCode() != null && AccountManager.INSTANCE.getAccount().getSecurityCode().equals(key);
    }

    private static boolean keyBelongsToInactiveLoggedInUser(Context context, String key) {
        for (Account a : AccountManager.INSTANCE.getLoggedInAccounts()) {
            if (key.equals(a.getSecurityCode()) && context == MainActivity.getInstance()) {
                changeActiveUser(a);
                return true;
            }
        }
        return false;
    }

    private static void changeActiveUser(Account a) {
        AccountButtons.getInstance().setActiveAccount(a);
    }

    private String errMessage(int errno) {
        String text = "";
        switch (errno) {
            case IButton.Response.ERR_PARAMETER:
                text = MainActivity.getInstance().getString(R.string.error_parameter);
                break;
            case IButton.Response.ERR_NOTOPEN:
                text = MainActivity.getInstance().getString(R.string.error_unopened);
                break;
            case IButton.Response.ERR_BOARD_NOTRUNNING:
                text = MainActivity.getInstance().getString(R.string.error_boardnotrun);
                break;
            case IButton.Response.ERR_SERVICE_CONNECTION:
                text = MainActivity.getInstance().getString(R.string.error_serviceconnection);
                break;
            case IButton.Response.ERR_OPENCONFLICT:
                text = MainActivity.getInstance().getString(R.string.error_openconflict);
                break;
            case IButton.Response.ERR_TIMEOUT:
                text = MainActivity.getInstance().getString(R.string.error_timeout);
                break;
            case IButton.Response.ERR_BOARD_CONNECTION:
                text = MainActivity.getInstance().getString(R.string.error_boadconnection);
                break;
            case IButton.Response.ERR_POWER_FAILURE:
                text = MainActivity.getInstance().getString(R.string.error_powerdown);
                break;
            default:
                text = MainActivity.getInstance().getString(R.string.error_generic);
                break;
        }

        return text;
    }

}
