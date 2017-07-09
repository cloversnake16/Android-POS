package no.susoft.mobile.pos.discount;

import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Discount;
import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * A discountable interface used for discount routines.
 *
 * @author Yesod
 */
public interface Discountable extends JSONSerializable {

    /**
     * Get the discount.
     *
     * @return
     */
    public Discount getDiscount();

    /**
     * Convenience method to return the discount percent, or zero when no discount has been applied.
     *
     * @return
     */
    public Decimal getDiscountPercent();

    /**
     * Set the discount.
     *
     * @param discount
     */
    public void setDiscount(final Discount discount);

    /**
     * Get the total vendor cost in cents.
     *
     * @return
     */
    public Decimal getTotalVendorCost();

    /**
     * Get the total retail cost in cents with the option to include discount rate.
     *
     * @return
     */
    public Decimal getTotalRetailCost(final boolean include_discount);

    /**
     * Get the quantity of units.
     *
     * @return
     */
    public Decimal getUnitQuantity();

    /**
     * Get the retail cost for a single unit.
     *
     * @return
     */
    public Decimal getUnitRetailCost();

    /**
     * Get the vendor cost for a single unit.
     *
     * @return
     */
    public Decimal getUnitVendorCost();

    /**
     * Get the VAT.
     *
     * @return
     */
    public double getVAT();
}