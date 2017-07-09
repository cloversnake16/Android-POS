package no.susoft.mobile.pos.search;

/**
 * Implement this class to define a concrete search profile.
 *
 * @author Yesod
 */
public abstract class SearchProfile {

    /**
     * Get the search query.
     *
     * @param input
     * @return
     */
    public abstract SearchQuery getSearchQuery(String input);

    /**
     * Get the search accountAdapter.
     *
     * @return
     */
    public abstract SearchAdapter getSearchAdapter();

    /**
     * Return whether only a single selected result should be returned.
     *
     * @return
     */
    public abstract boolean isSingleSelection();

    /**
     * Return the resource id for the search icon to be displayed.
     *
     * @return
     */
    public abstract int getSearchIconResourceID();

    /**
     * Return whether this profile will initiate with a search query.
     *
     * @return
     */
    public abstract boolean hasInitialSearchQuery();
}