package no.susoft.mobile.pos.server;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import no.susoft.mobile.pos.data.Customer;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class CustomerSearchOfflineAsync extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... term) {

        try {
            ArrayList<Customer> results = new ArrayList<>();
			if (term != null && term.length > 0) {
				String[] words = term[0].split(" ");

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
				sql.append("	ID <> '' ");

				for (String word : words) {
					word = word.trim();

					sql.append("AND (");
					sql.append("        ID LIKE '%" + word.toUpperCase() + "%' ");
					sql.append("    OR  FIRSTNAME LIKE '%" + word.toUpperCase() + "%' ");
					sql.append("    OR  LASTNAME LIKE '%" + word.toUpperCase() + "%' ");
					sql.append("    OR  EMAIL LIKE '%" + word.toUpperCase() + "%' ");
					sql.append("    OR  MOBILE LIKE '%" + word.toUpperCase() + "%' ");
					sql.append("    OR  PHONE LIKE '%" + word.toUpperCase() + "%') ");
				}

				sql.append("ORDER BY ID ");
				sql.append("LIMIT 10;");

				SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
				Cursor rs = db.rawQuery(sql.toString(), null);
				if (rs != null && rs.getCount() > 0) {
					rs.moveToFirst();
					do {
						Customer c = new Customer();
						c.setId(rs.getString(rs.getColumnIndex("ID")));
						c.setFirstName(rs.getString(rs.getColumnIndex("FIRSTNAME")));
						c.setLastName(rs.getString(rs.getColumnIndex("LASTNAME")));
						c.setEmail(rs.getString(rs.getColumnIndex("EMAIL")));
						c.setPhone(rs.getString(rs.getColumnIndex("PHONE")));
						c.setMobile(rs.getString(rs.getColumnIndex("MOBILE")));
						c.setCompany(rs.getInt(rs.getColumnIndex("COMPANY")) == 1);
						results.add(c);
					} while (rs.moveToNext());
				}
				if (rs != null) {
					rs.close();
				}
			}

            MainActivity.getInstance().getMainShell().setCustomerResults(results);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        MainActivity.getInstance().getServerCallMethods().customerSearchAsyncPostExecute();
    }

}