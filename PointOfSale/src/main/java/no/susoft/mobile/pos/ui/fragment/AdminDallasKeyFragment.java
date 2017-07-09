package no.susoft.mobile.pos.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import jp.co.casio.vx.framework.device.IButton;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Account;
import no.susoft.mobile.pos.ui.activity.AdminActivity;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.DallasKey;
import no.susoft.mobile.pos.ui.activity.util.IDallasKey;
import no.susoft.mobile.pos.ui.adapter.DallasKeyListAdapter;

import java.util.ArrayList;

public class AdminDallasKeyFragment extends Fragment implements IButton.StatCallback, IDallasKey {

    @InjectView(R.id.user_list_view)
    ListView userListView;
    @InjectView(R.id.current_key_selected)
    TextView currentKeyTV;
    @InjectView(R.id.account_with_this_key)
    TextView accountNameTV;
    @InjectView(R.id.replace_key)
    Button replaceKeyButton;
    @InjectView(R.id.delete_key)
    Button deleteKeyButton;
    @InjectView(R.id.give_key_to_other_account)
    ToggleButton giveKeyToOtherAccountButton;
    @InjectView(R.id.no_accounts_text)
    TextView noAccountsTV;
    @InjectView(R.id.user_key_utils_view)
    LinearLayout bottomUtilBar;
    @InjectView(R.id.dallas_key_fragment_bottom_bar_buttons)
    LinearLayout bottomBarButtons;
    @InjectView(R.id.dallas_key_fragment_bottom_bar_text)
    LinearLayout bottomBarText;

    private String selectedKey = "";
    private Account selectedAccount;
    private boolean receiverIsRegistered;
    DallasKeyListAdapter adapter;
    boolean isAlreadyCallingAccounts;
    public boolean isWaitingForKeyInput = false;
    AlertDialog typeInKeyDialog;
    EditText edittext;
    private ArrayList<Account> accounts;
    private DallasKey dallasKey;
    private String key;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dallasKey = new DallasKey();
        isAlreadyCallingAccounts = false;
        MainActivity.getInstance().setAdminDallasKeyFragment(this);
        edittext = new EditText(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        dallasKey.startIButton(this);
        refreshUserUtilsView();
    }


    private void refreshUserUtilsView() {
        if (selectedAccount == null) {
            setUtilViewToEnabledState(false);
        } else {
            Log.i("vilde", "selected account: " + selectedAccount.getName());
            setUtilViewToEnabledState(true);
        }
    }

    private void setUtilViewToEnabledState(boolean enabled) {
        if (enabled) {
            removeBottomUtilBarListenerIfExists();
            if (selectedAccountHasSecurityCode()) {
                setAllBarButtonChildrenEnabledState(true);
            } else {
                setReplaceButtonEnabledOnly();
            }
        } else {
            addBottomUtilBarClickListener();
            setAllBarButtonChildrenEnabledState(false);
        }
        setAllBarTextChildrenEnabledState(enabled);
    }

    private boolean selectedAccountHasSecurityCode() {
        return (selectedAccount != null && !selectedAccount.getSecurityCode().isEmpty());
    }

    private void addBottomUtilBarClickListener() {
        bottomUtilBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), R.string.click_account_to_enable_actions, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void removeBottomUtilBarListenerIfExists() {
        if (bottomUtilBar.hasOnClickListeners()) {
            bottomUtilBar.setOnClickListener(null);
        }
    }

    private void setReplaceButtonEnabledOnly() {
        deleteKeyButton.setEnabled(false);
        giveKeyToOtherAccountButton.setEnabled(false);
        replaceKeyButton.setEnabled(true);
    }

    private void setAllBarButtonChildrenEnabledState(boolean enabled) {
        for (int i = 0; i < bottomBarButtons.getChildCount(); i++) {
            bottomBarButtons.getChildAt(i).setEnabled(enabled);
        }
    }

    private void setAllBarTextChildrenEnabledState(boolean enabled) {
        for (int i = 0; i < bottomBarText.getChildCount(); i++) {
            bottomBarText.getChildAt(i).setEnabled(enabled);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.dallas_key_fragment, container, false);
        ButterKnife.inject(this, rootView);

        setTheOnItemClickListeners();
        setToggleButtonListeners();
        setButtonListeners();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity.getInstance().setAdminDallasKeyFragment(this);
        updateAdminAccountList();
        dallasKey.startIButton(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        dallasKey.removeReceiverIfExists(this);
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

    private void setToggleButtonListeners() {

        giveKeyToOtherAccountButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && selectedKey.length() > 0) {
                    Toast.makeText(getActivity(), getString(R.string.select_account_to_give_key_to), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setButtonListeners() {
        replaceKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceKeyOnClick();
            }
        });

        deleteKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedAccount != null) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.delete_key_from_this_user);
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            updateKeyForAccount(selectedAccount, "", false);
                            dialog.cancel();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alert11 = builder.create();
                    alert11.show();
                }
            }
        });
    }


    private void replaceKeyOnClick() {

        isWaitingForKeyInput = true;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.add_replace_key);
        alertDialog.setMessage(R.string.replace_key_with);

        alertDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newKey = edittext.getText().toString();
                isWaitingForKeyInput = false;

                updateKeyForAccount(selectedAccount, newKey, true);

                dialog.cancel();
            }
        });

        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                isWaitingForKeyInput = false;
                dialog.cancel();
            }
        });

        typeInKeyDialog = alertDialog.create();
        edittext = new EditText(getActivity());
        edittext.setSingleLine();
        typeInKeyDialog.setView(edittext);
        typeInKeyDialog.show();

    }

    public void setAccountListAdapterToThisList(ArrayList<Account> accounts) {
        isAlreadyCallingAccounts = false;
        this.accounts = accounts;
        adapter = new DallasKeyListAdapter(this.getActivity(), 0, accounts);
        userListView.setAdapter(adapter);

        if (accounts.isEmpty()) {
            userListView.setVisibility(View.GONE);
            bottomUtilBar.setVisibility(View.GONE);
            noAccountsTV.setVisibility(View.VISIBLE);
        } else {
            userListView.setVisibility(View.VISIBLE);
            bottomUtilBar.setVisibility(View.VISIBLE);
            noAccountsTV.setVisibility(View.GONE);
            refreshUserUtilsView();
        }
        Log.i("vilde", "done with result");
        Toast.makeText(getActivity(), R.string.accounts_loaded, Toast.LENGTH_SHORT).show();

        getActivity().findViewById(R.id.admin_menu_list).setClickable(true);


    }

    private void setTheOnItemClickListeners() {
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (giveKeyToOtherAccountButton.isChecked()) {
                    giveKeyFromAccountToAccount(selectedAccount, (Account) view.getTag(), selectedKey);
                    giveKeyToOtherAccountButton.setChecked(false);
                }
                userListView.setSelection(i);
                selectedAccount = (Account) view.getTag();
                highlightSelectedViewOnly(view);
                refreshSelectedKeyTextViews(selectedAccount);
                adapter.notifyDataSetChanged();
                refreshUserUtilsView();
            }
        });
    }


    private void giveKeyFromAccountToAccount(Account oldAccount, Account newAccount, String selectedKey) {
        confirmChangeOfKeys(oldAccount, newAccount, selectedKey);
    }

    private void highlightSelectedViewOnly(View view) {
        unhighlightAllChildViews(userListView);
        if (view != null) {
            highlightCurrentRow(view);
        }
    }

    private void highlightCurrentRow(View rowView) {
        rowView.setBackgroundColor(getResources().getColor(R.color.et_orange));
        rowView.setActivated(true);
    }

    private void unhighlightAllChildViews(ListView view) {
        if (view.getChildCount() > 0) {
            for (int i = 0; i < view.getChildCount(); i++) {
                view.getChildAt(i).setBackgroundColor(Color.WHITE);
                view.setActivated(false);
            }
        }
    }

    private void refreshSelectedKeyTextViews(Account a) {
        if (a.hasSecurityKey()) {
            currentKeyTV.setText(a.getSecurityCode());
            selectedKey = a.getSecurityCode();
            selectedAccount = a;
        } else {
            currentKeyTV.setText(R.string.no_key_selected);
            selectedKey = "";
        }
        accountNameTV.setText(a.getName());
    }

    private Account checkIfAnyUsersHasKey(String key) {

        for (Account a : accounts) {
            if (a.hasSecurityKey()) {
                if (a.getSecurityCode().equals(key)) {
                    Log.i("vilde", "this account has the key: " + a.getName());
                    return a;
                }
            }
        }
        return null;
    }

    private void allocateKey(Account a, String key) {
        a.setSecurityCode(key.trim());
    }

    private void clearKey(Account a) {
        if (a != null) {
            a.setSecurityCode("");
        }
    }

    private void assignNewKeyDialog(final Account account, String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Do you wish to allocate key " + key + "to " + account.getName() + ", replacing any key this user had?");

        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                MainActivity.getInstance().getServerCallMethods().updateKeyForAccount(account, selectedKey);
                dialog.cancel();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertNewKey = builder.create();
        alertNewKey.show();
    }

    private void confirmChangeOfKeys(final Account oldAccount, final Account newAccount, final String key) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(oldAccount.getName() + " already has the key " + key
                + " you are giving to " + newAccount.getName() + ".\n\n"
                + "Delete key from " + oldAccount.getName()
                + " and give it to " + newAccount.getName() + "?");

        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                updateKeyForAccount(newAccount, key, false);
                dialog.cancel();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert11 = builder.create();
        alert11.show();
    }

    private String lastKey = "";

    @Override
    public void onChangeIbutton(final boolean b, final byte[] data) {
        dallasKey.onChangeIButton(this, "AdminDallasKeyFragment", b, data);
    }


    private void updateKeyForAccount(final Account account, final String key, boolean checkForExistingKey) {
        final Account oldAccount = checkIfAnyUsersHasKey(key);

        if (checkForExistingKey) {
            if (oldAccount == null) {
                MainActivity.getInstance().getServerCallMethods().updateKeyForAccount(account, key);
            } else {
                confirmChangeOfKeys(oldAccount, selectedAccount, key);
            }
        } else {
            MainActivity.getInstance().getServerCallMethods().updateKeyForAccount(account, key);
        }

        updateKeys = new Runnable() {
            @Override
            public void run() {
                allocateKey(account, key);
                clearKey(oldAccount);
            }
        };
    }

    Runnable updateKeys;

    public void updateAdminAccountList() {
        if (!isAlreadyCallingAccounts) {
            MainActivity.getInstance().getServerCallMethods().loadAllUsersFromServer();
            isAlreadyCallingAccounts = true;
        }
    }

    public void updateKeysAfterSuccessfulCall() {
        Log.i("vilde", "running update");
        try {
            updateKeys.run();
            adapter.notifyDataSetChanged();
            refreshUserUtilsView();
        } catch (Exception ex) {

        }
    }

    AdminActivity parentActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = (AdminActivity) activity;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parentActivity.hideProgressDialog();
    }

    public void setInputText(String key) {
        edittext.setText(key);
    }


    @Override
    public void registerDallasKeyReceiver(BroadcastReceiver intentReceiver, IntentFilter intentFilter) {
        this.getActivity().registerReceiver(intentReceiver, intentFilter);
    }


    public String getLastKey() {
        return lastKey;
    }

    public void setLastKey(String lastKey) {
        this.lastKey = lastKey;
    }

    public String getKey() {
        return key;
    }
}
