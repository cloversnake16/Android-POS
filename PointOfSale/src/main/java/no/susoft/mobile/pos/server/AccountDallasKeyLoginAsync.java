package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import android.util.Log;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Account;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Protocol.State;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;

public class AccountDallasKeyLoginAsync extends AsyncTask<Account, Void, Account> {

    @Override
    protected Account doInBackground(Account... account) {
        try {
            Log.i("vilde", "license: " + account[0].getLicense());
            Log.i("vilde", "account code: " + account[0].getSecurityCode());

            // Build a request.
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendOperation(OperationCode.REQUEST_AUTHORIZATION);
            request.appendState(State.NOTAUTHORIZED);
            request.appendParameter(Parameters.LICENSE, account[0].getLicense());
            request.appendParameter(Parameters.SECURITYCODE, account[0].getSecurityCode());
			request.appendParameter(Parameters.VERSION, SusoftPOSApplication.getVersionName());

            // Get the response.
            return Server.INSTANCE.doGet(JSONFactory.INSTANCE.getFactory(), request, Account.class);
        } catch (Exception x) {
            x.printStackTrace();
            Log.i("vilde", "login exception");
            return null;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(Account result) {
        Log.i("vilde", "trying to run onauthorizationresponse");
        AccountManager.INSTANCE.onAccountAuthoriziationResponse(result);
    }

}



