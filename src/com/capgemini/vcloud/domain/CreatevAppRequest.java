package com.capgemini.vcloud.domain;

public class CreatevAppRequest {

	private String serviceName;

	private String hireDays;

	private String vmSize;

	private String maxWindQuantity;

	private String maxVisitors;

	private String templateName;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getHireDays() {
		return hireDays;
	}

	public void setHireDays(String hireDays) {
		this.hireDays = hireDays;
	}

	public String getVmSize() {
		return vmSize;
	}

	public void setVmSize(String vmSize) {
		this.vmSize = vmSize;
	}

	public String getMaxWindQuantity() {
		return maxWindQuantity;
	}

	public void setMaxWindQuantity(String maxWindQuantity) {
		this.maxWindQuantity = maxWindQuantity;
	}

	public String getMaxVisitors() {
		return maxVisitors;
	}

	public void setMaxVisitors(String maxVisitors) {
		this.maxVisitors = maxVisitors;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String toString() {
		return "\t[\n\t\tserviceName:" + serviceName + "\n\t\thireDays:"
				+ hireDays + "\n\t\ttemplateName:" + templateName + "\n\t]";
	}

}
