package com.capgemini.vcloud;

public enum ErrorCode {

	NONE(""),

	VAPP_ERR_001("create vApp exception"),

	VAPP_ERR_002("delete vApp exception"),

	VAPP_ERR_003("(un)deploy vApp exception"),

	VAPP_ERR_004("query vApp exception"),

	VAPP_ERR_005("power (ON|OFF) vApp exception"),

	ENV_ERR_001("initialize vCloud environment exception"),

	ENV_ERR_002("initialize vCloud API client exception");

	private String errorMessage;

	private ErrorCode(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
