package no.susoft.mobile.pos.ui.fragment.utils;

import java.util.ArrayList;

import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Payment;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class PaymentValues {
    private Decimal orderAmount = Decimal.ZERO;
    private Decimal tendered = Decimal.ZERO;
    private Decimal change = Decimal.ZERO;

    public Decimal getTendered() {
        return tendered;
    }

    public Decimal getChange() {
        return change;
    }

    public Decimal getRemainingPositiveAmountToPay() {
        Decimal paymentValue = orderAmount.subtract(tendered);
        if (paymentValue.isPositive()) {
            return paymentValue;
        }
        return null;
    }

    public void setDecimalsToZero() {
        orderAmount = Decimal.ZERO;
        tendered = Decimal.ZERO;
        change = Decimal.ZERO;
    }

    public void setChangeValueToOrderAmountWithDiscount() {
        if (Cart.INSTANCE.hasActiveOrder())
            orderAmount = Cart.INSTANCE.getOrder().getAmount(true);
    }

    private void setChangeValueToPositive() {
        change = tendered.subtract(orderAmount);
    }

    private void setChangeValueToNegative() {
        change = orderAmount.subtract(tendered);
    }

    public void refreshChange() {
        if (change.isPositive() || change.isEqual(Decimal.ZERO)) {
            setChangeValueToPositive();
            MainActivity.getInstance().getNumpadPayFragment().setChangeVisualsToEnoughPaid(true);
        } else {
            setChangeValueToNegative();
            MainActivity.getInstance().getNumpadPayFragment().setChangeVisualsToEnoughPaid(false);
        }
    }

    public void refreshTendered(ArrayList<Payment> paymentList) {
        Decimal newTendered = Decimal.ZERO;
        for (Payment p : paymentList) {
            newTendered = newTendered.add(p.getAmount());
        }
        tendered = newTendered;
    }

    public void refreshPaymentValues(ArrayList<Payment> paymentList) {
        setChangeValueToOrderAmountWithDiscount();
        refreshTendered(paymentList);
        setChangeValueToPositive();
        refreshChange();
    }

    public boolean orderAmountIsNegative() {
        return orderAmount.isNegative();
    }
}
