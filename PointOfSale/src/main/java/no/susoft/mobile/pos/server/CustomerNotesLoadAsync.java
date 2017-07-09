package no.susoft.mobile.pos.server;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.view.View;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.CustomerNote;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.dialog.CustomerNotesDialog;

public class CustomerNotesLoadAsync extends AsyncTask<String, Void, String> {

	String termString;
	ArrayList<CustomerNote> customerNotes = new ArrayList<>();
	CustomerNotesDialog caller;

    @Override
    protected String doInBackground(String... term) {
		termString = term[0];
        try {
            customerNotes = new ArrayList<>();
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendState(Protocol.State.AUTHORIZED);
			request.appendOperation(Protocol.OperationCode.REQUEST_CUSTOMER_NOTES_LOAD);
			request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
			request.appendParameter(Protocol.Parameters.SHOP, AccountManager.INSTANCE.getAccount().getShop().getID());
            request.appendParameter(Protocol.Parameters.ID, term[0]);

            String json = Server.INSTANCE.doGet(request);
            if (json != null) {
                Gson gson = JSONFactory.INSTANCE.getFactory();
                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(json).getAsJsonArray();
                if (array != null) {
                    for (JsonElement element : array) {
                        CustomerNote note = gson.fromJson(element, CustomerNote.class);
                        customerNotes.add(note);
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
		if (caller != null) {
			caller.getProgressBar().setVisibility(View.GONE);
			caller.setupAdapter(customerNotes);
		}
    }

	public void setCaller(CustomerNotesDialog caller) {
		this.caller = caller;
	}
}

