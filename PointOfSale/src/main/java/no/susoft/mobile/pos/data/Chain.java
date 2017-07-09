package no.susoft.mobile.pos.data;

import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * This class will contain chain specific meta data as necessary to support Android client/server routines.
 *
 * @author Yesod
 */
public final class Chain implements JSONSerializable {

    // The chain name.
    private String name;
    // The chain dbp.
    private String dbp;
    // The chain parameter name
    private String parameterName;
    // The chain id.
    private int id;
    // Does this chain have an external system for customer data management.
    private boolean hasExternalCustomerSystem;
    // Is the chain active?
    private boolean isActive;

    /**
     * Get the chain name.
     *
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the chain name.
     *
     * @param name
     * @return
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the chain database name.
     *
     * @return
     */
    public String getDBP() {
        return this.dbp;
    }

    /**
     * Set the chain database name.
     *
     * @param dbp
     */
    public void setDBP(final String dbp) {
        this.dbp = dbp;
    }

    /**
     * Get the chain login name.
     */
    public String getLoginName() {
        return this.parameterName;
    }

    /**
     * Set the chain login name.
     *
     * @param parameterName
     */
    public void setLoginName(final String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * Get the chain id.
     *
     * @return
     */
    public int getID() {
        return this.id;
    }

    /**
     * Set the chain id.
     *
     * @param id
     */
    public void setID(final int id) {
        this.id = id;
    }

    /**
     * Get whether this chain has an external customer system.
     *
     * @return
     */
    public boolean hasExternalCustomerSystem() {
        return this.hasExternalCustomerSystem;
    }

    /**
     * Set whether this chain has an external customer system.
     *
     * @param value
     */
    public void setExternalCustomerSystem(final boolean value) {
        this.hasExternalCustomerSystem = value;
    }

    /**
     * Set whether this chain is active.
     *
     * @return
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     * Get whether this chain is active.
     *
     * @param value
     */
    public void setActive(final boolean value) {
        this.isActive = value;
    }
}