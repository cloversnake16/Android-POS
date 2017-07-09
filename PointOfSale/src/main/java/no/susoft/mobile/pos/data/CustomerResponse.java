package no.susoft.mobile.pos.data;

import no.susoft.mobile.pos.json.JSONSerializable;
import no.susoft.mobile.pos.network.Protocol.Message;

public class CustomerResponse implements JSONSerializable {

	Message message = Message.ERROR_UNEXPECTED;
	Customer customer;

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}
