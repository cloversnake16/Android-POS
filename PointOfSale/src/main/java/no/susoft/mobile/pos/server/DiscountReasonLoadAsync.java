package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.DiscountReason;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;

import java.util.ArrayList;

public class DiscountReasonLoadAsync extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... term) {

        try {
            ArrayList<DiscountReason> discountReasons = new ArrayList<>();
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendOperation(Protocol.OperationCode.REQUEST_DISCOUNT_REASONS);
            request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());

            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(json).getAsJsonArray();
                if (array != null) {
                    for (JsonElement element : array) {
                        DiscountReason reason = gson.fromJson(element, DiscountReason.class);
                        discountReasons.add(reason);
                    }
                }
            }

            MainActivity.getInstance().setDiscountReasons(discountReasons);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

}
