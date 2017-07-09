package no.susoft.mobile.pos.server;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class ProductSearchOfflineAsync extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... term) {
		ErrorReporter.INSTANCE.filelog("ProductSearchOfflineAsync doInBackground..");
        try {
            ArrayList<Product> productResults = new ArrayList<>();
			if (term != null && term.length > 0) {
				String[] words = term[0].split(" ");

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
				sql.append("	ID <> '' ");

				for (String word : words) {
					word = word.trim();

					sql.append("AND (");
					sql.append("        ID LIKE '%" + word.toUpperCase() + "%' ");
					sql.append("    OR  NAME LIKE '%" + word.toUpperCase() + "%' ");
					sql.append("    OR  BARCODE LIKE '%" + word.toUpperCase() + "%' ");
					sql.append("    OR  DESCRIPTION LIKE '%" + word.toUpperCase() + "%') ");
				}

				sql.append("ORDER BY NAME ");
				sql.append("LIMIT 10;");

				SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
				Cursor rs = db.rawQuery(sql.toString(), null);
				if (rs != null && rs.getCount() > 0) {
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
						if (product.isBundle()) {
							product.setComponents(DbAPI.getBundleComponents(product.getId()));
						}

						productResults.add(product);
					} while (rs.moveToNext());
				}
				if (rs != null) {
					rs.close();
				}
			}

            MainActivity.getInstance().getMainShell().setProductResults(productResults);
        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("ProductSearchOfflineAsync", "", e);
        }

        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        MainActivity.getInstance().getServerCallMethods().productSearchAsyncPostExecute();
    }

}


