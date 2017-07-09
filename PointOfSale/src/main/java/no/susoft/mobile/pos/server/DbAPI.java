package no.susoft.mobile.pos.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.data.Payment.PaymentType;
import no.susoft.mobile.pos.db.DBHelper;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;

public class DbAPI {
	
	public static Order getOrder(String shopId, long orderId) {
		Order o = null;
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
			sql.append("    IS_SENT ");
			sql.append("FROM ");
			sql.append("	ORDERHEADER ");
			sql.append("WHERE ");
			sql.append("	ID = ? ");
			sql.append("AND SHOP = ?;");
			
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
			rs = db.rawQuery(sql.toString(), new String[]{"" + orderId, shopId});
			
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				do {
					o = new Order();
					o.setId(orderId);
					o.setShopId(shopId);
					o.setType(rs.getString(rs.getColumnIndex("TYPE")));
					o.setRemoteId(rs.getLong(rs.getColumnIndex("REMOTEID")));
					o.setPosNo(rs.getString(rs.getColumnIndex("POS")));
					o.setDate(DBHelper.DB_DATETIME_FORMAT.parse(rs.getString(rs.getColumnIndex("DATE"))));
					o.setSalesPersonId(rs.getString(rs.getColumnIndex("SALESPERSON")));
					o.setAlternativeId(rs.getString(rs.getColumnIndex("ALTERNATIVEID")));
					o.setCustomer(getCustomer(rs.getString(rs.getColumnIndex("CUSTOMERID"))));
					o.setArea(rs.getInt(rs.getColumnIndex("AREAID")));
					o.setTable(rs.getInt(rs.getColumnIndex("TABLEID")));
					o.setNote(rs.getString(rs.getColumnIndex("NOTE")));
					
					o.setLines(getOrderLines(orderId, o.getShopId()));
					o.setPayments(getPayments(orderId, o.getShopId()));
					
				} while (rs.moveToNext());
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("SynchronizeDataAsync", "", e);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		return o;
	}
	
	private static ArrayList<OrderLine> getOrderLines(long orderId, String shopId) {
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
	
	private static ArrayList<Payment> getPayments(long orderId, String shopId) {
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
	
	private static Product getProduct(String productId) throws Exception {
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
	
	protected static Customer getCustomer(String customerId) {
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
	
	public static long saveOrder(Order order) throws Exception {
		return saveOrder(order, "P");
	}
	
	public static long saveOrder(Order order, String type) throws Exception {
		long id = 0;
		StringBuilder sqlO = new StringBuilder();
		sqlO.append("INSERT ");
		sqlO.append("INTO ");
		sqlO.append("    ORDERHEADER ");
		sqlO.append("( ");
		sqlO.append("    SHOP, ");
		sqlO.append("    POS, ");
		sqlO.append("    DATE, ");
		sqlO.append("    SALESPERSON, ");
		sqlO.append("    CUSTOMERID, ");
		sqlO.append("    NOTE, ");
		sqlO.append("    TYPE, ");
		sqlO.append("    USE_ALTERNATIVE, ");
		sqlO.append("    REMOTEID, ");
		sqlO.append("    ALTERNATIVEID ) ");
		sqlO.append("VALUES (?,?,?,?,?,?,?,?,?,?); ");
		
		StringBuilder sqlL = new StringBuilder();
		sqlL.append("INSERT ");
		sqlL.append("INTO ");
		sqlL.append("    ORDERLINE ");
		sqlL.append("( ");
		sqlL.append("    ORDERID, ");
		sqlL.append("    SHOP, ");
		sqlL.append("    PRODUCT, ");
		sqlL.append("    TEXT, ");
		sqlL.append("    PRICE, ");
		sqlL.append("    DISCOUNTREASON, ");
		sqlL.append("    DISCOUNT, ");
		sqlL.append("    QTY, ");
		sqlL.append("    DELIVERED, ");
		sqlL.append("    NOTE, ");
		sqlL.append("    SALESPERSON ) ");
		sqlL.append("VALUES (?,?,?,?,?,?,?,?,?,?,?); ");
		
		StringBuilder sqlP = new StringBuilder();
		sqlP.append("INSERT ");
		sqlP.append("INTO ");
		sqlP.append("    PAYMENT ");
		sqlP.append("( ");
		sqlP.append("    ORDERID, ");
		sqlP.append("    SHOP, ");
		sqlP.append("    TYPE, ");
		sqlP.append("    AMOUNT, ");
		sqlP.append("    NUMBER, ");
		sqlP.append("    TERMINALTYPE, ");
		sqlP.append("    CARDID ) ");
		sqlP.append("VALUES (?,?,?,?,?,?,?); ");
		
		SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
		SQLiteStatement stOrder = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sqlO.toString());
		SQLiteStatement stLine = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sqlL.toString());
		SQLiteStatement stPayment = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sqlP.toString());
		db.beginTransaction();
		
		try {
			stOrder.clearBindings();
			stOrder.bindString(1, order.getShopId());
			stOrder.bindString(2, order.getPosNo());
			stOrder.bindString(3, DBHelper.DB_DATETIME_FORMAT.format(order.getDate()));
			stOrder.bindString(4, order.getSalesPersonId());
			if (order.getCustomer() != null) {
				stOrder.bindString(5, order.getCustomer().getId());
			}
			if (order.getNote() != null) {
				stOrder.bindString(6, order.getNote());
			}
			stOrder.bindString(7, type);
			stOrder.bindLong(8, order.isUseAlternative() ? 1 : 0);
			stOrder.bindLong(9, order.getRemoteId());
			stOrder.bindString(10, order.getAlternativeId());
			id = stOrder.executeInsert();
			
			ArrayList<OrderLine> lines = new ArrayList<>();
			for (OrderLine line : order.getLines()) {
				lines.add(line);
				if (line.getComponents() != null) {
					lines.addAll(line.getComponents());
				}
			}
			
			for (OrderLine line : lines) {
				stLine.clearBindings();
				stLine.bindLong(1, id);
				stLine.bindString(2, order.getShopId());
				stLine.bindString(3, line.getProduct().getId());
				stLine.bindString(4, line.getText());
				stLine.bindDouble(5, line.getPrice().toDouble());
				
				if (line.getDiscount() != null) {
					if (line.getDiscount().getReason() != null) {
						stLine.bindLong(6, line.getDiscount().getReason().getID());
					}
					stLine.bindDouble(7, line.getDiscount().getPercent().toDouble());
				}
				
				stLine.bindDouble(8, line.getQuantity().toDouble());
				stLine.bindDouble(9, line.getDeliveredQty().toDouble());
				if (line.getNote() != null) {
					stLine.bindString(10, line.getNote());
				}
				if (line.getSalesPersonId() != null) {
					stLine.bindString(11, line.getSalesPersonId());
				}
				stLine.executeInsert();
			}
			
			for (Payment payment : order.getPayments()) {
				stPayment.clearBindings();
				stPayment.bindLong(1, id);
				stPayment.bindString(2, order.getShopId());
				stPayment.bindLong(3, payment.getType().getValue());
				stPayment.bindDouble(4, payment.getAmount().toDouble());
				stPayment.bindString(5, payment.getNumber());
				stPayment.bindLong(6, payment.getCardTerminalType());
				stPayment.bindLong(7, payment.getCardId());
				stPayment.executeInsert();
			}
			
			db.setTransactionSuccessful();
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
			throw e;
		} finally {
			stOrder.close();
			stLine.close();
			db.endTransaction();
		}
		
		return id;
	}
	
	public static void updateOrder(Order order) throws Exception {
		SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
		db.beginTransaction();
		SQLiteStatement stLine = null;
		
		try {
			String date = DBHelper.DB_DATETIME_FORMAT.format(order.getDate() != null ? order.getDate() : new Date());
			ContentValues cv = new ContentValues();
			cv.put("DATE", date);
			cv.put("POS", order.getPosNo());
			cv.put("SALESPERSON", order.getSalesPersonId());
			cv.put("CUSTOMERID", order.getCustomer() != null ? order.getCustomer().getId() : null);
			cv.put("NOTE", order.getNote() != null ? order.getNote() : null);
			cv.put("TYPE", order.getType());
			cv.put("USE_ALTERNATIVE", order.isUseAlternative() ? 1 : 0);
			cv.put("REMOTEID", order.getRemoteId());
			cv.put("ALTERNATIVEID", order.getAlternativeId());
			
			db.update("ORDERHEADER", cv, "ID = ? AND SHOP = ?", new String[]{"" + order.getId(), order.getShopId()});
			db.delete("ORDERLINE", "ORDERID = ? AND SHOP = ?", new String[]{"" + order.getId(), order.getShopId()});
			db.delete("PAYMENT", "ORDERID = ? AND SHOP = ?", new String[]{"" + order.getId(), order.getShopId()});
			
			StringBuilder sqlL = new StringBuilder();
			sqlL.append("INSERT ");
			sqlL.append("INTO ");
			sqlL.append("    ORDERLINE ");
			sqlL.append("( ");
			sqlL.append("    ORDERID, ");
			sqlL.append("    SHOP, ");
			sqlL.append("    PRODUCT, ");
			sqlL.append("    TEXT, ");
			sqlL.append("    PRICE, ");
			sqlL.append("    DISCOUNTREASON, ");
			sqlL.append("    DISCOUNT, ");
			sqlL.append("    QTY, ");
			sqlL.append("    DELIVERED, ");
			sqlL.append("    NOTE, ");
			sqlL.append("    SALESPERSON ) ");
			sqlL.append("VALUES (?,?,?,?,?,?,?,?,?,?,?); ");
			
			stLine = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sqlL.toString());
			for (OrderLine line : order.getLines()) {
				stLine.clearBindings();
				stLine.bindLong(1, order.getId());
				stLine.bindString(2, order.getShopId());
				stLine.bindString(3, line.getProduct().getId());
				stLine.bindString(4, line.getText());
				stLine.bindDouble(5, line.getPrice().toDouble());
				
				if (line.getDiscount() != null) {
					if (line.getDiscount().getReason() != null) {
						stLine.bindLong(6, line.getDiscount().getReason().getID());
					}
					stLine.bindDouble(7, line.getDiscount().getPercent().toDouble());
				}
				
				stLine.bindDouble(8, line.getQuantity().toDouble());
				stLine.bindDouble(9, line.getDeliveredQty().toDouble());
				if (line.getNote() != null) {
					stLine.bindString(10, line.getNote());
				}
				if (line.getSalesPersonId() != null) {
					stLine.bindString(11, line.getSalesPersonId());
				}
				stLine.executeInsert();
			}
			
			if (order.getPayments() != null && !order.getPayments().isEmpty()) {
				StringBuilder sqlP = new StringBuilder();
				sqlP.append("INSERT ");
				sqlP.append("INTO ");
				sqlP.append("    PAYMENT ");
				sqlP.append("( ");
				sqlP.append("    ORDERID, ");
				sqlP.append("    SHOP, ");
				sqlP.append("    TYPE, ");
				sqlP.append("    AMOUNT, ");
				sqlP.append("    NUMBER, ");
				sqlP.append("    TERMINALTYPE, ");
				sqlP.append("    CARDID ) ");
				sqlP.append("VALUES (?,?,?,?,?,?,?); ");
				
				SQLiteStatement stPayment = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sqlP.toString());
				for (Payment payment : order.getPayments()) {
					stPayment.clearBindings();
					stPayment.bindLong(1, order.getId());
					stPayment.bindString(2, order.getShopId());
					stPayment.bindLong(3, payment.getType().getValue());
					stPayment.bindDouble(4, payment.getAmount().toDouble());
					stPayment.bindString(5, payment.getNumber());
					stPayment.bindLong(6, payment.getCardTerminalType());
					stPayment.bindLong(7, payment.getCardId());
					stPayment.executeInsert();
				}
				if (stPayment != null) {
					stPayment.close();
				}
			}
			
			db.setTransactionSuccessful();
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
			throw e;
		} finally {
			if (stLine != null) {
				stLine.close();
			}
			db.endTransaction();
		}
	}
	
	public static void deleteOrder(String shopId, long orderId) throws Exception {
		SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
		db.beginTransaction();
		try {
			db.delete("ORDERHEADER", "ID = ? AND SHOP = ?", new String[]{"" + orderId, shopId});
			db.delete("ORDERLINE", "ORDERID = ? AND SHOP = ?", new String[]{"" + orderId, shopId});
			db.setTransactionSuccessful();
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
			throw e;
		} finally {
			db.endTransaction();
		}
	}
	
	public static void setOrderRemoteId(String shopId, long orderId, long remoteId) throws Exception {
		SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
		try {
			ContentValues cv = new ContentValues();
			cv.put("REMOTEID", remoteId);
			db.update("ORDERHEADER", cv, "ID = ? AND SHOP = ?", new String[]{"" + orderId, shopId});
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
			throw e;
		}
	}
	
	public static void markOrderAsSent(String shopId, long orderId) throws Exception {
		SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
		try {
			ContentValues cv = new ContentValues();
			cv.put("IS_SENT", 1);
			db.update("ORDERHEADER", cv, "ID = ? AND SHOP = ?", new String[]{"" + orderId, shopId});
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
			throw e;
		}
	}
	
	public static void saveKitchenOrders(ArrayList<Order> orders) throws Exception {
		try {
			StringBuilder sqlO = new StringBuilder();
			sqlO.append("INSERT OR REPLACE ");
			sqlO.append("INTO ");
			sqlO.append("    TMPORDERHEADER ");
			sqlO.append("( ");
			sqlO.append("    ID, ");
			sqlO.append("    SHOP, ");
			sqlO.append("    POS, ");
			sqlO.append("    DATE, ");
			sqlO.append("    SALESPERSON, ");
			sqlO.append("    CUSTOMERID, ");
			sqlO.append("    AREAID, ");
			sqlO.append("    TABLEID, ");
			sqlO.append("    NOTE, ");
			sqlO.append("    TYPE, ");
			sqlO.append("    USE_ALTERNATIVE, ");
			sqlO.append("    REMOTEID, ");
			sqlO.append("    ALTERNATIVEID ) ");
			sqlO.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?); ");
			
			StringBuilder sqlL = new StringBuilder();
			sqlL.append("INSERT ");
			sqlL.append("INTO ");
			sqlL.append("    TMPORDERLINE ");
			sqlL.append("( ");
			sqlL.append("    ORDERID, ");
			sqlL.append("    SHOP, ");
			sqlL.append("    PRODUCT, ");
			sqlL.append("    TEXT, ");
			sqlL.append("    PRICE, ");
			sqlL.append("    DISCOUNTREASON, ");
			sqlL.append("    DISCOUNT, ");
			sqlL.append("    QTY, ");
			sqlL.append("    DELIVERED, ");
			sqlL.append("    NOTE, ");
			sqlL.append("    SALESPERSON ) ");
			sqlL.append("VALUES (?,?,?,?,?,?,?,?,?,?,?); ");
			
			StringBuilder sqlB = new StringBuilder();
			sqlB.append("INSERT ");
			sqlB.append("INTO ");
			sqlB.append("    TMPORDER_BUNDLE ");
			sqlB.append("( ");
			sqlB.append("    ORDERID, ");
			sqlB.append("    SHOP, ");
			sqlB.append("    LINEID, ");
			sqlB.append("    REFLINEID ) ");
			sqlB.append("VALUES (?,?,?,?); ");
			
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
			SQLiteStatement stOrder = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sqlO.toString());
			SQLiteStatement stLine = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sqlL.toString());
			SQLiteStatement stBundle = MainActivity.getInstance().getDbHelper().getWritableDatabase().compileStatement(sqlB.toString());
			db.beginTransaction();
			
			try {
				for (Order order : orders) {
					stOrder.clearBindings();
					if (order.getId() > 0) {
						stOrder.bindLong(1, order.getId());
					} else {
						stOrder.bindNull(1);
					}
					stOrder.bindString(2, order.getShopId());
					stOrder.bindString(3, AppConfig.getState().getPos() != null ? AppConfig.getState().getPos().getId() : "");
					stOrder.bindString(4, DBHelper.DB_DATETIME_FORMAT.format(order.getDate()));
					stOrder.bindString(5, order.getSalesPersonId());
					if (order.getCustomer() != null) {
						stOrder.bindString(6, order.getCustomer().getId());
					}
					stOrder.bindLong(7, order.getArea());
					stOrder.bindLong(8, order.getTable());
					if (order.getNote() != null) {
						stOrder.bindString(9, order.getNote());
					}
					stOrder.bindString(10, "K");
					stOrder.bindLong(11, order.isUseAlternative() ? 1 : 0);
					stOrder.bindLong(12, order.getId());
					stOrder.bindString(13, order.getAlternativeId());
					long id = stOrder.executeInsert();
					
					db.execSQL("DELETE FROM TMPORDERLINE WHERE ORDERID = ? AND SHOP = ?;", new Object[]{id, order.getShopId()});
					db.execSQL("DELETE FROM TMPORDER_BUNDLE WHERE ORDERID = ? AND SHOP = ?;", new Object[]{id, order.getShopId()});
					
					for (OrderLine line : order.getLines()) {
						long lineId = saveKitchenOrderLine(stLine, order, id, line);
						if (line.getComponents() != null) {
							for (OrderLine cLine : line.getComponents()) {
								long cLineId = saveKitchenOrderLine(stLine, order, id, cLine);
								stBundle.clearBindings();
								stBundle.bindLong(1, id);
								stBundle.bindString(2, order.getShopId());
								stBundle.bindLong(3, lineId);
								stBundle.bindLong(4, cLineId);
								stBundle.executeInsert();
							}
						}
					}
				}
				
				db.setTransactionSuccessful();
				
			} catch (Exception e) {
				ErrorReporter.INSTANCE.filelog(e);
			} finally {
				stOrder.close();
				stLine.close();
				db.endTransaction();
			}
			
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
			throw e;
		}
	}
	
	private static long saveKitchenOrderLine(SQLiteStatement statement, Order order, long orderId, OrderLine line) {
		statement.clearBindings();
		statement.bindLong(1, orderId);
		statement.bindString(2, order.getShopId());
		statement.bindString(3, line.getProduct().getId());
		statement.bindString(4, line.getText());
		statement.bindDouble(5, line.getPrice().toDouble());
		
		if (line.getDiscount() != null) {
			if (line.getDiscount().getReason() != null) {
				statement.bindLong(6, line.getDiscount().getReason().getID());
			}
			statement.bindDouble(7, line.getDiscount().getPercent().toDouble());
		}
		
		statement.bindDouble(8, line.getQuantity().toDouble());
		statement.bindDouble(9, line.getDeliveredQty().toDouble());
		if (line.getNote() != null) {
			statement.bindString(10, line.getNote());
		}
		if (line.getSalesPersonId() != null) {
			statement.bindString(11, line.getSalesPersonId());
		}
		
		return statement.executeInsert();
	}
	
	public static ArrayList<OrderPointer> getParkedOrders(String shopId) {
		ArrayList<OrderPointer> orders = new ArrayList<>();
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("    ORDERHEADER.ID AS ID, ");
			sql.append("    ORDERHEADER.SHOP AS SHOP, ");
			sql.append("    TYPE, ");
			sql.append("    POS, ");
			sql.append("    DATE, ");
			sql.append("    ORDERHEADER.SALESPERSON AS SALESPERSON, ");
			sql.append("    AREAID, ");
			sql.append("    TABLEID, ");
			sql.append("    ORDERHEADER.NOTE AS NOTE, ");
			sql.append("    USE_ALTERNATIVE, ");
			sql.append("    REMOTEID, ");
			sql.append("    ALTERNATIVEID, ");
			sql.append("    SUM(ORDERLINE.PRICE * ORDERLINE.QTY) AS TOTAL ");
			sql.append("FROM ");
			sql.append("	ORDERHEADER ");
			sql.append("INNER JOIN ");
			sql.append("	ORDERLINE ");
			sql.append("ON ");
			sql.append("	ORDERHEADER.ID = ORDERLINE.ORDERID ");
			sql.append("AND ");
			sql.append("	ORDERHEADER.SHOP = ORDERLINE.SHOP ");
			sql.append("WHERE ");
			sql.append("	ORDERHEADER.SHOP = ? ");
			sql.append("AND ORDERHEADER.TYPE = 'P' ");
			sql.append("GROUP BY ");
			sql.append("	ORDERHEADER.SHOP, ");
			sql.append("	ORDERHEADER.ID; ");
			
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), new String[]{shopId});
			
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				do {
					long orderId = rs.getLong(rs.getColumnIndex("ID"));
					
					OrderPointer o = new OrderPointer();
					o.id = orderId;
					o.shopId = rs.getString(rs.getColumnIndex("SHOP"));
					o.type = rs.getString(rs.getColumnIndex("TYPE"));
					o.posNo = rs.getString(rs.getColumnIndex("POS"));
					o.date = DBHelper.DB_DATETIME_FORMAT.parse(rs.getString(rs.getColumnIndex("DATE")));
					o.salesPersonId = rs.getString(rs.getColumnIndex("SALESPERSON"));
					o.alternativeId = rs.getString(rs.getColumnIndex("ALTERNATIVEID"));
					o.areaId = rs.getInt(rs.getColumnIndex("AREAID"));
					o.tableId = rs.getInt(rs.getColumnIndex("TABLEID"));
					o.amount = Decimal.make(rs.getDouble(rs.getColumnIndex("TOTAL")));
					orders.add(o);
					
				} while (rs.moveToNext());
			}
			if (rs != null) {
				rs.close();
			}
			
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}
		
		return orders;
	}
	
	public static ArrayList<OrderPointer> getKitchenOrders() {
		ArrayList<OrderPointer> orders = new ArrayList<>();
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("    TMPORDERHEADER.ID AS ID, ");
			sql.append("    TMPORDERHEADER.SHOP AS SHOP, ");
			sql.append("    TYPE, ");
			sql.append("    POS, ");
			sql.append("    DATE, ");
			sql.append("    TMPORDERHEADER.SALESPERSON AS SALESPERSON, ");
			sql.append("    AREAID, ");
			sql.append("    TABLEID, ");
			sql.append("    TMPORDERHEADER.NOTE AS NOTE, ");
			sql.append("    USE_ALTERNATIVE, ");
			sql.append("    REMOTEID, ");
			sql.append("    ALTERNATIVEID, ");
			sql.append("    SUM(TMPORDERLINE.PRICE * TMPORDERLINE.QTY) AS TOTAL ");
			sql.append("FROM ");
			sql.append("	TMPORDERHEADER ");
			sql.append("INNER JOIN ");
			sql.append("	TMPORDERLINE ");
			sql.append("ON ");
			sql.append("	TMPORDERHEADER.ID = TMPORDERLINE.ORDERID ");
			sql.append("AND ");
			sql.append("	TMPORDERHEADER.SHOP = TMPORDERLINE.SHOP ");
			sql.append("GROUP BY ");
			sql.append("	TMPORDERHEADER.SHOP, ");
			sql.append("	TMPORDERHEADER.ID; ");
			
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), null);
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				do {
					long orderId = rs.getLong(rs.getColumnIndex("ID"));
					
					OrderPointer o = new OrderPointer();
					o.id = orderId;
					o.shopId = rs.getString(rs.getColumnIndex("SHOP"));
					o.type = rs.getString(rs.getColumnIndex("TYPE"));
					o.posNo = rs.getString(rs.getColumnIndex("POS"));
					o.date = DBHelper.DB_DATETIME_FORMAT.parse(rs.getString(rs.getColumnIndex("DATE")));
					o.salesPersonId = rs.getString(rs.getColumnIndex("SALESPERSON"));
					o.alternativeId = rs.getString(rs.getColumnIndex("ALTERNATIVEID"));
					o.areaId = rs.getInt(rs.getColumnIndex("AREAID"));
					o.tableId = rs.getInt(rs.getColumnIndex("TABLEID"));
					o.amount = Decimal.make(rs.getDouble(rs.getColumnIndex("TOTAL")));
					orders.add(o);
					
				} while (rs.moveToNext());
				
			}
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}
		
		return orders;
	}
	
	public static Order getKitchenOrder(String shopId, long orderId) {
		Order o = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("    ID, ");
			sql.append("    SHOP, ");
			sql.append("    TYPE, ");
			sql.append("    POS, ");
			sql.append("    DATE, ");
			sql.append("    SALESPERSON, ");
			sql.append("    AREAID, ");
			sql.append("    TABLEID, ");
			sql.append("    NOTE, ");
			sql.append("    USE_ALTERNATIVE, ");
			sql.append("    REMOTEID, ");
			sql.append("    ALTERNATIVEID ");
			sql.append("FROM ");
			sql.append("	TMPORDERHEADER ");
			sql.append("WHERE ");
			sql.append("	ID = ? ");
			sql.append("AND SHOP = ?;");
			
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), new String[]{"" + orderId, shopId});
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				
				o = new Order();
				o.setId(orderId);
				o.setKitchenOrderId(orderId);
				o.setShopId(rs.getString(rs.getColumnIndex("SHOP")));
				o.setType(rs.getString(rs.getColumnIndex("TYPE")));
				o.setPosNo(rs.getString(rs.getColumnIndex("POS")));
				o.setDate(DBHelper.DB_DATETIME_FORMAT.parse(rs.getString(rs.getColumnIndex("DATE"))));
				o.setSalesPersonId(rs.getString(rs.getColumnIndex("SALESPERSON")));
				o.setAlternativeId(rs.getString(rs.getColumnIndex("ALTERNATIVEID")));
				o.setArea(rs.getInt(rs.getColumnIndex("AREAID")));
				o.setTable(rs.getInt(rs.getColumnIndex("TABLEID")));
				o.setNote(rs.getString(rs.getColumnIndex("NOTE")));
				o.setUseAlternative(rs.getInt(rs.getColumnIndex("USE_ALTERNATIVE")) == 1);
				o.setLines(getKitchenOrderLines(orderId, o.getShopId()));
				
			}
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}
		
		return o;
	}
	
	public static void deleteKitchenOrder(String shopId, long orderId) throws Exception {
		SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
		db.beginTransaction();
		try {
			db.delete("TMPORDERHEADER", "ID = ? AND SHOP = ?", new String[]{"" + orderId, shopId});
			db.delete("TMPORDERLINE", "ORDERID = ? AND SHOP = ?", new String[]{"" + orderId, shopId});
			db.delete("TMPORDER_BUNDLE", "ORDERID = ? AND SHOP = ?", new String[]{"" + orderId, shopId});
			db.setTransactionSuccessful();
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
			throw e;
		} finally {
			db.endTransaction();
		}
	}
	
	private static ArrayList<OrderLine> getKitchenOrderLines(long orderId, String shopId) {
		ArrayList<OrderLine> orderLines = new ArrayList<>();
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("    ID, ");
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
			sql.append("	TMPORDERLINE ");
			sql.append("WHERE ");
			sql.append("	ORDERID = ? ");
			sql.append("AND ");
			sql.append("	SHOP = ?;");
			
			ArrayList<OrderLine> lines = new ArrayList<>();
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), new String[]{"" + orderId, shopId});
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				do {
					OrderLine line = new OrderLine();
					line.setLineId(rs.getLong(rs.getColumnIndex("ID")));
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
			
			HashMap<Long, ArrayList<Long>> map = getKitchenComponentLinesMap(orderId, shopId);
			HashSet<Long> bundleLines = new HashSet<>();
			for (Long parentId : map.keySet()) {
				bundleLines.addAll(map.get(parentId));
			}
			
			for (OrderLine line : lines) {
				if (!bundleLines.contains(line.getLineId())) {
					orderLines.add(line);
				}
				
				if (map.containsKey(line.getLineId())) {
					ArrayList<OrderLine> components = new ArrayList<>();
					ArrayList<Long> lineBundles = map.get(line.getLineId());
					for (OrderLine cLine : lines) {
						if (lineBundles.contains(cLine.getLineId())) {
							components.add(cLine);
						}
					}
					line.setComponents(components);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return orderLines;
	}
	
	private static HashMap<Long, ArrayList<Long>> getKitchenComponentLinesMap(long orderId, String shopId) throws Exception {
		HashMap<Long, ArrayList<Long>> map = new HashMap<>();
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("    ORDERID, ");
			sql.append("    SHOP, ");
			sql.append("    LINEID, ");
			sql.append("    REFLINEID ");
			sql.append("FROM ");
			sql.append("	TMPORDER_BUNDLE ");
			sql.append("WHERE ");
			sql.append("	ORDERID = ? ");
			sql.append("AND ");
			sql.append("	SHOP = ?;");
			
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), new String[]{"" + orderId, shopId});
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				do {
					long lineId = rs.getLong(rs.getColumnIndex("LINEID"));
					long refLineId = rs.getLong(rs.getColumnIndex("REFLINEID"));
					
					ArrayList<Long> componentLines = new ArrayList<>();
					if (map.containsKey(lineId)) {
						componentLines = map.get(lineId);
					}
					componentLines.add(refLineId);
					map.put(lineId, componentLines);
				} while (rs.moveToNext());
			}
			if (rs != null) {
				rs.close();
			}
			
		} catch (Exception e) {
			throw e;
		}
		
		return map;
	}

	public static void mergeKithenOrders(String shopId, int fromTable, int fromArea, int toTable, int toArea) {
		SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put("AREAID", toArea);
			cv.put("TABLEID", toTable);
			db.update("TMPORDERHEADER", cv, "SHOP = ? AND AREAID = ? AND TABLEID = ?", new String[]{shopId, "" + fromArea, "" + fromTable});
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
			throw e;
		} finally {
			db.endTransaction();
		}
	}
	
	public static ArrayList<Product> getBundleComponents(String productId) {
		ArrayList<Product> productResults = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			sql.append("	PRODUCT.ID, ");
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
			sql.append("	TARE, ");
			sql.append("	MISC ");
			sql.append("FROM ");
			sql.append("	PRODUCT ");
			sql.append("INNER JOIN ");
			sql.append("	PRODUCT_BUNDLE ");
			sql.append("ON ");
			sql.append("	PRODUCT_BUNDLE.PRODUCT_ID = '" + productId + "' ");
			sql.append("AND	PRODUCT_BUNDLE.REF_ID = PRODUCT.ID; ");
			
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), null);
			
			if (rs != null && rs.getCount() > 0) {
				productResults = new ArrayList<>();
				rs.moveToFirst();
				do {
					Product product = new Product();
					product.setId(rs.getString(rs.getColumnIndex("ID")));
					product.setName(rs.getString(rs.getColumnIndex("NAME")));
					product.setBarcode(rs.getString(rs.getColumnIndex("BARCODE")));
					product.setDescription(rs.getString(rs.getColumnIndex("DESCRIPTION")));
					product.setType(rs.getString(rs.getColumnIndex("TYPE")));
					product.setCost(Decimal.make(rs.getDouble(rs.getColumnIndex("COST"))));
					product.setPrice(Decimal.make(rs.getDouble(rs.getColumnIndex("PRICE"))));
					product.setStockQty(Decimal.make(rs.getDouble(rs.getColumnIndex("STOCK"))));
					product.setAbcCode(rs.getString(rs.getColumnIndex("ABCCODE")));
					product.setVat(rs.getDouble(rs.getColumnIndex("VAT")));
					product.setUseAlternative(rs.getInt(rs.getColumnIndex("USE_ALTERNATIVE")) == 1);
					product.setAlternativePrice(Decimal.make(rs.getDouble(rs.getColumnIndex("ALTERNATIVE_PRICE"))));
					product.setAlternativeVat(rs.getDouble(rs.getColumnIndex("ALTERNATIVE_VAT")));
					product.setTare(rs.getInt(rs.getColumnIndex("TARE")));
					product.setMiscellaneous(rs.getInt(rs.getColumnIndex("MISC")) == 1);
					productResults.add(product);
				} while (rs.moveToNext());
			}
			if (rs != null) {
				rs.close();
			}
			
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog("ProductSearchOfflineAsync", "", e);
		}
		
		return productResults;
	}
	
	public static String saveReconciliation(String text) {
		
		String id = null;
		double amount = 0.0;
		try {
			int startIndex = text.toUpperCase().lastIndexOf("TOTAL") + 6;
			int lastIndex = text.toUpperCase().indexOf("KORTAVTALER");
			amount = Double.parseDouble(text.substring(startIndex, lastIndex).trim().replace(',','.'));
		} catch (NumberFormatException e) {
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT ");
		sql.append("INTO ");
		sql.append("    RECONCILIATION ");
		sql.append("( ");
		sql.append("    ID, ");
		sql.append("    SHOP, ");
		sql.append("    CONTENT, ");
		sql.append("    AMOUNT ) ");
		sql.append("VALUES (?,?,?,?); ");
		
		SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
		SQLiteStatement st = db.compileStatement(sql.toString());
		db.beginTransaction();
		
		try {
			id = DBHelper.DB_TIMESTAMP_FORMAT.format(new Date());
			st.clearBindings();
			st.bindString(1, id);
			st.bindString(2, AccountManager.INSTANCE.getAccount().getShop().getID());
			st.bindString(3, text);
			st.bindDouble(4, Decimal.make(amount).toDouble());
			st.executeInsert();
			
			ContentValues cv = new ContentValues();
			cv.put("RECONCILIATION_ID", id);
			db.update("ORDERHEADER", cv, "SHOP = ? AND TYPE = 'O' AND RECONCILIATION_ID IS NULL", new String[]{AccountManager.INSTANCE.getAccount().getShop().getID()});
			
			db.setTransactionSuccessful();
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
			throw e;
		} finally {
			st.close();
			db.endTransaction();
		}
		
		return id;
	}
	
	public static Reconciliation getReconciliation(String shopId, String id) {
		Reconciliation r = null;
		Cursor rs = null;
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
			sql.append("	ID = ? ");
			sql.append("AND SHOP = ?;");
			
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
			rs = db.rawQuery(sql.toString(), new String[]{id, shopId});
			
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				r = new Reconciliation();
				r.id = rs.getString(rs.getColumnIndex("ID"));
				r.shopId = rs.getString(rs.getColumnIndex("SHOP"));
				r.content = rs.getString(rs.getColumnIndex("CONTENT"));
				r.amount = rs.getDouble(rs.getColumnIndex("AMOUNT"));
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		
		if (r != null) {
			ArrayList<Long> reconciliationOrders = new ArrayList<>();
			rs = null;
			try {
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT ");
				sql.append("    REMOTEID ");
				sql.append("FROM ");
				sql.append("	ORDERHEADER ");
				sql.append("WHERE ");
				sql.append("	SHOP = ? ");
				sql.append("AND TYPE = 'O' ");
				sql.append("AND REMOTEID <> 0 ");
				sql.append("AND RECONCILIATION_ID = ?;");
				
				SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
				rs = db.rawQuery(sql.toString(), new String[]{r.shopId, r.id});
				
				if (rs != null && rs.getCount() > 0) {
					rs.moveToFirst();
					do {
						reconciliationOrders.add(rs.getLong(rs.getColumnIndex("REMOTEID")));
					} while (rs.moveToNext());
					
					r.orders = reconciliationOrders;
				}
			} catch (Exception e) {
				ErrorReporter.INSTANCE.filelog(e);
			} finally {
				if (rs != null) {
					rs.close();
				}
			}
		}
		
		return r;
	}

	public static void markReconciliationAsSent(String shopId, String id) throws Exception {
		SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
		try {
			ContentValues cv = new ContentValues();
			cv.put("IS_SENT", 1);
			db.update("RECONCILIATION", cv, "ID = ? AND SHOP = ?", new String[]{id, shopId});
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
			throw e;
		}
	}
	
}
