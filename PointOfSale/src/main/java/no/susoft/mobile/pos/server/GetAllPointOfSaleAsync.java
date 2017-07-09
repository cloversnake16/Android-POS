package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.PointOfSale;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;

import java.util.ArrayList;

public class GetAllPointOfSaleAsync extends AsyncTask<String, Void, String> {

    ArrayList<PointOfSale> posResults;

    @Override
    protected String doInBackground(String... term) {

        try {

            posResults = new ArrayList<>();
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getUserShopId());
            request.appendOperation(OperationCode.REQUEST_POINTS_OF_SALES);

            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(json).getAsJsonArray();
                if (array != null) {
                    for (JsonElement element : array) {
                        PointOfSale pos = gson.fromJson(element, PointOfSale.class);
                        posResults.add(pos);
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
        Log.i("vilde", "result size is : " + (posResults.size()));

        MainActivity.getInstance().getServerCallMethods().getPosForShopPostExecute(posResults);
    }

}