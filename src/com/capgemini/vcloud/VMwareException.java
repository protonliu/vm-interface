package com.capgemini.vcloud;

public class VMwareException extends Exception {

	private static final long serialVersionUID = 1L;

	private ErrorCode code;

	public VMwareException() {
		super();
	}

	public VMwareException(String message, Throwable cause) {
		super(message, cause);
	}

	public VMwareException(ErrorCode code, Throwable cause) {
		super(code.toString() + ":" + code.getErrorMessage(), cause);
		this.code = code;
	}

	public VMwareException(ErrorCode code, String message) {
		super(code.toString() + ":" + code.getErrorMessage(), new Throwable(
				message));
		this.code = code;
	}

	public ErrorCode getErrorCode() {
		return code;
	}
}
