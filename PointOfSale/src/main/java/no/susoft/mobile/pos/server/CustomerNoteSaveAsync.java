package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.CustomerNote;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.Message;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;

public class CustomerNoteSaveAsync extends AsyncTask<CustomerNote, Void, String> {

    @Override
    protected String doInBackground(CustomerNote... notes) {
        try {
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
			request.appendOperation(Protocol.OperationCode.REQUEST_CUSTOMER_NOTE_SAVE);
			request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
			request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());

            final String json = JSONFactory.INSTANCE.getFactory().toJson(notes[0]);
            final StringBody file = new StringBody(json, ContentType.APPLICATION_JSON);
            final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("file", file);

            Server.INSTANCE.doPost(Message.class, request, entity, JSONFactory.INSTANCE.getFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

}

