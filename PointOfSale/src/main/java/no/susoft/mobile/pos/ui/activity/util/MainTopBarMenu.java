package no.susoft.mobile.pos.ui.activity.util;

import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ToggleButton;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class MainTopBarMenu {

    private RadioGroup toggleGroup;
    private ToggleButton toggle_btn_scan;
    private ToggleButton toggle_btn_browse;
    private ToggleButton toggle_btn_search;
    private ToggleButton toggle_btn_edit;
    private ToggleButton toggle_btn_orders;
    private static MainTopBarMenu INSTANCE;
    private ToggleButton toggledButton;

    public MainTopBarMenu(RadioGroup toggleGroup,
                          ToggleButton toggle_btn_scan, ToggleButton toggle_btn_browse,
                          ToggleButton toggle_btn_search, ToggleButton toggle_btn_edit, ToggleButton toggle_btn_orders) {
        INSTANCE = this;
        this.toggleGroup = toggleGroup;
        this.toggle_btn_scan = toggle_btn_scan;
        this.toggle_btn_browse = toggle_btn_browse;
        this.toggle_btn_search = toggle_btn_search;
        this.toggle_btn_edit = toggle_btn_edit;
        this.toggle_btn_orders = toggle_btn_orders;
    }

    public static MainTopBarMenu getInstance() {
        return INSTANCE;
    }

    public void toggleLastUsedView() {
        if(toggledButton != null) {
            onToggle(toggledButton);
        } else {
            toggleDefaultView();
        }
    }

    private void toggleDefaultView() {
        toggleScanView();
    }

    public void toggleEditView() {
        if (toggle_btn_edit.isEnabled())
            onToggle(toggle_btn_edit);
    }

    public void toggleScanView() {
        if (toggle_btn_edit.isEnabled())
            onToggle(toggle_btn_scan);
    }

    public void toggleSearchView() {
        onToggle(toggle_btn_search);
    }

    public void toggleBrowseView() {
        onToggle(toggle_btn_browse);
    }

    public void toggleOrdersView() {
        if (toggle_btn_edit.isEnabled())
            onToggle(toggle_btn_orders);
    }

    public void onToggle(View view) {
        toggledButton = (ToggleButton) view;
        if (view.getId() == ((RadioGroup) view.getParent()).getCheckedRadioButtonId()) {
            ((ToggleButton) view).setChecked(true);
            if (view.getId() == R.id.toggle_btn_browse) {
                MainActivity.getInstance().getQuickLaunchFragment().refreshGridView();
            }
        } else {
            ((RadioGroup) view.getParent()).check(0);
            ((RadioGroup) view.getParent()).check(view.getId());
        }
    }


    public void setupTopBarToggleGroup() {
        //This is for the top bar right side menu buttons
        toggleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                for (int j = 0; j < radioGroup.getChildCount(); j++) {
                    final ToggleButton view = (ToggleButton) radioGroup.getChildAt(j);
                    view.setChecked(view.getId() == i);
                }
                changeRightFragmentViewToCheckedView(i);
            }
        });
    }

    /**
     * Controls what shows in the right side fragment panel
     *
     * @param id
     */
    public void changeRightFragmentViewToCheckedView(int id) {

        FragmentManager fm = MainActivity.getInstance().getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }

        switch (id) {
            case R.id.toggle_btn_scan: {
                MainActivity.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, MainActivity.getInstance().getNumpadScanFragment(), "Numpad").commit();
                break;
            }
            case R.id.toggle_btn_edit: {
                MainActivity.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, MainActivity.getInstance().getNumpadEditFragment(), "NumpadEdit").commit();
                break;
            }
            case R.id.toggle_btn_browse: {
                MainActivity.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, MainActivity.getInstance().getQuickLaunchFragment(), "QuickLaunch").commit();
                break;
            }
            case R.id.toggle_btn_search: {
                MainActivity.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, MainActivity.getInstance().getProductSearchFragment(), "Search").commit();
                break;
            }
            case R.id.toggle_btn_orders: {
				MainActivity.getInstance().getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, MainActivity.getInstance().getOrdersFragment(), "Orders").commit();
                break;
            }
        }
    }

    public void setupTopMenuBar() {
        setupTopBarToggleGroup();
    }

    public void toggleInitialNumpadFragment() {
        toggleGroup.check(R.id.toggle_btn_scan);
    }

    public void toggleButtonsEnabled(boolean b) {
        toggle_btn_browse.setEnabled(b);
        toggle_btn_edit.setEnabled(b);
        toggle_btn_scan.setEnabled(b);
        toggle_btn_search.setEnabled(b);
    }

    public void untoggleViews() {
        toggle_btn_browse.setChecked(false);
        toggle_btn_edit.setChecked(false);
        toggle_btn_scan.setChecked(false);
        toggle_btn_search.setChecked(false);
        toggleGroup.clearCheck();
    }
}
