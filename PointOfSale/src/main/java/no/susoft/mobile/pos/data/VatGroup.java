package no.susoft.mobile.pos.data;

public class VatGroup {

    private Double vatPercent = 0.0;
    private Decimal purchaseSumInclVat = Decimal.ZERO;
    private Decimal sumWithoutVat = Decimal.ZERO;
    private Decimal sumVat = Decimal.ZERO;

    public Decimal getSumWithoutVat() {
        return sumWithoutVat;
    }

    public Decimal getSumVat() {
        return sumVat;
    }

    public VatGroup(Double percent) {
        setVatPercent(percent);
    }

    public Double getVatPercent() {
        return vatPercent;
    }

    public void setVatPercent(Double vatPercent) {
        this.vatPercent = vatPercent;
    }

    public void addToPurchaseSumInclVat(Decimal amount) {
        purchaseSumInclVat = purchaseSumInclVat.add(amount);
        sumWithoutVat = purchaseSumInclVat.divide(Decimal.make(vatPercent).add(Decimal.HUNDRED).divide(Decimal.HUNDRED));
        sumVat = purchaseSumInclVat.subtract(sumWithoutVat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        VatGroup myObject = (VatGroup) o;

        if (Double.compare(myObject.getVatPercent(), getVatPercent()) != 0)
            return false;
        //if (Double.compare(myObject.getVatPercent(), getVatPercent()) != 0) return false;

        return true;
    }

}
