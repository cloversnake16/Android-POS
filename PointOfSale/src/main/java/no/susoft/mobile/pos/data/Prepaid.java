package no.susoft.mobile.pos.data;

import java.util.Date;

import no.susoft.mobile.pos.json.JSONSerializable;

public class Prepaid implements JSONSerializable {

	private String shopId;
	private String type;
	private long id;
	private String number;
	private Decimal issuedAmount;
	private Date issuedDate;
	private Date dueDate;
	private long orderNo;
	private String salespersonId;
	private String customerId;
	private Decimal amount;
	private Date lastUsedDate;

	public String getShopId() {
		return shopId;
	}

	public void setShopId(String shopId) {
		this.shopId = shopId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Decimal getIssuedAmount() {
		return issuedAmount;
	}

	public void setIssuedAmount(Decimal issuedAmount) {
		this.issuedAmount = issuedAmount;
	}

	public Date getIssuedDate() {
		return issuedDate;
	}

	public void setIssuedDate(Date issuedDate) {
		this.issuedDate = issuedDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public long getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(long orderNo) {
		this.orderNo = orderNo;
	}

	public String getSalespersonId() {
		return salespersonId;
	}

	public void setSalespersonId(String salespersonId) {
		this.salespersonId = salespersonId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public Decimal getAmount() {
		return amount;
	}

	public void setAmount(Decimal amount) {
		this.amount = amount;
	}

	public Date getLastUsedDate() {
		return lastUsedDate;
	}

	public void setLastUsedDate(Date lastUsedDate) {
		this.lastUsedDate = lastUsedDate;
	}
}
