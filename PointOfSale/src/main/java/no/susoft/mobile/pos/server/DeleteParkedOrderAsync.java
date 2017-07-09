package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import com.google.gson.Gson;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol.Message;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Protocol.State;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;

public class DeleteParkedOrderAsync extends AsyncTask<Order, Void, String> {

    @Override
    protected String doInBackground(Order... order) {

        try {
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(State.AUTHORIZED);
            request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
            request.appendOperation(OperationCode.REQUEST_ORDER_DELETE);
            request.appendParameter(Parameters.SHOP, order[0].getShopId());
            request.appendParameter(Parameters.ID, "" + order[0].getParkedId());

            // Send and Receive
            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                Message message = gson.fromJson(json, Message.class);
                if (message != Message.OK) {
                    //todo error wrapper
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
