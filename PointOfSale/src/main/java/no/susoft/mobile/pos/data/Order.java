package no.susoft.mobile.pos.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.susoft.mobile.pos.json.JSONSerializable;

public class Order implements JSONSerializable, Serializable {
	
	private long id;
	private String shopId;
	private String type;
	private String posNo;
	private String salesPersonId;
	private String salesPersonName;
	private String alternativeId;
	private Date date;
	private Customer customer;
	private List<OrderLine> lines = new ArrayList<>();
	private List<Payment> payments = new ArrayList<>();
	private String note;
	private boolean locked;
	private int area;
	private int table;
	private long remoteId;
	private long parkedId;
	private long kitchenOrderId;
	private String localOrderIdentifier;
	private String reconciliationId;
	private boolean useAlternative;
	private String controlCode;
	private String deviceSerialNumber;
	private long receiptId;
	public static final String roundId = "round";

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
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
	
	public long getRemoteId() {
		return remoteId;
	}
	
	public void setRemoteId(long remoteId) {
		this.remoteId = remoteId;
	}
	
	public String getPosNo() {
		return posNo;
	}
	
	public void setPosNo(String posNo) {
		this.posNo = posNo;
	}
	
	public String getSalesPersonId() {
		return salesPersonId;
	}
	
	public void setSalesPersonId(String salesPersonId) {
		this.salesPersonId = salesPersonId;
	}
	
	public String getSalesPersonName() {
		return salesPersonName;
	}
	
	public void setSalesPersonName(String salesPersonName) {
		this.salesPersonName = salesPersonName;
	}
	
	public String getAlternativeId() {
		return alternativeId;
	}
	
	public void setAlternativeId(String alternativeId) {
		this.alternativeId = alternativeId;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Customer getCustomer() {
		return customer;
	}
	
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
	public List<OrderLine> getLines() {
		return lines;
	}
	
	public void setLines(List<OrderLine> lines) {
		this.lines = lines;
	}
	
	public List<Payment> getPayments() {
		return payments;
	}
	
	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}
	
	public String getNote() {
		return note;
	}
	
	public void setNote(String note) {
		this.note = note;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	public int getArea() {
		return area;
	}
	
	public void setArea(int area) {
		this.area = area;
	}
	
	public int getTable() {
		return table;
	}
	
	public void setTable(int table) {
		this.table = table;
	}
	
	public long getParkedId() {
		return parkedId;
	}
	
	public void setParkedId(long parkedId) {
		this.parkedId = parkedId;
	}
	
	public long getKitchenOrderId() {
		return kitchenOrderId;
	}
	
	public void setKitchenOrderId(long kitchenOrderId) {
		this.kitchenOrderId = kitchenOrderId;
	}
	
	public String getReconciliationId() {
		return reconciliationId;
	}
	
	public void setReconciliationId(String reconciliationId) {
		this.reconciliationId = reconciliationId;
	}
	
	public String getLocalOrderIdentifier() {
		return localOrderIdentifier;
	}
	
	public void setLocalOrderIdentifier(String localOrderIdentifier) {
		this.localOrderIdentifier = localOrderIdentifier;
	}

	public void setControlCode(String controlCode) {
		this.controlCode = controlCode;
	}

	public String getControlCode() {
		return controlCode;
	}

	public void setDeviceSerialNumber(String deviceSerialNumber) {
		this.deviceSerialNumber = deviceSerialNumber;
	}

	public String getDeviceSerialNumber() {
		return deviceSerialNumber;
	}

	public void setReceiptId(long receiptId) {
		this.receiptId = receiptId;
	}

	public long getReceiptId() {
		return receiptId;
	}

	public boolean hasReturnLines() {
		for (OrderLine ol : getLines()) {
			if (ol.getQuantity().isNegative() && !ol.getProduct().getId().equalsIgnoreCase(roundId)) {
				return true;
			}
		}
		return false;
	}
	
	public void addOrderLine(OrderLine line) {
		if (line != null && line.getProduct() != null && line.getQuantity() != null) {
			if (!line.getProduct().isMiscellaneous()) {
				OrderLine aLine = getLineWithProduct(line.getProduct());
				if (aLine != null) {
					aLine.setQuantity(aLine.getQuantity().add(line.getQuantity()));
					aLine.setDeliveredQty(aLine.getDeliveredQty().add(line.getDeliveredQty()));
				} else {
					line.setOrderId(getId());
					line.setShopId(getShopId());
					lines.add(line);
				}
			} else {
				line.setOrderId(getId());
				line.setShopId(getShopId());
				lines.add(line);
			}
		}
	}
	
	public OrderLine addOrderLine(Product product) {
		OrderLine line = null;
		if (product != null && product.getId() != null && product.getBarcode() != null) {
			if (product.isMiscellaneous() || product.isBundle()) {
				line = addLineWithOneQty(product);
			} else if (product.isWeighted()) {
				line = addLineWithOneQty(product);
				if (product.getWeight() != null && product.getWeight().isPositive()) {
					line.setQuantity(product.getWeight());
				} else {
					line.setQuantity(Decimal.ZERO);
				}
			} else {
				OrderLine aLine = getLineWithProduct(product);
				if (aLine != null) {
					aLine.setQuantity(aLine.getQuantity().add(Decimal.ONE));
					line = aLine;
				} else {
					line = addLineWithOneQty(product);
				}
			}
		}
		return line;
	}
	
	public boolean hasReturnLineWithProduct(Product product) {
		OrderLine aLine = getLineWithProduct(product);
		return aLine != null && aLine.getQuantity().isNegative();
	}
	
	public boolean hasPositiveLineWithProduct(Product product) {
		OrderLine aLine = getPositiveLineWithProduct(product);
		return aLine != null && aLine.getQuantity().isPositive();
	}
	
	private OrderLine addLineWithOneQty(Product product) {
		OrderLine line = new OrderLine();
		line.setOrderId(getId());
		line.setShopId(getShopId());
		line.setProduct(product);
		line.setText(product.getName());
		if (isUseAlternative() && product.isUseAlternative()) {
			line.setPrice(product.getAlternativePrice());
		} else {
			line.setPrice(product.getPrice());
		}
		line.setQuantity(Decimal.ONE);
		if (product.getDiscount() != null) {
			line.setDiscount(product.getDiscount());
		}
		lines.add(line);
		return line;
	}
	
	public OrderLine addMiscOrderLine(Product product, Decimal qty) {
		OrderLine line = new OrderLine();
		line.setOrderId(getId());
		line.setShopId(getShopId());
		line.setProduct(product);
		line.setText(product.getName());
		line.setPrice(product.getPrice());
		line.setQuantity(qty);
		lines.add(line);
		return line;
	}
	
	public OrderLine removeOrderLine(OrderLine orderLine) {
		lines.remove(orderLine);
		return orderLine;
	}
	
	public void removeRoundingLine() {
		for (OrderLine ol : getLines()) {
			if (ol.getProduct().getId().equalsIgnoreCase(roundId)) {
				removeOrderLine(ol);
				return;
			}
		}
	}
	
	public boolean containsProduct(Product product) {
		boolean result = false;
		if (product != null) {
			for (OrderLine line : lines) {
				if (line.getProduct().getId().equals(product.getId())) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	public OrderLine getLineWithProduct(Product product) {
		OrderLine result = null;
		if (product != null) {
			for (OrderLine line : lines) {
				if (line.getProduct().getId().equals(product.getId())) {
					result = line;
					break;
				}
			}
		}
		return result;
	}
	
	public OrderLine getPositiveLineWithProduct(Product product) {
		OrderLine result = null;
		if (product != null) {
			for (OrderLine line : lines) {
				if (line.getProduct().getId().equals(product.getId()) && line.getQuantity().isPositive()) {
					result = line;
					break;
				}
			}
		}
		return result;
	}
	
	public OrderLine getLineWithProductId(String productId) {
		OrderLine result = null;
		if (productId != null) {
			for (OrderLine line : lines) {
				if (line.getProduct().getId().equals(productId)) {
					result = line;
					break;
				}
			}
		}
		return result;
	}
	
	public Decimal getAmount(boolean includeDiscount) {
		Decimal value = Decimal.ZERO;
		for (final OrderLine line : this.getLines()) {
			value = value.add(line.getAmount(includeDiscount));
			if (line.getComponents() != null) {
				for (OrderLine c : line.getComponents()) {
					value = value.add(c.getAmount(includeDiscount));
				}
			}
		}
		
		return value;
	}
	
	public Decimal getAmountWithoutRoundingLine(boolean includeDiscount) {
		Decimal value = Decimal.ZERO;
		for (final OrderLine line : this.getLines()) {
			if (!line.getProduct().getId().equalsIgnoreCase("round"))
				value = value.add(line.getAmount(includeDiscount));
			
			if (line.getComponents() != null) {
				for (OrderLine c : line.getComponents()) {
					value = value.add(c.getAmount(includeDiscount));
				}
			}
		}
		
		return value;
	}
	
	public Decimal getDiscountAmount() {
		Decimal value = Decimal.ZERO;
		for (OrderLine line : lines) {
			value = value.add(line.getDiscountAmount());
		}
		return value;
	}
	
	public boolean hasLines() {
		return lines != null && lines.size() > 0;
	}
	
	public Decimal getNumberOfItems() {
		Decimal i = Decimal.ZERO;
		for (OrderLine ol : getLines()) {
			i = i.add(ol.getQuantity());
		}
		return i;
	}
	
	public boolean hasGiftCard() {
		for (OrderLine ol : getLines()) {
			if (ol.getProduct().getType().equals("6")) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasCardPayment() {
		if (payments != null && !payments.isEmpty()) {
			for (Payment p : payments) {
				if (p.getType().equals(Payment.PaymentType.CARD)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public int getCardPaymentAmount() {
		if (hasCardPayment()) {
			for (Payment p : payments) {
				if (p.getType().equals(Payment.PaymentType.CARD)) {
					return (p.getAmount().multiply(Decimal.HUNDRED)).toInteger();
				}
			}
		}
		return 0;
	}
	
	public boolean hasNote() {
		return note != null && note.length() > 0;
	}
	
	public boolean isUseAlternative() {
		return useAlternative;
	}
	
	public void setUseAlternative(boolean useAlternative) {
		this.useAlternative = useAlternative;
		if (lines != null) {
			for (OrderLine line : lines) {
				if (line.getProduct() != null) {
					if (!line.getProduct().isMiscellaneous() && line.getProduct().isUseAlternative() && useAlternative) {
						line.setPrice(line.getProduct().getAlternativePrice());
					} else {
						if (!line.getProduct().isMiscellaneous()) {
							line.setPrice(line.getProduct().getPrice());
						}
					}
				}
				if (line.getComponents() != null) {
					for (OrderLine c : line.getComponents()) {
						if (c.getProduct() != null) {
							if (!c.getProduct().isMiscellaneous() && c.getProduct().isUseAlternative() && useAlternative) {
								c.setPrice(c.getProduct().getAlternativePrice());
							} else {
								if (!c.getProduct().isMiscellaneous()) {
									c.setPrice(c.getProduct().getPrice());
								}
							}
						}
					}
				}
			}
		}
	}
}
