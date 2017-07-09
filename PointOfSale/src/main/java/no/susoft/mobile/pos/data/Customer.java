package no.susoft.mobile.pos.data;

import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * This class represents a customer.
 * @author Yesod
 */
public final class Customer implements JSONSerializable {

	// The customer id.
	private String id;
	// The customer first name.
	private String firstName;
	// The customer last name.
	private String lastName;
	// The customer email.
	private String email;
	// The customer address.
	private String address1;
	private String address2;
	private String zip;
	// The customer phone number.
	private String phone;
	// The customer mobile number.
	private String mobile;
	// The customer alternative id.
	private String alternativeId;
	// Is company
	private boolean company;
	// Shop
	private String shopId;

	/**
	 * Build a customer instance.
	 */
	public Customer() {
		this.setId(null);
		this.setFirstName(null);
		this.setLastName(null);
		this.setEmail(null);
		this.setAddress1(null);
		this.setPhone(null);
		this.setMobile(null);
        this.setShopId(null);
	}

	/**
	 * Get the customer id.
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the customer id.
	 * @param value
	 */
	public void setId(String value) {
		this.id = this.formatValue(value);
	}

	/**
	 * Get the customer first name.
	 * @return
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Set the customer first name.
	 * @param value
	 */
	public void setFirstName(String value) {
		this.firstName = this.formatValue(value);
	}

	/**
	 * Get the customer first name.
	 * @return
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Set the customer last name.
	 * @param value
	 */
	public void setLastName(String value) {
		this.lastName = this.formatValue(value);
	}

	/**
	 * Get the customer email.
	 * @return
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * Set the customer email.
	 * @param value
	 */
	public void setEmail(String value) {
		this.email = this.formatValue(value);
	}
	
	public String getAddress1() {
		return address1;
	}
	
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	
	public String getAddress2() {
		return address2;
	}
	
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	
	public String getZip() {
		return zip;
	}
	
	public void setZip(String zip) {
		this.zip = zip;
	}
	
	/**
	 * Get the customer phone number.
	 * @return
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Set the customer phone number.
	 * @param value
	 */
	public void setPhone(String value) {
		this.phone = this.formatValue(value);
	}

	/**
	 * Get the customer mobile.
	 * @return
	 */
	public String getMobile() {
		return this.mobile;
	}
	
	/**
	 * Set the customer mobile.
	 * @param value
	 */
	public void setMobile(String value) {
		this.mobile = this.formatValue(value);
	}

	public String getAlternativeId() {
		return alternativeId;
	}

	public void setAlternativeId(String alternativeId) {
		this.alternativeId = alternativeId;
	}

	/**
	 * Is company customer
	 * @return
	 */
	public boolean isCompany() {
		return company;
	}

	/**
	 * Set customer as company
	 * @param company
	 */
	public void setCompany(boolean company) {
		this.company = company;
	}

	public String getShopId() {
		return shopId;
	}

	public void setShopId(String shopId) {
		this.shopId = shopId;
	}

	/**
	 * Get customer name for views
	 * @return
	 */
	public String getName() {
		if (isCompany()) {
			return formatValue(getLastName());
		}
		return formatValue(getFirstName() + " " + getLastName());
	}

	/**
	 * Format the value to be non-null and without whitespace.
	 * @param value
	 * @return
	 */
	private String formatValue(String value) {
		return this.formatValue(value, "");
	}
	
	/**
	 * If 'value' is null or empty, return the given default, else return 'value'.
	 * @param value
	 * @return
	 */
	private String formatValue(String value, String def) {
		return value == null || value.trim().isEmpty() ? def : value.trim();
	}

}