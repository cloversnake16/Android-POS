package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class ProductLoadByBarcodeAsync extends AsyncTask<String, Void, String> {

    Product p = null;
	String productBarcode = null;

    @Override
    protected String doInBackground(String... barcode) {

		productBarcode = barcode[0];
        try {
            p = null;
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendOperation(Protocol.OperationCode.REQUEST_SEARCH);
            request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
            request.appendEntity(Protocol.SearchEntity.PRODUCT);
            request.appendConstraint(Protocol.SearchConstraint.BARCODE);
            request.appendParameter(Protocol.Parameters.BARCODE, barcode[0]);
			if (Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().getCustomer() != null) {
				request.appendParameter(Parameters.CUSTOMER, Cart.INSTANCE.getOrder().getCustomer().getId());
			}

            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(json);
                if (element != null) {
                    p = gson.fromJson(element, Product.class);
                }
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
			MainActivity.getInstance().getServerCallMethods().productLoadByBarcodeAsyncPostExecute(p);
		} else {
			new ProductLoadByBarcodeOfflineAsync().execute(productBarcode);
		}
    }
}


