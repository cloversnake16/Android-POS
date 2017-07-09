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
import no.susoft.mobile.pos.network.Protocol.*;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.dialog.SupplyReportDialog;

public class SupplyReportAsync extends AsyncTask<String, Void, String> {

	ArrayList<Product> results = null;
    private SupplyReportDialog returnClass;

    @Override
    protected String doInBackground(String... dates) {
        try {
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(State.AUTHORIZED);
            request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendOperation(OperationCode.REQUEST_SUPPLY_REPORT);
            request.appendParameter(Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
            request.appendParameter(Parameters.FROM_DATE, dates[0]);
            request.appendParameter(Parameters.TO_DATE, dates[1]);

            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
	
			ErrorReporter.INSTANCE.filelog("SUPPLY REPORT JSON = " + json);
			
			if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(json).getAsJsonArray();
                if (array != null) {
					results = new ArrayList<>();
                    for (JsonElement element : array) {
                        Product p = gson.fromJson(element, Product.class);
                        results.add(p);
						
						ErrorReporter.INSTANCE.filelog("Product = " + p.getName());
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
		
		ErrorReporter.INSTANCE.filelog("returnClass = " + returnClass);
		if (returnClass != null) {
			MainActivity.getInstance().getServerCallMethods().getSupplyReportAsyncPostExecute(results, returnClass);
		}
    }

    public void setReturnClass(SupplyReportDialog returnClass) {
        this.returnClass = returnClass;
    }
}
