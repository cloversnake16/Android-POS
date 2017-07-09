package no.susoft.mobile.pos.ui.fragment.utils;

import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.db.DBHelper;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.server.DbAPI;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;
import no.susoft.mobile.pos.ui.activity.util.MainTopBarMenu;
import no.susoft.mobile.pos.ui.fragment.CartFragment;

public class Cart {
	
	public final static Cart INSTANCE = new Cart();
	public final static CartPersistingOrders persistingOrders = new CartPersistingOrders();
	public static int selectedOrderIndex;
	public static OrderLine selectedLine;
	private ArrayList<Order> orders = new ArrayList<>();
	private int selectedArea;
	private int selectedTable;
	private CartFragment fragment;
	public boolean takeAwayMode;
	
	public void setFragment(CartFragment fragment) {
		this.fragment = fragment;
	}
	
	// TABLE/AREA ==========================================================================================
	
	private void setTableArea(String userId) {
		
		if (persistingOrders.getTableFor(userId) != null) {
			setSelectedTable(persistingOrders.getTableFor(userId));
			if (persistingOrders.getAreaFor(userId) != null) {
				setSelectedArea(persistingOrders.getAreaFor(userId));
			}
			
		} else {
			resetSelectedAreaTable();
		}
	}
	
	private void resetSelectedAreaTable() {
		setSelectedArea(0);
		setSelectedTable(0);
	}
	
	public void setSelectedAreaTable(int area, int table) {
		setSelectedArea(area);
		setSelectedTable(table);
	}
	
	public void resetAllSelectedFields() {
		try {
			resetSelectedAreaTable();
			resetOrderFields();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void resetOrderFields() {
		setSelectedOrderIndex(0);
		setSelectedLine(null);
	}
	
	public int getSelectedArea() {
		return selectedArea;
	}
	
	public void setSelectedArea(int selectedArea) {
		this.selectedArea = selectedArea;
		if (AccountManager.INSTANCE.getAccount() != null) {
			persistingOrders.addArea(AccountManager.INSTANCE.getAccount().getUserId(), selectedArea);
		}
		if (getOrders().size() > 0) {
			for (Order o : getOrders()) {
				o.setArea(selectedArea);
			}
		}
	}
	
	public int getSelectedTable() {
		return selectedTable;
	}
	
	public void setSelectedTable(int selectedTable) {
		this.selectedTable = selectedTable;
		if (AccountManager.INSTANCE.getAccount() != null) {
			persistingOrders.addTable(AccountManager.INSTANCE.getAccount().getUserId(), selectedTable);
		}
		if (getOrders().size() > 0) {
			for (Order o : getOrders()) {
				o.setTable(selectedTable);
			}
		}
		fragment.updateTableNumber();
	}
	
	// ORDER SPLIT =========================================================================================
	private ArrayList<Order> splitOrders;
	private boolean splitOrderBeingPaid;
	
	public ArrayList<Order> getSplitOrders() {
		return splitOrders;
	}
	
	public void setSplitOrders(ArrayList<Order> splitOrders) {
		this.splitOrders = splitOrders;
	}
	
	public boolean hasSplitOrders() {
		return splitOrders != null && splitOrders.size() > 0;
	}
	
	public void setSplitOrderBeingPaid(boolean splitBillPaid) {
		this.splitOrderBeingPaid = splitBillPaid;
	}
	
	public boolean getSplitOrderBeingPaid() {
		return splitOrderBeingPaid;
	}
	
	public void resetSplit() {
		setSplitOrderBeingPaid(false);
		setSplitOrders(null);
		if (hasActiveOrder())
			fragment.selectLastRow();
	}
	
	// ==========================================
	// CART
	public void setOrders(ArrayList<Order> orders) {
		this.orders = orders;
	}
	
	public ArrayList<Order> getOrders() {
		return this.orders;
	}
	
	public Order getOrder() {
		int ordersSize = getOrders().size();
		if (ordersSize > 0) {
			if (selectedOrderIndex >= ordersSize) {
				return getOrders().get(ordersSize - 1);
			} else {
				return getOrders().get(selectedOrderIndex);
			}
		}
		return null;
	}
	
	public boolean hasActiveOrder() {
		return getOrder() != null;
	}
	
	public void addOrderToCart(Order order) {
		order.setArea(getSelectedArea());
		order.setTable(getSelectedTable());
		getOrders().add(order);
		Cart.selectedOrderIndex = getOrders().size() - 1;
		fragment.updateOrderSpinner();
	}
	
	public void setOrdersTo(ArrayList<Order> orders) {
		if (orders == null) {
			orders = new ArrayList<>();
		} else if (orders.size() == 0) {
			resetAllSelectedFields();
		} else {
			for (Order order : orders) {
				order.setArea(getSelectedArea());
				order.setTable(getSelectedTable());
			}
			Cart.selectedOrderIndex = orders.size() - 1;
		}
		setOrders(orders);
		Cart.persistingOrders.addOrders(AccountManager.INSTANCE.getAccount().getUserId(), orders);
		fragment.updateOrderSpinner();
	}
	
	public void setOrder(Order order) {
		if (order != null) {
			setSelectedArea(getSelectedArea());
			setSelectedTable(getSelectedTable());
			
			if (orders == null) {
				orders = new ArrayList<>();
			}
			
			if (selectedOrderIndex >= getOrders().size()) {
				orders.add(order);
			} else {
				orders.set(selectedOrderIndex, order);
			}
			
			fragment._orderSet(order);
			
		} else {
			deleteOrder();
			setSelectedLine(null);
		}
	}
	
	public void createNewEmptyOrderInCart() {
		Order order = createNewOrderAndReturnItHere();
		if (AppConfig.getState().getPos() != null) {
			order.setPosNo(AppConfig.getState().getPos().getId());
		}
		
		addOrderToCart(order);
		setSelectedLine(null);
		fragment._orderCustomerUpdated(order.getCustomer());
		fragment.refreshCart();
	}
	
	public Order createNewOrderAndReturnItHere() {
		Order newOrder = new Order();
		newOrder.setShopId(AccountManager.INSTANCE.getAccount().getShop().getID());
		newOrder.setPosNo(AccountManager.INSTANCE.getSavedPos() != null ? AccountManager.INSTANCE.getSavedPos().getId() : "");
		newOrder.setSalesPersonId(AccountManager.INSTANCE.getAccount().getUserId());
		newOrder.setSalesPersonName(AccountManager.INSTANCE.getAccount().getName());
		newOrder.setAlternativeId(newOrder.getShopId() + DBHelper.DB_TIMESTAMP_FORMAT.format(new Date()));
		newOrder.setUseAlternative(Cart.INSTANCE.isTakeAwayMode());
		return newOrder;
	}
	
	public void deleteOrder() {
		deleteFromParkedOrders();
		deleteFromPersistingOrders();
		deleteSelectedOrder();
		adjustSelectedOrderIndex();
		setSelectedLine(null);
		if (getOrders().size() == 0) {
			resetSelectedAreaTable();
		}
		
		fragment._deleteOrder();
	}
	
	public void closeOrder() {
		if (getOrder() != null && getOrder().getId() > 0) {
			try {
				if (AppConfig.getState().isRestaurant() && getOrder().getKitchenOrderId() > 0) {
					DbAPI.deleteKitchenOrder(getOrder().getShopId(), getOrder().getKitchenOrderId());
				} else if (AppConfig.getState().isWorkshop()) {
					if (MainActivity.getInstance().isConnected()) {
						MainActivity.getInstance().getServerCallMethods().deleteParkedOrderAsync(getOrder());
					}
				}
			} catch (Exception e) {
				ErrorReporter.INSTANCE.filelog(e);
			}
		}
		deleteFromPersistingOrders();
		deleteSelectedOrder();
		adjustSelectedOrderIndex();
		setSelectedLine(null);
		if (getOrders().size() == 0) {
			resetSelectedAreaTable();
		}
		
		fragment._deleteOrder();
	}
	
	private void deleteFromParkedOrders() {
		if (getOrder() != null && getOrder().getId() > 0) {
			try {
				if (AppConfig.getState().isRestaurant() && getOrder().getKitchenOrderId() > 0) {
					DbAPI.deleteKitchenOrder(getOrder().getShopId(), getOrder().getKitchenOrderId());
				} else if (AppConfig.getState().isWorkshop()) {
					if (MainActivity.getInstance().isConnected()) {
						MainActivity.getInstance().getServerCallMethods().deleteParkedOrderAsync(getOrder());
					}
				} else {
					DbAPI.deleteOrder(getOrder().getShopId(), getOrder().getId());
				}
			} catch (Exception e) {
				ErrorReporter.INSTANCE.filelog(e);
			}
		}
	}
	
	public void setCartOrdersToNewAccount() {
		
		String userId = AccountManager.INSTANCE.getAccount().getUserId();
		ArrayList<Order> orders = persistingOrders.getOrderAt(userId);
		
		if (orders != null) {
			setOrdersTo(orders);
			setTableArea(userId);
			
		} else {
			setOrdersTo(new ArrayList<Order>());
			resetSelectedAreaTable();
		}
		MainActivity.getInstance().getCartFragment().refreshCart();
	}
	
	public boolean hasNoneOrOneOrder() {
		return orders == null || orders.size() <= 1;
	}
	
	private void deleteFromPersistingOrders() {
		if (getOrders().size() > 0) {
			persistingOrders.removeOrderFromMaps(AccountManager.INSTANCE.getAccount().getUserId());
		}
	}
	
	private void deleteSelectedOrder() {
		if (getOrders().size() > selectedOrderIndex) {
			getOrders().remove(selectedOrderIndex);
		}
	}
	
	public void resetCart() {
		
		if (AppConfig.getState().isRestaurant()) {
			resetAllSelectedFields();
			fragment.resetTakeAway();
		} else {
			resetOrderFields();
		}
		
		setOrdersTo(new ArrayList<Order>());
		createNewEmptyOrderInCart();
	}
	
	public void setSelectedLine(OrderLine selectedLine) {
		Cart.selectedLine = selectedLine;
		fragment.getCartButtons().refreshCartButtonStates();
	}
	
	public boolean hasSelectedLine() {
		return selectedLine != null;
	}
	
	public boolean hasOrdersWithLines() {
		if (orders == null || orders.isEmpty()) {
			return false;
		}
		for (Order o : orders) {
			if (o.hasLines()) {
				return true;
			}
		}
		return false;
	}
	
	public void setSelectedOrderIndex(int selectedOrderIndex) {
		Cart.selectedOrderIndex = selectedOrderIndex;
	}
	
	private void adjustSelectedOrderIndex() {
		if (selectedOrderIndex > 0) {
			selectedOrderIndex--;
		}
	}
	
	// ==========================================
	// ORDER LINES
	
	public OrderLine addOrderLine(Product product) {
		OrderLine line = null;
		try {
			if (getOrder() == null) {
				createNewEmptyOrderInCart();
			}
			if (product != null) {
				if (product.isMiscellaneous() || (getOrder().hasReturnLineWithProduct(product) && !getOrder().hasPositiveLineWithProduct(product))) {
					line = addOrderLineFromDialog(product, Decimal.ONE);
				} else if (product.isWeighted() && !product.isBarcodeEANWithWeights()) {
					line = getOrder().addOrderLine(product);
					fragment.afterLineIsAdded(getOrder().getLines().indexOf(line));
					if (product.getWeight() == null || product.getWeight().isZero()) {
						if (MainActivity.getInstance().getDmScale() != null) {
							MainActivity.getInstance().showProgressDialog("Weighing..", false);
							try {
								MainActivity.getInstance().getDmScale().readWeight(2, 5000);
							} catch (Exception e) {
								ErrorReporter.INSTANCE.filelog(e);
							}
						}
					}
				} else if (getOrder().hasReturnLineWithProduct(product) && getOrder().hasPositiveLineWithProduct(product)) {
					OrderLine oldLine = getOrder().getPositiveLineWithProduct(product);
					oldLine.setQuantity(oldLine.getQuantity().add(Decimal.ONE));
					fragment.afterLineIsAdded(getOrder().getLines().indexOf(oldLine));
				} else {
					line = getOrder().addOrderLine(product);
					fragment.afterLineIsAdded(getOrder().getLines().indexOf(line));
				}
				
			}
			
			fragment.refreshFragmentView();
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}
		return line;
	}
	
	public OrderLine addOrderLineFromDialog(Product product, Decimal qty) {
		OrderLine line = null;
		if (getOrder() == null) {
			createNewEmptyOrderInCart();
		}
		if (!qty.equals(Decimal.ZERO)) {
			line = getOrder().addMiscOrderLine(product, qty);
			fragment.afterLineIsAdded(getOrder().getLines().indexOf(line));
		}
		
		fragment.refreshFragmentView();
		return line;
	}
	
	public void addOrderLineGiftCards(Product product, int qty) {
		if (getOrder() == null) {
			createNewEmptyOrderInCart();
		}
		if (qty > 0) {
			for (int i = 0; i < qty; i++) {
				OrderLine line = getOrder().addMiscOrderLine(product, Decimal.ONE);
				fragment.afterLineIsAdded(getOrder().getLines().indexOf(line));
			}
			
		}
		fragment.refreshFragmentView();
		//Do nothing if the quantity is zero (don't add product)
	}
	
	public void removeSelectedOrderLine() {
		if (hasOrdersWithLines()) {
			int lineNumber = getOrder().getLines().indexOf(selectedLine);
			getOrder().removeOrderLine(selectedLine);
			fragment._orderLineRemoved(lineNumber);
		}
		try {
			if (getOrder().getLines().size() == 0) {
				MainActivity.getInstance().getNumpadPayFragment().hideDoneButton();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	boolean hasZeroPriceLines() {
		for (OrderLine ol : getOrder().getLines()) {
			if (ol.getProduct().isMiscellaneous() && ol.getPrice().isEqual(Decimal.ZERO)) {
				return true;
			}
		}
		return false;
	}
	
	public Decimal getProductPrice(Product product) {
		if (Cart.INSTANCE.isTakeAwayMode() && product.isUseAlternative()) {
			return product.getAlternativePrice();
		}
		return product.getPrice();
	}
	
	public double getProductVat(Product product) {
		if (Cart.INSTANCE.isTakeAwayMode() && product.isUseAlternative()) {
			return product.getAlternativeVat();
		}
		return product.getVat();
	}
	
	// ==========================================
	// ORDER LINE DETAILS
	
	public void addDiscount(Discount discount) {
		selectedLine.setDiscount(discount);
		if (selectedLine.getComponents() != null) {
			for (OrderLine line : selectedLine.getComponents()) {
				line.setDiscount(discount);
			}
		}
		fragment.refreshCart();
	}
	
	public void setOrderCustomer(Customer customer) {
		if (getOrder() == null) {
			createNewEmptyOrderInCart();
		}
		getOrder().setCustomer(customer);
		fragment._orderCustomerUpdated(customer);
		MainActivity.getInstance().getServerCallMethods().reloadCartProducts();
	}
	
	// ==========================================
	// MISC
	
	private void garbageCollect() {
		System.gc();
	}
	
	public void doParkClick() {
		MainActivity.getInstance().getServerCallMethods().parkOrders();
	}
	
	public void doAddOrderClick() {
		createNewEmptyOrderInCart();
		MainTopBarMenu.getInstance().toggleLastUsedView();
	}
	
	public void doLoadCartClick() {
		MainTopBarMenu.getInstance().toggleOrdersView();
		MainActivity.getInstance().getServerCallMethods().loadOrdersFromServer();
	}
	
	public void doDeleteOrderClick() {
		if (hasActiveOrder()) {
			new Builder(MainActivity.getInstance()).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.delete_cart).setMessage(R.string.ask_delete_cart).setPositiveButton(R.string.yes, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (AppConfig.getState().isRestaurant()) {
						MainActivity.getInstance().getServerCallMethods().cancelOrderOnKitchen();
					} else {
						deleteOrder();
					}
					if (MainActivity.getInstance().getNumpadPayFragment() != null) {
						MainTopBarMenu.getInstance().toggleLastUsedView();
						MainActivity.getInstance().getCartFragment().getCartButtons().resetParkSuccessful();
					}
				}
				
			}).setNegativeButton(R.string.no, null).show();
		}
	}
	
	public void doDeleteLineClick() {
		if (AppConfig.getState().isRestaurant()) {
			MainActivity.getInstance().getServerCallMethods().cancelOrderLineOnKitchen();
		} else {
			removeSelectedOrderLine();
		}
	}
	
	public void doSplitOrderClick() {
		fragment.showSplitOrderDialog();
	}
	
	public int getSelectedLineIndex() {
		return getOrder().getLines().indexOf(selectedLine);
	}
	
	public boolean isTakeAwayMode() {
		return takeAwayMode;
	}
	
	public void setTakeAwayMode(boolean takeAwayMode) {
		this.takeAwayMode = takeAwayMode;
	}
	
	public void switchTakeAwayMode() {
		this.takeAwayMode = !takeAwayMode;
		if (Cart.INSTANCE.getOrder() != null) {
			Cart.INSTANCE.getOrder().setUseAlternative(takeAwayMode);
			fragment.refreshCart();
		}
		fragment.refreshFragmentView();
	}
	
}
