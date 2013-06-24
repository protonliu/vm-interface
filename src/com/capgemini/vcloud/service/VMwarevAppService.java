package com.capgemini.vcloud.service;

import com.capgemini.vcloud.VMwareException;
import com.vmware.vcloud.api.rest.schema.ReferenceType;
import com.vmware.vcloud.sdk.Vapp;
import com.vmware.vcloud.sdk.Vdc;

public interface VMwarevAppService {

	public Vapp createvAppFromTemplate(String vAppName, String vAppTemplateName)
			throws VMwareException;

	public void deletevAppByName(String vAppName) throws VMwareException;

	public void deletevAppById(String vAppId) throws VMwareException;

	public void configurevAppVMsIPAddressingMode(
			ReferenceType vAppReferenceType, Vdc vdc) throws VMwareException;

	public void deployvAppByName(String vAppName, boolean deploy)
			throws VMwareException;

	public void deployvAppById(String vAppId, boolean deploy)
			throws VMwareException;

	public Vapp findvAppById(String vAppId) throws VMwareException;

	public void powervAppById(String vAppId, boolean powerOn)
			throws VMwareException;

}
