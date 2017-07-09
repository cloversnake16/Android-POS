package no.susoft.mobile.pos.data;

import java.util.ArrayList;

import no.susoft.mobile.pos.json.JSONSerializable;

public class Reconciliation implements JSONSerializable {
	
	public String id;
	public String shopId;
	public String content;
	public double amount;
	public ArrayList<Long> orders;
	
}
