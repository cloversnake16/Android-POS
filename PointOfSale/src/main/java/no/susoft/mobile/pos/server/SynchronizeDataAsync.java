package no.susoft.mobile.pos.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.data.Payment.PaymentType;
import no.susoft.mobile.pos.db.DBHelper;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.Message;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.response.APIStatus;
import no.susoft.mobile.pos.response.StatusResponse;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class SynchronizeDataAsync extends AsyncTask<String, Void, String> {

	SynchronizeTransport transport = null;

    @Override
    protected String doInBackground(String... term) {
        try {
			if (AccountManager.INSTANCE.getAccount() == null) {
				return "";
			}
			if (AccountManager.INSTANCE.getAccount().getToken() != null && AccountManager.INSTANCE.getAccount().getToken().trim().length() == 0) {
				
				Request request = Server.INSTANCE.getEncryptedPreparedRequest();
				request.appendState(Protocol.State.NOTAUTHORIZED);
				request.appendOperation(Protocol.OperationCode.REQUEST_REAUTHORIZATION);
				request.appendParameter(Parameters.LICENSE, AccountManager.INSTANCE.getAccount().getLicense());
				request.appendParameter(Parameters.VERSION, SusoftPOSApplication.getVersionName());

				final String json = JSONFactory.INSTANCE.getFactory().toJson(AccountManager.INSTANCE.getLoggedInAccounts());
				final StringBody file = new StringBody(json, ContentType.APPLICATION_JSON);
				final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
				entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entity.addPart("file", file);
				SynchronizeTransport response = Server.INSTANCE.doPost(SynchronizeTransport.class, request, entity, JSONFactory.INSTANCE.getFactory());
				if (response == null) {
					return "";
				}
				
				for (Account account : response.getAccounts()) {
					for (int i = 0; i < AccountManager.INSTANCE.getLoggedInAccounts().size(); i++) {
						Account a = AccountManager.INSTANCE.getLoggedInAccounts().get(i);
						if (a.getLogin().equals(account.getLogin())) {
							AccountManager.INSTANCE.getLoggedInAccounts().set(i, account);
						}
					}
					if (AccountManager.INSTANCE.getAccount().getLogin().equals(account.getLogin())) {
						AccountManager.INSTANCE.setAccount(account);
					}
				}
			}

            transport = new SynchronizeTransport();
			String fromDate = "";
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("	KEY, ");
			sql.append("	VALUE ");
			sql.append("FROM ");
			sql.append("	PARAMETER ");
			sql.append("WHERE ");
			sql.append("	KEY = 'LAST_SYNC_DATE';");

			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), null);
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				fromDate = rs.getString(rs.getColumnIndex("VALUE"));
			}
			if (rs != null) {
				rs.close();
			}
	
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendOperation(OperationCode.REQUEST_SYNCHRONIZE);
            request.appendParameter(Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
            request.appendParameter(Parameters.FROM_DATE, fromDate);
			transport = Server.INSTANCE.doSyncronize(request);
			sendOrders();

		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("SynchronizeDataAsync.doInBackground()", e.getMessage(), e);
        }

        return "";
    }

	@Override
	protected void onPostExecute(String s) {
		super.onPostExecute(s);
		if (transport != null) {

			StringBuilder sql = new StringBuilder();
			sql.append("INSERT OR REPLACE ");
			sql.append("INTO ");
			sql.append("	PRODUCT ");
			sql.append("( ");
			sql.append("	ID, ");
			sql.append("	NAME, ");
			sql.append("	BARCODE, ");
			sql.append("	DESCRIPTION, ");
			sql.append("	TYPE, ");
			sql.append("	COST, ");
			sql.append("	PRICE, ");
			sql.append("	STOCK, ");
			sql.append("	ABCCODE, ");
			sql.append("	VAT, ");
			sql.append("	USE_ALTERNATIVE, ");
			sql.append("	ALTERNATIVE_PRICE, ");
			sql.append("	ALTERNATIVE_VAT, ");
			sql.append("	MISC ) ");
			sql.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?); ");

			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
			SQLiteStatement statement = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sql.toString());
			db.beginTransaction();
			
			try {
				for (Product product : transport.getProducts()) {
					statement.clearBindings();
					statement.bindString(1, product.getId());
					statement.bindString(2, product.getName());
					statement.bindString(3, product.getBarcode());
					statement.bindString(4, product.getDescription());
					statement.bindString(5, product.getType());
					statement.bindDouble(6, product.getCost().toDouble());
					statement.bindDouble(7, product.getPrice().toDouble());
					statement.bindDouble(8, product.getStockQty().toDouble());
					statement.bindString(9, product.getAbcCode());
					statement.bindDouble(10, product.getVat());
					statement.bindLong(11, product.isUseAlternative() ? 1 : 0);
					statement.bindDouble(12, product.getAlternativePrice().toDouble());
					statement.bindDouble(13, product.getAlternativeVat());
					statement.bindLong(14, product.isMiscellaneous() ? 1 : 0);
					statement.execute();
				}
				statement.close();

				db.execSQL("DELETE FROM ACCOUNT;");

				sql = new StringBuilder();
				sql.append("INSERT OR REPLACE ");
				sql.append("INTO ");
				sql.append("	ACCOUNT ");
				sql.append("( ");
				sql.append("	ID, ");
				sql.append("	SHOP, ");
				sql.append("	NAME, ");
				sql.append("	USERNAME, ");
				sql.append("	PASSWORD, ");
				sql.append("	TYPE, ");
				sql.append("	SECURITYGROUP, ");
				sql.append("	SECURITYCODE ) ");
				sql.append("VALUES (?,?,?,?,?,?,?,?); ");

				statement = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sql.toString());
				for (Account account : transport.getAccounts()) {
					statement.clearBindings();
					statement.bindString(1, account.getUserId());
					statement.bindString(2, account.getUserShopId());
					statement.bindString(3, account.getName());
					statement.bindString(4, account.getUserId());
					statement.bindString(5, account.getPassword());
					statement.bindString(6, account.getEmployeeType());
					statement.bindString(7, account.getSecurityGroup());
					statement.bindString(8, account.getSecurityCode());
					statement.execute();
				}
				statement.close();

				db.execSQL("DELETE FROM PRODUCT_BUNDLE;");

				sql = new StringBuilder();
				sql.append("INSERT ");
				sql.append("INTO ");
				sql.append("	PRODUCT_BUNDLE ");
				sql.append("( ");
				sql.append("	PRODUCT_ID, ");
				sql.append("	REF_ID, ");
				sql.append("	QTY ) ");
				sql.append("VALUES (?,?,?); ");

				statement = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sql.toString());
				for (ProductBundle c : transport.getBundles()) {
					statement.clearBindings();
					statement.bindString(1, c.getProductId());
					statement.bindString(2, c.getRefId());
					statement.bindDouble(3, c.getQty());

					statement.execute();
				}
				statement.close();

				sql = new StringBuilder();
				sql.append("INSERT OR REPLACE ");
				sql.append("INTO ");
				sql.append("	CUSTOMER ");
				sql.append("( ");
				sql.append("	ID, ");
				sql.append("	FIRSTNAME, ");
				sql.append("	LASTNAME, ");
				sql.append("	EMAIL, ");
				sql.append("	PHONE, ");
				sql.append("	MOBILE, ");
				sql.append("	COMPANY ) ");
				sql.append("VALUES (?,?,?,?,?,?,?); ");

				statement = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sql.toString());
				for (Customer customer : transport.getCustomers()) {
					statement.clearBindings();
					statement.bindString(1, customer.getId());
					statement.bindString(2, customer.getFirstName());
					statement.bindString(3, customer.getLastName());
					statement.bindString(4, customer.getEmail());
					statement.bindString(5, customer.getPhone());
					statement.bindString(6, customer.getMobile());
					statement.bindLong(7, customer.isCompany() ? 1 : 0);
					statement.execute();
				}
				statement.close();

				db.execSQL("DELETE FROM QLM_MENU_GRID;");
				db.execSQL("DELETE FROM QLM_CELL;");

				if (transport.getQlmGrids() != null) {
					sql = new StringBuilder();
					sql.append("INSERT OR REPLACE ");
					sql.append("INTO ");
					sql.append("	QLM_MENU_GRID ");
					sql.append("( ");
					sql.append("	ID, ");
					sql.append("	X_SIZE, ");
					sql.append("	Y_SIZE, ");
					sql.append("	PARENT_CELL_ID ) ");
					sql.append("VALUES (?,?,?,?); ");

					statement = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sql.toString());
					for (QuickLaunchMenuGrid grid : transport.getQlmGrids()) {
						statement.clearBindings();
						statement.bindLong(1, grid.getId());
						statement.bindLong(2, grid.getSizeX());
						statement.bindLong(3, grid.getSizeY());
						statement.bindLong(4, grid.getParentCellId());
						statement.execute();
					}
					statement.close();

					if (transport.getQlmCells() != null) {
						sql = new StringBuilder();
						sql.append("INSERT OR REPLACE ");
						sql.append("INTO ");
						sql.append("	QLM_CELL ");
						sql.append("( ");
						sql.append("	ID, ");
						sql.append("	PARENT_GRID_ID, ");
						sql.append("	IDX, ");
						sql.append("	TEXT, ");
						sql.append("	PRODUCT_ID, ");
						sql.append("	BARCODE ) ");
						sql.append("VALUES (?,?,?,?,?,?); ");

						statement = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sql.toString());
						for (QuickLaunchMenuCell cell : transport.getQlmCells()) {
							statement.clearBindings();
							statement.bindLong(1, cell.getId());
							statement.bindLong(2, cell.getParentGridId());
							statement.bindLong(3, cell.getIdx());
							statement.bindString(4, cell.getText());
							statement.bindString(5, cell.getProductId());
							statement.bindString(6, cell.getBarcode());
							statement.execute();
						}

						statement.close();
					}
				}

				db.execSQL("DELETE FROM SHOP_AREA;");
				db.execSQL("DELETE FROM SHOP_TABLE;");

				if (transport.getAreas() != null) {
					sql = new StringBuilder();
					sql.append("INSERT OR REPLACE ");
					sql.append("INTO ");
					sql.append("	SHOP_AREA ");
					sql.append("( ");
					sql.append("	ID, ");
					sql.append("	NAME ) ");
					sql.append("VALUES (?,?); ");

					statement = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sql.toString());
					for (Area area : transport.getAreas()) {
						statement.clearBindings();
						statement.bindLong(1, area.getId());
						statement.bindString(2, area.getName());
						statement.execute();
					}
					statement.close();

					if (transport.getAreas() != null) {
						sql = new StringBuilder();
						sql.append("INSERT OR REPLACE ");
						sql.append("INTO ");
						sql.append("	SHOP_TABLE ");
						sql.append("( ");
						sql.append("	AREAID, ");
						sql.append("	NUMBER, ");
						sql.append("	CAPACITY ) ");
						sql.append("VALUES (?,?,?); ");

						statement = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sql.toString());
						for (Area area : transport.getAreas()) {
							for (Table table : area.getTables()) {
								statement.clearBindings();
								statement.bindLong(1, table.getAreaId());
								statement.bindLong(2, table.getNumber());
								statement.bindLong(3, table.getCapacity());
								statement.execute();
							}
						}

						statement.close();
					}
				}

				sql = new StringBuilder();
				sql.append("INSERT OR REPLACE ");
				sql.append("INTO ");
				sql.append("	PARAMETER ");
				sql.append("( ");
				sql.append("	KEY, ");
				sql.append("	VALUE ) ");
				sql.append("VALUES (?,?); ");

				Calendar cal = Calendar.getInstance();
				statement = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sql.toString());
				statement.clearBindings();
				statement.bindString(1, Parameter.LAST_SYNC_DATE);
				statement.bindString(2, DBHelper.DB_DATE_FORMAT.format(cal.getTime()));
				statement.execute();

				db.setTransactionSuccessful();

			} catch (Exception e) {
				ErrorReporter.INSTANCE.filelog("SYNCHRONIZE", "Error on accessing DB", e);
			}

			db.endTransaction();
			statement.close();
		}
		
		ErrorReporter.INSTANCE.filelog("Syncronization end.");

		MainActivity.getInstance().initializePos();
	}

	private void sendOrders() {
		
		ArrayList<Order> orders = new ArrayList<>();
		ArrayList<Properties> keys = new ArrayList<>();
		HashMap<String, ArrayList<Order>> shopOrdersWithoutNumbers = new HashMap<>();
		Cursor rs = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("    ID, ");
			sql.append("    SHOP, ");
			sql.append("    TYPE, ");
			sql.append("    POS, ");
			sql.append("    DATE, ");
			sql.append("    SALESPERSON, ");
			sql.append("    CUSTOMERID, ");
			sql.append("    AREAID, ");
			sql.append("    TABLEID, ");
			sql.append("    NOTE, ");
			sql.append("    REMOTEID, ");
			sql.append("    ALTERNATIVEID, ");
			sql.append("    RECONCILIATION_ID ");
			sql.append("FROM ");
			sql.append("	ORDERHEADER ");
			sql.append("WHERE ");
			sql.append("	TYPE = 'O' ");
			sql.append("AND IS_SENT = 0;");
			
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
			rs = db.rawQuery(sql.toString(), null);
			
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				do {
					Order o = new Order();
					long orderId = rs.getLong(rs.getColumnIndex("ID"));
					long remoteId = rs.getLong(rs.getColumnIndex("REMOTEID"));
					String shopId = rs.getString(rs.getColumnIndex("SHOP"));
					String type = rs.getString(rs.getColumnIndex("TYPE"));
					
					o.setId(orderId);
					o.setShopId(shopId);
					o.setType(type);
					o.setRemoteId(remoteId);
					o.setPosNo(rs.getString(rs.getColumnIndex("POS")));
					o.setDate(DBHelper.DB_DATETIME_FORMAT.parse(rs.getString(rs.getColumnIndex("DATE"))));
					o.setSalesPersonId(rs.getString(rs.getColumnIndex("SALESPERSON")));
					o.setAlternativeId(rs.getString(rs.getColumnIndex("ALTERNATIVEID")));
					o.setCustomer(getCustomer(rs.getString(rs.getColumnIndex("CUSTOMERID"))));
					o.setArea(rs.getInt(rs.getColumnIndex("AREAID")));
					o.setTable(rs.getInt(rs.getColumnIndex("TABLEID")));
					o.setNote(rs.getString(rs.getColumnIndex("NOTE")));
					o.setReconciliationId(rs.getString(rs.getColumnIndex("RECONCILIATION_ID")));
					
					o.setLines(getOrderLines(orderId, o.getShopId()));
					o.setPayments(getPayments(orderId, o.getShopId()));
					
					orders.add(o);
					
				} while (rs.moveToNext());
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		
		for (Order order : orders) {
			try {
				if (order.getRemoteId() == 0) {
					Request request = Server.INSTANCE.getEncryptedPreparedRequest();
					request.appendState(Protocol.State.AUTHORIZED);
					request.appendOperation(OperationCode.REQUEST_GENERATE_NUMBER);
					request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
					request.appendParameter(Parameters.SHOP, order.getShopId());
					request.appendParameter(Parameters.TYPE, order.getType());
					
					StatusResponse<String> r = null;
					String json = Server.INSTANCE.doGet(request);
					if (json != null) {
						Gson gson = JSONFactory.INSTANCE.getFactory();
						JsonParser parser = new JsonParser();
						JsonElement element = parser.parse(json);
						if (element != null) {
							r = gson.fromJson(element, StatusResponse.class);
						}
					}
					
					if (r != null && r.getStatusCode() == APIStatus.OK.getCode()) {
						try {
							long remoteId = Long.parseLong(r.getResult());
							order.setRemoteId(remoteId);
							DbAPI.setOrderRemoteId(order.getShopId(), order.getId(), remoteId);
						} catch (Exception e) {
							ErrorReporter.INSTANCE.filelog("Coudnt generate order number. Result = " + r.getResult());
							throw new Exception("Coudnt generate order number.");
						}
					} else {
						ErrorReporter.INSTANCE.filelog("Coudnt generate order number. Result = " + r);
						throw new Exception("Coudnt generate order number.");
					}
				}
				
				ArrayList<Order> ordersToSend = new ArrayList<>();
				ordersToSend.add(order);
				
				Request request = Server.INSTANCE.getEncryptedPreparedRequest();
				request.appendState(Protocol.State.AUTHORIZED);
				request.appendOperation(Protocol.OperationCode.REQUEST_SYNCHRONIZE_ORDERS);
				request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
				request.appendParameter(Parameters.TYPE, Parameters.COMPLETE.ordinal());
				request.appendParameter(Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
				
				final String json = JSONFactory.INSTANCE.getFactory().toJson(ordersToSend);
				final StringBody file = new StringBody(json, ContentType.APPLICATION_JSON);
				final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
				entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entity.addPart("file", file);
				
				Message m = Server.INSTANCE.doPost(Message.class, request, entity, JSONFactory.INSTANCE.getFactory());
				if (m == Message.OK) {
					DbAPI.markOrderAsSent(order.getShopId(), order.getId());
				}
				
			} catch (Exception e) {
				ErrorReporter.INSTANCE.filelog(e);
			}
		}
	
		ArrayList<Reconciliation> reconciliations = new ArrayList<>();
		rs = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("    ID, ");
			sql.append("    SHOP, ");
			sql.append("    CONTENT, ");
			sql.append("    AMOUNT ");
			sql.append("FROM ");
			sql.append("	RECONCILIATION ");
			sql.append("WHERE ");
			sql.append("	IS_SENT = 0; ");
			
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
			rs = db.rawQuery(sql.toString(), null);
			
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				do {
					Reconciliation r = new Reconciliation();
					r.id = rs.getString(rs.getColumnIndex("ID"));
					r.shopId = rs.getString(rs.getColumnIndex("SHOP"));
					r.content = rs.getString(rs.getColumnIndex("CONTENT"));
					r.amount = rs.getDouble(rs.getColumnIndex("AMOUNT"));
					reconciliations.add(r);
					
				} while (rs.moveToNext());
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		
		if (reconciliations.size() > 0) {
			
			for (Reconciliation r : reconciliations) {
				Reconciliation reconciliation = DbAPI.getReconciliation(r.shopId, r.id);
				
				try {
					Request request = Server.INSTANCE.getEncryptedPreparedRequest();
					request.appendState(Protocol.State.AUTHORIZED);
					request.appendOperation(OperationCode.REQUEST_SYNCHRONIZE_RECONCILIATIONS);
					request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
					
					final String json = JSONFactory.INSTANCE.getFactory().toJson(reconciliation);
					final StringBody file = new StringBody(json, ContentType.APPLICATION_JSON);
					final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
					entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
					entity.addPart("file", file);
					
					StatusResponse result = Server.INSTANCE.doPost(StatusResponse.class, request, entity, JSONFactory.INSTANCE.getFactory());
					if (result != null && result.getStatusCode() == APIStatus.OK.getCode()) {
						DbAPI.markReconciliationAsSent(reconciliation.shopId, reconciliation.id);
						ErrorReporter.INSTANCE.filelog("Reconciliation sent. ID = " + reconciliation.id);
					}
				} catch (Exception e) {
					ErrorReporter.INSTANCE.filelog(e);
				}
			}
		}
	}

	private ArrayList<OrderLine> getOrderLines(long orderId, String shopId) {
		ArrayList<OrderLine> lines = new ArrayList<>();
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("    ORDERID, ");
			sql.append("    SHOP, ");
			sql.append("    PRODUCT, ");
			sql.append("    TEXT, ");
			sql.append("    PRICE, ");
			sql.append("    DISCOUNTREASON, ");
			sql.append("    DISCOUNT, ");
			sql.append("    QTY, ");
			sql.append("    DELIVERED, ");
			sql.append("    NOTE, ");
			sql.append("    SALESPERSON ");
			sql.append("FROM ");
			sql.append("	ORDERLINE ");
			sql.append("WHERE ");
			sql.append("	ORDERID = ? ");
			sql.append("AND ");
			sql.append("	SHOP = ?;");

			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), new String[]{"" + orderId, shopId});
			
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				do {
					OrderLine line = new OrderLine();
					line.setOrderId(orderId);
					line.setShopId(shopId);
					line.setProduct(getProduct(rs.getString(rs.getColumnIndex("PRODUCT"))));
					line.setText(rs.getString(rs.getColumnIndex("TEXT")));
					line.setPrice(Decimal.make(rs.getDouble(rs.getColumnIndex("PRICE"))));

					if (rs.getDouble(rs.getColumnIndex("DISCOUNT")) > 0) {
						Discount discount = new Discount(Decimal.make(rs.getDouble(rs.getColumnIndex("DISCOUNT"))), rs.getInt(rs.getColumnIndex("DISCOUNTREASON")));
						line.setDiscount(discount);
					}

					line.setQuantity(Decimal.make(rs.getDouble(rs.getColumnIndex("QTY"))));
					line.setDeliveredQty(Decimal.make(rs.getDouble(rs.getColumnIndex("DELIVERED"))));
					line.setNote(rs.getString(rs.getColumnIndex("NOTE")));
					line.setSalesPersonId(rs.getString(rs.getColumnIndex("SALESPERSON")));

					lines.add(line);
				} while (rs.moveToNext());
			}
			if (rs != null) {
				rs.close();
			}

		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}

		return lines;
	}

	private ArrayList<Payment> getPayments(long orderId, String shopId) {
		ArrayList<Payment> payments = new ArrayList<>();
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("    ORDERID, ");
			sql.append("    SHOP, ");
			sql.append("    TYPE, ");
			sql.append("    AMOUNT, ");
			sql.append("    NUMBER, ");
			sql.append("    TERMINALTYPE, ");
			sql.append("    CARDID ");
			sql.append("FROM ");
			sql.append("	PAYMENT ");
			sql.append("WHERE ");
			sql.append("	ORDERID = ? ");
			sql.append("AND ");
			sql.append("	SHOP = ?;");

			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), new String[]{"" + orderId, shopId});
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				do {
					Payment payment = new Payment(PaymentType.getType(rs.getInt(rs.getColumnIndex("TYPE"))), rs.getString(rs.getColumnIndex("NUMBER")), Decimal.make(rs.getDouble(rs.getColumnIndex("AMOUNT"))));
					payment.setCardTerminalType(rs.getInt(rs.getColumnIndex("TERMINALTYPE")));
					payment.setCardId(rs.getInt(rs.getColumnIndex("CARDID")));

					payments.add(payment);
				} while (rs.moveToNext());
			}
			if (rs != null) {
				rs.close();
			}

		} catch (Exception e) {
			throw e;
		}

		return payments;
	}

	private Product getProduct(String productId) throws Exception {
		Product product = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("    ID, ");
			sql.append("    NAME, ");
			sql.append("    BARCODE, ");
			sql.append("    DESCRIPTION, ");
			sql.append("    TYPE, ");
			sql.append("    COST, ");
			sql.append("    PRICE, ");
			sql.append("    ABCCODE, ");
			sql.append("    VAT, ");
			sql.append("	USE_ALTERNATIVE, ");
			sql.append("	ALTERNATIVE_PRICE, ");
			sql.append("	ALTERNATIVE_VAT, ");
			sql.append("	TARE, ");
			sql.append("    MISC ");
			sql.append("FROM ");
			sql.append("	PRODUCT ");
			sql.append("WHERE ");
			sql.append("	ID = ?;");

			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), new String[]{productId});
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				product = new Product();
				product.setId(rs.getString(rs.getColumnIndex("ID")));
				product.setName(rs.getString(rs.getColumnIndex("NAME")));
				product.setBarcode(rs.getString(rs.getColumnIndex("BARCODE")));
				product.setDescription(rs.getString(rs.getColumnIndex("DESCRIPTION")));
				product.setType(rs.getString(rs.getColumnIndex("TYPE")));
				product.setCost(Decimal.make(rs.getDouble(rs.getColumnIndex("COST"))));
				product.setPrice(Decimal.make(rs.getDouble(rs.getColumnIndex("PRICE"))));
				product.setAbcCode(rs.getString(rs.getColumnIndex("ABCCODE")));
				product.setVat(rs.getDouble(rs.getColumnIndex("VAT")));
				product.setUseAlternative(rs.getInt(rs.getColumnIndex("USE_ALTERNATIVE")) == 1);
				product.setAlternativePrice(Decimal.make(rs.getDouble(rs.getColumnIndex("ALTERNATIVE_PRICE"))));
				product.setAlternativeVat(rs.getDouble(rs.getColumnIndex("ALTERNATIVE_VAT")));
				product.setTare(rs.getInt(rs.getColumnIndex("TARE")));
				product.setMiscellaneous(rs.getInt(rs.getColumnIndex("MISC")) == 1);
			}
			if (rs != null) {
				rs.close();
			}

		} catch (Exception e) {
			throw e;
		}

		return product;
	}

    protected Customer getCustomer(String customerId) {
		Customer c = null;
		if (customerId != null && !customerId.isEmpty()) {

			try {
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT ");
				sql.append("	ID, ");
				sql.append("	FIRSTNAME, ");
				sql.append("	LASTNAME, ");
				sql.append("	EMAIL, ");
				sql.append("	PHONE, ");
				sql.append("	MOBILE, ");
				sql.append("	COMPANY ");
				sql.append("FROM ");
				sql.append("	CUSTOMER ");
				sql.append("WHERE ");
				sql.append("	ID = ?;");

				SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
				Cursor rs = db.rawQuery(sql.toString(), new String[]{customerId});
				if (rs != null && rs.getCount() > 0) {
					rs.moveToFirst();
					c = new Customer();
					c.setId(rs.getString(rs.getColumnIndex("ID")));
					c.setFirstName(rs.getString(rs.getColumnIndex("FIRSTNAME")));
					c.setLastName(rs.getString(rs.getColumnIndex("LASTNAME")));
					c.setEmail(rs.getString(rs.getColumnIndex("EMAIL")));
					c.setPhone(rs.getString(rs.getColumnIndex("PHONE")));
					c.setMobile(rs.getString(rs.getColumnIndex("MOBILE")));
					c.setCompany(rs.getInt(rs.getColumnIndex("COMPANY")) == 1);
				}
				if (rs != null) {
					rs.close();
				}

			} catch (Exception e) {
				throw e;
			}
		}

        return c;
    }

}