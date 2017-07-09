package no.susoft.mobile.pos.data;

import java.io.Serializable;

import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * This class represents a discount.
 * @author Yesod
 */
public class Discount implements JSONSerializable, Serializable {

	// The discount reason.
	private DiscountReason reason;
	// The discount percent.
	private Decimal percent;

	/**
	 * @param percent
	 * @param reason
	 */
	public Discount(final Decimal percent, final DiscountReason reason) {
		this.reason = reason;
		this.percent = percent;
	}

	/**
	 * @param percent
	 * @param reasonId
	 */
	public Discount(final Decimal percent, int reasonId) {
		this.reason = new DiscountReason(reasonId, "", 0);
		this.percent = percent;
	}
	
	/**
	 * Get the discount reason.
	 * @return
	 */
	public DiscountReason getReason() {
		return this.reason;
	}

	public void setReason(DiscountReason reason) {
		this.reason = reason;
	}

	/**
	 * Get the discount percent.
	 * @return
	 */
	public Decimal getPercent() {
		return this.percent;
	}

	public void setPercent(Decimal percent) {
		this.percent = percent;
	}

	/**
	 * Clamp the given value to the accepted discount percent domain of 0 to 100 inclusive.
	 */
	public static Decimal clamp(final Decimal value) {
		if (value.isNegative())
			return Decimal.ZERO;
		if (value.isGreater(Decimal.HUNDRED))
			return Decimal.HUNDRED;
		return value;
	}
	
	/**
	 * Return whether the given percent 'multiplier' is a valid discount rate.
	 */
	public static boolean valid(final Decimal value) {
		if (value.isNegative())
			return false;
		if (value.isGreater(Decimal.HUNDRED))
			return false;
		return true;
	}
	
	/**
	 * Calculate the remaining cents when the given 'percent' discount is applied.
	 * @param value
	 * @param percent
	 * @return
	 */
	public static Decimal calculateDiscount(final Decimal value, final Decimal percent) {
		if (percent.isPositive()) {
			double discount = value.toDouble() * (percent.toDouble() / 100);
			return Decimal.make(discount);
		} else {
			return value;
		}
	}

}