package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class ProductLoadByIDAsync extends AsyncTask<String, Void, String> {

	private Product p = null;
	private String productId = null;
	private String price = null;
	private boolean returnToPaymentFragment = false;

	@Override
	protected String doInBackground(String... id) {

		productId = id[0];
		try {
			if (id.length == 2 && id[1] != null)
				price = id[1];

			if (id.length == 3) {
				price = id[1];
				returnToPaymentFragment = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			p = null;
			Request request = Server.INSTANCE.getEncryptedPreparedRequest();
			request.appendState(Protocol.State.AUTHORIZED);
			request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
			request.appendOperation(Protocol.OperationCode.REQUEST_SEARCH);
			request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
			request.appendEntity(Protocol.SearchEntity.PRODUCT);
			request.appendConstraint(Protocol.SearchConstraint.ID);
			request.appendParameter(Protocol.Parameters.ID, id[0]);
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
			if (returnToPaymentFragment) {
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
			if (price != null) {
				MainActivity.getInstance().getServerCallMethods().productLoadByIDAsyncPostExecute(p, price);
			} else {
				MainActivity.getInstance().getServerCallMethods().productLoadByIDAsyncPostExecute(p);
			}
		} else {
			new ProductLoadByIDOfflineAsync().execute(productId);
		}

	}
}

