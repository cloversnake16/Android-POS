package no.susoft.mobile.pos.search;

import no.susoft.mobile.pos.data.Shop;
import no.susoft.mobile.pos.network.Protocol.SearchConstraint;
import no.susoft.mobile.pos.network.Protocol.SearchEntity;
import no.susoft.mobile.pos.network.Protocol.SearchType;

import java.util.Locale;
import java.util.UUID;

/**
 * This class contains information on a search query.
 *
 * @author Yesod
 */
public class SearchQuery {

    // The search universally unique identifier.
    private final String uuid;
    // The search term.
    private final String term;
    // The search type.
    private final SearchType type;
    // The search constraint.
    private final SearchConstraint constraint;
    // The search shops.
    private final Shop shop;
    // The search entity type
    private final SearchEntity entity;

    /**
     * @param entity
     * @param type
     * @param constraint
     * @param term
     * @param shop
     */
    public SearchQuery(final SearchEntity entity, final SearchType type, final SearchConstraint constraint, final String term, final Shop shop) {
        this(entity, type, constraint, term, UUID.randomUUID().toString().toUpperCase(Locale.US), shop);
    }

    /**
     * @param entity
     * @param type
     * @param constraint
     * @param term
     * @param uuid
     * @param shop
     */
    public SearchQuery(final SearchEntity entity, final SearchType type, final SearchConstraint constraint, final String term, final String uuid, final Shop shop) {
        this.entity = entity;
        this.type = type;
        this.term = term;
        this.shop = shop;
        this.uuid = uuid;
        this.constraint = constraint;
    }

    /**
     * Get the search entity type.
     *
     * @return
     */
    public final SearchEntity getSearchEntity() {
        return this.entity;
    }

    /**
     * Get the search shop.
     *
     * @return
     */
    public final Shop getShop() {
        return this.shop;
    }

    /**
     * Get the search universally unique identifier.
     *
     * @return
     */
    public final String getUUID() {
        return this.uuid;
    }

    /**
     * Get the search constraint.
     *
     * @return
     */
    public final SearchConstraint getSearchConstraint() {
        return this.constraint;
    }

    /**
     * Get the search type.
     *
     * @return
     */
    public final SearchType getSearchType() {
        return this.type;
    }

    /**
     * Get the search word.
     *
     * @return
     */
    public final String getSearchTerm() {
        return this.term;
    }

    /**
     * Return whether this query is bounded to a shop.
     *
     * @return
     */
    public boolean hasShop() {
        return this.shop != null;
    }

    /**
     * Return whether this query is not valid.
     *
     * @return
     */
    public boolean notValid() {
        if (this.uuid == null)
            return true;
        if (this.uuid.isEmpty())
            return true;
        if (this.type == null)
            return true;
        if (this.constraint == null)
            return true;
        if (this.term == null)
            return true;
        if (this.term.isEmpty())
            return true;
        if (this.entity == null)
            return true;
        // Valid
        return false;
    }
}