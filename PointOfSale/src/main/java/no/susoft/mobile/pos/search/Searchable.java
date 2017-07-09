package no.susoft.mobile.pos.search;

import no.susoft.mobile.pos.json.JSONSerializable;
import no.susoft.mobile.pos.network.Protocol.SearchEntity;

import java.util.Locale;
import java.util.UUID;

/**
 * Implement to indicate the class is searchable.
 *
 * @author Yesod
 */
public abstract class Searchable implements JSONSerializable {

    // The searchable entity type.
    private final SearchEntity entity;
    // The searchable universally unique identifier.
    private String uuid;

    /**
     * @param entity
     */
    public Searchable(SearchEntity entity) {
        this.entity = entity;
    }

    /**
     * Get the searchable element type.
     *
     * @return
     */
    public final SearchEntity getSearchEntityType() {
        return this.entity;
    }

    /**
     * Get the searchable universally unique identifier.
     *
     * @return
     */
    public final String getUUID() {
        if (this.uuid == null || this.uuid.isEmpty())
            this.uuid = UUID.randomUUID().toString().toUpperCase(Locale.US);
        return this.uuid;
    }

    /**
     * If valid, make a field-to-field copy of the 'searchable' instance, except its UUID.
     *
     * @param searchable
     */
    public abstract void copy(Searchable searchable);
}