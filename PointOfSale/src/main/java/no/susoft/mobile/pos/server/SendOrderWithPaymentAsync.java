package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.OrderPaymentResponse;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class SendOrderWithPaymentAsync extends AsyncTask<Order, Void, OrderPaymentResponse> {

	Order o = null;

    @Override
    protected OrderPaymentResponse doInBackground(Order... order) {
        OrderPaymentResponse response = null;
        try {
			o = order[0];
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendOperation(Protocol.OperationCode.REQUEST_ORDER_UPLOAD);
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendParameter(Protocol.Parameters.TYPE, Protocol.Parameters.COMMIT.ordinal());
            request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
	
            final String json = JSONFactory.INSTANCE.getFactory().toJson(order[0]);
            // File
            final StringBody file = new StringBody(json, ContentType.APPLICATION_JSON);
            // Build the form post.
            final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            // Package
            entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("file", file);

            // Request
            response = Server.INSTANCE.doPost(OrderPaymentResponse.class, request, entity, JSONFactory.INSTANCE.getFactory());

        } catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
        }

        return response;
    }

    @Override
    protected void onPostExecute(OrderPaymentResponse result) {
        super.onPostExecute(result);
		MainActivity.getInstance().getServerCallMethods().sendPaymentOrderToServerPostExecute(result);
    }
}