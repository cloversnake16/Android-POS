package no.susoft.mobile.pos.ui.activity.util;

import java.util.ArrayList;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Account;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class AccountButtons {

    LinearLayout accountNameButtonsLayout;
    private static AccountButtons instance;

    public AccountButtons(LinearLayout accountNameButtonsLayout) {
        instance = this;
        this.accountNameButtonsLayout = accountNameButtonsLayout;
    }

    public static AccountButtons getInstance() {
        return instance;
    }

    public void setActiveAccount(Account a) {
        simulateButtonClick(a);
    }
    public void updateButtons() {
        final ArrayList<Account> loggedInAccounts = AccountManager.INSTANCE.getLoggedInAccounts();

        accountNameButtonsLayout.removeAllViewsInLayout();

        setupButtons(loggedInAccounts);

        if (AccountManager.INSTANCE.getAccount() != null) {
            accountNameButtonsLayout.invalidate();
            accountNameButtonsLayout.getChildAt(AccountManager.INSTANCE.getLoggedInAccounts().indexOf(AccountManager.INSTANCE.getAccount())).callOnClick();
        }

    }
    public void removeButton(Account a) {

        for (int i = 0; i < accountNameButtonsLayout.getChildCount(); i++) {
            if (((Account) accountNameButtonsLayout.getChildAt(i).getTag()).getUserId().equals(a.getUserId())) {

                deleteButtonAtIndex(i);
                setNewActiveAccountAfterUserLogout(accountNameButtonsLayout, i);

                return;
            }
        }
    }

    private void deleteButtonAtIndex(Integer i) {
        accountNameButtonsLayout.removeView(accountNameButtonsLayout.getChildAt(i));
        accountNameButtonsLayout.invalidate();
    }

    private void setNewActiveAccountAfterUserLogout(LinearLayout accountButtonsLayout, Integer i) {
        if (accountButtonsLayout.getChildCount() > 0) {
            if (i > 0) {
                setActiveAccount((Account) accountButtonsLayout.getChildAt(i - 1).getTag());
            } else {
                setActiveAccount((Account) accountButtonsLayout.getChildAt(0).getTag());
            }
        }
    }

    private void simulateButtonClick(Account a) {
        AccountManager.INSTANCE.setAccount(a);

        for (int i = 0; i < accountNameButtonsLayout.getChildCount(); i++) {
            if (((Account) accountNameButtonsLayout.getChildAt(i).getTag()).getUserId().equals(a.getUserId())) {
                accountNameButtonsLayout.getChildAt(i).callOnClick();
            }
        }
    }

    private void setupButtons(ArrayList<Account> loggedInAccounts) {
        if (loggedInAccounts.size() > 0) {
            ArrayList<Button> buttons = createButtons(loggedInAccounts);

            addButtonToLinearLayout(buttons);
            setButtonsOnClickListener(buttons, loggedInAccounts);
        }
    }
    private ArrayList<Button> createButtons(final ArrayList<Account> loggedInAccounts) {
        ArrayList<Button> buttons = new ArrayList<>();

        for (Account account : loggedInAccounts) {
            Button button = new Button(MainActivity.getInstance(), null, R.layout.account_button);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

            button.setLayoutParams(params);
            button.setText(account.getName());
            button.setTag(account);
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, MainActivity.getInstance().getResources().getDimension(R.dimen.account_name_text_size));
            button.setPadding(15, 0, 15, 0);
            button.setGravity(Gravity.CENTER);

            buttons.add(button);
        }
        return buttons;
    }
    private void addButtonToLinearLayout(ArrayList<Button> buttons) {
        for (Button b : buttons) {
            accountNameButtonsLayout.addView(b);
        }
    }

    private void setButtonsOnClickListener(ArrayList<Button> buttons, final ArrayList<Account> loggedInAccounts) {
        for (Button b : buttons) {
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doButtonClick(view);
                }
            });
        }
    }

    private void doButtonClick(View view) {
        try {
            final Account a = (Account) view.getTag();

            AccountManager.INSTANCE.setAccount(a);
            updateButtonsVisualStates((Button)view);
            Cart.INSTANCE.setCartOrdersToNewAccount();
        } catch (Exception x) {
        }

    }
    private void updateButtonsVisualStates(Button button) {
        for (int i = 0; i < accountNameButtonsLayout.getChildCount(); i++) {
            setButtonStateColour((Button)accountNameButtonsLayout.getChildAt(i), button.equals((Button)accountNameButtonsLayout.getChildAt(i)));
        }
    }
    private void setButtonStateColour(Button button, boolean active) {
        if(active) {
            button.setTextColor(MainActivity.getInstance().getResources().getColor(android.R.color.black));
        } else {
            button.setTextColor(MainActivity.getInstance().getResources().getColor(android.R.color.darker_gray));
        }
    }


}
