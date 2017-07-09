package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol.*;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class GetPrepaidsAsync extends AsyncTask<String, Void, String> {

	List<Prepaid> prepaidResults = null;

    @Override
    protected String doInBackground(String... term) {
        try {
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(State.AUTHORIZED);
            request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendOperation(OperationCode.REQUEST_FIND_PREPAID);
            request.appendParameter(Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
            request.appendParameter(Parameters.FROM_DATE, term[0]);
            request.appendParameter(Parameters.TO_DATE, term[1]);

            //request.appendEntity(SearchEntity.PREPAID);

            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Log.i("vilde", "json wasnt null");
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(json).getAsJsonArray();
                if (array != null) {
                    Log.i("vilde", "array wasnt null");
                    prepaidResults = new ArrayList<>();
                    for (JsonElement element : array) {
                        Prepaid p = gson.fromJson(element, Prepaid.class);
                        prepaidResults.add(p);
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
        MainActivity.getInstance().getServerCallMethods().getPrepaidsAsyncPostExecute(prepaidResults);
    }

}