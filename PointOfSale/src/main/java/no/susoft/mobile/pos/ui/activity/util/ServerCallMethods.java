package no.susoft.mobile.pos.ui.activity.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.Utilities;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.db.DBHelper;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.ConnectionManager;
import no.susoft.mobile.pos.hardware.printer.BixolonPrinterJob;
import no.susoft.mobile.pos.hardware.printer.IPPrintKitchen;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.Message;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.response.APIStatus;
import no.susoft.mobile.pos.response.StatusResponse;
import no.susoft.mobile.pos.server.*;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.utils.TableItemOnDragListener;
import no.susoft.mobile.pos.ui.dialog.*;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class ServerCallMethods {
	
	MainActivity main;
	ProgressDialog dialog;
	private boolean loadOrder;
	private int area;
	private int table;
	
	public ServerCallMethods(MainActivity main) {
		this.main = main;
	}
	
	public void searchCustomer(String searchTerm) {
		if (MainActivity.getInstance().isConnected()) {
			new CustomerSearchAsync().execute(searchTerm);
		} else {
			new CustomerSearchOfflineAsync().execute(searchTerm);
		}
	}
	
	public void customerSearchAsyncPostExecute() {
		main.getMainShell().updateCustomerSearchAdapter();
	}
	
	public void loadCustomerNotes(CustomerNotesDialog caller) {
		if (MainActivity.getInstance().isConnected() && Cart.INSTANCE.getOrder() != null && Cart.INSTANCE.getOrder().getCustomer() != null) {
			CustomerNotesLoadAsync task = new CustomerNotesLoadAsync();
			task.setCaller(caller);
			task.execute(Cart.INSTANCE.getOrder().getCustomer().getId());
		}
	}
	
	public void searchProduct(String searchTerm) {
		if (MainActivity.getInstance().isConnected()) {
			new ProductSearchAsync().execute(searchTerm);
		} else {
			new ProductSearchOfflineAsync().execute(searchTerm);
		}
	}
	
	public void productSearchAsyncPostExecute() {
		main.getMainShell().updateProductSearchAdapter();
	}
	
	public void loadProductByID(String id) {
		try {
			//Fetch from cart instead of server if it already has the product
			if (Cart.INSTANCE.getOrder() != null && Cart.INSTANCE.getOrder().getLines() != null && Cart.INSTANCE.getOrder().getLines().size() > 0 && Cart.INSTANCE.getOrder().getLineWithProductId(id) != null && !Cart.INSTANCE.getOrder().getLineWithProductId(id).getProduct().isMiscellaneous() && !Cart.INSTANCE.getOrder().getLineWithProductId(id).getProduct().isBundle() && !Cart.INSTANCE.getOrder().getLineWithProductId(id).getProduct().isWeighted()) {
				
				Cart.INSTANCE.addOrderLine(Cart.INSTANCE.getOrder().getLineWithProductId(id).getProduct());
				MainActivity.getInstance().getCartFragment().refreshCart();
				focusOnInputField();
			} else {
				if (MainActivity.getInstance().isConnected()) {
					new ProductLoadByIDAsync().execute(id);
				} else {
					new ProductLoadByIDOfflineAsync().execute(id);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			if (MainActivity.getInstance().isConnected()) {
				new ProductLoadByIDAsync().execute(id);
			} else {
				new ProductLoadByIDOfflineAsync().execute(id);
			}
		}
	}
	
	public void loadCartRoundingProduct(String id, Decimal price, String location) {
		if (MainActivity.getInstance().isConnected()) {
			new ProductLoadByIDAsync().execute(id, String.valueOf(price), location);
		} else {
			new ProductLoadByIDOfflineAsync().execute(id, String.valueOf(price), location);
		}
	}
	
	public void loadProductByID(String id, Decimal price) {
		if (MainActivity.getInstance().isConnected()) {
			new ProductLoadByIDAsync().execute(id, String.valueOf(price));
		} else {
			new ProductLoadByIDOfflineAsync().execute(id, String.valueOf(price));
		}
	}
	
	public void productLoadByIDAsyncPostExecute(Product p) {
		if (p != null) {
			OrderLine ol = Cart.INSTANCE.addOrderLine(p);
			MainActivity.getInstance().getCartFragment().refreshCart();
			
			if (p.isMiscellaneous()) {
				Utilities.showSoftKeyboard(MainActivity.getInstance());
				OrderLineEditMiscDialog dialog = new OrderLineEditMiscDialog();
				dialog.setOrderLine(ol);
				dialog.show(MainActivity.getInstance().getFragmentManager(), "misc");
			} else if (p.isBundle() && p.getComponents() != null) {
				OrderLineEditBundleDialog dialog = new OrderLineEditBundleDialog();
				dialog.setOrderLine(ol);
				dialog.show(MainActivity.getInstance().getFragmentManager(), "bundle");
			}
			
		} else {
			Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.product_not_found), Toast.LENGTH_LONG).show();
		}
		focusOnInputField();
	}
	
	public void productLoadByIDAsyncPostExecute(Product p, String priceString) {
		if (p != null) {
			Decimal price = Decimal.make(priceString);
			p.setPrice(price);
			OrderLine ol = Cart.INSTANCE.addOrderLine(p);
			Cart.INSTANCE.getOrder().getLineWithProduct(p).setPrice(price);
			MainActivity.getInstance().getCartFragment().refreshCart();
			
			if (p.isMiscellaneous()) {
				Utilities.showSoftKeyboard(MainActivity.getInstance());
				OrderLineEditMiscDialog dialog = new OrderLineEditMiscDialog();
				dialog.setOrderLine(ol);
				dialog.show(MainActivity.getInstance().getFragmentManager(), "misc");
			} else if (p.isBundle() && p.getComponents() != null) {
				OrderLineEditBundleDialog dialog = new OrderLineEditBundleDialog();
				dialog.setOrderLine(ol);
				dialog.show(MainActivity.getInstance().getFragmentManager(), "bundle");
			}
			
		} else {
			Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.product_not_found), Toast.LENGTH_LONG).show();
		}
		focusOnInputField();
	}
	
	public void loadProductByBarcode(String barcode) {
		if (MainActivity.getInstance().isConnected()) {
			new ProductLoadByBarcodeAsync().execute(barcode);
		} else {
			new ProductLoadByBarcodeOfflineAsync().execute(barcode);
		}
	}
	
	public void productLoadByBarcodeAsyncPostExecute(Product p) {
		if (p != null) {
			OrderLine ol = Cart.INSTANCE.addOrderLine(p);
			MainActivity.getInstance().getCartFragment().refreshCart();
			
			if (p.isMiscellaneous()) {
				Utilities.showSoftKeyboard(MainActivity.getInstance());
				OrderLineEditMiscDialog dialog = new OrderLineEditMiscDialog();
				dialog.setOrderLine(ol);
				dialog.show(MainActivity.getInstance().getFragmentManager(), "misc");
			} else if (p.isBundle() && p.getComponents() != null) {
				OrderLineEditBundleDialog dialog = new OrderLineEditBundleDialog();
				dialog.setOrderLine(ol);
				dialog.show(MainActivity.getInstance().getFragmentManager(), "bundle");
			}
			
		} else {
			Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.product_not_found), Toast.LENGTH_LONG).show();
		}
		
		focusOnInputField();
	}
	
	public void reloadCartProducts() {
		if (MainActivity.getInstance().isConnected() && Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().hasLines()) {
			new ReloadProductsAsync().execute();
		}
	}
	
	public void loadCompleteOrderByReceipt(String receipt) {
		new LoadCompleteOrderByReceiptAsync().execute(receipt);
	}
	
	public void orderLoadByReceiptAsyncPostExecute(Order o) {
		if (o != null) {
			for (OrderLine line : o.getLines()) {
				if (Cart.INSTANCE.getOrder() == null) {
					Cart.INSTANCE.createNewEmptyOrderInCart();
				}
				line.setShopId(o.getShopId());
				line.setQuantity(line.getQuantity().multiply(Decimal.NEGATIVE_ONE));
				Cart.INSTANCE.getOrder().addOrderLine(line);
			}
			main.getCartFragment().refreshCart();
			
		} else if (!MainActivity.getInstance().isConnected()) {
			Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.order_not_found) + " " + MainActivity.getInstance().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.order_not_found), Toast.LENGTH_LONG).show();
		}
		focusOnInputField();
	}
	
	public void loadOrderByID(String id) {
		new LoadOrderByIDAsync().execute(id);
	}
	
	public void loadCompleteOrderByAlternativeID(String id) {
		new LoadCompleteOrderByAlternativeIDAsync().execute(id);
	}
	
	public void orderLoadByIDAsyncPostExecute(Order o) {
		if (o != null) {
			Cart.INSTANCE.setOrder(o);
			main.getCartFragment().refreshCart();
		} else if (!MainActivity.getInstance().isConnected()) {
			Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.order_not_found) + " " + MainActivity.getInstance().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.order_not_found), Toast.LENGTH_LONG).show();
		}
		focusOnInputField();
	}
	
	private void focusOnInputField() {
		MainActivity.getInstance().focusOnActiveFragmentInputField();
	}
	
	public void loadDiscountReasons() {
		new DiscountReasonLoadAsync().execute("");
	}
	
	public void parkOrders() {
		if (Cart.INSTANCE.hasOrdersWithLines()) {
			final ArrayList<Order> orders = Cart.INSTANCE.getOrders();
			for (Order order : orders) {
				if (order.getDate() == null) {
					order.setDate(new Date());
				}
			}
			if (AppConfig.getState().isWorkshop()) {
				if (MainActivity.getInstance().isConnected()) {
					android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.getInstance());
					builder.setMessage("Print receipt?").setPositiveButton("Yes", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.getInstance().showProgressDialog("Parking..", false);
							try {
								for (Order order : orders) {
									if (order.getParkedId() == 0) {
										StatusResponse r = new GenerateOrderingNumberAsync().execute(order).get();
										if (r != null && r.getStatusCode() == APIStatus.OK.getCode() && r.getResult() != null) {
											order.setParkedId(Long.parseLong((String) r.getResult()));
										}
									}
									ParkOrdersAsync t = new ParkOrdersAsync();
									t.setPrintReceipt(true);
									t.execute(Cart.INSTANCE.getOrders());
								}
							} catch (Exception e) {
								MainActivity.getInstance().hideProgressDialog();
								Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.error_parking_orders), Toast.LENGTH_LONG).show();
							}
						}
					}).setNegativeButton("No", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.getInstance().showProgressDialog("Parking..", false);
							try {
								for (Order order : orders) {
									if (order.getParkedId() == 0) {
										StatusResponse r = new GenerateOrderingNumberAsync().execute(order).get();
										if (r != null && r.getStatusCode() == APIStatus.OK.getCode() && r.getResult() != null) {
											order.setParkedId(Long.parseLong((String) r.getResult()));
										}
									}
									new ParkOrdersAsync().execute(Cart.INSTANCE.getOrders());
								}
							} catch (Exception e) {
								MainActivity.getInstance().hideProgressDialog();
								Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.error_parking_orders), Toast.LENGTH_LONG).show();
							}
						}
					}).show();
				} else {
					MainActivity.getInstance().hideProgressDialog();
					Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.error_offline), Toast.LENGTH_LONG).show();
				}
			} else if (AppConfig.getState().isRestaurant()) {
				try {
					boolean kitchenPrint = false;
					boolean barPrint = false;
					for (Order order : orders) {
						for (OrderLine line : order.getLines()) {
							if (line.getProduct().getAbcCode().equals("K") && !line.getDeliveredQty().isEqual(line.getQuantity())) {
								kitchenPrint = true;
							}
							if (line.getProduct().getAbcCode().equals("B") && !line.getDeliveredQty().isEqual(line.getQuantity())) {
								barPrint = true;
							}
						}
					}
					
					if (kitchenPrint && !AppConfig.getState().getKitchenPrinterIp().isEmpty()) {
						for (Order order : orders) {
							final Order orderForPrint = order;
							ConnectionManager.getInstance().execute(new BixolonPrinterJob(BixolonPrinterJob.KITCHEN_PRINTER) {
								@Override
								public int print() {
									IPPrintKitchen pk = new IPPrintKitchen();
									return pk.print(orderForPrint, IPPrintKitchen.DESTINATION_KITCHEN);
								}
							});
						}
					}
					
					if (barPrint && !AppConfig.getState().getBarPrinterIp().isEmpty()) {
						for (Order order : orders) {
							final Order orderForPrint = order;
							ConnectionManager.getInstance().execute(new BixolonPrinterJob(BixolonPrinterJob.BAR_PRINTER) {
								@Override
								public int print() {
									IPPrintKitchen pk = new IPPrintKitchen();
									return pk.print(orderForPrint, IPPrintKitchen.DESTINATION_BAR);
								}
							});
						}
					}
					
					ConnectionManager.getInstance().execute(new Runnable() {
						@Override
						public void run() {
							ArrayList<Order> orders = Cart.INSTANCE.getOrders();
							for (Order order : orders) {
								for (OrderLine line : order.getLines()) {
									line.setDeliveredQty(line.getQuantity());
								}
							}
							
							try {
								DbAPI.saveKitchenOrders(orders);
								MainActivity.getInstance().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										MainActivity.getInstance().getServerCallMethods().sendOrdersToServerPostExecute(Message.OK, Parameters.KITCHEN);
									}
								});
							} catch (Exception e) {
								MainActivity.getInstance().hideProgressDialog();
								Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.error_parking_orders), Toast.LENGTH_LONG).show();
							}
							
						}
					});
					
					if ((kitchenPrint && AppConfig.getState().getKitchenPrinterIp().isEmpty()) || barPrint && AppConfig.getState().getBarPrinterIp().isEmpty()) {
						MainActivity.getInstance().showPrinterNotConnectedToast();
					}
					
				} catch (Exception e) {
					MainActivity.getInstance().hideProgressDialog();
					Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.error_parking_orders), Toast.LENGTH_LONG).show();
				}
			} else {
				for (Order order : orders) {
					try {
						if (order.getId() > 0) {
							DbAPI.updateOrder(order);
						} else {
							long id = DbAPI.saveOrder(order);
							order.setId(id);
						}
						MainActivity.getInstance().getServerCallMethods().sendOrdersToServerPostExecute(Message.OK, Parameters.TERM);
					} catch (Exception e) {
						Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.error_parking_orders), Toast.LENGTH_LONG).show();
					}
				}
			}
		}
	}
	
	public void cancelOrderOnKitchen() {
		ArrayList<Order> orders = Cart.INSTANCE.getOrders();
		if (orders != null && !orders.isEmpty()) {
			try {
				boolean kitchenPrint = false;
				boolean barPrint = false;
				ArrayList<OrderLine> kitchenLinesToDelete = new ArrayList<>();
				ArrayList<OrderLine> barLinesToDelete = new ArrayList<>();
				
				for (Order order : orders) {
					for (OrderLine line : order.getLines()) {
						if (line.getProduct().getAbcCode().equals("K") && line.getDeliveredQty().isPositive()) {
							kitchenPrint = true;
							line.setQuantity(line.getDeliveredQty().multiply(Decimal.NEGATIVE_ONE));
							line.setDeliveredQty(Decimal.ZERO);
							kitchenLinesToDelete.add(line);
						}
						if (line.getProduct().getAbcCode().equals("B") && line.getDeliveredQty().isPositive()) {
							barPrint = true;
							line.setQuantity(line.getDeliveredQty().multiply(Decimal.NEGATIVE_ONE));
							line.setDeliveredQty(Decimal.ZERO);
							barLinesToDelete.add(line);
						}
					}
				}
				
				if (kitchenPrint && !AppConfig.getState().getKitchenPrinterIp().isEmpty()) {
					for (Order order : orders) {
						if (order.getId() > 0) {
							final Order orderForPrint = order;
							orderForPrint.setLines(kitchenLinesToDelete);
							ConnectionManager.getInstance().execute(new BixolonPrinterJob(BixolonPrinterJob.KITCHEN_PRINTER) {
								@Override
								public int print() {
									IPPrintKitchen pk = new IPPrintKitchen();
									return pk.print(orderForPrint, IPPrintKitchen.DESTINATION_KITCHEN);
								}
							});
						}
					}
				}
				
				if (barPrint && !AppConfig.getState().getBarPrinterIp().isEmpty()) {
					for (Order order : orders) {
						if (order.getId() > 0) {
							final Order orderForPrint = order;
							orderForPrint.setLines(barLinesToDelete);
							ConnectionManager.getInstance().execute(new BixolonPrinterJob(BixolonPrinterJob.BAR_PRINTER) {
								@Override
								public int print() {
									IPPrintKitchen pk = new IPPrintKitchen();
									return pk.print(orderForPrint, IPPrintKitchen.DESTINATION_BAR);
								}
							});
						}
					}
				}
				
				if ((kitchenPrint && AppConfig.getState().getKitchenPrinterIp().isEmpty()) || barPrint && AppConfig.getState().getBarPrinterIp().isEmpty()) {
					Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getResources().getString(R.string.no_printer_connected), Toast.LENGTH_LONG).show();
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
				Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getResources().getString(R.string.no_printer_connected), Toast.LENGTH_LONG).show();
			}
			
			ConnectionManager.getInstance().execute(new Runnable() {
				@Override
				public void run() {
					MainActivity.getInstance().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Cart.INSTANCE.deleteOrder();
							Cart.INSTANCE.resetCart();
						}
					});
				}
			});
		}
	}
	
	public void cancelOrderLineOnKitchen() {
		Order order = Cart.INSTANCE.getOrder();
		if (order != null && !order.getLines().isEmpty() && order.getId() > 0) {
			OrderLine line = order.getLines().get(Cart.INSTANCE.getSelectedLineIndex());
			printCancelLineOnKitchen(line);
			ConnectionManager.getInstance().execute(new Runnable() {
				@Override
				public void run() {
					MainActivity.getInstance().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Cart.INSTANCE.removeSelectedOrderLine();
						}
					});
				}
			});
			
		} else {
			Cart.INSTANCE.removeSelectedOrderLine();
		}
	}
	
	public void printCancelLineOnKitchen(OrderLine line) {
		try {
			Order order = Cart.INSTANCE.getOrder();
			boolean kitchenPrint = false;
			boolean barPrint = false;
			
			if (line.getProduct().getAbcCode().equals("K") && line.getDeliveredQty().isPositive()) {
				kitchenPrint = true;
				line.setQuantity(line.getDeliveredQty().multiply(Decimal.NEGATIVE_ONE));
				line.setDeliveredQty(Decimal.ZERO);
			}
			if (line.getProduct().getAbcCode().equals("B") && line.getDeliveredQty().isPositive()) {
				barPrint = true;
				line.setQuantity(line.getDeliveredQty().multiply(Decimal.NEGATIVE_ONE));
				line.setDeliveredQty(Decimal.ZERO);
			}
			
			if (kitchenPrint && !AppConfig.getState().getKitchenPrinterIp().isEmpty()) {
				final Order orderForPrint = (Order) Utilities.copy(order);
				if (orderForPrint != null) {
					orderForPrint.setLines(new ArrayList<OrderLine>());
					orderForPrint.addOrderLine(line);
					ConnectionManager.getInstance().execute(new BixolonPrinterJob(BixolonPrinterJob.KITCHEN_PRINTER) {
						@Override
						public int print() {
							IPPrintKitchen pk = new IPPrintKitchen();
							return pk.print(orderForPrint, IPPrintKitchen.DESTINATION_KITCHEN);
						}
					});
				}
			}
			
			if (barPrint && !AppConfig.getState().getBarPrinterIp().isEmpty()) {
				final Order orderForPrint = (Order) Utilities.copy(order);
				if (orderForPrint != null) {
					orderForPrint.setLines(new ArrayList<OrderLine>());
					orderForPrint.addOrderLine(line);
					ConnectionManager.getInstance().execute(new BixolonPrinterJob(BixolonPrinterJob.BAR_PRINTER) {
						@Override
						public int print() {
							IPPrintKitchen pk = new IPPrintKitchen();
							return pk.print(orderForPrint, IPPrintKitchen.DESTINATION_BAR);
						}
					});
				}
			}
			
			if ((kitchenPrint && AppConfig.getState().getKitchenPrinterIp().isEmpty()) || barPrint && AppConfig.getState().getBarPrinterIp().isEmpty()) {
				Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getResources().getString(R.string.no_printer_connected), Toast.LENGTH_LONG).show();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getResources().getString(R.string.no_printer_connected), Toast.LENGTH_LONG).show();
		}
	}
	
	public void sendOrdersToServerPostExecute(Protocol.Message result, Protocol.Parameters type) {
		if (result == Protocol.Message.OK) {
			if (type.equals(Protocol.Parameters.TERM)) {
				MainActivity.getInstance().getCartFragment().getCartButtons().setParkSuccessfulMessage();
			} else if (type.equals(Protocol.Parameters.COMPLETE)) {
				Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.sent_to_pos), Toast.LENGTH_SHORT).show();
			}
			Cart.INSTANCE.resetCart();
			MainTopBarMenu.getInstance().toggleLastUsedView();
			MainActivity.getInstance().getCartFragment().getCartButtons().resetParkSuccessful();
			MainActivity.getInstance().getCartFragment().setCartTitleToDefault();
			
			if (loadOrder) {
				loadOrdersByTable(area, table);
				loadOrder = false;
				area = 0;
				table = 0;
			} else {
				MainActivity.getInstance().hideProgressDialog();
			}
			
		} else {
			MainActivity.getInstance().hideProgressDialog();
			Toast.makeText(SusoftPOSApplication.getContext(), MainActivity.getInstance().getString(R.string.error_parking_orders), Toast.LENGTH_LONG).show();
		}
	}
	
	public void loadOrdersFromServer() {
		dialog = ProgressDialog.show(MainActivity.getInstance(), "", MainActivity.getInstance().getString(R.string.loading_orders), true);
		if (AppConfig.getState().isRestaurant()) {
			ArrayList<OrderPointer> kitchenOrders = DbAPI.getKitchenOrders();
			MainActivity.getInstance().getServerCallMethods().getLoadOrdersAsyncPostExecute(kitchenOrders);
		} else if (AppConfig.getState().isWorkshop()) {
			if (MainActivity.getInstance().isConnected()) {
				new LoadParkedOrdersAsync().execute(AccountManager.INSTANCE.getAccount().getShop().getID());
			}
		} else {
			ArrayList<OrderPointer> parkedOrders = DbAPI.getParkedOrders(AccountManager.INSTANCE.getAccount().getShop().getID());
			MainActivity.getInstance().getServerCallMethods().getLoadOrdersAsyncPostExecute(parkedOrders);
		}
	}
	
	public void searchCompleteOrdersFromServer(OrderSearchDialog orderSearchDialog, String orderNo, String from, String to, String amountFrom, String amountTo, String customerId) {
		SearchCompleteOrdersAsync search = new SearchCompleteOrdersAsync();
		search.setReturnClass(orderSearchDialog);
		search.execute(orderNo, from, to, amountFrom, amountTo, customerId);
	}
	
	public void loadSupplyReportFromServer(SupplyReportDialog supplyReportDialog, Date from, Date to) {
		SupplyReportAsync search = new SupplyReportAsync();
		search.setReturnClass(supplyReportDialog);
		search.execute(DBHelper.DB_TIMESTAMP_FORMAT.format(from), DBHelper.DB_TIMESTAMP_FORMAT.format(to));
	}
	
	public void getLoadOrdersAsyncPostExecute(ArrayList<OrderPointer> orders) {
		if (dialog != null) {
			dialog.dismiss();
		}
		if (orders == null) {
			MainTopBarMenu.getInstance().toggleScanView();
		} else {
			MainActivity.getInstance().setOrderResults(orders);
			MainActivity.getInstance().getMainShell().updateOrderSearchAdapter();
		}
	}
	
	public void loadOrdersByTable(int area, int table) {
		new LoadKitchenOrdersByTableAsync().execute(String.valueOf(area), String.valueOf(table));
	}
	
	public void queueLoadOrderByTableAfterPark(int area, int table) {
		doLoadOrderByTable(area, table, true);
	}
	
	private void doLoadOrderByTable(int area, int table, boolean state) {
		if (state) {
			this.loadOrder = true;
			this.area = area;
			this.table = table;
		}
	}
	
	public void loadAllUsersFromServer() {
		new GetAllAccountsInChainAsync().execute(AccountManager.INSTANCE.getAccount().getChainName());
	}
	
	public void getAllAccountsAsyncPostExecute(ArrayList<Account> accounts) {
		if (main.getAdminDallasKeyFragment().isVisible()) {
			main.getAdminDallasKeyFragment().setAccountListAdapterToThisList(accounts);
		}
	}
	
	public void updateKeyForAccount(Account selectedAccount, String key) {
		UpdateSecurityKeyAsync call = new UpdateSecurityKeyAsync();
		call.setKey(key);
		call.execute(selectedAccount);
	}
	
	public void sendSplitOrderToServerPostExecute() {
		MainActivity.getInstance().getCartFragment().getCartButtons().setParkSuccessfulMessage();
		MainTopBarMenu.getInstance().toggleLastUsedView();
		MainActivity.getInstance().getCartFragment().getCartButtons().resetParkSuccessful();
		
		if (main.getNumpadPayFragment() != null) {
			main.getNumpadPayFragment().clearPayments();
		}
	}
	
	public void sendPaymentOrderToServer(Order order) {
		try {
			order.setDate(new Date());
			if (order.getId() > 0) {
				order.setType("O");
				DbAPI.updateOrder(order);
			} else {
				long id = DbAPI.saveOrder(order, "O");
				order.setId(id);
			}
			
			if (MainActivity.getInstance().isConnected()) {
				StatusResponse<String> r = new GenerateOrderNumberAsync().execute(Cart.INSTANCE.getOrder()).get();
				if (r != null && r.getStatusCode() == APIStatus.OK.getCode() && r.getResult() != null) {
					long remoteId = Long.parseLong(r.getResult());
					order.setRemoteId(remoteId);
					DbAPI.setOrderRemoteId(order.getShopId(), order.getId(), remoteId);
					
					new SendOrderWithPaymentAsync().execute(order);
				}
			} else {
				// Id set to zero to use alternative ID in receipts
				order.setId(0);
				OrderPaymentResponse result = new OrderPaymentResponse();
				result.setOrder(order);
				MainActivity.getInstance().getServerCallMethods().sendPaymentOrderToServerPostExecute(result);
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}
	}
	
	public void sendPaymentOrderToServerPostExecute(OrderPaymentResponse result) {
		main.getNumpadPayFragment().paymentPostExecute(result);
		if (main.getNumpadPayFragment() != null) {
			main.getNumpadPayFragment().clearPayments();
		}
	}
	
	public void deleteParkedOrderAsync(Order o) {
		new DeleteParkedOrderAsync().execute(o);
	}
	
	public void sendSplitOrderToServer(Order order) {
		if (order != null && order.hasLines()) {
			order.setDate(new Date());
			parkOrders();
		}
	}
	
	public void updateOrdersTableNumbers(int fromTable, int fromTableArea, int toTable, int toTableArea, TableItemOnDragListener tableListener) {
		DbAPI.mergeKithenOrders(AccountManager.INSTANCE.getAccount().getShop().getID(), fromTable, fromTableArea, toTable, toTableArea);
		tableListener.updateTableView();
	}
	
	public void ordersLoadByTableAsyncPostExecute(ArrayList<Order> orderResults, int area, int table) {
		//Adding any non-table orders in the cart to the selected table
		if (Cart.INSTANCE.hasOrdersWithLines() && Cart.INSTANCE.getSelectedTable() == 0) {
			for (Order o : orderResults) {
				Cart.INSTANCE.addOrderToCart(o);
			}
		} else {
			if (orderResults != null && !orderResults.isEmpty()) {
				Cart.INSTANCE.setOrdersTo(orderResults);
			} else {
				Cart.INSTANCE.resetCart();
			}
		}
		
		Cart.INSTANCE.setSelectedAreaTable(area, table);
		Cart.INSTANCE.setSelectedOrderIndex(0);
		
		if (Cart.INSTANCE.hasActiveOrder()) {
			MainActivity.getInstance().getCartFragment()._orderCustomerUpdated(Cart.INSTANCE.getOrder().getCustomer());
			Cart.INSTANCE.setTakeAwayMode(Cart.INSTANCE.getOrder().isUseAlternative());
		}
		
		MainActivity.getInstance().getCartFragment().refreshCart();
		MainActivity.getInstance().getCartFragment().selectLastRow();
		MainActivity.getInstance().hideProgressDialog();
	}
	
	public void updateKeyForAccountPostExecute(Protocol.Message message) {
		if (message == Protocol.Message.OK) {
			//Toast.makeText(MainActivity.getInstance(), R.string.update_complete, Toast.LENGTH_SHORT).show();
			MainActivity.getInstance().getAdminDallasKeyFragment().updateKeysAfterSuccessfulCall();
		} else {
			Toast.makeText(MainActivity.getInstance(), R.string.update_failed, Toast.LENGTH_SHORT).show();
		}
	}
	
	public void getPosForShopAsync() {
		new GetAllPointOfSaleAsync().execute();
	}
	
	public void getPosForShopPostExecute(ArrayList<PointOfSale> posResults) {
		if (MainActivity.getInstance().getPosChooserDialog() != null) {
			MainActivity.getInstance().getPosChooserDialog().setCallResult(posResults);
			MainActivity.getInstance().setAlreadyCallingPosDialog(false);
			MainActivity.getInstance().showPosChooserDialog();
		}
		
	}
	
	public void getPrepaidsAsyncPostExecute(List<Prepaid> prepaidResults) {
		if (prepaidResults != null) {
			Log.i("vilde", "found prepaids size: " + prepaidResults.size());
			MainActivity.getInstance().getGiftcardLookupDialog().updateNewResults(prepaidResults);
		} else {
			Log.i("vilde", "found no prepaids");
		}
		
	}
	
	public void createNewCustomer(Customer customer) {
		new CreateNewCustomer().execute(customer);
	}
	
	public void createNewCustomerPostExecute(CustomerResponse cr) {
		try {
			if (cr.getMessage() == null) {
				
			} else if (cr.getMessage().equals(Protocol.Message.OK)) {
				Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.customer_added), Toast.LENGTH_SHORT).show();
				Cart.INSTANCE.setOrderCustomer(cr.getCustomer());
			} else {
				
			}
		} catch (Exception ex) {
			ErrorReporter.INSTANCE.filelog(ex);
			ex.printStackTrace();
		}
	}
	
	public void getOrderSearchAsyncPostExecute(ArrayList<OrderPointer> orderResults, OrderSearchDialog returnClass) {
		if (dialog != null) {
			dialog.dismiss();
		}
		
		if (orderResults == null) {
			orderResults = new ArrayList<>();
			new AlertDialog.Builder(MainActivity.getInstance()).setTitle("Error").setMessage("A error has occured.\nRetry, or cancel and return to the previous screen.").setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					loadOrdersFromServer();
					dialog.dismiss();
				}
			}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).setIcon(android.R.drawable.ic_dialog_alert).show();
		}
		
		returnClass.refreshAdapter(orderResults);
	}
	
	public void getSupplyReportAsyncPostExecute(ArrayList<Product> results, SupplyReportDialog returnClass) {
		if (dialog != null) {
			dialog.dismiss();
		}
		returnClass.printReport(results);
	}
}
