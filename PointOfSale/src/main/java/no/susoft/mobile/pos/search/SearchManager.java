package no.susoft.mobile.pos.search;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is the search manager singleton.
 *
 * @author Yesod
 */
public enum SearchManager {
    INSTANCE;
    // The search queue.
    private final BlockingQueue<SearchRequest> queue;
    // The search resolver.
    private final SearchResolver resolver;
    // The search resolver observer.
    private final SearchResolverObserver observer;
    // The search request map ... ?
    private final Map<String, WeakReference<SearchListener>> listeners;
    // The search manager available listener
    private WeakReference<SearchListener> listener;

    /**
     * A search listener.
     *
     * @author Yesod
     */
    public interface SearchListener {

        /**
         * Invoked when a search request has completed.
         *
         * @param query
         * @param response
         */
        void onSearchComplete(SearchQuery query, SearchResponse response);

        /**
         * Invoked to indicate the that search manager is available for new search requests.
         */
        void onSearchAvailable();
    }

    /**
     * This class allows this manager to observe the search resolver and react
     * to event without having to expose sensitive methods.
     *
     * @author Yesod
     */
    private static final class SearchResolverObserver implements SearchResolver.SearchResolverListener {

        /*
         * (non-Javadoc)
         * @see no.susoft.mobile.search.SearchResolver.SearchResolverListener#onSearchResponsePublished(no.susoft.mobile.search.SearchRequest)
         */
        @Override
        public void onSearchResponsePublished(SearchRequest request) {
            SearchManager.INSTANCE.onSearchResponsePublished(request);
        }
    }

    // Singleton
    private SearchManager() {
        this.listeners = new HashMap<String, WeakReference<SearchListener>>();
        this.queue = new LinkedBlockingQueue<SearchRequest>();
        this.observer = new SearchResolverObserver();
        this.resolver = new SearchResolver(this.observer, this.queue);
        this.resolver.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Request search.
     *
     * @param listener
     * @param query
     */
    public boolean onRequestSearch(SearchListener listener, SearchQuery query) {
        try {
            if (query == null)
                return false;
            // Valid query?
            if (query.notValid())
                return false;
            // Already searching?
            if (this.isSearching(query.getUUID()))
                return false;
            // Build the request.
            SearchRequest request = new SearchRequest(query);
            // Add.
            this.listeners.put(request.getSearchQuery().getUUID(), new WeakReference<SearchListener>(listener));
            // Add.
            this.queue.add(request);
            // Success?
            return true;
        } catch (Exception x) {
            return false;
        }
    }

    /**
     * This should only be called by a SearchResolver.
     *
     * @param request
     */
    private void onSearchResponsePublished(SearchRequest request) {
        try {
            // Pull it.
            WeakReference<SearchListener> reference = this.listeners.remove(request.getSearchQuery().getUUID());
            // Was there an observer in the first place?
            if (reference == null)
                return;
            final SearchListener listener = reference.get();
            // Was the observer garbage collected?
            if (listener == null)
                return;
            // Notify.
            listener.onSearchComplete(request.getSearchQuery(), request.getSearchResponse());
            // If there are no further searches pending, notify the manager is available.
            if (!this.isSearching()) {
                // Notify the listener that requested the search.
                listener.onSearchAvailable();
                // Notify the observer that the manager is available.
                if (this.listener != null) {
                    final SearchListener observer = this.listener.get();
                    // Anyone?
                    if (observer != null)
                        observer.onSearchAvailable();
                }
            }
        } catch (Exception x) {
            // Do not care.
        }
    }

    /**
     * Set the given 'listener' to observe a pending search having the UUID of the specified 'uuid'.
     *
     * @param uuid
     * @param listener
     */
    public void rebindListener(String uuid, SearchListener listener) {
        // Pull it.
        WeakReference<SearchListener> reference = this.listeners.remove(uuid);
        // Was there an observer in the first place?
        if (reference == null)
            return;
        // Was the observer garbage collected?
        if (reference.get() == null)
            return;
        // Add
        this.listeners.put(uuid, new WeakReference<SearchListener>(listener));
    }

    /**
     * Set the given 'listener' to be notified when the search manager is available for new searches.
     *
     * @param listener
     */
    public void setAvailabilityListener(SearchListener listener) {
        this.listener = new WeakReference<SearchListener>(listener);
    }

    /**
     * Return whether a search is in progress with the UUID of 'uuid'.
     *
     * @param uuid
     * @return
     */
    public boolean isSearching(String uuid) {
        return this.listeners.containsKey(uuid);
    }

    /**
     * Return whether a search is in progress.
     *
     * @return
     */
    public boolean isSearching() {
        return !this.listeners.isEmpty();
    }
}