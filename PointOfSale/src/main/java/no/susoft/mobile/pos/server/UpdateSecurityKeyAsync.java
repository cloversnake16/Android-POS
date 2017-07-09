package no.susoft.mobile.pos.server;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Account;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class UpdateSecurityKeyAsync extends AsyncTask<Account, Void, Protocol.Message> {

    private String key;
    ProgressDialog progDialog;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progDialog = new ProgressDialog(MainActivity.getInstance());
        progDialog.setMessage("Updating...");
        progDialog.setIndeterminate(false);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setCancelable(true);
        progDialog.show();
    }

    @Override
    protected Protocol.Message doInBackground(Account... account) {

        try {
            Log.i("vilde", "sending request..");
            Log.i("vilde", "user id: " + account[0].getUserId());
            Log.i("vilde", "shop id: " + account[0].getUserShopId());
            Log.i("vilde", "key    : " + key);

            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendOperation(OperationCode.REQUEST_APPLY_SECURITYCODE);
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendParameter(Protocol.Parameters.ID, account[0].getUserId());
            request.appendParameter(Protocol.Parameters.SHOP, account[0].getUserShopId());
            request.appendParameter(Protocol.Parameters.SECURITYCODE, key);

            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                Protocol.Message message = gson.fromJson(json, Protocol.Message.class);
                if (message != Protocol.Message.OK) {
                    if (message != null) {
                        Log.i("vilde", message.toString());
                        return message;
                    } else {
                        Log.i("vilde", "message was null");
                    }
                } else {
                    Log.i("vilde", "updated successfully");
                    return message;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void onPostExecute(Protocol.Message message) {
        progDialog.dismiss();
        MainActivity.getInstance().getServerCallMethods().updateKeyForAccountPostExecute(message);

    }

    public void setKey(String key) {
        this.key = key;
    }
}
