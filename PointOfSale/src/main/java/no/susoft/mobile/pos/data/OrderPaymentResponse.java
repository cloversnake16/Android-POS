package no.susoft.mobile.pos.data;

import java.util.ArrayList;
import java.util.List;

import no.susoft.mobile.pos.json.JSONSerializable;
import no.susoft.mobile.pos.network.Protocol.Message;

public class OrderPaymentResponse implements JSONSerializable {

	Message message = Message.ERROR_UNEXPECTED;
	Order order;
	List<Prepaid> giftCards = new ArrayList<>();

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public List<Prepaid> getGiftCards() {
		return giftCards;
	}

	public void setGiftCards(List<Prepaid> giftCards) {
		this.giftCards = giftCards;
	}
}
