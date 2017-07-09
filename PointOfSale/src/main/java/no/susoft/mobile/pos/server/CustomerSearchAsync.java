package no.susoft.mobile.pos.server;

import java.util.ArrayList;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Customer;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class CustomerSearchAsync extends AsyncTask<String, Void, String> {

	String termString;

    @Override
    protected String doInBackground(String... term) {
		termString = term[0];
        try {
            ArrayList<Customer> customerResults = new ArrayList<>();
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendOperation(Protocol.OperationCode.REQUEST_SEARCH);
            request.appendEntity(Protocol.SearchEntity.CUSTOMER);
            request.appendConstraint(Protocol.SearchConstraint.TERM);
            request.appendParameter(Protocol.Parameters.TERM, term[0]);
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

            MainActivity.getInstance().getMainShell().setCustomerResults(customerResults);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
		if (MainActivity.getInstance().isConnected()) {
			MainActivity.getInstance().getServerCallMethods().customerSearchAsyncPostExecute();
		} else {
			new CustomerSearchOfflineAsync().execute(termString);
		}

    }
}

