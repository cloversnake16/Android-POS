package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Protocol.State;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;

/**
 * Worker to load gift card from server and populate the values
 */
public class GiftCardLoadAsync extends AsyncTask<String, Void, Prepaid> {

    @Override
    protected Prepaid doInBackground(String... number) {
        Prepaid p = null;
        try {
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(State.AUTHORIZED);
            request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getToken());
            request.appendOperation(OperationCode.REQUEST_PREPAID);
            request.appendParameter(Parameters.ID, number[0]);
            request.appendParameter(Parameters.TYPE, "G");

            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(json);
                if (element != null) {
                    p = gson.fromJson(element, Prepaid.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }

    @Override
    protected void onPostExecute(Prepaid p) {
        super.onPostExecute(p);
        MainActivity.getInstance().getNumpadPayFragment().showPayWithGiftCardDialog(p);
    }
}
