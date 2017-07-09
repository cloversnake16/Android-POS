package no.susoft.mobile.pos.server;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class ProductLoadByIDOfflineAsync extends AsyncTask<String, Void, String> {

	private Product p = null;
	private String price = null;
	private boolean returnToPaymentFragment = false;

	@Override
	protected String doInBackground(String... id) {

		try {
			if (id.length == 2 && id[1] != null)
				price = id[1];

			if(id.length == 3) {
				price = id[1];
				returnToPaymentFragment = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			p = null;

			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
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
			sql.append("	TARE, ");
			sql.append("	MISC ");
			sql.append("FROM ");
			sql.append("	PRODUCT ");
			sql.append("WHERE ");
			sql.append("	ID = ?; ");

			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), new String[]{id[0]});
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				p = new Product();
				p.setId(rs.getString(rs.getColumnIndex("ID")));
				p.setName(rs.getString(rs.getColumnIndex("NAME")));
				p.setBarcode(rs.getString(rs.getColumnIndex("BARCODE")));
				p.setDescription(rs.getString(rs.getColumnIndex("DESCRIPTION")));
				p.setType(rs.getString(rs.getColumnIndex("TYPE")));
				p.setCost(Decimal.make(rs.getDouble(rs.getColumnIndex("COST"))));
				p.setPrice(Decimal.make(rs.getDouble(rs.getColumnIndex("PRICE"))));
				p.setStockQty(Decimal.make(rs.getDouble(rs.getColumnIndex("STOCK"))));
				p.setAbcCode(rs.getString(rs.getColumnIndex("ABCCODE")));
				p.setVat(rs.getDouble(rs.getColumnIndex("VAT")));
				p.setUseAlternative(rs.getInt(rs.getColumnIndex("USE_ALTERNATIVE")) == 1);
				p.setAlternativePrice(Decimal.make(rs.getDouble(rs.getColumnIndex("ALTERNATIVE_PRICE"))));
				p.setAlternativeVat(rs.getDouble(rs.getColumnIndex("ALTERNATIVE_VAT")));
				p.setTare(rs.getInt(rs.getColumnIndex("TARE")));
				p.setMiscellaneous(rs.getInt(rs.getColumnIndex("MISC")) == 1);

				if (p.isBundle()) {
					p.setComponents(DbAPI.getBundleComponents(p.getId()));
				}
			}
			if (rs != null) {
				rs.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	@Override
	protected void onPostExecute(String s) {
		super.onPostExecute(s);
		if(returnToPaymentFragment) {
			try {
				if (p != null) {
					MainActivity.getInstance().getNumpadPayFragment().setRoundingProduct(p);
				} else {
					Product product = new Product();
					product.setBarcode(Order.roundId.toUpperCase());
					product.setCost(Decimal.ZERO);
					product.setDescription("Avrunding av ører, skal ikke brukes i salg");
					product.setDiscount(null);
					product.setId(Order.roundId);
					product.setType("0");
					product.setName("Øreavrunding");
					product.setPrice(Decimal.ZERO);
					product.setMiscellaneous(false);
					product.setVat(0.00);
					MainActivity.getInstance().getNumpadPayFragment().setRoundingProduct(product);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return;
		}
		if(price != null) {
			MainActivity.getInstance().getServerCallMethods().productLoadByIDAsyncPostExecute(p, price);
		} else {
			MainActivity.getInstance().getServerCallMethods().productLoadByIDAsyncPostExecute(p);
		}

	}
}

