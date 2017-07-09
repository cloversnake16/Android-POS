package no.susoft.mobile.pos.ui.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Customer;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;
import no.susoft.mobile.pos.ui.activity.util.MainTopBarMenu;
import no.susoft.mobile.pos.ui.activity.util.SecondaryDisplay;
import no.susoft.mobile.pos.ui.adapter.CartOrderLinesListAdapter;
import no.susoft.mobile.pos.ui.adapter.OrderIndexSpinnerAdapter;
import no.susoft.mobile.pos.ui.dialog.CustomerOrderSearchDialog;
import no.susoft.mobile.pos.ui.dialog.SplitOrderDialog;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;
import no.susoft.mobile.pos.ui.fragment.utils.CartButtons;
import no.susoft.mobile.pos.ui.fragment.utils.CartViewUtils;

public class CartFragment extends Fragment {

	//region InjectView
	@InjectView(R.id.title)
	TextView title;
	@InjectView(R.id.lvOrderLines)
	ListView lvOrderLines;
	@InjectView(R.id.tvTotalSum)
	TextView totalSum;
	@InjectView(R.id.tvTotalQuantity)
	TextView totalQty;
	@InjectView(R.id.cartFooter)
	LinearLayout cartFooter;
	@InjectView(R.id.cartHeader)
	LinearLayout cartHeader;
	@InjectView(R.id.tableNumber)
	TextView tvTableNumber;
	@InjectView(R.id.orderIndex)
	Spinner orderSpinner;
	@InjectView(R.id.iv_customer_notes)
	ImageView ivCustomerNotes;
	@InjectView(R.id.iv_customer_order_list)
	ImageView ivCustomerOrderList;

	//endregion

	private CartViewUtils cartViewUtils;
	private CartButtons buttons;

	private int lastAddedLineNumber;
	private SplitOrderDialog splitOrderDialog;
	public CartOrderLinesListAdapter adapter;

	private Cart cart;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MainActivity.getInstance().setCartFragment(this);
		cartViewUtils = new CartViewUtils();
		cart = Cart.INSTANCE;
		cart.setFragment(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.cart_fragment, container, false);
		ButterKnife.inject(this, view);
		setupCartOrderLineAdapter();

		LinearLayout buttonRoot = (LinearLayout) view.findViewById(R.id.cart_menu_bar);
		setupButtons(buttonRoot);
		setupListeners();

		updateTableNumber();
		setupOrderSpinner();
		refreshCart();

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		//setupButtons(getView());
	}

	private void setupButtons(LinearLayout view) {
		buttons = new CartButtons(view);
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			if (Cart.INSTANCE.hasActiveOrder()) {
				cartViewUtils.highlightLineOnResume(lvOrderLines, cart.getSelectedLineIndex());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//LISTENERS ========================================================================================================
	private void setupListeners() {
		setLvOrderLinesOnItemClickListener();
		setTitleOnClickListener();
		setCustomerNotesOnClickListener();
		ivCustomerOrderList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CustomerOrderSearchDialog searchDialog = new CustomerOrderSearchDialog();
				searchDialog.show(MainActivity.getInstance().getSupportFragmentManager(), "ordersearch");
				MainActivity.getInstance().getServerCallMethods().searchCompleteOrdersFromServer(searchDialog, "", "", "", "", "", Cart.INSTANCE.getOrder().getCustomer().getId());
			}
		});
	}

	private void setLvOrderLinesOnItemClickListener() {
		lvOrderLines.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				doCartItemClick(i);
			}
		});
	}

	private void setTitleOnClickListener() {
		title.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				doTitleClick();
			}
		});
	}

	private void setCustomerNotesOnClickListener() {
		ivCustomerNotes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				MainActivity.getInstance().getMainShell().showCustomerNotesDialog();
			}
		});
	}

	private void setCartFooterListener() {
		cartFooter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doFooterClick();
			}
		});
	}

	//LISTENER CLICKS ==================================================================================================

	private void doCartItemClick(int position) {
		if (adapter.getItem(position) != null) {
			setCartListViewSelectionToIndex(position);
			enableEditFragment();
		}
	}

	private void enableEditFragment() {
		Fragment currentFragment = MainActivity.getInstance().getSupportFragmentManager().findFragmentByTag("NumpadEdit");
		if (currentFragment != null) {
			((NumpadEditFragment) currentFragment).loadOrderLineInfo(Cart.selectedLine);
		} else {
			MainTopBarMenu.getInstance().toggleEditView();
		}
	}

	private void doTitleClick() {
		MainActivity.getInstance().getMainShell().showCustomerSearchDialog();
	}

	private void doFooterClick() {
		buttons.togglePayView();
	}

	//ADAPTERS ETC =====================================================================================================

	public void updateOrderSpinner() {
		if (cart.hasNoneOrOneOrder()) {
			orderSpinner.setVisibility(View.GONE);
		} else {
			orderSpinner.setAdapter(new OrderIndexSpinnerAdapter(this.getActivity(), R.id.order_id_spinner_text, cart.getOrders()));

			//Set selected index to last item if index is too high
			if (Cart.selectedOrderIndex >= cart.getOrders().size()) {
				Cart.selectedOrderIndex = (orderSpinner.getCount() - 1);
			}

			orderSpinner.setSelection(Cart.selectedOrderIndex, false);
			orderSpinner.setVisibility(View.VISIBLE);
		}
	}

	private void setupCartOrderLineAdapter() {
		if (adapter == null) {
			adapter = new CartOrderLinesListAdapter(MainActivity.getInstance(), 0, new ArrayList<OrderLine>());
			lvOrderLines.setAdapter(adapter);
		}
	}

	private void setupOrderSpinner() {
		updateOrderSpinner();
		if (orderSpinner.getOnItemSelectedListener() == null) {
			orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
					//Only do something if the previously selected index is different than what is now selected,
					//else infinite loop
					if (Cart.selectedOrderIndex != i) {
						Cart.selectedOrderIndex = i;
						cart.setOrder(cart.getOrders().get(i));
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {
				}

			});
		}
	}

	//======================================================================================================
	//======================================================================================================
	// CART

	public void refreshCart() {
		if (cart.hasOrdersWithLines()) {
			if (adapter.getList().equals(cart.getOrder().getLines())) {
				adapter.notifyDataSetChanged();
			} else {
				adapter.clear();
				adapter.addAll(cart.getOrder().getLines());
			}
		}
		
		updateCartVisibility(cart.hasOrdersWithLines());
		refreshCartButtonStates();
		if (cart.getOrder() != null) {
			_orderCustomerUpdated(cart.getOrder().getCustomer());
			if (AppConfig.getState().getDisplayName().length() > 0) {
				MainActivity.getInstance().sendCustomerToSecondaryDisplay(cart.getOrder().getCustomer());
			}
		} else {
			_orderCustomerUpdated(null);
		}

		updateTableNumber();
		updateOrderSpinner();
		if (AppConfig.getState().getDisplayName().length() > 0) {
			if (cart.hasOrdersWithLines()) {
				MainActivity.getInstance().sendOrderLinesToSecondaryDisplay();
			} else {
				MainActivity.getInstance().sendResetViewToSecondaryDisplay();
			}
		}
	}

	private boolean resetPayments() {
		Fragment paymentFragment = MainActivity.getInstance().getSupportFragmentManager().findFragmentByTag("Pay");
		if (paymentFragment != null) {
			MainActivity.getInstance().getNumpadPayFragment().resetPayments();
			return true;
		}
		return false;
	}

	private void refreshCartButtonStates() {
		try {
			buttons.refreshCartButtonStates();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public CartButtons getCartButtons() {
		return buttons;
	}

	//======================================================================================================
	//======================================================================================================
	// ORDERS

	public void setActiveOrder(Order order) {
		if (order != null) {
			Cart.selectedOrderIndex = cart.getOrders().indexOf(order);
			_orderCustomerUpdated(order.getCustomer());

			refreshCart();

			int lastLineIndex = order.getLines().size() - 1;
			setCartListViewSelectionToIndex(lastLineIndex);

		} else {
			cart.deleteOrder();
			cart.setSelectedLine(null);
		}
	}

	public void _deleteOrder() {
		updateOrderSpinner();
		updateTableNumber();
		this.refreshCart();
		this.selectLastRow();
		try {
			buttons.refreshCartButtonStates();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// ORDER LINES ========================================================================================

	public void afterLineIsAdded(int line) {
		refreshCart();
		cartRequestFocus();
		setCartListViewSelectionToIndex(line);
	}

	private void handleCartLineSelectionAfterDelete(int deletedLineIndex) {
		try {

			int orderLines = cart.getOrder().getLines().size();

			if (orderLines > deletedLineIndex) {
				setCartListViewSelectionToIndex(deletedLineIndex);
			} else if (deletedLineIndex > 0 && orderLines > 1) {
				setCartListViewSelectionToIndex(deletedLineIndex - 1);
			} else if (orderLines == 1) {
				setCartListViewSelectionToIndex(0);
			} else if (orderLines == 0) {
				cart.setSelectedLine(null);
			}

			refreshFragmentView();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void refreshFragmentView() {
		//Refreshing the edit view
		MainActivity.getInstance().refreshActiveFragment();
	}

	public void updateTableNumber() {
		try {
			if (cart.getOrder() != null) {
				tvTableNumber.setText(getString(R.string.table_first_letter) + " " + String.valueOf(cart.getOrder().getTable()));
				if (cart.getOrder().getTable() > 0) {
					tvTableNumber.setVisibility(View.VISIBLE);
				} else {
					tvTableNumber.setVisibility(View.GONE);
				}
			} else {
				tvTableNumber.setVisibility(View.GONE);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void showSplitOrderDialog() {
		splitOrderDialog = new SplitOrderDialog();
		splitOrderDialog.show(MainActivity.getInstance().getSupportFragmentManager(), "SplitOrderDialog");
	}

	public SplitOrderDialog getSplitOrderDialog() {
		return splitOrderDialog;
	}

	// ORDERS UTILS =========================================================================================

	/**
	 * Hides or shows the cart footer, header and orderlines on both main and secondary screen depending on the parameter it receives, true for show and false for hide.
	 * Used in refreshCart()
	 **/
	private void updateCartVisibility(boolean show) {
		if (show && cart.getOrder() != null) {
			setFooterNumbers(cart.getOrder());
			showCart(true);
			setCartFooterListener();
			refreshSecondaryDisplayCart(true);
		} else {
			cartFooter.setOnClickListener(null);
			showCart(false);
			refreshSecondaryDisplayCart(false);
		}
	}

	private void setFooterNumbers(Order order) {
		totalSum.setText(String.valueOf(order.getAmount(true).toString()));
		totalQty.setText(String.valueOf(order.getNumberOfItems()));
	}

	private void refreshSecondaryDisplayCart(boolean state) {
		if (SecondaryDisplay.getInstance() != null) {
			SecondaryDisplay.getInstance().refreshSecondaryDisplayCart(state);
		}
	}

	private void showCart(boolean show) {
		if (show) {
			lvOrderLines.setVisibility(View.VISIBLE);
			cartFooter.setVisibility(View.VISIBLE);
			cartHeader.setVisibility(View.VISIBLE);
		} else {
			lvOrderLines.setVisibility(View.GONE);
			cartFooter.setVisibility(View.GONE);
			cartHeader.setVisibility(View.GONE);
		}
	}

	//==================================================================================================================
	// ORDER LINE SELECTION STUFF

	public void selectLastRow() {
		if (cart.getOrder() != null && cart.getOrder().getLines() != null && cart.getOrder().getLines().size() > 0) {
			setCartListViewSelectionToIndex(cart.getOrder().getLines().size() - 1);
		}
	}

	private void setCartListViewSelectionToIndex(int lineIndex) {
		try {
			cart.setSelectedLine((OrderLine) lvOrderLines.getItemAtPosition(lineIndex));
			cartRequestFocus();
			//			lvOrderLines.setItemChecked(lineIndex, true);
			//			lvOrderLines.setSelection(lineIndex);
			//			lvOrderLines.setActivated(true);

			//			doSecondaryDisplay(lineIndex);

			//cartViewUtils.highlightThisRowOnly(lvOrderLines.getChildAt(lineIndex), lvOrderLines);
			cartViewUtils.doHighlightOnThisRowOnly(lvOrderLines.getChildAt(lineIndex), lvOrderLines);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doSecondaryDisplay(int lineIndex) {
		if (SecondaryDisplay.getInstance().getSimplePresentation() != null) {
			SecondaryDisplay.getInstance().getSimplePresentation().setSecondaryDisplaySelection(lineIndex);
		}
	}

	private void cartRequestFocus() {
		MainActivity.getInstance().findViewById(R.id.root).requestFocusFromTouch();
		lvOrderLines.requestFocusFromTouch();
	}

	//===================================================================================================
	// TITLE

	public void _orderCustomerUpdated(Customer customer) {
		if (customer != null) {
			setCartTitleToCustomer(customer);
		} else {
			setCartTitleToDefault();
		}
	}

	public void setCartTitleToDefault() {
		title.setText(R.string.customer);
		ivCustomerNotes.setVisibility(View.GONE);
		ivCustomerOrderList.setVisibility(View.GONE);
	}

	private void setCartTitleToCustomer(Customer customer) {
		title.setText(customer.getName());
		ivCustomerNotes.setVisibility(View.VISIBLE);
		ivCustomerOrderList.setVisibility(View.VISIBLE);
	}

	public void setCartTitleVisibility(boolean visible) {
		if (visible) {
			title.setVisibility(View.VISIBLE);
			ivCustomerNotes.setVisibility(View.VISIBLE);
			ivCustomerOrderList.setVisibility(View.VISIBLE);
		} else {
			title.setVisibility(View.INVISIBLE);
			ivCustomerNotes.setVisibility(View.INVISIBLE);
			ivCustomerOrderList.setVisibility(View.INVISIBLE);
		}
	}

	// PAY ================================================================================================

	public void doPayStuff() {
		if (cart.hasSplitOrders()) {
			if (cart.getSplitOrders().size() == 1) {
				cart.setOrder(cart.getSplitOrders().get(0));
				MainTopBarMenu.getInstance().toggleButtonsEnabled(false);
				buttons.togglePayView();
			} else {
				showSplitOrderDialog();
			}
		} else {
			MainTopBarMenu.getInstance().toggleButtonsEnabled(true);
		}
	}

	// CALLED FROM CARTORDERS CLASS ====================================================================================

	public void _orderSet(Order order) {
		_orderCustomerUpdated(order.getCustomer());
		refreshCart();
		if (order.getLines() != null && order.getLines().size() > 0) {
			setCartListViewSelectionToIndex(order.getLines().size() - 1);
		}

	}

	public void _orderLineRemoved(int lineNumber) {
		adapter.notifyDataSetChanged();
		refreshCart();
		handleCartLineSelectionAfterDelete(lineNumber);
		getCartButtons().refreshCartButtonStates();
	}

	public void resetTakeAway() {
		Cart.INSTANCE.setTakeAwayMode(false);
		if (buttons != null) {
			buttons.updateTakeAwayColour();
		}
	}

}
