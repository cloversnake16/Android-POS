package no.susoft.mobile.pos.server;

import java.util.ArrayList;

import android.os.AsyncTask;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Product;
import no.susoft.mobile.pos.data.Reconciliation;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.response.APIStatus;
import no.susoft.mobile.pos.response.StatusResponse;
import no.susoft.mobile.pos.ui.dialog.SupplyReportDialog;

public class SendReconciliationAsync extends AsyncTask<Reconciliation, Void, String> {

	ArrayList<Product> results = null;
    private SupplyReportDialog returnClass;

    @Override
    protected String doInBackground(Reconciliation... reconciliations) {
		try {
			Request request = Server.INSTANCE.getEncryptedPreparedRequest();
			request.appendState(Protocol.State.AUTHORIZED);
			request.appendOperation(OperationCode.REQUEST_SYNCHRONIZE_RECONCILIATIONS);
			request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getAccount().getToken());
		
			Reconciliation reconciliation = reconciliations[0];
			final String json = JSONFactory.INSTANCE.getFactory().toJson(reconciliation);
			final StringBody file = new StringBody(json, ContentType.APPLICATION_JSON);
			final MultipartEntityBuilder entity = MultipartEntityBuilder.create();
			entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			entity.addPart("file", file);
		
			StatusResponse r = Server.INSTANCE.doPost(StatusResponse.class, request, entity, JSONFactory.INSTANCE.getFactory());
			if (r != null && r.getStatusCode() == APIStatus.OK.getCode()) {
				DbAPI.markReconciliationAsSent(reconciliation.shopId, reconciliation.id);
				ErrorReporter.INSTANCE.filelog("Reconciliation sent. ID = " + reconciliation.id);
			}
		} catch (Exception e) {
			ErrorReporter.INSTANCE.filelog(e);
		}
        return "";
    }
}
				