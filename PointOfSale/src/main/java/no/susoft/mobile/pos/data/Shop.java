package no.susoft.mobile.pos.data;

import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * This class represents a shop.
 * @author Yesod
 */
public class Shop implements JSONSerializable {

	// The shop name.
	private final String name;
	// The shop id.
	private final String id;
	// The shop customer directory id.
	private final String customer_directory_id;
	// Is this shop a point-of-sale?
	private final boolean isPOS;
	private String orgNo;
	private String phone;
	private String address;
	private String zip;
	private String city;

	/**
	 * @param id
	 * @param name
	 * @param customer_directory_id
	 * @param is_point_of_sale
	 */
	public Shop(String id, String name, String customer_directory_id, boolean is_point_of_sale) {
		this.id = id;
		this.name = name;
		this.customer_directory_id = customer_directory_id;
		this.isPOS = is_point_of_sale;
	}

	/**
	 * Get the shop customer directory id.
	 * @return
	 */
	public String getCustomerDirectoryID() {
		return this.customer_directory_id;
	}

	/**
	 * Get the shop id.
	 * @return
	 */
	public String getID() {
		return this.id;
	}

	/**
	 * Get the shop name.
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return whether this shop is a point-of-sale.
	 * @return
	 */
	public boolean isPointOfSale() {
		return this.isPOS;
	}

	public String getOrgNo() {
		return orgNo;
	}

	public void setOrgNo(String orgNo) {
		this.orgNo = orgNo;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String toString() {
		return name + " [" + id + "]";
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Shop) {
			Shop shop = (Shop) object;

			if (shop.id.equals(this.id)) {
				if (shop.name.equals(this.name)) {
					return true;
				}
			}
		}
		return false;
	}
}