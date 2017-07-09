package no.susoft.mobile.pos.search;

import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * This class contains a search response.
 *
 * @author Yesod
 */
public final class SearchResponse implements JSONSerializable {

    // An empty search response.
    public final static SearchResponse EMPTY = new SearchResponse(new Searchable[0]);
    // The search results.
    private final Searchable[] result;

    /**
     * @param query
     * @param result
     */
    public SearchResponse(Searchable... result) {
        this.result = result;
    }

    /**
     * Get the search result.
     *
     * @return
     */
    public Searchable[] getSearchResult() {
        if (this.result == null)
            return SearchResponse.EMPTY.getSearchResult();
        return this.result;
    }

    /**
     * Return whether there is any result.
     *
     * @return
     */
    public boolean hasResult() {
        return this.result.length > 0;
    }
}