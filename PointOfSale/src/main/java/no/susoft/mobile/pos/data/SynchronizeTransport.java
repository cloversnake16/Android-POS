package no.susoft.mobile.pos.data;

import java.util.ArrayList;

import no.susoft.mobile.pos.json.JSONSerializable;

public class SynchronizeTransport implements JSONSerializable {

	ArrayList<Product> products = new ArrayList<>();
	ArrayList<Account> accounts = new ArrayList<>();
	ArrayList<QuickLaunchMenuGrid> qlmGrids = new ArrayList<>();
	ArrayList<QuickLaunchMenuCell> qlmCells = new ArrayList<>();
	ArrayList<Customer> customers = new ArrayList<>();
	ArrayList<Area> areas = new ArrayList<>();
	ArrayList<ProductBundle> bundles = new ArrayList<>();

	public ArrayList<Product> getProducts() {
		return products;
	}

	public void setProducts(ArrayList<Product> products) {
		this.products = products;
	}

	public ArrayList<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(ArrayList<Account> accounts) {
		this.accounts = accounts;
	}

	public ArrayList<QuickLaunchMenuGrid> getQlmGrids() {
		return qlmGrids;
	}

	public void setQlmGrids(ArrayList<QuickLaunchMenuGrid> qlmGrids) {
		this.qlmGrids = qlmGrids;
	}

	public ArrayList<QuickLaunchMenuCell> getQlmCells() {
		return qlmCells;
	}

	public void setQlmCells(ArrayList<QuickLaunchMenuCell> qlmCells) {
		this.qlmCells = qlmCells;
	}

	public ArrayList<Customer> getCustomers() {
		return customers;
	}

	public void setCustomers(ArrayList<Customer> customers) {
		this.customers = customers;
	}

	public ArrayList<Area> getAreas() {
		return areas;
	}

	public void setAreas(ArrayList<Area> areas) {
		this.areas = areas;
	}

	public ArrayList<ProductBundle> getBundles() {
		return bundles;
	}

	public void setBundles(ArrayList<ProductBundle> bundles) {
		this.bundles = bundles;
	}
}
