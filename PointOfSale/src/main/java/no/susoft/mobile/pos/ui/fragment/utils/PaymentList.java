package no.susoft.mobile.pos.ui.fragment.utils;

import java.util.ArrayList;

import android.widget.Toast;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.Payment;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class PaymentList {

    private PaymentValues paymentValues;
    private ArrayList<Payment> paymentList = new ArrayList<>();

    public PaymentList(PaymentValues paymentValues) {
        this.paymentValues = paymentValues;
    }

    public ArrayList<Payment> getPaymentList() {
        return paymentList;
    }

    public void clearPaymentList() {
        paymentList = new ArrayList<>();
        MainActivity.getInstance().getCartFragment().getCartButtons().refreshCartButtonStates();
    }

    public void addPayment(Payment.PaymentType type, String number, Decimal amount) {

        switch (type) {
            case CASH: {
                addToCashPayment(amount);
                break;
            }
            case CARD: {
                addToCardPayment(number, amount);
                break;
            }
            case GIFT_CARD: {
                addToGiftCardPayment(number, amount);
                break;
            }
            case INVOICE: {
                addToInvoicePayment(amount);
                break;
            }
            default: {
                MainActivity.getInstance().getNumpadPayFragment().makeInvalidInputToast();
                break;
            }
        }

        MainActivity.getInstance().getCartFragment().getCartButtons().refreshCartButtonStates();
    }

    private void addToCashPayment(Decimal amount) {
        Decimal roundedAmount = Decimal.make(Math.round(Float.valueOf(amount.toString())));
        addPaymentToList(Payment.PaymentType.CASH, "", roundedAmount);
    }

    private void addToCardPayment(String cardType, Decimal amount) {

        addPaymentToList(Payment.PaymentType.CARD, cardType, amount);
    }

    private void addToInvoicePayment(Decimal amount) {
        if(paymentList.size() == 0 && Cart.INSTANCE.getOrder().getCustomer() != null) {
            addPaymentToList(Payment.PaymentType.INVOICE, "", amount);
        } else if (paymentList.size() > 0) {
            Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.remove_payments_before_using_invoice), Toast.LENGTH_LONG).show();
        } else if (Cart.INSTANCE.getOrder().getCustomer() == null) {
            Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.action_requires_customer_set), Toast.LENGTH_LONG).show();
        }
    }

    private void addToGiftCardPayment(String number, Decimal amount) {
        for (Payment p : paymentList) {
            if (p.getNumber().equalsIgnoreCase(number)) {
                Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.gift_card_already_added), Toast.LENGTH_LONG).show();
                return;
            }
        }
        addPaymentToList(Payment.PaymentType.GIFT_CARD, number, amount);
    }

    public void addCardPayment(String number, Decimal amount, int cardId, String cardName, int terminalType) {
        paymentList.add(new Payment(Payment.PaymentType.CARD, number, amount)
                .setCardId(cardId)
                .setCardName(cardName)
                .setCardTerminalType(terminalType));
    }

    private void addPaymentToList(Payment.PaymentType type, String number, Decimal amount) {
        boolean typeAlreadyExists = false;
        for (Payment p : paymentList) {
            if (p.getType().equals(type) && p.getNumber().equals(number)) {
                typeAlreadyExists = true;
                p.setAmount(p.getAmount().add(amount));
            }
        }
        if (!typeAlreadyExists) {
            paymentList.add(new Payment(type, number, amount));
        }
	}

    public void removeNullAmountPayments() {
        ArrayList<Payment> newPayment = new ArrayList<>(paymentList);
        for (Payment p : newPayment) {
            if (p.getAmount().isEqual(Decimal.ZERO)) {
                paymentList.remove(p);
            }
        }
    }


    private Payment getPaymentListCashPayment(ArrayList<Payment> paymentList) {
		if (paymentList != null) {
			for (Payment p : paymentList) {
				if (p.getType().equals(Payment.PaymentType.CASH))
					return p;
			}
		}
        return null;
    }

    public ArrayList<Payment> handleCashChange(Order order) {
		ArrayList<Payment> result = new ArrayList<>();
		for (Payment payment : paymentList) {
			result.add(new Payment(payment));
		}
		if (paymentValues.getTendered().isGreater(order.getAmount(true))) {
            try {
                if (getPaymentListCashPayment(result) != null) {
                    Decimal newCashAmount = getPaymentListCashPayment(result).getAmount().subtract(paymentValues.getChange());
                    getPaymentListCashPayment(result).setAmount(newCashAmount);
                } else {
                    result.add(new Payment(Payment.PaymentType.CASH, "", paymentValues.getChange().multiply(Decimal.make(-1))));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

		return result;
    }


    public void handleCreditVoucherChange(Order order) {
        String returnCreditVoucher = "-1";
        if (paymentValues.getTendered().isGreater(order.getAmount(true))) {
            try {
                for (Payment p : paymentList) {
                    if (p.getType().equals(Payment.PaymentType.GIFT_CARD) && p.getNumber().equals(returnCreditVoucher)) {
                        p.setAmount(paymentValues.getChange().multiply(Decimal.make(-1)));
                        return;
                    }
                }
                paymentList.add(new Payment(Payment.PaymentType.GIFT_CARD, returnCreditVoucher, paymentValues.getChange().multiply(Decimal.make(-1))));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean hasGiftCard() {
        for (Payment p : paymentList) {
            if (p.getType().equals(Payment.PaymentType.GIFT_CARD)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasOnlyPaymentOfType(Payment.PaymentType type) {
        if (type != null) {
            if(paymentList.size() == 0) {
                return false;
            }
            for (Payment p : paymentList) {
                if (!p.getType().equals(type)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasPaymentOfType(Payment.PaymentType type) {
        if (type != null) {
            for (Payment p : paymentList) {
                if (p.getType().equals(type)) {
                    return true;
                }
            }
        }
        return false;
    }
}
