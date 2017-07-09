package no.susoft.mobile.pos.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.SusoftPOSApplication;

public class TabHandlerFragment extends Fragment {

    private FragmentTabHost mTabHost;

    public TabHandlerFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tab_handler_fragment, container, false);

        mTabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);

        //		mTabHost.addTab(mTabHost.newTabSpec("numpad").setIndicator("Numpad", getResources().getDrawable(R.drawable.ic_calculator)), NumpadScanFragment.class, null);
        //		mTabHost.addTab(mTabHost.newTabSpec("quicklaunch").setIndicator("Quick launch", getResources().getDrawable(R.drawable.ic_qlm_menu)), QuickLaunchFragment.class, null);

        try {
            TabSpec numpadSpec = mTabHost.newTabSpec("numpad");
            View numpadTabIndicator = LayoutInflater.from(SusoftPOSApplication.getContext()).inflate(R.layout.tab_indicator_fragment, mTabHost.getTabWidget(), false);
            ((TextView) numpadTabIndicator.findViewById(R.id.tabTitle)).setText("NUMPAD");
            ((ImageView) numpadTabIndicator.findViewById(R.id.tabIcon)).setImageResource(R.drawable.ic_calculator);
            numpadTabIndicator.setBackground(getResources().getDrawable(R.drawable.tab_bg_selector_first));
            numpadSpec.setIndicator(numpadTabIndicator);
            mTabHost.addTab(numpadSpec, NumpadScanFragment.class, null);

            TabSpec qlmSpec = mTabHost.newTabSpec("quicklaunch");
            View qlmTabIndicator = LayoutInflater.from(SusoftPOSApplication.getContext()).inflate(R.layout.tab_indicator_fragment, mTabHost.getTabWidget(), false);
            ((TextView) qlmTabIndicator.findViewById(R.id.tabTitle)).setText("QUICK LAUNCH");
            ((ImageView) qlmTabIndicator.findViewById(R.id.tabIcon)).setImageResource(R.drawable.ic_qlm_menu);
            qlmTabIndicator.setBackground(getResources().getDrawable(R.drawable.tab_bg_selector_last));
            qlmSpec.setIndicator(qlmTabIndicator);
            mTabHost.addTab(qlmSpec, QuickLaunchFragment.class, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int tabs = mTabHost.getTabWidget().getChildCount();
        int tabWeight = 100;
        if (tabs > 0) {
            tabWeight = Math.round(100 / tabs);
        }
        for (int i = 0; i < tabs; i++) {
            LinearLayout x = (LinearLayout) mTabHost.getTabWidget().getChildAt(i).findViewById(R.id.tabLayout);
            if (x.getLayoutParams() != null) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) x.getLayoutParams();
                params.weight = tabWeight;
                params.gravity = Gravity.CENTER;
                x.setLayoutParams(params);
            }
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }
}
