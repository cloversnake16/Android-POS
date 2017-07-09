package no.susoft.mobile.pos.ui.activity;

import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import jp.co.casio.vx.framework.device.IButton;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.Utilities;
import no.susoft.mobile.pos.ui.utils.DrawerLayoutInstaller;
import no.susoft.mobile.pos.ui.view.GlobalMenuView;

public class BaseActivity extends AppCompatActivity implements GlobalMenuView.OnHeaderClickListener {

    @Optional
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private DrawerLayout drawerLayout;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.inject(this);
        setupToolbar();
        if (shouldInstallDrawer()) {
            setupDrawer();
        }
    }

    protected void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            toolbar.setNavigationIcon(R.drawable.ic_navigation_menu);
            toolbar.setTitle("");
        }
    }

    protected boolean shouldInstallDrawer() {
        return true;
    }

    private void setupDrawer() {
        GlobalMenuView menuView = new GlobalMenuView(this);
        menuView.setOnHeaderClickListener(this);

        drawerLayout = DrawerLayoutInstaller.from(this).drawerRoot(R.layout.drawer_root).drawerLeftView(menuView).drawerLeftWidth(Utilities.dpToPx(300)).drawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED).withNavigationIconToggler(getToolbar()).build();
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void onGlobalMenuHeaderClick(final View v) {
        drawerLayout.closeDrawer(GravityCompat.START);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int[] startingLocation = new int[2];
                v.getLocationOnScreen(startingLocation);
                startingLocation[0] += v.getWidth() / 2;
                overridePendingTransition(0, 0);
            }
        }, 200);
    }
}
