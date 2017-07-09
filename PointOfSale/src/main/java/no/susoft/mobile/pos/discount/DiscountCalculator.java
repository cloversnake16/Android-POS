package no.susoft.mobile.pos.discount;

import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Discount;

/**
 * A helper class to help calculate revenue, retail, gross margin metrics based on a discount percent.
 *
 * @author Yesod
 */
public class DiscountCalculator {

    // The item vendor price.
    protected Decimal vendor_price;
    // The item retail price (VAT included)
    protected Decimal retail_price;
    // The item discount rate multiplier;
    private Decimal discount;

    /**
     * @param discountable
     */
    public DiscountCalculator(final Discountable discountable) {
        this.prime(discountable);
    }

    /**
     * This will prime the calculator constants based on the CURRENT values of the given
     * Discountable instance. Since this instance is not saved as member variable, changes
     * to this instance will not be reflected in this calculator.
     *
     * @param discountable
     */
    public void prime(final Discountable discountable) {
        // The item vendor price; constant.
        this.vendor_price = discountable.getTotalVendorCost();
        // The item retail price; constant.
        this.retail_price = discountable.getTotalRetailCost(false);
        // The item retail value;
        if (discountable.getDiscount() != null) {
            // The default discount.
            this.discount = Discount.clamp(discountable.getDiscount().getPercent());
        } else {
            this.discount = Discount.clamp(Decimal.ZERO);
        }
    }

    /**
     * Get the revenue subtotal.
     * This is how much money the shop keeps after paying VAT.
     *
     * @return
     */
    @Deprecated
    public final Decimal getRevenueSubtotal() {
        //return Money.toCents(Money.toDecimalCents(this.retail_price) / VAT_MULTIPLE);
        final Decimal result = this.retail_price.divide(Decimal.VAT);
        return result;
    }

    /**
     * Get the retail subtotal.
     * This is an undiscounted cost of the item.
     *
     * @return
     */
    public final Decimal getRetailSubtotal() {
        return this.retail_price;
    }

    /**
     * Get the revenue discount.
     *
     * @return
     */
    public final Decimal getRevenueDiscount() {
        //		final double r = Money.toDecimalCents(this.getRevenueSubtotal());
        //		final double d = Money.toDecimalCents(this.getDiscountPercent());
        //		final int result = Money.toCents(r * d) * -1;
        //		return result;
        //final SusoftDecimal result = this.getRevenueSubtotal().multiply(this.getDiscountPercent()).multiply(SusoftDecimal.NEGATIVE_ONE);
        final Decimal result = Discount.calculateDiscount(this.getRevenueSubtotal(), this.getDiscountPercent());
        return result;
    }

    /**
     * Get the retail discount.
     *
     * @return
     */
    public final Decimal getRetailDiscount() {
        //		final double r = Money.toDecimalCents(this.getRetailSubtotal());
        //		final double d = Money.toDecimalCents(this.getDiscountPercent());
        //		final int result = Money.toCents(r * d) * -1;
        //		return result;
        //final SusoftDecimal result = this.getRetailSubtotal().multiply(this.getDiscountPercent()).multiply(SusoftDecimal.NEGATIVE_ONE);
        final Decimal result = Discount.calculateDiscount(this.getRetailSubtotal(), this.getDiscountPercent());
        return result;
    }

    /**
     * Get the revenue total price.
     * This is how much money the shop keeps after paying VAT.
     *
     * @return
     */
    public final Decimal getRevenueTotal() {
        final Decimal result = this.getRevenueSubtotal().add(this.getRevenueDiscount());
        return result;
    }

    /**
     * Get the retail total price.
     * This is how much money the customer is expected to pay.
     *
     * @return
     */
    public final Decimal getRetailTotal() {
        final Decimal result = this.getRetailSubtotal().add(this.getRetailDiscount());
        return result;
    }

    /**
     * Get the vendor total price.
     *
     * @return
     */
    public final Decimal getVendorTotal() {
        return this.vendor_price;
    }

    /**
     * Get the gross margin.
     *
     * @return
     */
    public final Decimal getGrossMargin() {
        final Decimal result = this.getRevenueTotal().subtract(this.getVendorTotal());
        return result;
    }

    /**
     * Get the gross margin percent.
     *
     * @return
     */
    public final Decimal getGrossMarginPercent() {
        final Decimal revenue_total = this.getRevenueTotal();
        // Let's avoid dividing by zero.
        if (revenue_total.isZero()) {
            return Decimal.ONE;
        } else {
            //			final double g = Money.toDecimalCents(this.getGrossMargin());
            //			final double r = Money.toDecimalCents(revenue_total);
            //			final double p = (g / r) * 100d;
            //			return p;
            final Decimal result = this.getGrossMargin().divide(revenue_total);
            return result;
        }
    }

    /**
     * Get the discount percent.
     *
     * @return
     */
    public final Decimal getDiscountPercent() {
        return this.discount;
    }

    /**
     * Set the discount percent, and return the final value (in case modifications were made);
     *
     * @param percent
     */
    public final Decimal setDiscountPercent(final Decimal percent) {
        this.discount = Discount.clamp(percent);
        return this.discount;
    }

    /**
     * Set the retail price manually.
     *
     * @param value
     */
    public final void setRetailPrice(final Decimal value) {
        //this.retail_price = Money.toCents(value);
        this.retail_price = value;
    }
}