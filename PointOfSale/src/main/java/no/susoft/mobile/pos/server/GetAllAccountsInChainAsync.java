package no.susoft.mobile.pos.server;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Account;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class GetAllAccountsInChainAsync extends AsyncTask<String, Void, String> {

    ArrayList<Account> accountResults;

    @Override
    protected String doInBackground(String... term) {

        try {
            accountResults = new ArrayList<>();
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendOperation(Protocol.OperationCode.REQUEST_ACCOUNT_LIST);

            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(json).getAsJsonArray();
                if (array != null) {
                    for (JsonElement element : array) {
                        Account a = gson.fromJson(element, Account.class);
                        accountResults.add(a);
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
        Log.i("vilde", "result size is : " + (accountResults.size()));

        MainActivity.getInstance().getServerCallMethods().getAllAccountsAsyncPostExecute(accountResults);
    }

}
