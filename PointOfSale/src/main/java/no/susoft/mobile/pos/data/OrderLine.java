package no.susoft.mobile.pos.data;

import java.io.Serializable;
import java.util.ArrayList;

import no.susoft.mobile.pos.json.JSONSerializable;

public class OrderLine implements JSONSerializable, Serializable {

    private long lineId;
    private long orderId;
    private String shopId;
    private Product product;
    private Discount discount;
    private String text;
    private Decimal price;
    private Decimal quantity;
    private String note;
	private Decimal deliveredQty = Decimal.ZERO;
	private String salesPersonId;
	private ArrayList<OrderLine> components;

	public long getLineId() {
		return lineId;
	}

	public void setLineId(long lineId) {
		this.lineId = lineId;
	}

	public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Decimal getPrice() {
        return price;
    }

    public void setPrice(Decimal price) {
        this.price = price;
    }

    public Decimal getQuantity() {
        return quantity;
    }

    public void setQuantity(Decimal quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

	public Decimal getDeliveredQty() {
		return deliveredQty;
	}

	public void setDeliveredQty(Decimal deliveredQty) {
		this.deliveredQty = deliveredQty;
	}

	public String getSalesPersonId() {
		return salesPersonId;
	}

	public void setSalesPersonId(String salesPersonId) {
		this.salesPersonId = salesPersonId;
	}

	public ArrayList<OrderLine> getComponents() {
		return components;
	}

	public void setComponents(ArrayList<OrderLine> components) {
		this.components = components;
	}

	/**
     * Get amount for current line
     */
    public Decimal getAmount(boolean includeDiscount) {
        final Decimal subtotal = getPrice().multiply(getQuantity());
        // Are we including the discount?
        if (includeDiscount && getDiscount() != null)
            return subtotal.subtract(Discount.calculateDiscount(subtotal, getDiscount().getPercent()));
        else
            return subtotal;
    }

    /**
     * Get the discount amount
     */
    public Decimal getDiscountAmount() {
        Decimal value = Decimal.ZERO;
        if (getDiscount() != null) {
            final Decimal subtotal = getPrice().multiply(getQuantity());
            value = Discount.calculateDiscount(subtotal, getDiscount().getPercent());
        }
        return value;
    }

    public boolean hasNote() {
        return note != null && note.length() > 0;
    }

}
