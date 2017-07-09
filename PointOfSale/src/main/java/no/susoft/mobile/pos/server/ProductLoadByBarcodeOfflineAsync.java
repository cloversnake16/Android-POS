package no.susoft.mobile.pos.server;

import java.math.BigDecimal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class ProductLoadByBarcodeOfflineAsync extends AsyncTask<String, Void, String> {

    Product p = null;

    @Override
    protected String doInBackground(String... barcode) {

		// 2054931281777
		// price = 81.77
		String code = barcode[0];
		// checking if barcode is EAN with weights
		if (code != null && code.length() == 13 && (code.startsWith("20") || code.startsWith("21") || code.startsWith("22") || code.startsWith("23") || code.startsWith("24") || code.startsWith("25"))) {
			Product product = loadProductFromDatabase(code.substring(0, 8));
			if (product != null && product.isWeighted()) {
				p = product;

				String leadingCode = code.substring(0, 2);
				String value = code.substring(8, 12);
			
				switch (leadingCode) {
					case "20":
						double price = Double.parseDouble(value);
						price = price * 0.01;
						if (!Cart.INSTANCE.getProductPrice(p).isZero()) {
							BigDecimal weight = new BigDecimal(price / Cart.INSTANCE.getProductPrice(p).toDouble());
							p.setWeight(Decimal.make(weight, 3));
						}
						break;
					case "21":
						price = Double.parseDouble(value);
						price = price * 0.1;
						if (!Cart.INSTANCE.getProductPrice(p).isZero()) {
							BigDecimal weight = new BigDecimal(price / Cart.INSTANCE.getProductPrice(p).toDouble());
							p.setWeight(Decimal.make(weight, 3));
						}
						break;
					case "22":
						price = Double.parseDouble(value);
						if (!Cart.INSTANCE.getProductPrice(p).isZero()) {
							BigDecimal weight = new BigDecimal(price / Cart.INSTANCE.getProductPrice(p).toDouble());
							p.setWeight(Decimal.make(weight, 3));
						}
						break;
					case "23":
						double weight = Double.parseDouble(value);
						weight = weight * 0.001;
						p.setWeight(Decimal.make(new BigDecimal(weight), 3));
						break;
					case "24":
						weight = Double.parseDouble(value);
						weight = weight * 0.01;
						p.setWeight(Decimal.make(new BigDecimal(weight), 3));
						break;
					case "25":
						weight = Double.parseDouble(value);
						weight = weight * 0.1;
						p.setWeight(Decimal.make(new BigDecimal(weight), 3));
						break;
				}
			}
		}

		if (p == null) {
			p = loadProductFromDatabase(barcode[0]);
		}

        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        MainActivity.getInstance().getServerCallMethods().productLoadByBarcodeAsyncPostExecute(p);
    }

	private Product loadProductFromDatabase(String barcode) {
		Product p = null;
        try {
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
			sql.append("	BARCODE = ?; ");

			SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
			Cursor rs = db.rawQuery(sql.toString(), new String[]{barcode});
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
				p.setVat(rs.getDouble(rs.getColumnIndex("VAT")));
				p.setAbcCode(rs.getString(rs.getColumnIndex("ABCCODE")));
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

		return p;
	}
}


