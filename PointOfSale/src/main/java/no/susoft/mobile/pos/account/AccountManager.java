package no.susoft.mobile.pos.account;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;
import jp.co.casio.vx.framework.device.IButton;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.annotations.Internal;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.db.DBHelper;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.network.SettingParameterResponseDelegate;
import no.susoft.mobile.pos.server.AccountDallasKeyLoginAsync;
import no.susoft.mobile.pos.server.OrderNoteTemplateLoadAsync;
import no.susoft.mobile.pos.server.ReceiptTemplateLoadAsync;
import no.susoft.mobile.pos.server.SettingParameterLoadAsync;
import no.susoft.mobile.pos.ui.activity.AccountActivity;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AccountBar;
import no.susoft.mobile.pos.ui.activity.util.AccountButtons;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;

/**
 * This class will handle basic account management and authentication routines.
 *
 * @author Yesod
 */
public enum AccountManager {
    // Singleton instance of this class.
    INSTANCE, ErrorReporter;
    // This class holds account authorization task.
    private AccountAuthorizationTaskWorker task;

    // This class holds account authorization task when using dallas key
    private AccountDallasKeyLoginAsync taskDallasKey;
    // This class holds active account information.
    private Account account;
    // This class holds all connected accounts.
    private ArrayList<Account> loggedInAccounts = new ArrayList<>();
    // Listeners
    private List<WeakReference<AccountManagerListener>> listeners;
    // Preferences
    private final SharedPreferences preferences;

    public void savePos(PointOfSale pos) {
        final SharedPreferences.Editor editor = this.preferences.edit();
        editor.putString(Preferences.POS_ID.toString(), pos.getId());
        editor.putString(Preferences.POS_NAME.toString(), pos.getName());
        editor.commit();
    }


    public PointOfSale getSavedPos() {
        if (this.preferences.contains(Preferences.POS_ID.toString()) && this.preferences.contains(Preferences.POS_NAME.toString())) {
            return new PointOfSale(this.preferences.getString(Preferences.POS_ID.toString(), ""), this.preferences.getString(Preferences.POS_NAME.toString(), null));
        }
        return null;
    }

    // Listener Class
    public interface AccountManagerListener {

        void onAccountAuthorizationChange();
    }

    /**
     * This class handles a client authorization request to the server.
     *
     * @author Yesod
     */
    public final class AccountAuthorizationTaskWorker extends AsyncTask<Account, Void, Account> {

        /**
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
         */
        @Override
        protected Account doInBackground(Account... account) {
            try {
                // Build the challenge.
                String challenge = account[0].getLogin() + "|" + account[0].getPassword() + "|" + account[0].getLicense();
                // Build a request.
                Request request = Server.INSTANCE.getEncryptedPreparedRequest();
                request.appendOperation(Protocol.OperationCode.REQUEST_AUTHORIZATION);
                request.appendState(Protocol.State.NOTAUTHORIZED);
                request.appendParameter(Protocol.Parameters.CHALLENGE, challenge);
                request.appendParameter(Protocol.Parameters.VERSION, SusoftPOSApplication.getVersionName());
	
                // Get the response.
                return Server.INSTANCE.doGet(JSONFactory.INSTANCE.getFactory(), request, Account.class);
            } catch (Exception x) {
                x.printStackTrace();
                return null;
            }
        }

        /**
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Account result) {
			if (!MainActivity.getInstance().isConnected()) {
				findUserInLocalDatabase();
			} else {
				AccountManager.INSTANCE.onAccountAuthoriziationResponse(result);
			}
        }
    }

    /**
     * Singleton.
     */
    private AccountManager() {
        this.listeners = new ArrayList<WeakReference<AccountManagerListener>>();
        // Load up the private preferences.
        this.preferences = SusoftPOSApplication.getContext().getSharedPreferences(Account.class.toString(), Context.MODE_PRIVATE);
    }

    /**
     * Returns whether this manage has an active listener.
     *
     * @return
     */
    public boolean hasListener() {
        return !this.listeners.isEmpty();
    }

    /**
     * Add the given account to this manager.
     *
     * @param account
     */
    private boolean doAccountVerification(Account account) {

        if (this.isAuthorizing()) {
            return false;
        }

        if (account == null) {
            return false;
        }

        // Remember the account?
        this.account = account;

        executeTask();

        return true;
    }

    private void executeTask() {
		if (MainActivity.getInstance().isConnected()) {
			if (account != null && account.hasSecurityKey() && getSavedDallasKey()) {
				this.taskDallasKey = (AccountDallasKeyLoginAsync) new AccountDallasKeyLoginAsync().execute(account);
			} else {
				this.task = (AccountAuthorizationTaskWorker) new AccountAuthorizationTaskWorker().execute(account);
			}
		} else {
			findUserInLocalDatabase();
		}
    }

	public void findUserInLocalDatabase() {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("	ID, ");
		sql.append("	SHOP, ");
		sql.append("	NAME, ");
		sql.append("	TYPE, ");
		sql.append("	SECURITYGROUP, ");
		sql.append("	SECURITYCODE ");
		sql.append("FROM ");
		sql.append("	ACCOUNT ");
		sql.append("WHERE ");
		sql.append("	SHOP = ? ");
		sql.append("AND ");
		sql.append("	USERNAME = ? ");
		sql.append("AND ");
		sql.append("	PASSWORD = ?;");

		SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
		String[] split = account.getLogin().split(Pattern.quote("."));

		if (split.length == 2) {
			try {
				Cursor rs = db.rawQuery(sql.toString(), new String[]{split[1], split[0], DBHelper.md5(account.getPassword())});
				if (rs != null && rs.getCount() > 0) {
					rs.moveToFirst();
					Chain chain = new Chain();
					chain.setLoginName("");

					String shopName = preferences.getString(Preferences.SHOP.toString(), "");
					String shopOrgNo = preferences.getString(Preferences.SHOP_ORG_NO.toString(), "");
					String shopPhone = preferences.getString(Preferences.SHOP_PHONE.toString(), "");
					String shopAddress = preferences.getString(Preferences.SHOP_ADDRESS.toString(), "");
					String shopZip = preferences.getString(Preferences.SHOP_ZIP.toString(), "");
					String shopCity = preferences.getString(Preferences.SHOP_CITY.toString(), "");

					Shop shop = new Shop(split[1], shopName, "1", false);
					shop.setOrgNo(shopOrgNo);
					shop.setPhone(shopPhone);
					shop.setAddress(shopAddress);
					shop.setZip(shopZip);
					shop.setCity(shopCity);

					ArrayList<Shop> accessibleShops = new ArrayList<>();
					accessibleShops.add(shop);

					account = new Account(account.getLogin(), DBHelper.md5(account.getPassword()), chain, account.getLicense(), account.getToken(), accessibleShops);
					account.setUserId(rs.getString(rs.getColumnIndex("ID")));
					account.setUserShopId(rs.getString(rs.getColumnIndex("SHOP")));
					account.setName(rs.getString(rs.getColumnIndex("NAME")));
					account.setEmployeeType(rs.getString(rs.getColumnIndex("TYPE")));
					account.setSecurityGroup(rs.getString(rs.getColumnIndex("SECURITYGROUP")));
					account.setSecurityCode(rs.getString(rs.getColumnIndex("SECURITYCODE")));
					account.setShop(shop);

					addLoggedInAccount(account);
					rememberAccountCredentials();
					setParameters();
					MainActivity.getInstance().handleIfIsInInactiveState();
					MainActivity.getInstance().switchOffline();

					invalidateTasks();
					notifyAccountManagerListeners();

					if (numberOfLoggedInAccountsHasChanged()) {
						if (!getLoggedInAccounts().isEmpty()) {
							refreshAccountButtons();
						}
					}
					checkForAdministratorUser();
				} else {
					invalidateTasks();
					notifyAccountManagerListeners();
				}

				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				invalidateTasks();
				notifyAccountManagerListeners();
			}
		} else {
			invalidateTasks();
			notifyAccountManagerListeners();
		}
	}

    /**
     * Create a new account profile using the given credentials.
     *
     * @param username
     * @param license
     * @param password
     * @return
     */
    public boolean onRequestAccountAuthorization(final String username, final String password, final String license) {
        if (this.isAuthorizing()) {
            return false;
        } else {
            return this.doAccountVerification(new Account(username, password, license));
        }
    }

    /**
     * Create a new account profile using the given credentials.
     *
     * @param key
     * @param license
     * @return
     */
    public boolean onRequestAccountAuthorization(final String key, final String license) {
        if (this.isAuthorizing()) {
            return false;
        } else {
            Log.i("vilde", "trying to run doaccountverification");
            return this.doAccountVerification(new Account(key, license));
        }
    }

    /**
     * This method is called when the authorization task returns a response.
     *
     * @param response
     */
    public void onAccountAuthoriziationResponse(final Account response) {
        try {
            if (anyOfTheResponseAttributesAreNull(response)) {
                Log.i("vilde", "some account response attribute was null and it finished");
                return;
            }
            finalizeAccount(response);
            addLoggedInAccount(account);
            rememberAccountCredentials();
			setParameters();
            if (loggedInAccounts.size() == 1) {
                rememberShopSettings();
            }
			MainActivity.getInstance().handleIfIsInInactiveState();

        } catch (Exception x) {
            x.printStackTrace();

        } finally {
            invalidateTasks();
            notifyAccountManagerListeners();
        }

        if (numberOfLoggedInAccountsHasChanged()) {
            if (!getLoggedInAccounts().isEmpty()) {
                refreshAccountButtons();
            }
        }
        checkForAdministratorUser();
    }

    private void setParameters() {
        if (this.preferences.contains(Preferences.RESTAURANT.toString())) {
            AppConfig.getState().setRestaurant(this.preferences.getBoolean(Preferences.RESTAURANT.toString(), false));
        }
        if (this.preferences.contains(Preferences.WORKSHOP.toString())) {
            AppConfig.getState().setWorkshop(this.preferences.getBoolean(Preferences.WORKSHOP.toString(), false));
        }
        if (this.preferences.contains(Preferences.DALLASKEY.toString())) {
            AppConfig.getState().setUsingDallasKey(this.preferences.getBoolean(Preferences.DALLASKEY.toString(), false));
        }
        if (this.preferences.contains(Preferences.POS_ACCEPT_PAYMENT.toString())) {
            AppConfig.getState().setPosAcceptPayment(this.preferences.getBoolean(Preferences.POS_ACCEPT_PAYMENT.toString(), false));
        }
    }

    private boolean numberOfLoggedInAccountsHasChanged() {
        return AccountBar.getInstance().getAccountsBeforeLoginAttempt() != getLoggedInAccounts().size();
    }

    private void checkForAdministratorUser() {
        try {
            if (account.getUserId() != null && account.getUserId().length() >= 3 && account.getUserId().startsWith("998")) {
                MainActivity.getInstance().handleIfIsInInactiveState();
                MainActivity.getInstance().startAdminActivity();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean anyOfTheResponseAttributesAreNull(Account response) {
        return response == null || response.getToken() == null || response.getToken().isEmpty() || !response.hasShops();
    }

    private void notifyAccountManagerListeners() {
		if (this.hasListener())
            this.notifyListeners();
    }

    private void rememberAccountCredentials() {
        this.onRememberingAccountCredentials(this.isRememberingAccountCredentials());
    }

    private void rememberShopSettings() {
        rememberParameters();
    }

    private void finalizeAccount(Account response) {
        this.account = response;
        for (Shop shop : account.getAccessibleShops()) {
            if (shop.getID().equals(account.getUserShopId())) {
                account.setShop(shop);
            }
        }
    }

    private void invalidateTasks() {
        this.task = null;
        this.taskDallasKey = null;
    }

    private void refreshAccountButtons() {
        AccountBar.getInstance().setupAccountMenuBar();
    }
	
	public void reauthenticateAccounts() {
		
		if (AccountManager.INSTANCE.getLoggedInAccounts().size() > 0) {
			no.susoft.mobile.pos.error.ErrorReporter.INSTANCE.filelog("AccountManager ReauthenticateAccounts..");
			
			Request request = Server.INSTANCE.getEncryptedPreparedRequest();
			request.appendState(Protocol.State.NOTAUTHORIZED);
			request.appendOperation(Protocol.OperationCode.REQUEST_REAUTHORIZATION);
			request.appendParameter(Parameters.LICENSE, AccountManager.INSTANCE.getAccount().getLicense());
			request.appendParameter(Parameters.VERSION, SusoftPOSApplication.getVersionName());
			
			final String json = JSONFactory.INSTANCE.getFactory().toJson(AccountManager.INSTANCE.getLoggedInAccounts());
			final StringBody file = new StringBody(json, ContentType.APPLICATION_JSON);
			final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
			entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			entity.addPart("file", file);
			
			SynchronizeTransport response = Server.INSTANCE.doPost(SynchronizeTransport.class, request, entity, JSONFactory.INSTANCE.getFactory());
			if (response == null) {
				return;
			}
			
			for (Account account : response.getAccounts()) {
				for (int i = 0; i < AccountManager.INSTANCE.getLoggedInAccounts().size(); i++) {
					Account a = AccountManager.INSTANCE.getLoggedInAccounts().get(i);
					if (a.getLogin().equals(account.getLogin())) {
						AccountManager.INSTANCE.getLoggedInAccounts().set(i, account);
					}
				}
				if (AccountManager.INSTANCE.getAccount().getLogin().equals(account.getLogin())) {
					AccountManager.INSTANCE.setAccount(account);
				}
			}
			
			if (response.getAccounts().size() > 0) {
				MainActivity.getInstance().networkAvailable();
			}
		}
	}
	
	/**
     * Ask the user to supply account credentials.
     *
     * @param activity
     */
    public void showAccountDialog(Activity activity) {
        AccountActivity.start(activity, true);
    }

    /**
     * Returns whether the active account
     *
     * @return
     */
    public boolean isActiveAccountAuthorized() {
        return !(this.account == null || this.account.isInvalidated());
    }

    /**
     * If any, invalidate the currently active account, then notify the listener.
     */
    public void doActiveAccountInvalidation() {
        if (this.account != null)
            this.account.invalidate();
        if (this.hasListener())
            this.notifyListeners();
    }

	public void doCompleteInvalidation() {
		for (Account loggedInAccount : loggedInAccounts) {
			loggedInAccount.invalidate();
		}
		setAccount(null);
	}

    /**
     * Return true when this manager is currently authorizing account details.
     *
     * @return
     */
    public boolean isAuthorizing() {

        if (account != null && account.hasSecurityKey()) {
            return this.taskDallasKey != null;
        } else {
            return this.task != null;
        }
    }

    /**
     * Get the authorization token.
     *
     * @return
     */
    public String getToken() {
        return this.account != null ? this.account.getToken() : null;
    }

    /**
     * Add the given 'listener' as a listener.
     *
     * @param listener
     */
    public void addListener(final AccountManagerListener listener) {
        this.listeners.add(new WeakReference<AccountManagerListener>(listener));
    }

    /**
     * Remove the given 'listener'.
     *
     * @param listener
     */
    public void removeListener(final AccountManagerListener listener) {
        for (Iterator<WeakReference<AccountManagerListener>> it = this.listeners.iterator(); it.hasNext(); ) {
            AccountManagerListener l = it.next().get();
            // Match?
            if (l == listener) {
                it.remove();
                break;
            }
        }
    }

    /**
     * Notify all the account manager listeners that the account has changed.
     */
    private void notifyListeners() {
        ArrayList<WeakReference<AccountManagerListener>> listenersList = new ArrayList<>(listeners);
        for (Iterator<WeakReference<AccountManagerListener>> it = listenersList.iterator(); it.hasNext(); ) {
            WeakReference<AccountManagerListener> reference = it.next();
            if (reference != null) {
                AccountManagerListener listener = reference.get();
                // Was the listener garbage collected?
                if (listener != null) {
                    try {
                        listener.onAccountAuthorizationChange();
                    } catch (Exception x) {
                    }
                }
            }
        }
    }

    public enum Preferences {
        USERNAME,
        LICENSE,
        CHAIN,
        SHOP,
        SHOP_ORG_NO,
        SHOP_PHONE,
        SHOP_ADDRESS,
        SHOP_ZIP,
        SHOP_CITY,
        REMEMBER,
        DALLASKEY,
        RESTAURANT,
        POS_ID,
        POS_NAME,
        POS_ACCEPT_PAYMENT,
		RECEIPT_HEADER,
		RECEIPT_FOOTER,
		WORKSHOP,
		ORDER_NOTE
    }

    /**
     * Toggle whether account user and chain is saved.
     */
    @Internal
    public void setRememberingAccountCredentials(boolean remember) {

        final SharedPreferences.Editor editor = this.preferences.edit();

        saveRememberState(editor, remember);

        if (!remember) {
            clearAccountCredentials(editor);
        }
        editor.commit();
    }

    private void saveRememberState(SharedPreferences.Editor editor, boolean remember) {
        editor.putBoolean(Preferences.REMEMBER.toString(), remember);

    }

    private void clearAccountCredentials(SharedPreferences.Editor editor) {
        editor.remove(Preferences.USERNAME.toString());
        editor.remove(Preferences.CHAIN.toString());
        editor.remove(Preferences.SHOP.toString());
        editor.remove(Preferences.SHOP_ORG_NO.toString());
        editor.remove(Preferences.SHOP_PHONE.toString());
        editor.remove(Preferences.SHOP_ADDRESS.toString());
        editor.remove(Preferences.SHOP_ZIP.toString());
        editor.remove(Preferences.SHOP_CITY.toString());
        editor.remove(Preferences.RECEIPT_HEADER.toString());
        editor.remove(Preferences.RECEIPT_FOOTER.toString());
        editor.remove(Preferences.ORDER_NOTE.toString());
    }

    public void clearAccountCredentials() {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.remove(Preferences.LICENSE.toString());
        editor.remove(Preferences.USERNAME.toString());
        editor.remove(Preferences.CHAIN.toString());
        editor.remove(Preferences.SHOP.toString());
        editor.remove(Preferences.SHOP_ORG_NO.toString());
        editor.remove(Preferences.SHOP_PHONE.toString());
        editor.remove(Preferences.SHOP_ADDRESS.toString());
        editor.remove(Preferences.SHOP_ZIP.toString());
        editor.remove(Preferences.SHOP_CITY.toString());
        editor.remove(Preferences.RECEIPT_HEADER.toString());
        editor.remove(Preferences.RECEIPT_FOOTER.toString());
        editor.remove(Preferences.ORDER_NOTE.toString());
        editor.commit();
    }

    public void clearShopSettings() {
        SharedPreferences.Editor editor = this.preferences.edit();
        try {
            editor.remove(Preferences.RESTAURANT.toString());
            editor.remove(Preferences.DALLASKEY.toString());
            editor.remove(Preferences.POS_ID.toString());
            editor.remove(Preferences.POS_NAME.toString());
            editor.commit();
        } catch (Exception ex) {
            Log.i("vilde", "error clearing shop settings...");
        }
    }

    /**
     * If there is an account, save its username and chain.
     *
     * @param rememberingUserCredentials
     */
    private void onRememberingAccountCredentials(boolean rememberingUserCredentials) {
        if (this.account == null)
            return;
        // Get the preferences editor:
        final SharedPreferences.Editor editor = this.preferences.edit();
        saveAccountCredentials(editor, rememberingUserCredentials);
        editor.commit();
    }

    private void rememberParameters() {
        final SharedPreferences.Editor editor = this.preferences.edit();
        final int parameterNumber = 3;
        new SettingParameterLoadAsync(new SettingParameterResponseDelegate() {
            @Override
            public void processFinish(String value) {
				try {
					if (value != null && (value.equals("Y") || value.equals("1"))) {
						AppConfig.getState().setRestaurant(true);
						editor.putBoolean(Preferences.RESTAURANT.toString(), true);
					} else {
						AppConfig.getState().setRestaurant(false);
						editor.putBoolean(Preferences.RESTAURANT.toString(), false);
					}
					parametersSet++;

					if (parameterNumber == parametersSet) {
						editor.commit();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        }).execute("RESTAURANT");

        new SettingParameterLoadAsync(new SettingParameterResponseDelegate() {
            @Override
            public void processFinish(String value) {
				try {
					if (value != null && (value.equals("Y") || value.equals("1"))) {
						AppConfig.getState().setWorkshop(true);
						editor.putBoolean(Preferences.WORKSHOP.toString(), true);
					} else {
						AppConfig.getState().setWorkshop(false);
						editor.putBoolean(Preferences.WORKSHOP.toString(), false);
					}
					parametersSet++;

					if (parameterNumber == parametersSet) {
						editor.commit();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        }).execute("WORKSHOP");

        new SettingParameterLoadAsync(new SettingParameterResponseDelegate() {
            @Override
            public void processFinish(String value) {
				try {
					if (value != null && (value.equals("Y") || value.equals("1")) && deviceHasDallasKey()) {
						AppConfig.getState().setUsingDallasKey(true);
						editor.putBoolean(Preferences.DALLASKEY.toString(), true);
						MainActivity.getInstance().dallasKey.startIButton(MainActivity.getInstance());
					} else {
						AppConfig.getState().setUsingDallasKey(false);
						editor.putBoolean(Preferences.DALLASKEY.toString(), false);
					}
					parametersSet++;

					if (parameterNumber == parametersSet) {
						editor.commit();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        }).execute("DALLASKEY");

        new SettingParameterLoadAsync(new SettingParameterResponseDelegate() {
            @Override
            public void processFinish(String value) {
				try {
					if (value == null) {
						AppConfig.getState().setPosAcceptPayment(true);
						editor.putBoolean(Preferences.POS_ACCEPT_PAYMENT.toString(), true);
					}
					if (value != null && (value.equals("Y") || value.equals("-1"))) {
						AppConfig.getState().setPosAcceptPayment(true);
						editor.putBoolean(Preferences.POS_ACCEPT_PAYMENT.toString(), true);
					} else {
						AppConfig.getState().setPosAcceptPayment(false);
						editor.putBoolean(Preferences.POS_ACCEPT_PAYMENT.toString(), false);
					}
					parametersSet++;

					if (parameterNumber == parametersSet) {
						editor.commit();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        }).execute("POS_ACCEPT_PAYMENT");

		new ReceiptTemplateLoadAsync().execute();
		new OrderNoteTemplateLoadAsync().execute();
    }

    private boolean deviceHasDallasKey() {
        try {
            IButton testButton = new IButton();
            int ret = testButton.open(IButton.OPENMODE_COMMON, IButton.DEVICE_HOST_LOCALHOST);
            if (ret < 0) {
                testButton.close();
                return false;
            } else {
                testButton.close();
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private int parametersSet = 0;

    private void saveAccountCredentials(SharedPreferences.Editor editor, boolean rememberingUserCredentials) {
        if (rememberingUserCredentials) {
            editor.putString(Preferences.USERNAME.toString(), this.account.getLogin());
        }
        editor.putString(Preferences.LICENSE.toString(), this.account.getLicense());
        editor.putString(Preferences.CHAIN.toString(), this.account.getChainName());
        editor.putString(Preferences.SHOP.toString(), this.account.getShop().getName());
        editor.putString(Preferences.SHOP_ORG_NO.toString(), this.account.getShop().getOrgNo());
        editor.putString(Preferences.SHOP_PHONE.toString(), this.account.getShop().getPhone());
        editor.putString(Preferences.SHOP_ADDRESS.toString(), this.account.getShop().getAddress());
        editor.putString(Preferences.SHOP_ZIP.toString(), this.account.getShop().getZip());
        editor.putString(Preferences.SHOP_CITY.toString(), this.account.getShop().getCity());
    }

    /**
     * Return whether the user wishes for account credentials (user, chain) to be saved upon successful login.
     *
     * @return
     */
    public boolean isRememberingAccountCredentials() {
        return this.preferences.getBoolean(Preferences.REMEMBER.toString(), false);
    }

    public String getSavedShopPhone() {
        return this.preferences.getString(Preferences.SHOP_PHONE.toString(), null);
    }

    public String getSavedShopAddress() {
        return this.preferences.getString(Preferences.SHOP_ADDRESS.toString(), null);
    }

    public String getSavedShopZip() {
        return this.preferences.getString(Preferences.SHOP_ZIP.toString(), null);
    }

    public String getSavedShopCity() {
        return this.preferences.getString(Preferences.SHOP_CITY.toString(), null);
    }

    public String getSavedReceiptHeader() {
        return this.preferences.getString(Preferences.RECEIPT_HEADER.toString(), null);
    }

    public String getSavedReceiptFooter() {
        return this.preferences.getString(Preferences.RECEIPT_FOOTER.toString(), null);
    }

    public String getSavedOrderNote() {
        return this.preferences.getString(Preferences.ORDER_NOTE.toString(), null);
    }

    @Internal
    public String getSavedChain() {
        return this.preferences.getString(Preferences.CHAIN.toString(), "");
    }

    @Internal
    public String getSavedShopName() {
        return this.preferences.getString(Preferences.SHOP.toString(), "");
    }

    @Internal
    public String getSavedOrgNo() {
        return this.preferences.getString(Preferences.SHOP_ORG_NO.toString(), null);
    }

    @Internal
    public String getSavedLicense() {
        return this.preferences.getString(Preferences.LICENSE.toString(), null);
    }

    public boolean getSavedRestaurant() {
        return this.preferences.getBoolean(Preferences.RESTAURANT.toString(), false);
    }

    public boolean getSavedDallasKey() {
        return this.preferences.getBoolean(Preferences.DALLASKEY.toString(), false);
    }

    /**
     * @return
     */
    @Internal
    public String getSavedUsername() {
        return this.preferences.getString(Preferences.USERNAME.toString(), null);
    }

    /**
     * author VSB
     *
     * @return List of the active account's shops
     */
    public List<Shop> getAccessibleShops() {
        return this.account.getAccessibleShops();
    }

    /**
     * author VSB
     *
     * @return arrayList of logged in accounts, null if empty
     */
    public ArrayList<Account> getLoggedInAccounts() {
        for (Account a : loggedInAccounts) {
            if (a == null) {
                loggedInAccounts.remove(a);
            }
        }
        return loggedInAccounts;
    }

    /**
     * author VSB
     *
     * @return currently active account
     */
    public Account getAccount() {
        return account;
    }

    /**
     * author VSB
     *
     * @param account sets active account to supplied account if there are any logged in accounts registered
     */
    public void setAccount(Account account) {

        if (loggedInAccounts.size() > 0) {
            this.account = account;
        } else {
            this.account = null;
            if (AppConfig.getState().isUsingDallasKey()) {
                if (MainActivity.getInstance().isPaused()) {
                    MainActivity.getInstance().onPostResume();
                    Log.i("vilde", "was paused");
                }
                if (MainActivity.getInstance().getAdminDallasKeyFragment() != null) {
                    if (MainActivity.getInstance().getAdminDallasKeyFragment().getActivity() != null) {
                        MainActivity.getInstance().getAdminDallasKeyFragment().getActivity().finish();
                    }
                    AppConfig.getState().setAdminLoggingInWithCredentials(false);
                }
                MainActivity.getInstance().goToInactiveState();
            } else {
                goToLoginActivity();
            }
        }
    }

    /**
     * TODO: Duplicate class in Account. Tidy up?
     * Set the currently active shop for this account and returns true if set or false if not
     *
     * @return
     */
    public boolean setShop(Shop s) {
        return account.setShop(s);
    }

    /**
     * author VSB
     * Adds an account to the loggedinaccounts arraylist which holds all active accounts
     *
     * @param account
     */
    public void addLoggedInAccount(Account account) {

        initializeLoggedInAccountsList();

        if (account == null) {
            return;
        } else {
            addNewAccount();
        }
    }

    private void initializeLoggedInAccountsList() {
        if (loggedInAccounts == null) {
            loggedInAccounts = new ArrayList<>();
        }
    }

    private void addNewAccount() {
        Account exists = null;

        for (Account a : loggedInAccounts) {
            if (a.getLogin().equals(account.getLogin())) {
                exists = a;
                break;
            }
        }
        removeOldAccountIfExists(exists);
        setNewAccount(account);
        notifyAdapters();

    }

    private void setNewAccount(Account account) {
        loggedInAccounts.add(account);
        setAccount(account);
    }

    private void notifyAdapters() {
        notifyAccountAdapter();
        notifyShopAdapter();
    }

    private void removeOldAccountIfExists(Account exists) {
        if (exists != null) {
            loggedInAccounts.remove(exists);
        }
    }

    private void notifyAccountAdapter() {
        if (MainActivity.getInstance().accountAdapter != null) {
            MainActivity.getInstance().accountAdapter.notifyDataSetChanged();
        }
    }

    private void notifyShopAdapter() {
        if (MainActivity.getInstance().shopAdapter != null) {
            MainActivity.getInstance().shopAdapter.notifyDataSetChanged();
        }
    }

    /**
     * author VSB
     * logout account
     *
     * @return the account which was invalidated
     */
    public Account logoutAccount() {

        Account deletedAccount = getAccount();
        removeAccountButton(deletedAccount);
        deleteAccount(deletedAccount);
        setNewSelectedAccount();

        return deletedAccount;
    }

    private void deleteAccount(Account deletedAccount) {
        if (deletedAccount != null) {
            loggedInAccounts.remove(deletedAccount);
            deletedAccount.invalidate();
        }
    }

    private void removeAccountButton(Account deletedAccount) {
        AccountButtons.getInstance().removeButton(deletedAccount);
    }

    private void setNewSelectedAccount() {
        //Larger than 1 because it is called before the account is deleted
        Log.i("vilde", "loggedinaccounts: " + loggedInAccounts.size());
        if (loggedInAccounts.size() > 0) {
            setAccount(loggedInAccounts.get(0));
        } else {
            setAccount(null);
        }
    }

    public void logInWithDallasKey(Context context, String key, String license) {
        if (context == MainActivity.getInstance()) {
            Log.i("vilde", "dallas task");
            onRequestAccountAuthorization(key, license);
        }
        else if (context == MainActivity.getInstance().getAccountActivity()) {
            MainActivity.getInstance().getAccountActivity().refreshThisAuthorizingState(key, license);
        }
    }

    /**
     * author VSB
     * Starts a login activity from MainActivity
     */
    private void goToLoginActivity() {
        Intent accountIntent = new Intent(SusoftPOSApplication.getContext(), AccountActivity.class);
        MainActivity.getInstance().startActivityForResult(accountIntent, MainActivity.LOGIN_REQUEST);
    }
}