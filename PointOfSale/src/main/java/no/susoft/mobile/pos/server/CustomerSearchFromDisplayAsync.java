package no.susoft.mobile.pos.server;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Customer;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class CustomerSearchFromDisplayAsync extends AsyncTask<String, Void, String> {

	String phone = "";
	String email = "";
	Customer c = null;

    @Override
    protected String doInBackground(String... term) {
		String phone = term[0];
		String email = term[1];

		String termString;
		if (!phone.isEmpty()) {
			termString = phone;
		} else {
			termString = email;
		}

        try {
            ArrayList<Customer> customerResults = new ArrayList<>();
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendOperation(Protocol.OperationCode.REQUEST_SEARCH);
            request.appendEntity(Protocol.SearchEntity.CUSTOMER);
            request.appendConstraint(Protocol.SearchConstraint.TERM);
            request.appendParameter(Protocol.Parameters.TERM, termString);
            request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(json).getAsJsonArray();
                if (array != null) {
                    for (JsonElement element : array) {
                        Customer c = gson.fromJson(element, Customer.class);
                        customerResults.add(c);
                    }
                }
            }

			if (customerResults.size() > 0) {
				c = customerResults.get(0);
			}

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
		if (MainActivity.getInstance().isConnected()) {
			if (c != null) {
                Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.customer_added), Toast.LENGTH_SHORT).show();
                Cart.INSTANCE.setOrderCustomer(c);
				MainActivity.getInstance().sendCustomerToSecondaryDisplay(c);
			} else {
				if (!phone.isEmpty() || !email.isEmpty()) {
					Customer customer = new Customer();
					if (!phone.isEmpty()) {
						customer.setFirstName(phone);
						customer.setMobile(phone);
					} else {
						customer.setEmail(email);
					}
					new CreateNewCustomer().execute(customer);
				}
			}
		}
    }
}

