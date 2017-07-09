package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Protocol.State;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;

public class SettingParameterLoadAsync extends AsyncTask<String, Void, String> {

    public interface AsyncResponse {

        void processFinish(String value);
    }

    public AsyncResponse delegate = null;

    public SettingParameterLoadAsync(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    String result = null;

    @Override
    protected String doInBackground(String... name) {

        try {
            result = null;
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(State.AUTHORIZED);
            request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendOperation(OperationCode.REQUEST_SETTING);
            request.appendParameter(Parameters.ID, name[0]);

            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(json);
                if (element != null) {
                    result = gson.fromJson(element, String.class);
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

        if (delegate != null) {
            delegate.processFinish(result);
        }
    }
}
