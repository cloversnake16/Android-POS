package no.susoft.mobile.pos.discount;

import android.os.AsyncTask;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;

/**
 * This class will fetch discount reasons from the server.
 *
 * @author Yesod
 */
public final class DiscountReasonFetcher extends AsyncTask<Void, Void, DiscountReasonContainer> {

    private final DiscountReasonFetcherListener listener;

    /**
     * @author Yesod
     */
    public interface DiscountReasonFetcherListener {

        public void onDiscountReasonRequestComplete(DiscountReasonContainer result);
    }

    /**
     * @param listener
     */
    public DiscountReasonFetcher(final DiscountReasonFetcherListener listener) {
        this.listener = listener;
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
     */
    @Override
    protected DiscountReasonContainer doInBackground(Void... arg0) {
        try {
            // To prevent spamming.
            Thread.sleep(2000);
            return DiscountReasonFetcher.request();
        } catch (Exception x) {
            return null;
        }
    }

    /**
     * @return
     */
    private static DiscountReasonContainer request() {
        if (AccountManager.INSTANCE.isActiveAccountAuthorized()) {
            Request request = Server.INSTANCE.getEncryptedPreparedRequest();
            request.appendOperation(Protocol.OperationCode.REQUEST_DISCOUNT_REASONS);
            request.appendState(Protocol.State.AUTHORIZED);
            request.appendParameter(Protocol.Parameters.TOKEN, AccountManager.INSTANCE.getToken());
            // Send and Receive
            return Server.INSTANCE.doGet(JSONFactory.INSTANCE.getFactory(), request, DiscountReasonContainer.class);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(DiscountReasonContainer result) {
        if (listener != null)
            listener.onDiscountReasonRequestComplete(result);
    }
}