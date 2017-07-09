package no.susoft.mobile.pos.server;

import java.util.ArrayList;

import android.os.AsyncTask;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.OrderLine;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.data.SynchronizeTransport;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class ReloadProductsAsync extends AsyncTask<String, Void, String> {

    ArrayList<Product> products = new ArrayList<>();

    @Override
    protected String doInBackground(String... barcode) {

        try {
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendOperation(Protocol.OperationCode.REQUEST_RELOAD_PRODUCTS);
            request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
			if (Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().getCustomer() != null) {
				request.appendParameter(Parameters.CUSTOMER, Cart.INSTANCE.getOrder().getCustomer().getId());
			}
	
			ArrayList<String> productIds = new ArrayList<>();
			for (OrderLine line : Cart.INSTANCE.getOrder().getLines()) {
				if (!line.getProduct().isMiscellaneous()) {
					productIds.add(line.getProduct().getId());
				}
			}
			
            final String json = JSONFactory.INSTANCE.getFactory().toJson(productIds);
            final StringBody file = new StringBody(json, ContentType.APPLICATION_JSON);
            final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("file", file);

            SynchronizeTransport result = Server.INSTANCE.doPost(SynchronizeTransport.class, request, entity, JSONFactory.INSTANCE.getFactory());
			if (result != null && result.getProducts() != null) {
				products = result.getProducts();
			}
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
		for (Product product : products) {
			for (OrderLine line : Cart.INSTANCE.getOrder().getLines()) {
				if (product.getId().equals(line.getProduct().getId())) {
					line.setProduct(product);
					line.setText(product.getName());
					if (Cart.INSTANCE.getOrder().isUseAlternative() && product.isUseAlternative()) {
						line.setPrice(product.getAlternativePrice());
					} else {
						line.setPrice(product.getPrice());
					}
					if (product.getDiscount() != null) {
						line.setDiscount(product.getDiscount());
					}
				}
			}
		}
		MainActivity.getInstance().getCartFragment().refreshCart();
    }
}


