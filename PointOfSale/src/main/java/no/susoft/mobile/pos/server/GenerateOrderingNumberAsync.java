package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.response.StatusResponse;

public class GenerateOrderingNumberAsync extends AsyncTask<Order, Void, StatusResponse> {
	
	Order order = null;
	
	@Override
	protected StatusResponse doInBackground(Order... order) {

		StatusResponse response = null;
		this.order = order[0];
		
		try {
			Request request = Server.INSTANCE.getEncryptedPreparedRequest();
			request.appendState(Protocol.State.AUTHORIZED);
			request.appendOperation(OperationCode.REQUEST_GENERATE_NUMBER);
			request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
			request.appendParameter(Protocol.Parameters.SHOP, this.order.getShopId());
			request.appendParameter(Protocol.Parameters.TYPE, "P");
			
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(json);
                if (element != null) {
                    response = gson.fromJson(element, StatusResponse.class);
                }
            }

		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}
}
