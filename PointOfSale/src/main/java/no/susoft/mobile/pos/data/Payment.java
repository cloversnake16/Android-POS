package no.susoft.mobile.pos.data;

import java.io.Serializable;

import no.susoft.mobile.pos.json.JSONSerializable;

public class Payment implements JSONSerializable, Serializable {

    private PaymentType type;
    private Decimal amount;
    private String number;

    private int cardTerminalType = 1;
    private int cardId;
    private String cardName;

    public Payment(PaymentType type, String number, Decimal amount) {
        this.type = type;
        this.number = number;
        this.amount = amount;
    }

	public Payment(Payment payment) {
		this.type = payment.getType();
		this.amount = payment.getAmount();
		this.number = payment.getNumber();
		this.cardTerminalType = payment.getCardTerminalType();
		this.cardId = payment.getCardId();
		this.cardName = payment.getCardName();
	}

    public int getCardTerminalType() {
        return cardTerminalType;
    }

    public Payment setCardTerminalType(int cardTerminalType) {
        this.cardTerminalType = cardTerminalType;return this;
    }

    public PaymentType getType() {
        return type;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }

    public Decimal getAmount() {
        return amount;
    }

    public void setAmount(Decimal amount) {
        this.amount = amount;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getCardId() {
        return cardId;
    }

    public Payment setCardId(int cardId) {
        this.cardId = cardId;return this;
    }

    public String getCardName() {
        return cardName;
    }

    public Payment setCardName(String cardName) {
        this.cardName = cardName;return this;
    }

    public enum PaymentType implements Serializable {
        CASH(1), CARD(2), GIFT_CARD(3), TIP(4), INVOICE(5);
        private int value;

        PaymentType(int value) {
            this.value = value;
        }

		public static PaymentType getType(int value) {
			for (PaymentType type : PaymentType.values()) {
				if (type.value == value) {
					return type;
				}
			}
			throw new IllegalArgumentException("Type not found.");
		}

		public int getValue() {
			return value;
		}
	}
}
