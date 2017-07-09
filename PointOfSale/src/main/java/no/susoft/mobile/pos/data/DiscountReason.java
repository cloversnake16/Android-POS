package no.susoft.mobile.pos.data;

import java.io.Serializable;

import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * This class represents a discount name.
 * @author Yesod
 */
public final class DiscountReason implements JSONSerializable, Serializable {

	// The discount id.
	private final int id;
	// The discount name.
	private final String name;
	// The discount percent for this name.
	private final int percent;
	
	/**
	 * @param id
	 * @param name
	 * @param discount
	 */
	public DiscountReason(int id, String name, int discount) {
		this.id = id;
		this.name = name;
		this.percent = discount;
	}
	
	/**
	 * Get the discount id.
	 * @return
	 */
	public int getID() {
		return this.id;
	}
	
	/**
	 * Get the discount name.
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get the discount percent.
	 * @return
	 */
	public int getPercent() {
		return this.percent;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.name;
	}
	
	/**
	 * Return whether the given 'name' matches that of this instance. This method
	 * exists to avoid overriding {@link #equals(Object)} which always requires
	 * an override to {@link #hashCode()}, of which, I, (Yesod), was not 100% on how
	 * to canonically implement at the time.
	 * @param reason
	 * @return
	 */
	public boolean matches(DiscountReason reason) {
		if (reason instanceof DiscountReason) {
			// Same ID?
			if (reason.id != this.id)
				return false;
			// Pass.
			return true;
		} else
			return false;
	}
}