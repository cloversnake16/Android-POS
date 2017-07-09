package no.susoft.mobile.pos.ui.activity.util;

import java.util.ArrayList;
import java.util.Properties;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.DialogFragment;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Customer;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.utils.UpdateCustomerSearchAdapter;
import no.susoft.mobile.pos.ui.adapter.utils.UpdateProductSearchAdapter;
import no.susoft.mobile.pos.ui.dialog.CustomerNotesDialog;
import no.susoft.mobile.pos.ui.dialog.CustomerSearchDialog;

public class MainShell {

    MainActivity main;
    private ArrayList<Product> productResults;
    private ArrayList<Customer> customerResults;
    private DialogFragment customerSearchDialog;
    private ArrayList<Properties> salesPersons;

    public MainShell(MainActivity main) {

        this.main = main;
    }

    /**
     * Sets the account menu bar as well as the top menu bar. Called when returning from login activity
     */
    public void setupUIElements() {
        if (!AccountManager.INSTANCE.getLoggedInAccounts().isEmpty()) {
            AccountBar.getInstance().setupAccountMenuBar();
        }
        MainTopBarMenu.getInstance().setupTopMenuBar();
		if (main.isConnected()) {
			main.getServerCallMethods().loadDiscountReasons();
		}
    }

    public DialogFragment getCustomerSearchDialog() {
        return customerSearchDialog;
    }

    public void showCustomerSearchDialog() {
        customerSearchDialog = new CustomerSearchDialog();
        customerSearchDialog.show(main.getSupportFragmentManager(), "CustomerSearchDialog");
    }

    public void showCustomerNotesDialog() {
        CustomerNotesDialog notesDialog = new CustomerNotesDialog();
        notesDialog.show(main.getSupportFragmentManager(), "CustomerNotesDialog");
    }

    public void updateProductSearchAdapter() {
        if (productResults != null) {
            new UpdateProductSearchAdapter(productResults, MainActivity.getInstance().getProductSearchFragment());
        }
    }

    public void updateCustomerSearchAdapter() {
        if (customerResults != null && customerSearchDialog != null) {
            new UpdateCustomerSearchAdapter(customerResults, customerSearchDialog);
        }
    }

    public void updateOrderSearchAdapter() {
        main.getOrdersFragment().refreshAdapter(main.getOrderResults());
    }

	public ArrayList<Properties> loadSalesPersons() {

		ArrayList<Properties> data = new ArrayList<>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("	ID, ");
		sql.append("	NAME ");
		sql.append("FROM ");
		sql.append("	ACCOUNT ");
		sql.append("WHERE ");
		sql.append("	SHOP = ? ");
		sql.append("ORDER BY ");
		sql.append("	NAME ASC, ");
		sql.append("	ID ASC; ");

		SQLiteDatabase db = main.getDbHelper().getReadableDatabase();
		try {
			Cursor rs = db.rawQuery(sql.toString(), new String[]{AccountManager.INSTANCE.getAccount().getShop().getID()});
			if (rs != null && rs.getCount() > 0) {
				rs.moveToFirst();
				do {
					Properties p = new Properties();
					p.put("id", rs.getString(rs.getColumnIndex("ID")));
					p.put("name", rs.getString(rs.getColumnIndex("NAME")));
					data.add(p);
				} while (rs.moveToNext());
			}
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		salesPersons = data;
		return data;
	}

    public void setProductResults(ArrayList<Product> productResults) {
        this.productResults = productResults;
    }

    public void setCustomerResults(ArrayList<Customer> customerResults) {
        this.customerResults = customerResults;
    }

	public ArrayList<Properties> getSalesPersons() {
		return salesPersons;
	}
}
