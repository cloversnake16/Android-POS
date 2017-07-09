package no.susoft.mobile.pos.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.account.AccountManager.Preferences;
import no.susoft.mobile.pos.data.Account;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Protocol.State;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.util.AppConfig;

public class ReceiptTemplateLoadAsync extends AsyncTask<String, Void, String> {

    String[] result = null;

    @Override
    protected String doInBackground(String... shopId) {

        try {
            result = null;
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(State.AUTHORIZED);
            request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendOperation(OperationCode.REQUEST_RECEIPT_TEMPLATE);
            request.appendParameter(Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());

            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(json);
                if (element != null) {
                    result = gson.fromJson(element, String[].class);
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
        if (result != null) {
			SharedPreferences preferences = SusoftPOSApplication.getContext().getSharedPreferences(Account.class.toString(), Context.MODE_PRIVATE);
			final SharedPreferences.Editor editor = preferences.edit();
			if (!result[0].trim().isEmpty()) {
				AppConfig.getState().setReceiptHeader(result[0]);
				editor.putString(Preferences.RECEIPT_HEADER.toString(), result[0]);
			} else {
				editor.remove(Preferences.RECEIPT_HEADER.toString());
			}
			if (!result[1].trim().isEmpty()) {
				AppConfig.getState().setReceiptFooter(result[1]);
				editor.putString(Preferences.RECEIPT_FOOTER.toString(), result[1]);
			} else {
				editor.remove(Preferences.RECEIPT_FOOTER.toString());
			}
			editor.commit();
        }
    }
}
