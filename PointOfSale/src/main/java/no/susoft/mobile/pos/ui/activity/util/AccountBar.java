package no.susoft.mobile.pos.ui.activity.util;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Account;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.dialog.AccountSubsequentLoginDialog;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class AccountBar {

    AccountButtons nameButtons;
    ImageView loginUserButton;
    ImageView logoutUserButton;
    LinearLayout nameButtonsLayout;
    private int accountsBeforeLoginAttempt;
    private static AccountBar instance;

    public AccountBar(AccountButtons nameButtons, ImageView loginUserButton, ImageView logoutUserButton, LinearLayout nameButtonsLayout) {
        instance = this;
        this.nameButtons = nameButtons;
        this.nameButtonsLayout = nameButtonsLayout;
        this.loginUserButton = loginUserButton;
        this.logoutUserButton = logoutUserButton;
    }

    public static AccountBar getInstance() {
        return instance;
    }

    public void setupAccountMenuBar() {
        //Must be before login and logout buttons are created as their listeners are based on this objects
        nameButtons = new AccountButtons(nameButtonsLayout);
        createLoginButton();
        createLogoutButton();
        nameButtons.updateButtons();
    }

    public int getAccountsBeforeLoginAttempt() {
        return accountsBeforeLoginAttempt;
    }
    private void setAccountsBeforeLoginAttempt(int i) {
        accountsBeforeLoginAttempt = i;
    }

    private void createLoginButton() {
        final ArrayList<Account> loggedInAccounts = AccountManager.INSTANCE.getLoggedInAccounts();
        loginUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loggedInAccounts == null || loggedInAccounts.isEmpty()) {
                    //Shouldn't happen
                } else {
                    showAccountLoginDialog();
                }
            }
        });
    }
    private void createLogoutButton() {
        final ArrayList<Account> loggedInAccounts = AccountManager.INSTANCE.getLoggedInAccounts();

        logoutUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loggedInAccounts == null || loggedInAccounts.isEmpty()) {
                    //Shouldn't happen
                } else {
                    showAccountLogoutDialog(AccountManager.INSTANCE.getAccount());
                }
            }
        });
    }

    public void showAccountLoginDialog() {
        //To aid check for if it should redraw, else errors might occur.
        setAccountsBeforeLoginAttempt(AccountManager.INSTANCE.getLoggedInAccounts().size());
        AccountSubsequentLoginDialog accountSubDialog = new AccountSubsequentLoginDialog();
		try {
			accountSubDialog.show(MainActivity.getInstance().getSupportFragmentManager(), "AccountSubDialog");
		} catch (Exception e) {
		}
	}
    private Account showAccountLogoutDialog(Account account) {

        //Checks that the account attempted to log out is the same as the active account
        if (AccountManager.INSTANCE.getAccount() != null && AccountManager.INSTANCE.getAccount().getLogin() != null && AccountManager.INSTANCE.getAccount().getLogin().equals(account.getLogin())) {
            new AlertDialog.Builder(MainActivity.getInstance()).setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(MainActivity.getInstance().getString(R.string.logging_out))
                    .setMessage(MainActivity.getInstance().getString(R.string.log_out_user) + " " + account.getName() + "?")
                    .setPositiveButton(MainActivity.getInstance().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parkOrDeleteOrderDialog();
                        }
                    }).setNegativeButton(MainActivity.getInstance().getString(R.string.no), null).show();
        }
        return account;
    }

    private void parkOrDeleteOrderDialog() {
        if (Cart.INSTANCE.hasOrdersWithLines()) {

            new AlertDialog.Builder(MainActivity.getInstance())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(MainActivity.getInstance().getString(R.string.cart_has_orders))
                    .setMessage(MainActivity.getInstance().getString(R.string.park_existing_orders))
                    .setPositiveButton(MainActivity.getInstance().getString(R.string.park), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parkExistingOrders();
                        }
                    }).setNegativeButton(MainActivity.getInstance().getString(R.string.delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Cart.INSTANCE.setOrdersTo(new ArrayList<Order>());
                    postLogoutOrderCleanup();
                }
            }).setCancelable(false).show();

        } else {
            postLogoutOrderCleanup();
        }
    }
    private void parkExistingOrders() {
        if (Cart.INSTANCE.getOrders() != null && !Cart.INSTANCE.getOrders().isEmpty()) {
            ordersBackup = Cart.INSTANCE.getOrders();
            MainActivity.getInstance().getServerCallMethods().parkOrders();
        }
    }
    public void postLogoutOrderCleanup() {
        //To ensure that their order isn't still in the system after parking or deleting it post-logout.
        Cart.persistingOrders.deleteOrderForThisAccount(AccountManager.INSTANCE.getAccount().getUserId());
        AccountManager.INSTANCE.logoutAccount();
    }

    private ArrayList<Order> ordersBackup;
    public void parkOrderFailed() {
        //TODO
        //Send again? Store somewhere=
    }
}
