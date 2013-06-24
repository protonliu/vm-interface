package com.capgemini.vcloud.domain;

import java.util.ArrayList;
import java.util.List;

public final class CreatevAppData {
	
	private String vappId;	

	private String customerName;

	private String vappName;

	private String hireDays;

	private String serviceName;

	private List<VmType> vms = new ArrayList<VmType>();

	public List<VmType> getVms() {
		return vms;
	}

	public void setVms(List<VmType> vms) {
		this.vms = vms;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getVappName() {
		return vappName;
	}

	public void setVappName(String vappName) {
		this.vappName = vappName;
	}

	public String getHireDays() {
		return hireDays;
	}

	public void setHireDays(String hireDays) {
		this.hireDays = hireDays;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getVappId() {
		return vappId;
	}

	public void setVappId(String vappId) {
		this.vappId = vappId;
	}

	public final static class VmType {

		private String name;

		private String cpu;

		private String os;
		
		private String memory;		

		private String powerStatus;

		private List<NetworkType> network = new ArrayList<NetworkType>();
		
		private List<HardDiskType> hardDisk = new  ArrayList<HardDiskType>();		

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCpu() {
			return cpu;
		}

		public void setCpu(String cpu) {
			this.cpu = cpu;
		}

		public String getOs() {
			return os;
		}

		public void setOs(String os) {
			this.os = os;
		}

		public String getPowerStatus() {
			return powerStatus;
		}

		public void setPowerStatus(String powerStatus) {
			this.powerStatus = powerStatus;
		}

		public List<NetworkType> getNetwork() {
			return network;
		}

		public void setNetwork(List<NetworkType> network) {
			this.network = network;
		}
		
		public String getMemory() {
			return memory;
		}

		public void setMemory(String memory) {
			this.memory = memory;
		}
		
		public List<HardDiskType> getHardDisk() {
			return hardDisk;
		}

		public void setHardDisk(List<HardDiskType> hardDisk) {
			this.hardDisk = hardDisk;
		}
	}

	public final static class NetworkType {
		
		private String name;		

		private String ipAddress;
		
		private String macAddress;
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getMacAddress() {
			return macAddress;
		}

		public void setMacAddress(String macAddress) {
			this.macAddress = macAddress;
		}

		public String getIpAddress() {
			return ipAddress;
		}

		public void setIpAddress(String ipAddress) {
			this.ipAddress = ipAddress;
		}
	}
	
	public final static class HardDiskType {
		
		private String size;
		
		private String busType;

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public String getBusType() {
			return busType;
		}

		public void setBusType(String busType) {
			this.busType = busType;
		}		
	}

}
