package no.susoft.mobile.pos.discount;

import no.susoft.mobile.pos.data.DiscountReason;

import java.lang.ref.WeakReference;

/**
 * This singleton class handles fetching and caching discount reasons from the server.
 *
 * @author Yesod
 */
public enum DiscountReasonManager implements DiscountReasonFetcher.DiscountReasonFetcherListener {
    INSTANCE;
    // The void discount reason.
    private static DiscountReason VOIDED = new DiscountReason(0, "Void", 0);
    // The discount reason fetcher
    private DiscountReasonFetcher fetcher;
    // The discount reason container.
    private DiscountReasonContainer container;
    // The discount manager listener.
    private WeakReference<DiscountReasonRefreshListener> listener;

    // The discount manager listener interface
    public interface DiscountReasonRefreshListener {

        public void onDiscountReasonsRefreshed();
    }

    // Singleton.
    private DiscountReasonManager() {
        this.onDiscountReasonRequestComplete(null);
    }

    /**
     * Get discount reasons.
     *
     * @return
     */
    public DiscountReasonContainer getDiscountReasons() {
        return this.container;
    }

    /**
     * Return whether this manager is currently fetching discount reasons.
     *
     * @return
     */
    public boolean isFetching() {
        return this.fetcher != null;
    }

    /**
     * Return whether there exists discount reasons.
     *
     * @return
     */
    public boolean hasReasons() {
        return this.container.getReasons().size() > 1;
    }

    /**
     * Refresh the discount reasons.
     */
    public void doRefresh(DiscountReasonRefreshListener listener) {
        if (this.isFetching())
            return;
        // Clear the current reasons.
        this.container.getReasons().clear();
        // Add the default.
        this.container.getReasons().add(0, DiscountReasonManager.VOIDED);
        // Host the listener.
        this.setRefreshListener(listener);
        // Host the request and execute.
        fetcher = new DiscountReasonFetcher(this);
        fetcher.execute();
    }

    /*
     * (non-Javadoc)
     * @see no.susoft.mobile.discount.DiscountReasonFetcher.DiscountReasonFetcherListener#onDiscountReasonRequestComplete(no.susoft.mobile.discount.DiscountReasonContainer)
     */
    @Override
    public void onDiscountReasonRequestComplete(DiscountReasonContainer result) {
        // Never have a null result/
        if (result == null)
            result = new DiscountReasonContainer();
        // Host the discount reason container.
        this.container = result;
        // Add the default.
        this.container.getReasons().add(0, DiscountReasonManager.VOIDED);
        // Invalidate
        this.fetcher = null;
        // Notify.
        this.notifyRefreshListener();
    }

    /**
     * Set the discount refreshThisAuthorizingState listener.
     *
     * @param listener
     */
    public void setRefreshListener(DiscountReasonRefreshListener listener) {
        // WeakReference so that in-case the listener is an activity, Android can garbage collect it in a memory emergency.
        if (listener != null)
            this.listener = new WeakReference<DiscountReasonRefreshListener>(listener);
        else
            this.listener = null;
    }

    /**
     * Notify the refreshThisAuthorizingState listener, if any.
     */
    private void notifyRefreshListener() {
        // Notify..
        if (this.listener != null) {
            DiscountReasonRefreshListener observer = this.listener.get();
            if (observer != null)
                observer.onDiscountReasonsRefreshed();
        }
    }
}