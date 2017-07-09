package no.susoft.mobile.pos.ui.activity;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.ui.activity.util.NavigationEnum;
import no.susoft.mobile.pos.ui.activity.util.NavigationMenuItem;
import no.susoft.mobile.pos.ui.adapter.AdminMenuAdapter;
import no.susoft.mobile.pos.ui.fragment.AdminDallasKeyFragment;
import no.susoft.mobile.pos.ui.fragment.AdminDevicesFragment;
import no.susoft.mobile.pos.ui.fragment.AdminSettingsFragment;

public class AdminActivity extends FragmentActivity {

    ListView menuList;

    ArrayList<NavigationMenuItem> menuItems;
    AdminDallasKeyFragment adminDallasKeyFragment;
    AdminSettingsFragment settingsFragment;
    AdminDevicesFragment deviceFragment;
    private DialogInterface loadingCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.admin_activity);
        menuList = (ListView) findViewById(R.id.admin_menu_list);
        menuItems = new ArrayList<>();
        createMenuItems();
        setupMenuListView();
        setMenuOnClickListener();
        changeFragmentViewToSelectedView(null, menuItems.get(0));
    }

    @Override
    public void onStart() {
        super.onStart();

        //menuList.performItemClick(menuList, 1, menuList.getItemIdAtPosition(1));
    }

    public void setMenuOnClickListener() {
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeFragmentViewToSelectedView(view, (NavigationMenuItem)view.getTag());
            }
        });

    }

    private void highlightThisItem(View view) {
        for (int i = 0; i < menuList.getChildCount(); i++) {
            if (view.equals(menuList.getChildAt(i))) {
                menuList.getChildAt(i).setBackground(ContextCompat.getDrawable(this, R.drawable.orange_border));
            } else {
                menuList.getChildAt(i).setBackground(null);
            }
        }
    }

    public void changeFragmentViewToSelectedView(View view, NavigationMenuItem item) {
        if (view != null) {
            highlightThisItem(view);
        }

        if (!item.getId().equals(activeFragment)) {
            switchFragment(item);
        }

    }

    private void switchFragment(NavigationMenuItem item) {
        FragmentManager fm = MainActivity.getInstance().getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }

        switch (item.getId()) {
            case DALLAS_KEYS: {
                adminDallasKeyFragment = new AdminDallasKeyFragment();
                showProgressDialog(getString(R.string.loading_account_list));
                this.getSupportFragmentManager().beginTransaction().replace(R.id.admin_fragment, adminDallasKeyFragment, "dallas").commitAllowingStateLoss();
                break;
            }
            case ADMIN_SETTINGS: {
                settingsFragment = new AdminSettingsFragment();
                this.getSupportFragmentManager().beginTransaction().replace(R.id.admin_fragment, settingsFragment, "settings").commitAllowingStateLoss();
                break;
            }
            case PERIPHERAL_DEVICES: {
                deviceFragment = new AdminDevicesFragment();
                this.getSupportFragmentManager().beginTransaction().replace(R.id.admin_fragment, deviceFragment, "devices").commitAllowingStateLoss();
				break;
            }
			case DB_SYNC: {
				SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
				db.execSQL("DELETE FROM PARAMETER WHERE KEY = 'LAST_SYNC_DATE';");
				db.execSQL("DELETE FROM PRODUCT;");
				break;
            }
        }
        activeFragment = item.getId();
    }

    private NavigationEnum activeFragment;

    private void setupMenuListView() {
        AdminMenuAdapter adapter = new AdminMenuAdapter(this, 0, menuItems);
        menuList.setAdapter(adapter);
    }

    private void createMenuItems() {
        menuItems.add(new NavigationMenuItem(NavigationEnum.ADMIN_SETTINGS, "Settings"));
        menuItems.add(new NavigationMenuItem(NavigationEnum.DALLAS_KEYS, "Dallas keys"));
        menuItems.add(new NavigationMenuItem(NavigationEnum.PERIPHERAL_DEVICES, "Devices"));
        menuItems.add(new NavigationMenuItem(NavigationEnum.DB_SYNC, "DB Sync"));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i("vilde", "admin pressed back");
        AccountManager.INSTANCE.logoutAccount();
        finish();
    }


    ProgressDialog progDialog;

    private void showProgressDialog(String message) {
        progDialog = new ProgressDialog(MainActivity.getInstance());
        progDialog.setMessage(message);
        progDialog.setIndeterminate(false);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setCancelable(false);
        progDialog.setCanceledOnTouchOutside(false);
        progDialog.show();
    }

    public void hideProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }
}
