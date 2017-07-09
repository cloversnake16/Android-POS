package no.susoft.mobile.pos.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * ...
 *
 * @author Yesod
 */
public final class Decimal implements JSONSerializable, Serializable {

    // NEGATIVE ONE
    public final static Decimal NEGATIVE_ONE = Decimal.make(-1);
    // ZERO
    public final static Decimal ZERO = Decimal.make(BigDecimal.ZERO);
    // ONE
    public final static Decimal ONE = Decimal.make(BigDecimal.ONE);
    // TWO
    public final static Decimal TWO = Decimal.make(2);
    // TEN
    public final static Decimal TEN = Decimal.make(BigDecimal.TEN);
    // HUNDRED
    public final static Decimal HUNDRED = Decimal.make(100);
    // VAT
    public final static Decimal VAT = Decimal.make(1.25);

    // The default and preferred scale for Susoft.
    private final static int DefaultSCALE = 2;

    // The decimal value.
    private final BigDecimal value;

    /**
     * Constructs a new instance.
     *
     * @param value
     */
    private Decimal(final double value) {
        this(BigDecimal.valueOf(value), Decimal.DefaultSCALE);
    }

    /**
     * Constructs a new instance from a string representation.
     *
     * @param value
     */
    private Decimal(final String value) {
        this(new BigDecimal(value), Decimal.DefaultSCALE);
    }

    /**
     * @param value
     */
    private Decimal(final BigDecimal value) {
        this(value, Decimal.DefaultSCALE);
    }

    /**
     * Constructs a new instance.
     *
     * @param value
     */
    private Decimal(final BigDecimal value, final int scale) {
        this.value = value.setScale(scale, RoundingMode.HALF_EVEN);
    }

    /**
     * Constructs a new instance.
     *
     * @param value
     * @return
     */
    public static Decimal make(final double value) {
        return new Decimal(value);
    }

    /**
     * Constructs a new instance from a string representation.
     *
     * @param value
     * @return
     */
    public static Decimal make(final String value) {
        final String input = value.trim();
        if (input == null || input.isEmpty())
            return Decimal.ZERO;
        else
            return new Decimal(value);
    }

    /**
     * Constructs a new instance.
     *
     * @param value
     * @return
     */
    public static Decimal make(final BigDecimal value) {
        return new Decimal(value);
    }

    /**
     * Constructs a new instance.
     *
     * @param value
     * @return
     */
    public static Decimal make(final BigDecimal value, int scale) {
        return new Decimal(value, scale);
    }

    /**
     * If the given value is less than zero, then return zero.
     *
     * @param value
     * @return
     */
    public static Decimal minZero(final Decimal value) {
        return value == null || value.isNegative() ? Decimal.ZERO : value;
    }

    /**
     * If the given value invalid, then return zero.
     *
     * @param value
     * @return
     */
    public static Decimal validate(final Decimal value) {
        return value == null ? Decimal.ZERO : value;
    }

    /**
     * Returns a new instance whose value is this + that.
     * The scale of the result is the maximum of the scales of the two arguments.
     */
    public Decimal add(final Decimal that) {
        return Decimal.make(this.value.add(that.value));
    }

    /**
     * Returns a new instance whose value is this - that.
     * The scale of the result is the maximum of the scales of the two arguments.
     *
     * @param that
     * @return
     */
    public Decimal subtract(final Decimal that) {
        return Decimal.make(this.value.subtract(that.value));
    }

    /**
     * Returns a new instance whose value is this * that.
     * The scale of the result is the sum of the scales of the two arguments.
     *
     * @param that
     * @return
     */
    public Decimal multiply(final Decimal that) {
        return Decimal.make(this.value.multiply(that.value));
    }

    /**
     * Returns a new instance whose value is this / that.
     * The scale of the result is the scale of this.
     * If rounding is required to meet the specified scale, then RoundingMode.HALF_EVEN is applied.
     *
     * @param that
     * @return
     */
    public Decimal divide(final Decimal that) {
        return Decimal.make(this.value.divide(that.value, RoundingMode.HALF_EVEN));
    }

    /**
     * Returns a new instance whose value is this % that.
     * The scale of the result is the scale of this.
     *
     * @param that
     * @return
     */
    public Decimal remainder(final Decimal that) {
        return Decimal.make(this.value.remainder(that.value));
    }

    /**
     * Returns a array which contains the integral part of this / divisor at index 0
     * and the remainder this % divisor at index 1. The quotient is rounded down towards zero to the
     * next integer.
     * The scale of the result is the scale of this.
     *
     * @param that
     * @return
     */
    public Decimal[] divideAndRemainder(final Decimal that) {
        BigDecimal[] value = this.value.divideAndRemainder(that.value);
        Decimal[] result = new Decimal[2];
        result[0] = Decimal.make(value[0]);
        result[1] = Decimal.make(value[1]);
        return result;
    }

    /**
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        return this.value.equals(o);
    }

    /**
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    /**
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.value.toString();
    }

    /**
     * Return whether this value is negative.
     *
     * @return
     */
    public boolean isNegative() {
        return this.value.signum() == -1;
    }

    /**
     * Return whether this value is zero.
     *
     * @return
     */
    public boolean isZero() {
        return this.value.signum() == 0;
    }

    /**
     * Return whether this value is positive.
     * Note that zero is neither a positive nor a negative number.
     *
     * @return
     */
    public boolean isPositive() {
        return this.value.signum() == 1;
    }

    public Decimal abs() {return Decimal.make(this.value.abs());}

    /**
     * Returns whether the value of this < that.
     *
     * @param that
     * @return
     */
    public boolean isLess(final Decimal that) {
        return -1 == this.value.compareTo(that.value);
    }

    /**
     * Returns whether the value of this = that.
     *
     * @return
     */
    public boolean isEqual(final Decimal that) {
        return 0 == this.value.compareTo(that.value);
    }

    /**
     * Returns whether the value of this = that.
     *
     * @param that
     * @return
     */
    public boolean isEqual(final double that) {
        return this.toDouble() == that;
    }

    /**
     * Returns whether the value of this > that.
     *
     * @param that
     * @return
     */
    public boolean isGreater(final Decimal that) {
        return 1 == this.value.compareTo(that.value);
    }

    /**
     * Returns this this as a int value.
     * If this is too big to be represented as an int ArithmeticException is thrown.
     *
     * @return
     * @throws ArithmeticException
     */
    public int toInteger() {
        BigDecimal number = value;
        number = number.setScale(0, BigDecimal.ROUND_DOWN);
        return number.intValueExact();
    }

    /**
     * Returns this this as a double value.
     * If this is too big to be represented as a float,
     * then Double.POSITIVE_INFINITY or Double.NEGATIVE_INFINITY is returned.
     *
     * @return
     */
    public double toDouble() {
        return this.value.doubleValue();
    }

    public Decimal negate() {
        return make(value.negate());
    }
}