package no.susoft.mobile.pos.data;

import no.susoft.mobile.pos.json.JSONSerializable;

public class Parameter implements JSONSerializable {

	public static String LAST_SYNC_DATE = "LAST_SYNC_DATE";

    private String key;
    private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}