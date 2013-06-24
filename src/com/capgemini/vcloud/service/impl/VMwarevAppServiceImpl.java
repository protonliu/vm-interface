package com.capgemini.vcloud.service.impl;

import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.xml.bind.JAXBElement;
import com.vmware.vcloud.api.rest.schema.ObjectFactory;
import com.vmware.vcloud.api.rest.schema.ovf.SectionType;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import com.capgemini.vcloud.Environment;
import com.capgemini.vcloud.ErrorCode;
import com.capgemini.vcloud.VMwareException;
import com.capgemini.vcloud.service.VMwarevAppService;
import com.capgemini.vcloud.util.VMwareUtil;
import com.vmware.vcloud.api.rest.schema.InstantiateVAppTemplateParamsType;
import com.vmware.vcloud.api.rest.schema.InstantiationParamsType;
import com.vmware.vcloud.api.rest.schema.NetworkConfigSectionType;
import com.vmware.vcloud.api.rest.schema.NetworkConfigurationType;
import com.vmware.vcloud.api.rest.schema.NetworkConnectionSectionType;
import com.vmware.vcloud.api.rest.schema.NetworkConnectionType;
import com.vmware.vcloud.api.rest.schema.ReferenceType;
import com.vmware.vcloud.api.rest.schema.VAppNetworkConfigurationType;
import com.vmware.vcloud.api.rest.schema.ovf.MsgType;
import com.vmware.vcloud.sdk.Catalog;
import com.vmware.vcloud.sdk.Task;
import com.vmware.vcloud.sdk.VCloudException;
import com.vmware.vcloud.sdk.VM;
import com.vmware.vcloud.sdk.Vapp;
import com.vmware.vcloud.sdk.VappTemplate;
import com.vmware.vcloud.sdk.VcloudClient;
import com.vmware.vcloud.sdk.Vdc;
import com.vmware.vcloud.sdk.constants.FenceModeValuesType;
import com.vmware.vcloud.sdk.constants.IpAddressAllocationModeType;
import com.vmware.vcloud.sdk.constants.UndeployPowerActionType;

@Service
public class VMwarevAppServiceImpl implements VMwarevAppService {

	protected static Logger logger = Logger.getLogger(VMwarevAppService.class);

	@Override
	public Vapp createvAppFromTemplate(String vAppName, String vAppTemplateName)
			throws VMwareException {
		Vapp vapp = null;
		VcloudClient client = Environment.getVcloudClient();
		logger.info("Task(create vApp '" + vAppName + "' from template '"
				+ vAppTemplateName + "'): start...");
		try {
			// get catalog which stores vApp template mapping
			Catalog catalog = VMwareUtil.findCatalogByName(client,
					Environment.getOrganization(), Environment.getCatalog());
			// get vApp template by template name, this name is mapped in
			// catalog with 'catalog item name'
			VappTemplate template = VMwareUtil.findVappTemplateByName(client,
					catalog, vAppTemplateName);
			// get vdc which defined in template
			Vdc vdc = Vdc.getVdcByReference(client, template.getVdcReference());

			NetworkConfigurationType networkConfiguration = new NetworkConfigurationType();
			if (vdc.getAvailableNetworkRefs().size() == 0) {
				throw new VMwareException(ErrorCode.VAPP_ERR_001,
						"No Networks in vdc '" + vdc.getReference().getName()
								+ "'");
			}
			// specify the NetworkConfiguration for the vApp network
			networkConfiguration.setParentNetwork(vdc.getAvailableNetworkRefs()
					.iterator().next());
			networkConfiguration.setFenceMode(FenceModeValuesType.BRIDGED
					.value());
			VAppNetworkConfigurationType vAppNetworkConfiguration = new VAppNetworkConfigurationType();
			vAppNetworkConfiguration.setConfiguration(networkConfiguration);
			vAppNetworkConfiguration.setNetworkName(vdc
					.getAvailableNetworkRefs().iterator().next().getName());

			// fill in the NetworkConfigSection
			NetworkConfigSectionType networkConfigSection = new NetworkConfigSectionType();
			MsgType networkInfo = new MsgType();
			networkConfigSection.setInfo(networkInfo);
			List<VAppNetworkConfigurationType> vAppNetworkConfigs = networkConfigSection
					.getNetworkConfig();
			vAppNetworkConfigs.add(vAppNetworkConfiguration);

			// fill in remaining InstantititonParams (name, Source)
			InstantiationParamsType instantiationParams = new InstantiationParamsType();
			List<JAXBElement<? extends SectionType>> sections = instantiationParams
					.getSection();
			sections.add(new ObjectFactory()
					.createNetworkConfigSection(networkConfigSection));

			// create the request body (InstantiateVAppTemplateParams)
			InstantiateVAppTemplateParamsType instVappTemplParams = new InstantiateVAppTemplateParamsType();
			instVappTemplParams.setName(vAppName);
			instVappTemplParams.setSource(template.getReference());
			instVappTemplParams.setInstantiationParams(instantiationParams);
			logger.info("Task(create vApp '" + vAppName + "' from template '"
					+ vAppTemplateName + "'): network configuration is OK.");
			// make the request, and get an href to the vApp in return
			vapp = vdc.instantiateVappTemplate(instVappTemplParams);
			List<Task> tasks = vapp.getTasks();
			if (tasks.size() > 0) {
				tasks.get(0).waitForTask(0);
			}
			logger.info("Task(create vApp '" + vAppName + "' from template '"
					+ vAppTemplateName + "'): instantiate vApp is OK.");
			this.configurevAppVMsIPAddressingMode(vapp.getReference(), vdc);
			logger.info("Task(create vApp '" + vAppName + "' from template '"
					+ vAppTemplateName + "'): VMs' ip addresses setting is OK.");
			logger.info("Task(create vApp '" + vAppName + "' from template '"
					+ vAppTemplateName + "'): completed.");
		} catch (VCloudException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_001, e);
		} catch (TimeoutException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_001, e);
		} catch (VMwareException e) {
			throw e;
		}
		return vapp;
	}

	@Override
	public void deletevAppByName(String vAppName) throws VMwareException {
		Vapp vapp = null;
		VcloudClient client = Environment.getVcloudClient();
		logger.info("Task(delete vApp '" + vAppName + "'): start...");
		try {
			vapp = VMwareUtil.findvAppByName(client,
					Environment.getOrganization(), vAppName);
			vapp.delete().waitForTask(0);
			logger.info("Task(delete vApp '" + vAppName + "'): completed.");
		} catch (VCloudException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_002, e);
		} catch (TimeoutException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_002, e);
		}
	}

	@Override
	public void configurevAppVMsIPAddressingMode(
			ReferenceType vAppReferenceType, Vdc vdc) throws VMwareException {
		logger.info("Configuring VM Ip Addressing Mode...");
		VcloudClient client = Environment.getVcloudClient();
		try {
			Vapp vapp = Vapp.getVappByReference(client, vAppReferenceType);
			List<VM> childVms = vapp.getChildrenVms();
			for (VM childVm : childVms) {
				NetworkConnectionSectionType networkConnectionSection = childVm
						.getNetworkConnectionSection();
				List<NetworkConnectionType> networkConnections = networkConnectionSection
						.getNetworkConnection();
				for (NetworkConnectionType networkConnection : networkConnections) {
					networkConnection
							.setIpAddressAllocationMode(IpAddressAllocationModeType.POOL
									.value());
					networkConnection.setNetwork(vdc.getAvailableNetworkRefs()
							.iterator().next().getName());
				}
				childVm.updateSection(networkConnectionSection).waitForTask(0);
				StringBuffer sb = new StringBuffer("\n\tVM '"
						+ childVm.getResource().getName() + "' ip addresses:(");
				for (String ip : VM
						.getVMByReference(client, childVm.getReference())
						.getIpAddressesById().values()) {
					sb.append(ip).append(",");
				}
				sb.append(")");
				logger.info(sb.toString());
			}
		} catch (VCloudException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_001, e);
		} catch (TimeoutException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_001, e);
		}
	}

	@Override
	public void deployvAppByName(String vAppName, boolean deploy)
			throws VMwareException {
		Vapp vapp = null;
		VcloudClient client = Environment.getVcloudClient();
		logger.info("Task(" + (deploy ? "" : "un") + "deploy vApp '" + vAppName
				+ "'): start...");
		try {
			vapp = VMwareUtil.findvAppByName(client,
					Environment.getOrganization(), vAppName);
			if (deploy) {
				vapp.deploy(false, 1000000, false).waitForTask(0);
			} else {
				vapp.undeploy(UndeployPowerActionType.FORCE).waitForTask(0);
			}
			logger.info("Task(" + (deploy ? "" : "un") + "deploy vApp '"
					+ vAppName + "'): completed.");
		} catch (VCloudException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_003, e);
		} catch (TimeoutException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_003, e);
		}
	}

	@Override
	public Vapp findvAppById(String vAppId) throws VMwareException {
		VcloudClient client = Environment.getVcloudClient();
		Vapp vapp = null;
		try {
			vapp = Vapp.getVappById(client, vAppId);
		} catch (VCloudException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_004, e);
		}
		return vapp;
	}

	@Override
	public void deployvAppById(String vAppId, boolean deploy)
			throws VMwareException {
		Vapp vapp = null;
		logger.info("Task(" + (deploy ? "" : "un") + "deploy vApp '" + vAppId
				+ "'): start...");
		try {
			vapp = this.findvAppById(vAppId);
			if (deploy) {
				vapp.deploy(false, 1000000, false).waitForTask(0);
			} else {
				vapp.undeploy(UndeployPowerActionType.FORCE).waitForTask(0);
			}
			logger.info("Task(" + (deploy ? "" : "un") + "deploy vApp '"
					+ vAppId + "'): completed.");
		} catch (VCloudException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_003, e);
		} catch (TimeoutException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_003, e);
		}
	}

	@Override
	public void deletevAppById(String vAppId) throws VMwareException {
		Vapp vapp = null;		
		logger.info("Task(delete vApp '" + vAppId + "'): start...");
		try {
			vapp = this.findvAppById(vAppId);
			vapp.delete().waitForTask(0);
			logger.info("Task(delete vApp '" + vAppId + "'): completed.");
		} catch (VCloudException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_002, e);
		} catch (TimeoutException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_002, e);
		}
	}

	@Override
	public void powervAppById(String vAppId, boolean powerOn)
			throws VMwareException {
		Vapp vapp = null;
		logger.info("Task(power " + (powerOn ? "on" : "off") + " vApp '"
				+ vAppId + "'): start...");
		try {
			vapp = this.findvAppById(vAppId);
			if (powerOn) {
				vapp.powerOn().waitForTask(0);
			} else {
				vapp.powerOff().waitForTask(0);
			}
		} catch (VCloudException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_005, e);
		} catch (TimeoutException e) {
			throw new VMwareException(ErrorCode.VAPP_ERR_005, e);
		}
	}

}
