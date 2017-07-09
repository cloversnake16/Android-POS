package no.susoft.mobile.pos.search;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Protocol.State;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;

import java.util.concurrent.BlockingQueue;

/**
 * This class asynchronously delegates a search request between client and server.
 *
 * @author Yesod
 */
public final class SearchResolver extends AsyncTask<Void, SearchRequest, Void> {

    // The JSON factory equipped with the searchable accountAdapter.
    private static Gson GSON = new GsonBuilder().registerTypeAdapter(Searchable.class, new SearchableAdapter()).create();
    // The search blocking queue.
    private final BlockingQueue<SearchRequest> queue;
    // The search resolver listener
    private final SearchResolverListener listener;

    /**
     * @author Yesod
     */
    public interface SearchResolverListener {

        void onSearchResponsePublished(SearchRequest request);
    }

    /**
     * Build a new search resolver.
     */
    public SearchResolver(SearchResolverListener listener, BlockingQueue<SearchRequest> queue) throws IllegalArgumentException {
        if (listener == null)
            throw new IllegalArgumentException("A search resolver cannot have a null listener.");

        this.listener = listener;
        this.queue = queue;

    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
     */
    @Override
    protected Void doInBackground(Void... ignore) {
        while (true) {
            try {
                // The queue will block the thread until there is a new element to take.
                SearchRequest request = this.queue.take();
                // Wait
                Thread.sleep(1000);
                // Canceled?
                if (this.isCancelled())
                    break;
                // Request
                SearchResponse response = this.request(request.getSearchQuery());
                // Canceled?
                if (this.isCancelled())
                    break;
                // Attach the response.
                request.setSearchResponse(response);
                // Publish
                this.publishProgress(request);
            } catch (Exception x) {
                ErrorReporter.INSTANCE.reportError(SearchResolver.class, "There was an unexpected problem attempting to request a search.", x);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onProgressUpdate(java.lang.Object[])
     */
    @Override
    protected void onProgressUpdate(SearchRequest... requests) {
        if (this.listener != null) {
            for (SearchRequest request : requests) {
                this.listener.onSearchResponsePublished(request);
            }
        }
    }

    /**
     * Send a request to the server.
     *
     * @param query
     * @return
     */
    private SearchResponse request(SearchQuery query) {
        // Check whether the current account is authorized to invoke gated requests.
        //		if (AccountManager.INSTANCE.isActiveAccountAuthorized()) {

        System.out.println("SEARCH REQUEST !!");

        Request request = Server.INSTANCE.getEncryptedPreparedRequest();
        request.appendOperation(OperationCode.REQUEST_SEARCH);
        request.appendState(State.AUTHORIZED);
        request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getToken());
        request.appendParameter(Parameters.ENTITY, Integer.toString(query.getSearchEntity().ordinal()));
        request.appendParameter(Parameters.TYPE, Integer.toString(query.getSearchType().ordinal()));
        request.appendParameter(Parameters.CONSTRAINT, Integer.toString(query.getSearchConstraint().ordinal()));
        request.appendParameter(Parameters.TERM, query.getSearchTerm());

        // TODO: Attach all the shops.
        if (query.hasShop()) {
            request.appendParameter(Parameters.SHOP, query.getShop().getID());
        }
        // Send
        return Server.INSTANCE.doGet(SearchResolver.GSON, request, SearchResponse.class);
        //		}
        //		return null;
    }
}