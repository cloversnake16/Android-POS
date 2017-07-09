package no.susoft.mobile.pos.response;

import no.susoft.mobile.pos.json.JSONSerializable;

public class StatusResponse<T> implements JSONSerializable{
	
	private T result;
	private String description;
	private int statusCode;
	private Long totalRecords;
	private Long serverTime;
	
	public StatusResponse() {
	}
	
	public StatusResponse(APIStatus status) {
		this.statusCode = status.getCode();
		this.description = status.getDescription();
	}
	
	public StatusResponse(int status, T result) {
		this.statusCode = status;
		this.result = result;
	}
	
	public StatusResponse(int status, T result, long totalRecords) {
		this.statusCode = status;
		this.result = result;
		this.totalRecords = new Long(totalRecords);
	}
	
	public StatusResponse(T result, String description, int statusCode) {
		this.result = result;
		this.description = description;
		this.statusCode = statusCode;
	}
	
	public StatusResponse(int status, String desc) {
		this.statusCode = status;
		this.description = desc;
	}
	
	public void setStatusCode(int status) {
		this.statusCode = status;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	
	public void setResult(T result) {
		this.result = result;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public T getResult() {
		return result;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Long getTotalRecords() {
		return totalRecords;
	}
	
	public void setTotalRecords(Long totalRecords) {
		this.totalRecords = totalRecords;
	}
	
	public Long getServerTime() {
		return serverTime;
	}
	
	public void setServerTime(Long serverTime) {
		this.serverTime = serverTime;
	}
	
}
