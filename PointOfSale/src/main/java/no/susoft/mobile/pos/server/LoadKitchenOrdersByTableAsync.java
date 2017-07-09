package no.susoft.mobile.pos.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import no.susoft.mobile.pos.data.*;
import no.susoft.mobile.pos.db.DBHelper;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class LoadKitchenOrdersByTableAsync extends AsyncTask<String, Void, String> {

    ArrayList<Order> orderResults = new ArrayList<>();
    int table;
    int area;

    @Override
    protected String doInBackground(String... params) {

        area = Integer.valueOf(params[0]);
        table = Integer.valueOf(params[1]);

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
			sql.append("    USE_ALTERNATIVE, ");
			sql.append("    REMOTEID, ");
			sql.append("    ALTERNATIVEID ");
			sql.append("FROM ");
			sql.append("	TMPORDERHEADER ");
			sql.append("WHERE ");
			sql.append("	TMPORDERHEADER.AREAID = ? ");
			sql.append("AND	TMPORDERHEADER.TABLEID = ?; ");
			
			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), new String[]{"" + area, "" + table});

			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				do {
					long orderId = rs.getLong(rs.getColumnIndex("ID"));

					Order o = new Order();
					o.setId(orderId);
					o.setShopId(rs.getString(rs.getColumnIndex("SHOP")));
					o.setType(rs.getString(rs.getColumnIndex("TYPE")));
					o.setPosNo(rs.getString(rs.getColumnIndex("POS")));
					o.setDate(DBHelper.DB_DATETIME_FORMAT.parse(rs.getString(rs.getColumnIndex("DATE"))));
					o.setSalesPersonId(rs.getString(rs.getColumnIndex("SALESPERSON")));
					o.setAlternativeId(rs.getString(rs.getColumnIndex("ALTERNATIVEID")));
					o.setCustomer(getCustomer(rs.getString(rs.getColumnIndex("CUSTOMERID"))));
					o.setArea(rs.getInt(rs.getColumnIndex("AREAID")));
					o.setTable(rs.getInt(rs.getColumnIndex("TABLEID")));
					o.setNote(rs.getString(rs.getColumnIndex("NOTE")));
					o.setUseAlternative(rs.getInt(rs.getColumnIndex("USE_ALTERNATIVE")) == 1);
					o.setKitchenOrderId(orderId);
					
					System.out.println("o.getKitchenOrderId() = " + o.getKitchenOrderId());
					
					o.setLines(getOrderLines(orderId, o.getShopId()));
					orderResults.add(o);

				} while (rs.moveToNext());
			}
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}

        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        MainActivity.getInstance().getServerCallMethods().ordersLoadByTableAsyncPostExecute(orderResults, area, table);
    }

	private ArrayList<OrderLine> getOrderLines(long orderId, String shopId) {
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
					Product product = getProduct(rs.getString(rs.getColumnIndex("PRODUCT")));
					if (product != null) {
						OrderLine line = new OrderLine();
						line.setLineId(rs.getLong(rs.getColumnIndex("ID")));
						line.setOrderId(orderId);
						line.setShopId(shopId);
						line.setProduct(product);
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
					}
				} while (rs.moveToNext());
			}
			if (rs != null) {
				rs.close();
			}

			HashMap<Long, ArrayList<Long>> map = getComponentLinesMap(orderId, shopId);
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
				if (product.isBundle()) {
					product.setComponents(DbAPI.getBundleComponents(product.getId()));
				}
			}
			if (rs != null) {
				rs.close();
			}

		} catch (Exception e) {
			throw e;
		}

		return product;
	}

	private HashMap<Long, ArrayList<Long>> getComponentLinesMap(long orderId, String shopId) throws Exception {
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
				e.printStackTrace();
			}
		}

        return c;
    }

}