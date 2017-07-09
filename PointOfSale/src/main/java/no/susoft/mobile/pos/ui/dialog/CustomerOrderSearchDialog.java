package no.susoft.mobile.pos.ui.dialog;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.OrderPointer;
import no.susoft.mobile.pos.server.LoadCompleteOrderByIDAsync;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.OrderListAdapter;

import static no.susoft.mobile.pos.ui.dialog._DialogUtils.setVisibility;

public class CustomerOrderSearchDialog extends OrderSearchDialog {
	
	@InjectView(R.id.progress_bar_orders)
	ProgressBar progressBar;
	@InjectView(R.id.no_orders)
	TextView noOrdersAvailable;
	@InjectView(R.id.order_list_header)
	LinearLayout listHeader;
	@InjectView(R.id.order_list)
	ListView orderList;
	@InjectView(R.id.search_wrapper)
	RelativeLayout searchWrapper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//MainActivity.getInstance().setOrdersFragment(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		final View rootView = inflater.inflate(R.layout.orders_search_fragment, container, false);
		ButterKnife.inject(this, rootView);
		
		initialiseViews();
		final CustomerOrderSearchDialog d = this;
		orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				adapterView.setSelection(i);
				OrderPointer o = (OrderPointer) adapterView.getItemAtPosition(i);
				//displayOrder(o);
				LoadCompleteOrderByIDAsync task = new LoadCompleteOrderByIDAsync();
				task.setSearchDialog(d);
				task.execute(o.shopId + o.id);
			}
		});
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		try {
			getDialog().setTitle(R.string.search_for_orders);
			//getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		fromDate.setCalendarViewShown(false);
		toDate.setCalendarViewShown(false);
	}
	
	private void setView(View view, int state) {
		view.setVisibility(state);
	}
	
	private void initialiseViews() {
		setVisibility(View.GONE, progressBar, noOrdersAvailable, listHeader, searchWrapper, newSearchButton);
	}
	
	private void showProgressBar() {
		setVisibility(View.VISIBLE, progressBar);
		setVisibility(View.GONE, noOrdersAvailable, listHeader);
	}
	
	public void refreshAdapter(ArrayList<OrderPointer> orders) {
		progressBar.setVisibility(View.GONE);
		
		if (orders.size() > 0) {
			showFoundOrdersViews();
			
			if (orders.size() > 0) {
				OrderListAdapter adapter = new OrderListAdapter(MainActivity.getInstance(), 0, orders);
				orderList.setAdapter(adapter);
			} else {
				showFoundNoOrdersViews();
				Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.found_no_orders), Toast.LENGTH_SHORT).show();
			}
		} else {
			showFoundNoOrdersViews();
			
		}
	}
	
	private void showFoundOrdersViews() {
		setVisibility(View.GONE, progressBar, noOrdersAvailable);
		setVisibility(View.VISIBLE, listHeader);
		orderList.setVisibility(View.VISIBLE);
	}
	
	private void showFoundNoOrdersViews() {
		setVisibility(View.VISIBLE, noOrdersAvailable);
		setVisibility(View.GONE, progressBar, listHeader, newSearchButton);
		orderList.setVisibility(View.GONE);
	}
	
}
