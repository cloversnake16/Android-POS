package no.susoft.mobile.pos.server;

import android.os.AsyncTask;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.dialog.OrderSearchDialog;
import no.susoft.mobile.pos.ui.dialog.OrderSearchSelectionDialog;

public class LoadCompleteOrderByIDAsync extends AsyncTask<String, Void, String> {
	
	Order o = null;
	OrderSearchDialog osd = null;
	
	@Override
	protected String doInBackground(String... receipt) {
		
		try {
			o = null;
			Request request = Server.INSTANCE.getEncryptedPreparedRequest();
			request.appendState(Protocol.State.AUTHORIZED);
			request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
			request.appendOperation(Protocol.OperationCode.REQUEST_ORDER_LOAD);
			request.appendParameter(Parameters.ID, receipt[0]);
			
			// Send and Receive
			String json = Server.INSTANCE.doGet(request);
			if (json != null) {
				Gson gson = JSONFactory.INSTANCE.getFactory();
				JsonParser parser = new JsonParser();
				JsonElement element = parser.parse(json);
				if (element != null) {
					o = gson.fromJson(element, Order.class);
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
		if (osd != null) {
			OrderSearchSelectionDialog ossd = new OrderSearchSelectionDialog();
			ossd.setup(osd, o);
			ossd.show(MainActivity.getInstance().getSupportFragmentManager(), "details");
		} else {
			Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.found_no_orders), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void setSearchDialog(OrderSearchDialog osd) {
		this.osd = osd;
	}
}


