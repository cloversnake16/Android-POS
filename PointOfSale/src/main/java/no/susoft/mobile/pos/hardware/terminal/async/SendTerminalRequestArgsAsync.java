package no.susoft.mobile.pos.hardware.terminal.async;

import android.os.AsyncTask;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.terminal.TerminalRequest;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;

/**
 * Created on 3/7/2016.
 */
public class SendTerminalRequestArgsAsync extends AsyncTask<TerminalRequest, Void, Boolean> {

    @Override
    protected Boolean doInBackground(TerminalRequest... params) {

        if (params == null || params.length < 1) return false;

        try {
            ErrorReporter.INSTANCE.writeFile("SendTerminalRequestArgsAsync -> before calling server");

            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            //request.appendOperation(Protocol.OperationCode.SAVE_TERMINAL_REQUEST_ARGS);
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendParameter(Protocol.Parameters.TYPE, Protocol.Parameters.COMMIT.ordinal());
            request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());

            final String json = JSONFactory.INSTANCE.getFactory().toJson(params[0]);

            // File
            final StringBody file = new StringBody(json, ContentType.APPLICATION_JSON);
            // Build the form post.
            final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            // Package
            entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("file", file);

            // Request
            return Server.INSTANCE.doPost(Protocol.Message.class, request, entity, JSONFactory.INSTANCE.getFactory()) == Protocol.Message.OK;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
