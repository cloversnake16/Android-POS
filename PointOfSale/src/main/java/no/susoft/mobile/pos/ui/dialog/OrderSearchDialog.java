package no.susoft.mobile.pos.ui.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.OrderPointer;
import no.susoft.mobile.pos.server.LoadCompleteOrderByIDAsync;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.OrderListAdapter;

import static no.susoft.mobile.pos.ui.dialog._DialogUtils.*;

public class OrderSearchDialog extends DialogFragment {
	
	@InjectView(R.id.progress_bar_orders)
	ProgressBar progressBar;
	@InjectView(R.id.no_orders)
	TextView noOrdersAvailable;
	@InjectView(R.id.order_list_header)
	LinearLayout listHeader;
	@InjectView(R.id.order_list)
	ListView orderList;
	@InjectView(R.id.order_search_button)
	Button searchButton;
	@InjectView(R.id.order_search_from_amount)
	EditText fromAmount;
	@InjectView(R.id.order_search_to_amount)
	EditText toAmount;
	@InjectView(R.id.order_search_from_date)
	DatePicker fromDate;
	@InjectView(R.id.order_search_to_date)
	DatePicker toDate;
	@InjectView(R.id.order_search_id)
	EditText orderId;
	@InjectView(R.id.order_search_date_layout)
	RelativeLayout dateLayout;
	@InjectView(R.id.order_search_params_layout)
	RelativeLayout paramsLayout;
	@InjectView(R.id.order_new_search_button)
	Button newSearchButton;
	@InjectView(R.id.search_wrapper)
	RelativeLayout searchWrapper;
	
	private boolean optionsAreHidden = false;
	private Decimal fromAm = null;
	private Decimal toAm = null;
	
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
		setView(newSearchButton, View.GONE);
		setSearchOnClickListener();
		setNewSearchOnClickListener();
		
		final OrderSearchDialog d = this;
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
	
	private void setSearchOnClickListener() {
		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (optionsAreHidden) {
					showOptions(true);
				} else {
					doSearch();
				}
			}
		});
	}
	
	private void setNewSearchOnClickListener() {
		newSearchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showOptions(true);
				orderList.setAdapter(null);
				setView(newSearchButton, View.GONE);
			}
		});
	}
	
	private void doSearch() {
		showProgressBar();
		showOptions(false);
		
		if (datesValid(getDateFromDatePicker(fromDate), getDateFromDatePicker(toDate)) && amountInputValid()) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			String from = df.format(getDateFromDatePicker(fromDate));
			String to = df.format(getDateFromDatePicker(toDate));
			MainActivity.getInstance().getServerCallMethods().searchCompleteOrdersFromServer(this, orderId.getText().toString(), from, to, fromAmount.getText().toString(), toAmount.getText().toString(), "");
		}
	}
	
	private void showOptions(boolean b) {
		if (b) {
			setVisibility(View.VISIBLE, searchWrapper, dateLayout, paramsLayout);
			orderList.setVisibility(View.GONE);
			initialiseViews();
			optionsAreHidden = false;
		} else {
			setVisibility(View.GONE, searchWrapper, dateLayout, paramsLayout);
			orderList.setVisibility(View.VISIBLE);
			optionsAreHidden = true;
		}
	}
	
	private void initialiseViews() {
		setVisibility(View.GONE, progressBar, noOrdersAvailable, listHeader);
	}
	
	private void showProgressBar() {
		setVisibility(View.VISIBLE, progressBar);
		setVisibility(View.GONE, noOrdersAvailable, listHeader);
	}
	
	public void refreshAdapter(ArrayList<OrderPointer> orders) {
		setView(newSearchButton, View.VISIBLE);
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
	
	private boolean amountInputValid() {
		try {
			if (fromAmount.getText().toString().length() > 0) {
				fromAm = Decimal.make(fromAmount.getText().toString());
			}
			if (toAmount.getText().toString().length() > 0) {
				toAm = Decimal.make(toAmount.getText().toString());
			}
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
			Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	private void showFoundOrdersViews() {
		setVisibility(View.GONE, progressBar, noOrdersAvailable);
		setVisibility(View.VISIBLE, listHeader, newSearchButton);
		showOptions(false);
	}
	
	private void showFoundNoOrdersViews() {
		setVisibility(View.VISIBLE, noOrdersAvailable);
		setVisibility(View.GONE, progressBar, listHeader, newSearchButton);
		showOptions(true);
	}
	
}
