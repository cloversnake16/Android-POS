package no.susoft.mobile.pos.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.MainTopBarMenu;
import no.susoft.mobile.pos.ui.adapter.CartOrderLinesListDragDropAdapter;
import no.susoft.mobile.pos.ui.adapter.SplitOrderOrderListAdapter;
import no.susoft.mobile.pos.ui.adapter.utils.LinearLayoutListView;
import no.susoft.mobile.pos.ui.adapter.utils.OrderLinePassObject;
import no.susoft.mobile.pos.ui.adapter.utils.OrderPassObject;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;
import no.susoft.mobile.pos.ui.utils.SwipeDetector;

public class SplitOrderDialog extends DialogFragment {
	
	ListView mainOrderListView;
	ListView splitOrderListView;
	LinearLayout wholeFooter;
	LinearLayout splitFooter;
	TextView wholeQty;
	TextView wholeSum;
	TextView splitQty;
	TextView splitSum;
	EditText quantityEditText;
	Button buttonPlus;
	Button buttonMinus;
	Button buttonMove;
	Button buttonSplitOptions;
	LinearLayoutListView mainOrderArea;
	LinearLayoutListView splitOrderArea;
	ArrayList<Order> splitOrdersList;
	TextView splitOrderTvButton;
	OrderLine selectedLine;
	SwipeDetector swipeDetectorCurrent;
	Order newOrder;
	private boolean isMainList;
	private ArrayList<Order> startOrders;
	CartOrderLinesListDragDropAdapter mainAdapter;
	CartOrderLinesListDragDropAdapter newOrderAdapter;
	SplitOrderOrderListAdapter orderListAdapter;
	Order cartOrder;
	Order mainOrder;
	boolean hadSplitBeforeStart;
	
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		final View view = (inflater.inflate(R.layout.split_order_dialog, null));
		builder.setView(view).setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				clickCancel();
			}
		}).setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				clickConfirm();
			}
		}).setPositiveButton(R.string.pay_split_order, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				paySplitOrderClick();
			}
		});
		
		return builder.create();
	}
	
	private void clickCancel() {
		restoreOrdersToOriginalStateUponCancelling();
		//setPOSToPayOnlyModeIfPayingSplits();
	}
	
	private void clickConfirm() {
		try {
			addMainOrderToListIfHasLines();
			addNewOrderToListIfHasLines();
			if (splitOrdersList != null && !splitOrdersList.isEmpty()) {
				ArrayList<Order> extraOrder = new ArrayList<>();
				for (Order o : splitOrdersList) {
					if (!o.hasLines()) {
						extraOrder.add(o);
					}
				}
				for (Order o : extraOrder) {
					splitOrdersList.remove(o);
				}
				
				Cart.INSTANCE.setSplitOrderBeingPaid(true);
				
				sendOrdersToServerUponLeavingDialog();
				Log.i("vilde", "neworder has lines: " + newOrder.hasLines());
				if (newOrder.hasLines()) {
					setupCartFragmentWithSplitOrdersAndSetOrderToThis(newOrder);
				} else {
					setupCartFragmentWithSplitOrdersAndSetOrderToThis(null);
				}
			} else {
				Cart.INSTANCE.setSplitOrderBeingPaid(false);
				setupCartFragmentWithSingleOrder();
			}
		} catch (Exception ex) {
			restoreOrdersToOriginalStateUponCancelling();
			Log.i("vilde", ex.getMessage());
			Toast.makeText(MainActivity.getInstance(), R.string.error_splitting_try_again, Toast.LENGTH_LONG).show();
		}
	}
	
	private void addNewOrderToListIfHasLines() {
		if (newOrder.hasLines() && !splitOrdersList.contains(newOrder)) {
			splitOrdersList.add(newOrder);
		}
	}
	
	private void setPOSToPayOnlyModeIfPayingSplits() {
		if (Cart.INSTANCE.getSplitOrderBeingPaid()) {
			MainActivity.getInstance().getCartFragment().getCartButtons().togglePayView();
			MainTopBarMenu.getInstance().toggleButtonsEnabled(false);
		} else {
			MainTopBarMenu.getInstance().toggleButtonsEnabled(true);
		}
		Log.i("vilde", "split being paid: " + Cart.INSTANCE.getSplitOrderBeingPaid());
	}
	
	private void restoreOrdersToOriginalStateUponCancelling() {
		if (hadSplitBeforeStart) {
			if (startOrders != null) {
				Cart.INSTANCE.setOrdersTo(startOrders);
			}
		} else {
			splitOrdersList = null;
		}
		
		MainActivity.getInstance().getCartFragment().refreshCart();
		
	}
	
	private void paySplitOrderClick() {
		try {
			clickConfirm();
			MainActivity.getInstance().getCartFragment().getCartButtons().togglePayView();
		} catch (Exception ex) {
			ex.printStackTrace();
			restoreOrdersToOriginalStateUponCancelling();
			Log.i("vilde", ex.getMessage());
			Toast.makeText(MainActivity.getInstance(), R.string.error_splitting_try_again, Toast.LENGTH_LONG).show();
		}
		//setPOSToPayOnlyModeIfPayingSplits();
	}
	
	private void setupCartFragmentWithSplitOrdersAndSetOrderToThis(Order selectedOrder) {
		Cart.INSTANCE.setOrdersTo(splitOrdersList);
		if (selectedOrder != null) {
			MainActivity.getInstance().getCartFragment().setActiveOrder(selectedOrder);
		}
		MainActivity.getInstance().getCartFragment().refreshCart();
	}
	
	private void setupCartFragmentWithSingleOrder() {
		if (mainOrder.hasLines()) {
			MainActivity.getInstance().getCartFragment().setActiveOrder(mainOrder);
			MainActivity.getInstance().getCartFragment().refreshCart();
		}
	}
	
	private void addMainOrderToListIfHasLines() {
		if (mainOrder.hasLines() && !splitOrdersList.contains(mainOrder)) {
			splitOrdersList.add(mainOrder);
			Log.i("vilde", "number of orders: " + Cart.INSTANCE.getOrders().size());
			//			MainActivity.getInstance().getCartFragment().setRestOfSplitOrder(mainOrder);
		}
	}
	
	private void sendOrdersToServerUponLeavingDialog() {
		if (splitOrdersList.size() > 0) {
			sendAllSplitOrdersToServer();
		}
	}
	
	private void sendAllSplitOrdersToServer() {
		for (Order o : splitOrdersList) {
			if (!o.equals(newOrder)) {
				MainActivity.getInstance().getServerCallMethods().sendSplitOrderToServer(o);
			}
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		cartOrder = Cart.INSTANCE.getOrder();
		
		checkIfHadSplitViewsBefore();
		storeInitialOrderList();
		setFullScreenDialog();
		findViews();
		createNewSwipeDetector();
		createInitialNewOrders();
		setInitialListeners();
		setInitialOrders();
		setInitialAdapters();
		updateFootersWithTotals();
		checkIfOrdersHasMoreThanOneOrder();
	}
	
	private void checkIfHadSplitViewsBefore() {
		hadSplitBeforeStart = !ifCartOrdersIsNullOrEmpty();
	}
	
	private void storeInitialOrderList() {
		startOrders = new ArrayList<>();
		if (!ifCartOrdersIsNullOrEmpty()) {
			for (Order o : Cart.INSTANCE.getOrders()) {
				Order tempOrder = Cart.INSTANCE.createNewOrderAndReturnItHere();
				for (OrderLine ol : o.getLines()) {
					tempOrder.addOrderLine(ol.getProduct());
					tempOrder.getLineWithProduct(ol.getProduct()).setQuantity(ol.getQuantity());
					tempOrder.getLineWithProduct(ol.getProduct()).setPrice(ol.getPrice());
					tempOrder.setLocalOrderIdentifier(o.getLocalOrderIdentifier());
					if (o.getCustomer() != null) {
						tempOrder.setCustomer(o.getCustomer());
					}
				}
				startOrders.add(tempOrder);
			}
		}
	}
	
	private void setFullScreenDialog() {
		Dialog dialog = getDialog();
		if (dialog != null) {
			dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			int width = ViewGroup.LayoutParams.MATCH_PARENT;
			int height = ViewGroup.LayoutParams.MATCH_PARENT;
			dialog.getWindow().setLayout(width, height);
		}
	}
	
	private void findViews() {
		mainOrderListView = (ListView) getDialog().findViewById(R.id.current_order_list);
		splitOrderListView = (ListView) getDialog().findViewById(R.id.new_order_list);
		quantityEditText = (EditText) getDialog().findViewById(R.id.quantity_edit_text);
		wholeFooter = (LinearLayout) getDialog().findViewById(R.id.whole_cart_footer);
		splitFooter = (LinearLayout) getDialog().findViewById(R.id.split_cart_footer);
		wholeQty = (TextView) wholeFooter.findViewById(R.id.tvTotalQuantity);
		wholeSum = (TextView) wholeFooter.findViewById(R.id.tvTotalSum);
		splitQty = (TextView) splitFooter.findViewById(R.id.tvTotalQuantity);
		splitSum = (TextView) splitFooter.findViewById(R.id.tvTotalSum);
		buttonPlus = (Button) getDialog().findViewById(R.id.buttonPlus);
		buttonMinus = (Button) getDialog().findViewById(R.id.buttonMinus);
		buttonMove = (Button) getDialog().findViewById(R.id.buttonMove);
		buttonSplitOptions = (Button) getDialog().findViewById(R.id.buttonSplitOption);
		mainOrderArea = (LinearLayoutListView) getDialog().findViewById(R.id.split_list1);
		splitOrderArea = (LinearLayoutListView) getDialog().findViewById(R.id.split_list2);
		splitOrderTvButton = (TextView) getDialog().findViewById(R.id.split_order_header);
		
	}
	
	private void createNewSwipeDetector() {
		swipeDetectorCurrent = new SwipeDetector();
	}
	
	private void createInitialNewOrders() {
		mainOrder = createNewOrder();
		newOrder = createNewOrder();
	}
	
	private void setInitialListeners() {
		setInitialAreaListeners();
		setMainOrderListeners();
		setNewOrderListeners();
		setDialogCancelListener();
		setButtonListeners();
	}
	
	private void setInitialOrders() {
		if ((ifCartOrdersIsNullOrEmpty() && Cart.INSTANCE.getOrder() != null) || Cart.INSTANCE.getOrders().size() == 1) {
			mainOrder = Cart.INSTANCE.getOrder();
			splitOrdersList = new ArrayList<>();
		} else if (!ifCartOrdersIsNullOrEmpty() && Cart.INSTANCE.getOrders().size() > 1) {
			splitOrdersList = Cart.INSTANCE.getOrders();
		}
		
		//		else {
		//				mainOrder = cartOrder;
		//			}
		
	}
	
	private void setInitialAdapters() {
		mainAdapter = new CartOrderLinesListDragDropAdapter(getActivity(), 0, mainOrder.getLines());
		mainOrderListView.setAdapter(mainAdapter);
		newOrderAdapter = new CartOrderLinesListDragDropAdapter(getActivity(), 0, newOrder.getLines());
		splitOrderListView.setAdapter(newOrderAdapter);
	}
	
	public void updateFootersWithTotals() {
		wholeQty.setText(String.valueOf(mainOrder.getNumberOfItems()));
		wholeSum.setText(mainOrder.getAmount(true).toString());
		splitQty.setText(String.valueOf(newOrder.getNumberOfItems()));
		splitSum.setText(newOrder.getAmount(true).toString());
	}
	
	private void checkIfOrdersHasMoreThanOneOrder() {
		if (ifCartOrdersIsNullOrEmpty()) {
			splitOrdersList = new ArrayList<>();
		} else if (Cart.INSTANCE.getOrders().size() > 1) {
			splitOrdersList = Cart.INSTANCE.getOrders();
			switchToOrderListView();
		}
		updateAllAdaptersAndFooters();
	}
	
	//	private boolean ifCartFragmentHasRestOrder() {
	//		return MainActivity.getInstance().getCartFragment().getRestOfSplitOrder() != null;
	//	}
	
	//	private boolean ifCartFragmentRestOrderEqualsThisOrder(Order order) {
	//		return MainActivity.getInstance().getCartFragment().getRestOfSplitOrder().equals(order);
	//	}
	
	private boolean ifCartOrdersIsNullOrEmpty() {
		return (Cart.INSTANCE.getOrders() == null || Cart.INSTANCE.getOrders().isEmpty());
	}
	
	private void setInitialAreaListeners() {
		setMainDragListener();
		setSplitDetailListeners();
	}
	
	private void setMainDragListener() {
		mainOrderArea.setOnDragListener(getOrderLineListViewOnDragListener());
		mainOrderArea.setListView(mainOrderListView);
	}
	
	private void setSplitDetailListeners() {
		splitOrderArea.setOnDragListener(getOrderLineListViewOnDragListener());
		splitOrderArea.setListView(splitOrderListView);
		splitOrderListView.setOnItemLongClickListener(getOrderLineLongClickListener());
	}
	
	private void switchToOrderListView() {
		setNewOrderTextForOrderListView();
		setSplitOrderOrderListAdapter();
		clearSplitOrderListListeners();
		setSplitOrderSwitchToDetailListener();
		hideSplitFooter();
		payButtonEnabled(false);
		updateAllAdaptersAndFooters();
	}
	
	private void setSplitOrderOrderListAdapter() {
		orderListAdapter = new SplitOrderOrderListAdapter(getActivity(), 0, splitOrdersList);
		splitOrderListView.setAdapter(orderListAdapter);
	}
	
	private void clearSplitOrderListListeners() {
		splitOrderArea.setOnDragListener(null);
		splitOrderListView.setOnItemClickListener(null);
		splitOrderListView.setOnItemLongClickListener(null);
	}
	
	private void setNewOrderTextForOrderListView() {
		splitOrderTvButton.setText(R.string.split_order);
	}
	
	private void switchToOrderDetailView(Order order) {
		setOrderDetailAdapter(order);
		setSplitDetailListeners();
		showSplitFooter();
		payButtonEnabled(true);
	}
	
	private void setOrderDetailAdapter(Order order) {
		newOrderAdapter = new CartOrderLinesListDragDropAdapter(getActivity(), 0, order.getLines());
		splitOrderListView.setAdapter(newOrderAdapter);
	}
	
	private void setSplitOrderSwitchToDetailListener() {
		splitOrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				newOrder = (Order) view.getTag();
				switchToOrderDetailView(newOrder);
				setNewOrderListeners();
				updateFootersWithTotals();
				setNewOrderTextToIncludeOrderIdentifier(newOrder.getLocalOrderIdentifier());
			}
		});
		splitOrderListView.setOnItemLongClickListener(getOrderLongClickListener());
	}
	
	private void setNewOrderTextToIncludeOrderIdentifier(String localOrderIdentifier) {
		if (localOrderIdentifier == null || localOrderIdentifier.isEmpty()) {
			localOrderIdentifier = "";
		}
		splitOrderTvButton.setText(getString(R.string.split_order) + localOrderIdentifier);
	}
	
	private void hideSplitFooter() {
		splitFooter.setVisibility(View.INVISIBLE);
	}
	
	private void showSplitFooter() {
		splitFooter.setVisibility(View.VISIBLE);
	}
	
	private void payButtonEnabled(boolean bool) {
		((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(bool);
	}
	
	private Order createNewOrder() {
		return Cart.INSTANCE.createNewOrderAndReturnItHere();
	}
	
	private void setButtonListeners() {
		setButtonPlusListener();
		setButtonMinusListener();
		setButtonMoveListener();
		setButtonSplitOptionsListener();
		setSplitOrderTvButtonListener();
		
	}
	
	private void setButtonPlusListener() {
		buttonPlus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addToQtyEditText();
			}
		});
	}
	
	private void setButtonMinusListener() {
		buttonMinus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				subtractFromQtyEditText();
			}
		});
	}
	
	private void setButtonMoveListener() {
		buttonMove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectedLine != null) {
					if (quantityEditTextHasText()) {
						try {
							Decimal qty = handleDecimalFractionIfExists();
							qty = setQtyFromEditText(qty);
							moveSelectedOrderLineWithQty(qty);
						} catch (Exception ex) {
							return;
						}
					} else {
						moveSelectedOrderLineWithQty(selectedLine.getQuantity());
						//moveWholeOrderLine();
					}
				}
				highlightThisRowOnly(null);
				updateAllAdaptersAndFooters();
			}
		});
	}
	
	private void setButtonSplitOptionsListener() {
		buttonSplitOptions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createSplitOptionsDialog();
			}
		});
	}
	
	private void setSplitOrderTvButtonListener() {
		splitOrderTvButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (splitOrderListView.getAdapter().getClass().equals(CartOrderLinesListDragDropAdapter.class) && splitOrdersList.size() > 0) {
					switchToOrderListView();
				}
			}
		});
	}
	
	private void enableButtons(boolean bool) {
		buttonMove.setEnabled(bool);
		buttonPlus.setEnabled(bool);
		buttonMinus.setEnabled(bool);
	}
	
	private boolean quantityEditTextHasText() {
		return (quantityEditText.getText().length() > 0);
	}
	
	private Decimal setQtyFromEditText(Decimal qty) {
		if (qty == null) {
			qty = Decimal.make(quantityEditText.getText().toString());
		} else {
			qty = selectedLine.getQuantity().multiply(qty);
		}
		return qty;
	}
	
	private Decimal handleDecimalFractionIfExists() {
		if (quantityEditText.getText().toString().contains("/")) {
			String str = quantityEditText.getText().toString();
			String[] strings = str.split("/");
			try {
				Decimal number = Decimal.make(strings[0]).divide(Decimal.make(strings[1]));
				return number;
			} catch (Exception ex) {
			}
		}
		return null;
	}
	
	private void addToQtyEditText() {
		if (quantityEditTextHasText()) {
			try {
				subtractOneFromQtyEditText();
			} catch (Exception ex) {
			}
		}
	}
	
	private void addOneToQtyEditText() {
		Decimal num = Decimal.make(getQtyEditText()).subtract(Decimal.ONE);
		if (num.isLess(Decimal.ZERO)) {
			num = Decimal.ZERO;
		}
		setQtyEditText(num.toString());
	}
	
	private void subtractOneFromQtyEditText() {
		Decimal num = Decimal.make(getQtyEditText()).add(Decimal.ONE);
		if (num.isGreater(selectedLine.getQuantity())) {
			num = selectedLine.getQuantity();
		}
		setQtyEditText(num.toString());
	}
	
	private void subtractFromQtyEditText() {
		if (quantityEditTextHasText()) {
			try {
				addOneToQtyEditText();
			} catch (Exception ex) {
			}
		}
	}
	
	private void setQtyEditText(String text) {
		quantityEditText.setText(text);
	}
	
	private String getQtyEditText() {
		return quantityEditText.getText().toString();
	}
	
	private void populateQtyEditText(OrderLine ol) {
		quantityEditText.setText(ol.getQuantity().toString());
	}
	
	private void setDialogCancelListener() {
		getDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				restoreOrdersToOriginalStateUponCancelling();
			}
		});
	}
	
	private void setMainOrderListeners() {
		mainOrderListView.setOnItemLongClickListener(getOrderLineLongClickListener());
		mainOrderListView.setOnTouchListener(swipeDetectorCurrent);
		setMainOrderListOnClickListener();
	}
	
	private void setMainOrderListOnClickListener() {
		mainOrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//                if (swipeDetectorCurrent.swipeDetected()) {
				//                    if (swipeDetectorCurrent.getAction() == SwipeDetector.Action.LR) {
				//                        moveProductToNewOrder(MainActivity.getInstance().getCartFragment().adapter.getItem((int) view.getTag()));  }}
				doMainOrderItemClick(view, position);
				
			}
		});
	}
	
	private void doMainOrderItemClick(View view, int position) {
		mainOrderListView.setSelection(position);
		selectedLine = mainOrder.getLines().get(position);
		highlightThisRowOnly(view);
		isMainList = true;
		enableButtonsIfOrderDetailsViewIsShowing();
		try {
			populateQtyEditText(Cart.INSTANCE.getOrder().getLines().get(position));
		} catch (Exception ex) {
			Log.i("vilde", "something went wrong, it didnt add");
		}
	}
	
	private void setNewOrderListeners() {
		splitOrderListView.setOnItemLongClickListener(getOrderLineLongClickListener());
		splitOrderListView.setOnTouchListener(swipeDetectorCurrent);
		splitOrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (swipeDetectorCurrent.swipeDetected()) {
					//                    if (swipeDetectorCurrent.getAction() == SwipeDetector.Action.RL) {
					//                        moveProductToMainOrder(newOrderAdapter.getItem((int) view.getTag()));
					//                    }
				} else {
					splitOrderListView.setSelection(position);
					selectedLine = newOrderAdapter.getItem((int) view.getTag());
					highlightThisRowOnly(view);
					isMainList = false;
					enableButtons(true);
					try {
						populateQtyEditText(newOrderAdapter.getItem((int) view.getTag()));
					} catch (Exception ex) {
						Log.i("vilde", "something went wrong, it didnt add");
					}
				}
			}
		});
	}
	
	private void enableButtonsIfOrderDetailsViewIsShowing() {
		if (splitOrderListView.getAdapter().getClass().equals(CartOrderLinesListDragDropAdapter.class)) {
			enableButtons(true);
		} else {
			enableButtons(false);
		}
	}
	
	private void highlightThisRowOnly(View view) {
		unhighlightAllChildViews(mainOrderListView);
		unhighlightAllChildViews(splitOrderListView);
		if (view != null) {
			highlightRow(view);
		}
	}
	
	private void highlightRow(View rowView) {
		rowView.setBackgroundColor(getResources().getColor(R.color.et_orange));
		rowView.setActivated(true);
	}
	
	private void unhighlightAllChildViews(ListView view) {
		if (view.getChildCount() > 0) {
			for (int i = 0; i < view.getChildCount(); i++) {
				view.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
				view.setActivated(false);
			}
		}
	}
	
	private void moveSelectedOrderLineWithQty(Decimal qty) {
		if (isMainList) {
			moveOrderLineToNewOrder(selectedLine, qty);
		} else {
			moveOrderLineToMainOrder(selectedLine, qty);
		}
	}
	
	public void moveOneOrderLineQtyFromListToList(ListView oldParent, OrderLine passedItem) {
		if (oldParent.getId() == mainOrderListView.getId()) {
			moveOrderLineToNewOrder(passedItem, Decimal.ONE);
		} else if (oldParent.getId() == splitOrderListView.getId()) {
			moveOrderLineToMainOrder(passedItem, Decimal.ONE);
		}
	}
	
	private void moveWholeOrderLineFromOrderToOrder(OrderLine ol, Order fromOrder, Order toOrder) {
		addProductToOrderWithQuantity(toOrder, ol.getProduct(), ol.getQuantity());
		subtractQuantityFromOrderLineWithProduct(fromOrder, ol.getProduct(), ol.getQuantity());
		updateAllAdaptersAndFooters();
		selectedLine = null;
	}
	
	private void moveOrderLineToMainOrder(OrderLine ol, Decimal qty) {
		addProductToOrderWithQuantity(mainOrder, ol.getProduct(), qty);
		subtractQuantityFromOrderLineWithProduct(newOrder, ol.getProduct(), qty);
		updateAllAdaptersAndFooters();
		selectedLine = null;
	}
	
	private void moveOrderLineToNewOrder(OrderLine ol, Decimal qty) {
		setNewOrderAdapterIfNull();
		addProductToOrderWithQuantity(newOrder, ol.getProduct(), qty);
		subtractQuantityFromOrderLineWithProduct(mainOrder, ol.getProduct(), qty);
		updateFootersWithTotals();
		selectedLine = null;
	}
	
	private void moveAllLinesFromNewToMainOrder() {
		if (newOrder.hasLines()) {
			for (OrderLine ol : newOrder.getLines()) {
				moveWholeOrderLineFromOrderToOrder(ol, newOrder, mainOrder);
			}
		}
	}
	
	public void moveOneFromMainOrderToOrderListOrder(List<OrderLine> destinationList, OrderLine passedItem) {
		for (Order o : splitOrdersList) {
			if (o.getLines() == destinationList) {
				if (ifQuantityIsGreaterOrEqualToOne(passedItem)) {
					addProductToOrderWithQuantity(o, passedItem.getProduct(), Decimal.ONE);
				} else {
					addProductToOrderWithQuantity(o, passedItem.getProduct(), passedItem.getQuantity());
				}
				
				if (ifOrderHasLineWithProduct(mainOrder, passedItem.getProduct())) {
					subtractQuantityFromOrderLineWithProduct(mainOrder, passedItem.getProduct(), Decimal.ONE);
				}
			}
		}
	}
	
	private void addProductToOrderWithQuantity(Order order, Product product, Decimal qty) {
		if (!ifOrderHasLineWithProduct(order, product)) {
			order.addOrderLine(product);
			order.getLineWithProduct(product).setQuantity(qty);
		} else {
			order.getLineWithProduct(product).setQuantity(order.getLineWithProduct(product).getQuantity().add(qty));
		}
	}
	
	private void addProductToOrderWithThisPercentOfQuantity(Order order, Product product, Decimal qty, Decimal percent) {
		order.addOrderLine(product);
		order.getLineWithProduct(product).setQuantity(qty.multiply(percent));
	}
	
	private void subtractQuantityFromOrderLineWithProduct(Order order, Product product, Decimal qty) {
		order.getLineWithProduct(product).setQuantity(order.getLineWithProduct(product).getQuantity().subtract(qty));
		removeOrderLineIfQuantityIsZeroOrLess(order, order.getLineWithProduct(product));
	}
	
	private void removeOrderLineIfQuantityIsZeroOrLess(Order order, OrderLine orderLine) {
		if (ifQuantityIsLessOrEqualToZero(orderLine)) {
			order.removeOrderLine(orderLine);
		}
	}
	
	public void updateAllAdaptersAndFooters() {
		updateMainAdapter();
		updateCartAdapter();
		updateOrderListAdapter();
		updateNewOrderAdapter();
		updateFootersWithTotals();
	}
	
	private void updateCartAdapter() {
		if (MainActivity.getInstance().getCartFragment().adapter != null) {
			MainActivity.getInstance().getCartFragment().refreshCart();
		}
	}
	
	private void updateMainAdapter() {
		if (mainAdapter != null)
			mainAdapter.notifyDataSetChanged();
	}
	
	private void updateOrderListAdapter() {
		if (orderListAdapter != null)
			orderListAdapter.notifyDataSetChanged();
	}
	
	private void updateNewOrderAdapter() {
		if (newOrderAdapter != null)
			newOrderAdapter.notifyDataSetChanged();
	}
	
	private void setNewOrderAdapterIfNull() {
		if (newOrderAdapter == null) {
			newOrderAdapter = new CartOrderLinesListDragDropAdapter(getActivity(), 0, newOrder.getLines());
			splitOrderListView.setAdapter(newOrderAdapter);
		}
	}
	
	public void moveAllLinesFromOrderToOrder(Order fromOrder, Order toOrder) {
		for (OrderLine ol : fromOrder.getLines()) {
			toOrder.addOrderLine(ol);
		}
		fromOrder.getLines().clear();
		splitOrdersList.remove(fromOrder);
		updateAllAdaptersAndFooters();
	}
	
	public void moveAllLinesFromOrderToMainOrder(Order fromOrder) {
		
		for (OrderLine ol : fromOrder.getLines()) {
			addProductToOrderWithQuantity(mainOrder, ol.getProduct(), ol.getQuantity());
		}
		fromOrder.getLines().clear();
		splitOrdersList.remove(fromOrder);
		updateAllAdaptersAndFooters();
	}
	
	public View.OnDragListener getOrderLineListViewOnDragListener() {
		
		View.OnDragListener myOnDragListener = new View.OnDragListener() {
			
			@Override
			public boolean onDrag(View v, DragEvent event) {
				
				switch (event.getAction()) {
					case DragEvent.ACTION_DRAG_STARTED:
						Log.i("vilde", "ACTION_DRAG_STARTED: " + "\n");
						break;
					case DragEvent.ACTION_DROP:
						Log.i("vilde", "ACTION_DROP: " + "\n");
						doOrderLineDragActionDropEvent(v, event);
						break;
					default:
						break;
				}
				return true;
			}
			
		};
		
		return myOnDragListener;
	}
	
	private void doOrderLineDragActionDropEvent(View v, DragEvent event) {
		
		if (event.getLocalState().getClass().equals(OrderLinePassObject.class)) {
			OrderLinePassObject passObj = (OrderLinePassObject) event.getLocalState();
			passOrderLine(passObj, v);
		} else if (event.getLocalState().getClass().equals(OrderPassObject.class)) {
			OrderPassObject passObj = (OrderPassObject) event.getLocalState();
			passWholeOrder(passObj, v);
		}
		
	}
	
	private void passOrderLine(OrderLinePassObject passObj, View v) {
		try {
			Log.i("vilde", "tried to pass line");
			
			View view = passObj.view;
			OrderLine passedItem = passObj.item;
			
			ListView oldParent = (ListView) view.getParent();
			CartOrderLinesListDragDropAdapter srcAdapter = (CartOrderLinesListDragDropAdapter) (oldParent.getAdapter());
			
			LinearLayoutListView newParent = (LinearLayoutListView) v;
			CartOrderLinesListDragDropAdapter destAdapter = (CartOrderLinesListDragDropAdapter) (newParent.listView.getAdapter());
			
			moveOneOrderLineQtyFromListToList(oldParent, passedItem);
			
			srcAdapter.notifyDataSetChanged();
			destAdapter.notifyDataSetChanged();
			
			updateFootersWithTotals();
			
			newParent.listView.smoothScrollToPosition(destAdapter.getCount() - 1);
			
			return;
		} catch (Exception ex) {
			Log.i("vilde", "tried to pass line and failed");
		}
		
	}
	
	private void passWholeOrder(OrderPassObject passObj, View v) {
		try {
			Log.i("vilde", "tried to pass whole order");
			
			View view = passObj.view;
			Order passedItem = passObj.item;
			
			if (!v.equals(view))
				;
			{
				ListView oldParent = (ListView) view.getParent();
				SplitOrderOrderListAdapter srcAdapter = (SplitOrderOrderListAdapter) (oldParent.getAdapter());
				
				LinearLayoutListView newParent = (LinearLayoutListView) v;
				CartOrderLinesListDragDropAdapter destAdapter = (CartOrderLinesListDragDropAdapter) (newParent.listView.getAdapter());
				
				moveAllLinesFromOrderToMainOrder(passedItem);
				
				srcAdapter.notifyDataSetChanged();
				destAdapter.notifyDataSetChanged();
				
				updateFootersWithTotals();
				
				newParent.listView.smoothScrollToPosition(destAdapter.getCount() - 1);
			}
			
			return;
			
		} catch (Exception ex) {
			Log.i("vilde", "tried to pass whole order and failed");
			Log.i("vilde", ex.getMessage());
			ex.printStackTrace();
			
		}
		
	}
	
	private AdapterView.OnItemLongClickListener getOrderLineLongClickListener() {
		AdapterView.OnItemLongClickListener myOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
			
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					OrderLine selectedItem = (OrderLine) (parent.getItemAtPosition(position));
					
					CartOrderLinesListDragDropAdapter associatedAdapter = (CartOrderLinesListDragDropAdapter) (parent.getAdapter());
					List<OrderLine> associatedList = associatedAdapter.getList();
					
					OrderLinePassObject passObj = new OrderLinePassObject(view, selectedItem, associatedList);
					
					ClipData data = ClipData.newPlainText("", "");
					View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
					view.startDrag(data, shadowBuilder, passObj, 0);
					
				} catch (Exception ex) {
					
				}
				return true;
			}
			
		};
		return myOnItemLongClickListener;
	}
	
	public View.OnDragListener getOrderListListOnDragListener(final int position, final List<Order> list) {
		
		View.OnDragListener myOnDragListener = new View.OnDragListener() {
			
			@Override
			public boolean onDrag(View v, DragEvent event) {
				
				switch (event.getAction()) {
					case DragEvent.ACTION_DRAG_STARTED:
						Log.i("vilde", "ACTION_DRAG_STARTED: " + "\n");
						break;
					case DragEvent.ACTION_DROP:
						Log.i("vilde", "ACTION_DROP: " + "\n");
						
						try {
							OrderLinePassObject passObj = (OrderLinePassObject) event.getLocalState();
							View view = passObj.view;
							OrderLine passedItem = passObj.item;
							ListView oldParent = (ListView) view.getParent();
							
							List<OrderLine> destList = list.get(position).getLines();
							
							//moveWholeLineFromMainToOrderInOrderList(oldParent, destList, passedItem);
							moveOneFromMainOrderToOrderListOrder(destList, passedItem);
							
							updateAllAdaptersAndFooters();
						} catch (Exception ex) {
						}
						
						try {
							
						} catch (Exception ex) {
							OrderPassObject passObj = (OrderPassObject) event.getLocalState();
							View view = passObj.view;
							Order passedItem = passObj.item;
							ListView oldParent = (ListView) view.getParent();
							
							Order destOrder = list.get(position);
							
							//moveWholeLineFromMainToOrderInOrderList(oldParent, destList, passedItem);
							moveAllLinesFromOrderToOrder(passedItem, destOrder);
							
						}
						
						break;
					case DragEvent.ACTION_DRAG_ENDED:
						Log.i("vilde", "ACTION_DRAG_ENDED: " + "\n");
					default:
						break;
				}
				return true;
			}
			
		};
		return myOnDragListener;
	}
	
	private AdapterView.OnItemLongClickListener getOrderLongClickListener() {
		AdapterView.OnItemLongClickListener myOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
			
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					Order selectedItem = (Order) (parent.getItemAtPosition(position));
					
					SplitOrderOrderListAdapter associatedAdapter = (SplitOrderOrderListAdapter) (parent.getAdapter());
					List<Order> associatedList = associatedAdapter.getList();
					
					OrderPassObject passObj = new OrderPassObject(view, selectedItem, associatedList);
					
					ClipData data = ClipData.newPlainText("", "");
					View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
					view.startDrag(data, shadowBuilder, passObj, 0);
					
				} catch (Exception ex) {
					
				}
				return true;
			}
			
		};
		return myOnItemLongClickListener;
	}
	
	private void createSplitOptionsDialog() {
		MainActivity.getInstance().getCartFragment().getSplitOrderDialog().getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		
		final String[] option = new String[]{"Split evenly on number of people", "Split on number of people manually", "Split on amount", "Clear split"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item, option);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select Option");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0:
						dialog.dismiss();
						createSplitAmountDialog(which);
						break;
					case 1:
						dialog.dismiss();
						createSplitAmountDialog(which);
						break;
					case 2:
						dialog.dismiss();
						createSplitAmountDialog(which);
						break;
					case 3:
						dialog.dismiss();
						createConfirmClearSplitDialog();
						break;
				}
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void createSplitAmountDialog(final int which) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		switch (which) {
			case 0:
				alert.setTitle((R.string.enter_amount_of_people));
				break;
			case 1:
				alert.setTitle((R.string.enter_amount_of_people));
				break;
			case 2:
				alert.setTitle((R.string.enter_amount));
				break;
		}
		
		final EditText input = new EditText(getActivity());
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setRawInputType(Configuration.KEYBOARD_12KEY);
		alert.setView(input);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				View view = getDialog().getCurrentFocus();
				if (view != null) {
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
				switch (which) {
					case 0: {
						splitMainOrderEvenlyOnThisNumberOfPeople(input.getText().toString());
						dialog.dismiss();
						break;
					}
					case 1: {
						splitMainOrderManuallyOnThisNumberOfPeople(input.getText().toString());
						dialog.dismiss();
						break;
					}
					case 2: {
						createSplitOrderOfThisAmount(input.getText().toString());
						dialog.dismiss();
						break;
					}
				}
				hideKeyboard();
			}
		});
		alert.setNegativeButton((R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				setCartFragmentSplitOrderToNull();
				hideKeyboard();
			}
		});
		alert.show();
	}
	
	private void createConfirmClearSplitDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Confirm split reset");
		builder.setMessage(R.string.confirm_clear_split_order);
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				moveAllOrderLinesBackToMainFromSplit();
				Cart.INSTANCE.setOrdersTo(new ArrayList<Order>());
				Cart.INSTANCE.addOrderToCart(mainOrder);
				MainActivity.getInstance().getCartFragment().refreshCart();
				Cart.INSTANCE.resetSplit();
				dismiss();
				
			}
		}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// do nothing
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void setCartFragmentSplitOrderToNull() {
		Cart.INSTANCE.setSplitOrders(null);
	}
	
	private void hideKeyboard() {
		View view = getDialog().getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
	
	private void clearMainOrder() {
		mainOrder.getLines().clear();
	}
	
	private void moveAllOrderLinesBackToMainFromSplit() {
		try {
			if (!splitOrdersList.isEmpty()) {
				for (Order o : splitOrdersList) {
					for (OrderLine ol : o.getLines()) {
						moveWholeOrderLineToOrder(ol, mainOrder);
					}
				}
			} else if (newOrder.hasLines() && !splitOrdersList.contains(newOrder)) {
				for (OrderLine ol : newOrder.getLines()) {
					moveWholeOrderLineToOrder(ol, mainOrder);
				}
			}
			//Clearing at the end to avoid ConcurrentModificationException
			splitOrdersList.clear();
			newOrder.getLines().clear();
			Cart.INSTANCE.setSplitOrderBeingPaid(false);
			updateAllAdaptersAndFooters();
		} catch (Exception ex) {
		}
	}
	
	private void moveWholeOrderLineToOrder(OrderLine ol, Order mainOrder) {
		addProductToOrderWithQuantity(mainOrder, ol.getProduct(), ol.getQuantity());
	}
	
	private void setCartFragmentSplitOrderList() {
		Cart.INSTANCE.setOrdersTo(splitOrdersList);
		Cart.INSTANCE.setSplitOrders(splitOrdersList);
	}
	
	private void splitMainOrderEvenlyOnThisNumberOfPeople(String s) {
		try {
			moveAllOrderLinesBackToMainFromSplit();
			int number = Integer.parseInt(s);
			
			createThisNumberOfNewOrders(number);
			
			moveAllLinesFromNewToMainOrder();
			
			for (OrderLine ol : mainOrder.getLines()) {
				Decimal lineQty = ol.getQuantity();
				for (Order o : splitOrdersList) {
					if (splitOrdersList.indexOf(o) != splitOrdersList.size() - 1) {
						o.addOrderLine(ol.getProduct());
						o.getLineWithProduct(ol.getProduct()).setQuantity(lineQty.divide(Decimal.make(number)));
						ol.setQuantity(ol.getQuantity().subtract(o.getLineWithProduct(ol.getProduct()).getQuantity()));
					} else {
						o.addOrderLine(ol.getProduct());
						o.getLineWithProduct(ol.getProduct()).setQuantity(ol.getQuantity());
					}
				}
			}
			
			//Need to clear it as we cannot remove the lines in the iterator
			clearMainOrder();
			updateMainAdapter();
			
			setCartFragmentSplitOrderList();
			switchToOrderListView();
			
		} catch (Exception ex) {
			
		}
	}
	
	private void splitMainOrderManuallyOnThisNumberOfPeople(String s) {
		try {
			moveAllOrderLinesBackToMainFromSplit();
			int number = Integer.parseInt(s);
			createThisNumberOfNewOrders(number);
			
			moveAllLinesFromNewToMainOrder();
			
			setCartFragmentSplitOrderList();
			switchToOrderListView();
			
		} catch (Exception ex) {
		}
	}
	
	private void createThisNumberOfNewOrders(int number) {
		splitOrdersList = new ArrayList<>();
		for (int i = 0; i < number; i++) {
			splitOrdersList.add(createNewOrder());
		}
	}
	
	private void createSplitOrderOfThisAmount(String s) {
		try {
			moveAllOrderLinesBackToMainFromSplit();
			Decimal number = Decimal.make(s);
			if (number.isLess(mainOrder.getAmount(true))) {
				
				moveAllLinesFromNewToMainOrder();
				
				Decimal percent = number.divide(mainOrder.getAmount(true));
				
				for (OrderLine ol : mainOrder.getLines()) {
					addProductToOrderWithThisPercentOfQuantity(newOrder, ol.getProduct(), ol.getQuantity(), percent);
					subtractQuantityFromOrderLineWithProduct(mainOrder, ol.getProduct(), newOrder.getLineWithProduct(ol.getProduct()).getQuantity());
					ol.setQuantity(ol.getQuantity().subtract(newOrder.getLineWithProduct(ol.getProduct()).getQuantity()));
				}
				
				updateAllAdaptersAndFooters();
			}
		} catch (Exception ex) {
		}
	}
	
	private boolean ifQuantityIsGreaterOrEqualToOne(OrderLine ol) {
		return (ol.getQuantity().isEqual(Decimal.ZERO) || ol.getQuantity().isGreater(Decimal.ZERO));
	}
	
	private boolean ifQuantityIsLessOrEqualToZero(OrderLine ol) {
		return (ol.getQuantity().isEqual(Decimal.ZERO) || ol.getQuantity().isLess(Decimal.ZERO));
	}
	
	private boolean ifOrderHasLineWithProduct(Order order, Product p) {
		return (order.getLineWithProduct(p) != null);
	}
	
	public CartOrderLinesListDragDropAdapter getMainAdapter() {
		return mainAdapter;
	}
	
	public Order getMainOrder() {
		return mainOrder;
	}
}
