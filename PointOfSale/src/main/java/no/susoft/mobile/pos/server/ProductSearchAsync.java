package no.susoft.mobile.pos.server;

import java.util.ArrayList;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class ProductSearchAsync extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... term) {
		ErrorReporter.INSTANCE.filelog("ProductSearchAsync doInBackground..");
        try {
            ArrayList<Product> productResults = new ArrayList<>();
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendOperation(Protocol.OperationCode.REQUEST_SEARCH);
            request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
            request.appendEntity(Protocol.SearchEntity.PRODUCT);
            request.appendConstraint(Protocol.SearchConstraint.TERM);
            request.appendParameter(Protocol.Parameters.TERM, term[0]);
			if (Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().getCustomer() != null) {
				request.appendParameter(Parameters.CUSTOMER, Cart.INSTANCE.getOrder().getCustomer().getId());
			}
			
            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(json).getAsJsonArray();
                if (array != null) {
                    for (JsonElement element : array) {
                        Product p = gson.fromJson(element, Product.class);
                        productResults.add(p);
                    }
                }
            }

            MainActivity.getInstance().getMainShell().setProductResults(productResults);
        } catch (Exception e) {
            ErrorReporter.INSTANCE.filelog("ProductSearchAsync", "", e);
        }

        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        MainActivity.getInstance().getServerCallMethods().productSearchAsyncPostExecute();
    }

}


