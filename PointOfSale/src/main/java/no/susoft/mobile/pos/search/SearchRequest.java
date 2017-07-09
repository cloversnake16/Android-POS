package no.susoft.mobile.pos.search;

/**
 * This class contains a search request.
 *
 * @author Yesod
 */
public final class SearchRequest {

    // The search query.
    private final SearchQuery query;
    // The search response.
    private SearchResponse response;

    /**
     * Build a new search request.
     *
     * @param uuid
     * @param query
     */
    public SearchRequest(SearchQuery query) throws IllegalArgumentException {
        // Valid?
        if (query == null)
            throw new IllegalArgumentException("A search request query cannot be null.");
        this.query = query;
    }

    /**
     * Get the search query.
     *
     * @return
     */
    public SearchQuery getSearchQuery() {
        return this.query;
    }

    /**
     * Get the search response.
     *
     * @return
     */
    public SearchResponse getSearchResponse() {
        return this.response;
    }

    /**
     * Set the search response.
     *
     * @param response
     */
    public void setSearchResponse(SearchResponse response) {
        if (response == null)
            response = SearchResponse.EMPTY;
        this.response = response;
    }
}