package no.susoft.mobile.pos.data;

import java.util.Date;

import no.susoft.mobile.pos.json.JSONSerializable;

public class OrderPointer implements JSONSerializable {
	
	public long id;
	public String shopId;
	public String type;
	public String posNo;
	public String salesPersonId;
	public String salesPersonName;
	public String alternativeId;
	public Date date;
	public int areaId;
	public int tableId;
	public String note;
	public Decimal amount;
	
}
