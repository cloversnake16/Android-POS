package no.susoft.mobile.pos.server;

import java.util.ArrayList;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.OrderPointer;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol.*;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.dialog.OrderSearchDialog;

public class SearchCompleteOrdersAsync extends AsyncTask<String, Void, String> {

	ArrayList<OrderPointer> orderResults = null;
    private OrderSearchDialog returnClass;

    @Override
    protected String doInBackground(String... term) {
        try {
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(State.AUTHORIZED);
            request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendOperation(OperationCode.REQUEST_SEARCH);
            request.appendParameter(Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
            request.appendEntity(SearchEntity.ORDER);
            request.appendConstraint(SearchConstraint.TERM);
			request.appendParameter(Parameters.ID, term[0]);
			request.appendParameter(Parameters.FROM_DATE, term[1]);
			request.appendParameter(Parameters.TO_DATE, term[2]);
			request.appendParameter(Parameters.FROM_AMOUNT, term[3]);
			request.appendParameter(Parameters.TO_AMOUNT, term[4]);
			request.appendParameter(Parameters.CUSTOMER, term[5]);
	
            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(json).getAsJsonArray();
                if (array != null) {
					orderResults = new ArrayList<>();
                    for (JsonElement element : array) {
                        OrderPointer o = gson.fromJson(element, OrderPointer.class);
                        orderResults.add(o);
                    }
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
		if (returnClass != null) {
			MainActivity.getInstance().getServerCallMethods().getOrderSearchAsyncPostExecute(orderResults, returnClass);
		}
    }

    public void setReturnClass(OrderSearchDialog returnClass) {
        this.returnClass = returnClass;
    }
}