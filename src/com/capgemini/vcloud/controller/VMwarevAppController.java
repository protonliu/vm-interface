package com.capgemini.vcloud.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.capgemini.vcloud.VMwareException;
import com.capgemini.vcloud.domain.Response;
import com.capgemini.vcloud.domain.CreatevAppRequest;
import com.capgemini.vcloud.domain.CreatevAppData;
import com.capgemini.vcloud.service.VMwarevAppService;
import com.vmware.vcloud.sdk.VCloudException;
import com.vmware.vcloud.sdk.VM;
import com.vmware.vcloud.sdk.Vapp;
import com.vmware.vcloud.sdk.VirtualDisk;
import com.vmware.vcloud.sdk.VirtualNetworkCard;

@Controller
@RequestMapping()
public class VMwarevAppController extends VMwareController {

	@Autowired
	private VMwarevAppService vMwarevAppService;

	@ResponseBody
	@RequestMapping(value = "/createvAppMock", method = RequestMethod.GET)
	public CreatevAppData createvAppMock(
			@ModelAttribute CreatevAppRequest vmAppEntity) {
		logger.debug("[VCloud Operation=createvAppMock] - Request Data:\n"
				+ vmAppEntity);
		CreatevAppData vmEntity = new CreatevAppData();
		vmEntity.setCustomerName("envision");
		vmEntity.setHireDays("200");
		vmEntity.setServiceName("addVmApp");
		vmEntity.setVappName("envisionApp");
		return vmEntity;
	}

	@ResponseBody
	@RequestMapping(value = "/createvApp", method = RequestMethod.POST)
	public Response<CreatevAppData> createvApp(
			@ModelAttribute CreatevAppRequest request) {
		logger.info("[VCloud Operation=createvApp] - Request Data:\n" + request);
		try {
			Vapp vapp = vMwarevAppService.createvAppFromTemplate(
					request.getServiceName(), request.getTemplateName());
			CreatevAppData data = new CreatevAppData();
			data.setVappName(request.getServiceName());
			data.setHireDays(request.getHireDays());
			String vappId = vapp.getReference().getId();
			data.setVappId(vappId);
			List<VM> vms = vMwarevAppService.findvAppById(vappId)
					.getChildrenVms();
			for (VM vm : vms) {
				CreatevAppData.VmType vmType = new CreatevAppData.VmType();
				vmType.setName(vm.getResource().getName());
				vmType.setCpu(String.valueOf(vm.getCpu().getItemResource()
						.getAllocationUnits().getValue())
						+ " X " + vm.getCpu().getNoOfCpus());
				vmType.setMemory(vm.getMemory().getMemorySize() + " Mb");
				vmType.setOs(String.valueOf(vm.getOperatingSystemSection()
						.getDescription().getValue()));
				vmType.setPowerStatus(String.valueOf(vm.getVMStatus()));
				for (VirtualNetworkCard networkCard : vm.getNetworkCards()) {
					CreatevAppData.NetworkType networkType = new CreatevAppData.NetworkType();
					networkType.setIpAddress(networkCard.getIpAddress());
					networkType.setMacAddress(networkCard.getMacAddress());
					networkType.setName(networkCard.getNetwork());
					vmType.getNetwork().add(networkType);
				}
				for (VirtualDisk disk : vm.getDisks()) {
					CreatevAppData.HardDiskType hardDiskType = new CreatevAppData.HardDiskType();
					if (disk.isHardDisk()) {
						hardDiskType.setSize(disk.getHardDiskSize() + " Mb");
						hardDiskType.setBusType(disk.getHardDiskBusType());
						vmType.getHardDisk().add(hardDiskType);
					}
				}
				data.getVms().add(vmType);
			}
			return Response.success(data,
					"create vApp '" + request.getServiceName()
							+ "' successfully");
		} catch (VMwareException e) {
			logger.error(e.getMessage(), e);
			return Response.fault(e);
		} catch (VCloudException e) {
			logger.error(e.getMessage(), e);
			return Response.fault(e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/deployvAppByName", method = RequestMethod.POST)
	@Deprecated
	public Response<Object> deployvAppByName(@RequestParam String serviceName,
			@RequestParam int action) {
		logger.info("[VCloud Operation=deployvApp] - Request Data:\n\t[\n\t\tserviceName:"
				+ serviceName + "\n\t\taction:" + action + "\n\t]");
		try {
			vMwarevAppService.deployvAppByName(serviceName, action == 1 ? true
					: false);
			return Response.success(null, "deploy vApp '" + serviceName
					+ "' successfully");
		} catch (VMwareException e) {
			logger.error(e.getMessage(), e);
			return Response.fault(e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/deployvAppById", method = RequestMethod.POST)
	public Response<Object> deployvAppById(@RequestParam String vappId,
			@RequestParam int action) {
		logger.info("[VCloud Operation=deployvAppById] - Request Data:\n\t[\n\t\tvappId:"
				+ vappId + "\n\t\taction:" + action + "\n\t]");
		try {
			vMwarevAppService
					.deployvAppById(vappId, action == 1 ? true : false);
			return Response.success(null, (action == 1 ? "" : "un")
					+ "deploy vApp '" + vappId + "' successfully");
		} catch (VMwareException e) {
			logger.error(e.getMessage(), e);
			return Response.fault(e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/deletevAppByName", method = RequestMethod.POST)
	@Deprecated
	public Response<Object> deletevAppByName(@RequestParam String serviceName) {
		logger.info("[VCloud Operation=deletevAppByName] - Request Data:\n\t[\n\t\tserviceName:"
				+ serviceName + "\n\t]");
		try {
			vMwarevAppService.deletevAppByName(serviceName);
			return Response.success(null, "delete vApp '" + serviceName
					+ "' successfully");
		} catch (VMwareException e) {
			logger.error(e.getMessage(), e);
			return Response.fault(e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/deletevAppById", method = RequestMethod.POST)
	public Response<Object> deletevAppById(@RequestParam String vappId) {
		logger.info("[VCloud Operation=deletevAppById] - Request Data:\n\t[\n\t\tvappId:"
				+ vappId + "\n\t]");
		try {
			vMwarevAppService.deletevAppById(vappId);
			return Response.success(null, "delete vApp '" + vappId
					+ "' successfully");
		} catch (VMwareException e) {
			logger.error(e.getMessage(), e);
			return Response.fault(e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/powervAppById", method = RequestMethod.POST)
	public Response<Object> powervAppById(@RequestParam String vappId,
			@RequestParam int action) {
		logger.info("[VCloud Operation=powervAppById] - Request Data:\n\t[\n\t\tvappId:"
				+ vappId + "\n\t\taction:" + action + "\n\t]");
		try {
			vMwarevAppService.powervAppById(vappId, action == 1 ? true : false);
			return Response.success(null, "power "
					+ (action == 1 ? "on" : "off") + " vApp '" + vappId
					+ "' successfully");
		} catch (VMwareException e) {
			logger.error(e.getMessage(), e);
			return Response.fault(e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/findvAppById", method = RequestMethod.POST)
	public Response<CreatevAppData> findvAppById(@RequestParam String vappId) {
		logger.info("[VCloud Operation=findvAppById] - Request Data:\n\t[\n\t\tvappId:"
				+ vappId + "\n\t]");
		try {
			Vapp vapp = vMwarevAppService.findvAppById(vappId);
			CreatevAppData data = new CreatevAppData();
			data.setVappName(vapp.getResource().getName());
			data.setVappId(vapp.getReference().getId());
			List<VM> vms = vapp.getChildrenVms();
			for (VM vm : vms) {
				CreatevAppData.VmType vmType = new CreatevAppData.VmType();
				vmType.setName(vm.getResource().getName());
				vmType.setMemory(vm.getMemory().getMemorySize() + " Mb");
				vmType.setCpu(String.valueOf(vm.getCpu().getItemResource()
						.getAllocationUnits().getValue())
						+ " X " + vm.getCpu().getNoOfCpus());
				vmType.setOs(String.valueOf(vm.getOperatingSystemSection()
						.getDescription().getValue()));
				vmType.setPowerStatus(String.valueOf(vm.getVMStatus()));
				/*
				for (Map.Entry<Integer, String> ip : vm.getIpAddressesById()
						.entrySet()) {
					CreatevAppData.NetworkType networkType = new CreatevAppData.NetworkType();
					networkType.setIpAddress(ip.getValue());
					vmType.getNetwork().add(networkType);
				}*/
				for (VirtualNetworkCard networkCard : vm.getNetworkCards()) {
					CreatevAppData.NetworkType networkType = new CreatevAppData.NetworkType();
					networkType.setIpAddress(networkCard.getIpAddress());
					networkType.setMacAddress(networkCard.getMacAddress());
					networkType.setName(networkCard.getNetwork());
					vmType.getNetwork().add(networkType);
				}
				for (VirtualDisk disk : vm.getDisks()) {
					CreatevAppData.HardDiskType hardDiskType = new CreatevAppData.HardDiskType();
					if (disk.isHardDisk()) {
						hardDiskType.setSize(disk.getHardDiskSize() + " Mb");
						hardDiskType.setBusType(disk.getHardDiskBusType());
						vmType.getHardDisk().add(hardDiskType);
					}
				}
				data.getVms().add(vmType);
			}
			return Response.success(data, "find vApp '" + vappId
					+ "' successfully");
		} catch (VMwareException e) {
			logger.error(e.getMessage(), e);
			return Response.fault(e);
		} catch (VCloudException e) {
			logger.error(e.getMessage(), e);
			return Response.fault(e);
		}
	}
}