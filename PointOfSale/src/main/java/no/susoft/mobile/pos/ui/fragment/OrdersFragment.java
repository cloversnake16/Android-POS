package no.susoft.mobile.pos.ui.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderPointer;
import no.susoft.mobile.pos.server.DbAPI;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;
import no.susoft.mobile.pos.ui.activity.util.MainTopBarMenu;
import no.susoft.mobile.pos.ui.adapter.OrderListAdapter;

public class OrdersFragment extends Fragment {

    @InjectView(R.id.progress_bar_orders)
    ProgressBar progressBar;
    @InjectView(R.id.no_orders)
    TextView noOrdersAvailable;
    @InjectView(R.id.order_list_header)
    LinearLayout listHeader;
    @InjectView(R.id.order_list)
    ListView orderList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.getInstance().setOrdersFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.orders_fragment, container, false);
        ButterKnife.inject(this, rootView);

        showProgressBar();

        orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapterView.setSelection(i);
                OrderPointer o = (OrderPointer) adapterView.getItemAtPosition(i);
                if (o != null && MainActivity.getInstance().getCartFragment() != null && MainActivity.getInstance().getNumpadScanFragment() != null) {
					if (AppConfig.getState().isRestaurant()) {
						Order order = DbAPI.getKitchenOrder(o.shopId, o.id);
						MainActivity.getInstance().getServerCallMethods().orderLoadByIDAsyncPostExecute(order);
					} else if (AppConfig.getState().isWorkshop()) {
						if (MainActivity.getInstance().isConnected()) {
							MainActivity.getInstance().getServerCallMethods().loadOrderByID("" + o.id);
						}
					} else {
						Order order = DbAPI.getOrder(o.shopId, o.id);
						MainActivity.getInstance().getServerCallMethods().orderLoadByIDAsyncPostExecute(order);
					}
                    MainTopBarMenu.getInstance().toggleScanView();
                }
            }
        });

		refreshAdapter(MainActivity.getInstance().getOrderResults());
		
        return rootView;
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        noOrdersAvailable.setVisibility(View.GONE);
        listHeader.setVisibility(View.GONE);
    }

    public void refreshAdapter(ArrayList<OrderPointer> orders) {
		if (orderList != null ) {
			if (progressBar != null) {
				progressBar.setVisibility(View.GONE);
			}
			try {
				if (orders.size() > 0) {
					showFoundOrdersViews();
					OrderListAdapter adapter = new OrderListAdapter(MainActivity.getInstance(), 0, orders);
					orderList.setAdapter(adapter);
				} else {
					showFoundNoOrdersViews();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
    }

    private void showFoundOrdersViews() {
		if (noOrdersAvailable != null && listHeader != null) {
			noOrdersAvailable.setVisibility(View.GONE);
			listHeader.setVisibility(View.VISIBLE);
		}
    }

    private void showFoundNoOrdersViews() {
        noOrdersAvailable.setVisibility(View.VISIBLE);
        listHeader.setVisibility(View.GONE);
    }

}
