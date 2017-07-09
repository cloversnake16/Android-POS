package no.susoft.mobile.pos.data;

import no.susoft.mobile.pos.json.JSONSerializable;

public class ProductBundle implements JSONSerializable {

	String productId;
	String refId;
	double qty;

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public double getQty() {
		return qty;
	}

	public void setQty(double qty) {
		this.qty = qty;
	}
}
