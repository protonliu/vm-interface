package com.capgemini.vcloud.util;

import com.vmware.vcloud.api.rest.schema.ReferenceType;
import com.vmware.vcloud.sdk.Catalog;
import com.vmware.vcloud.sdk.CatalogItem;
import com.vmware.vcloud.sdk.Organization;
import com.vmware.vcloud.sdk.VCloudException;
import com.vmware.vcloud.sdk.Vapp;
import com.vmware.vcloud.sdk.VappTemplate;
import com.vmware.vcloud.sdk.VcloudClient;
import com.vmware.vcloud.sdk.Vdc;

public class VMwareUtil {

	/**
	 * Find catalog by Organization name and catalog name.
	 * 
	 * @param vcloudClient
	 * @param orgName
	 * @param catalogName
	 * @return
	 * @throws VCloudException
	 */
	public static final Catalog findCatalogByName(VcloudClient vcloudClient,
			String orgName, String catalogName) throws VCloudException {
		ReferenceType orgRef = vcloudClient.getOrgRefsByName().get(orgName);
		Organization org = Organization.getOrganizationByReference(
				vcloudClient, orgRef);
		ReferenceType catalogRef = null;
		for (ReferenceType ref : org.getCatalogRefs()) {
			if (ref.getName().equals(catalogName))
				catalogRef = ref;
		}
		return Catalog.getCatalogByReference(vcloudClient, catalogRef);
	}

	/**
	 * Find vApp Template by name which mapped on Catalog.
	 * 
	 * @param vcloudClient
	 * @param catalog
	 * @param catalogItemName
	 * @return
	 * @throws VCloudException
	 */
	public static final VappTemplate findVappTemplateByName(
			VcloudClient vcloudClient, Catalog catalog, String catalogItemName)
			throws VCloudException {
		ReferenceType catalogItemRefByName = catalog
				.getCatalogItemRefByName(catalogItemName);
		if (catalogItemRefByName == null) {
			throw new VCloudException("Item '" + catalogItemName
					+ "' not found in catalog");
		}
		CatalogItem catalogItem = CatalogItem.getCatalogItemByReference(
				vcloudClient, catalogItemRefByName);
		VappTemplate vappTemplate = VappTemplate.getVappTemplateByReference(
				vcloudClient, catalogItem.getResource().getEntity());
		return vappTemplate;
	}

	/**
	 * Find Vdc(Virtual Data Center) by Organization name and Vdc name.
	 * 
	 * @param vcloudClient
	 * @param orgName
	 * @param vdcName
	 * @return
	 * @throws VCloudException
	 */
	public static final Vdc findVdcByName(VcloudClient vcloudClient,
			String orgName, String vdcName) throws VCloudException {
		ReferenceType orgRef = vcloudClient.getOrgRefsByName().get(orgName);
		Organization org = Organization.getOrganizationByReference(
				vcloudClient, orgRef);
		ReferenceType vdcRef = org.getVdcRefByName(vdcName);
		return Vdc.getVdcByReference(vcloudClient, vdcRef);
	}

	public static final Vapp findvAppByName(VcloudClient vcloudClient,
			String orgName, String vAppName) throws VCloudException {
		ReferenceType vAppRef = null;
		ReferenceType orgRef = vcloudClient.getOrgRefsByName().get(orgName);
		Organization org = Organization.getOrganizationByReference(
				vcloudClient, orgRef);
		for (ReferenceType vdcRef : org.getVdcRefs()) {
			Vdc vdc = Vdc.getVdcByReference(vcloudClient, vdcRef);
			vAppRef = vdc.getVappRefsByName().get(vAppName);
			if (vAppRef != null) {
				break;
			}
		}
		if (vAppRef == null) {
			throw new VCloudException("vApp '" + vAppName
					+ "' is not found in vDC.");
		}
		return Vapp.getVappByReference(vcloudClient, vAppRef);
	}

}
