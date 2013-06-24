package com.capgemini.vcloud.domain;

import com.capgemini.vcloud.ErrorCode;
import com.capgemini.vcloud.VMwareException;

public class Response<T> {
	
	private String status;
	
	private String code;

	private T data;	

	private String message;	

	private Response(T data, String status, String code, String message) {
		this.data = data;
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public static <T> Response<T> success(T data, String message) {
		return new Response<T>(data, "OK", ErrorCode.NONE.toString(), message);
	}

	public static <T> Response<T> fault(T data, ErrorCode code) {
		if (code != null) {
			return new Response<T>(data, "FAULT", code.toString(),
					code.getErrorMessage());
		} else {
			return new Response<T>(data, "FAULT", null, null);
		}
	}

	public static <T> Response<T> fault(T data, String message) {
		return new Response<T>(data, "FAULT", ErrorCode.NONE.toString(),
				message);
	}

	public static <T> Response<T> fault(Exception e) {
		ErrorCode code = null;		
		if (e instanceof VMwareException) {
			code = ((VMwareException) e).getErrorCode();
		}
		if (code == null) {
			code = ErrorCode.NONE;
		}
		StringBuffer message = new StringBuffer(e.toString());
		if (e.getCause() != null) {
			message.append("\n  Cause by: "+ e.getCause().toString());
			for (StackTraceElement ste : e.getCause().getStackTrace()) {
				message.append("\n\t" + ste);
			}
		}
		return new Response<T>(null, "FAULT", code.toString(),
				message.toString());
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
