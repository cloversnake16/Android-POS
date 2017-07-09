package no.susoft.mobile.pos.server;

import android.os.AsyncTask;

import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Customer;
import no.susoft.mobile.pos.data.CustomerResponse;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;


import java.util.ArrayList;

/**
 * Created by Vilde on 29.02.2016.
 */
public class CreateNewCustomer extends AsyncTask<Customer, Void, CustomerResponse> {

    @Override
    protected CustomerResponse doInBackground(Customer... term) {

        CustomerResponse cr = null;
        try {

            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendOperation(Protocol.OperationCode.REQUEST_CUSTOMER_SUBMIT);
            request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            // Send and Receive

            final String json = JSONFactory.INSTANCE.getFactory().toJson(term[0]);
            // File
            final StringBody file = new StringBody(json, ContentType.APPLICATION_JSON);
            // Build the form post.
            final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            // Package
            entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("file", file);

            // Request
            cr = Server.INSTANCE.doPost(CustomerResponse.class, request, entity, JSONFactory.INSTANCE.getFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cr;
    }

    @Override
    protected void onPostExecute(CustomerResponse cr) {
        super.onPostExecute(cr);
        if(cr != null) {
            MainActivity.getInstance().getServerCallMethods().createNewCustomerPostExecute(cr);
        }

    }
}
