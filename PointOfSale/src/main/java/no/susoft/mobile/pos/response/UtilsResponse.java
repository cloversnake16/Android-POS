package no.susoft.mobile.pos.response;

import java.util.Date;

public class UtilsResponse {
	
	public UtilsResponse() {
	}
	
	public static StatusResponse successResponse(String result, String desciption) {
		StatusResponse response = new StatusResponse();
		response.setResult(result);
		response.setStatusCode(APIStatus.OK.getCode());
		response.setDescription(desciption);
		return response;
	}
	
	public static StatusResponse successResponse(Object result, String desciption) {
		StatusResponse response = new StatusResponse();
		response.setResult(result);
		response.setStatusCode(APIStatus.OK.getCode());
		response.setDescription(desciption);
		return response;
	}
	
	public static StatusResponse successResponse(Object result, String desciption, Long totalRecords) {
		StatusResponse response = new StatusResponse();
		response.setResult(result);
		response.setStatusCode(APIStatus.OK.getCode());
		response.setDescription(desciption);
		response.setTotalRecords(totalRecords);
		response.setServerTime(new Date().getTime());
		return response;
	}
	
	public static StatusResponse errorResponse(int errorCode, String result, String desciption) {
		StatusResponse response = new StatusResponse();
		response.setResult(result);
		response.setStatusCode(errorCode);
		response.setDescription(desciption);
		return response;
	}
	
	public static StatusResponse errorResponse(String desciption) {
		StatusResponse response = new StatusResponse();
		response.setResult(APIStatus.INVALID_PARAMETER.getDescription());
		response.setStatusCode(APIStatus.INVALID_PARAMETER.getCode());
		response.setDescription(desciption);
		return response;
	}
	
}
