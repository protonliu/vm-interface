package com.capgemini.vcloud;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

import com.vmware.vcloud.sdk.VCloudException;
import com.vmware.vcloud.sdk.VcloudClient;
import com.vmware.vcloud.sdk.constants.Version;

public class Environment {

	public static final String VMWARE_VCLOUD_VERSION = "vmware.vcloud.version";

	public static final String VMWARE_VCLOUD_LOG = "vmware.vcloud.log";

	public static final String VMWARE_VCLOUD_URL = "vmware.vcloud.url";

	public static final String VMWARE_VCLOUD_USERNAME = "vmware.vcloud.username";

	public static final String VMWARE_VCLOUD_PASSWORD = "vmware.vcloud.password";

	private Properties prop;

	private static VcloudClient client;

	private static Environment environment;

	private Environment() throws VMwareException {
		prop = new Properties();
		FileInputStream fis = null;
		try {
			prop.load(this.getClass().getResourceAsStream(
					"/environment.properties"));
		} catch (Exception e) {
			throw new VMwareException(ErrorCode.ENV_ERR_001, e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private static synchronized Environment instance() throws VMwareException {
		if (environment == null) {
			environment = new Environment();
		}
		return environment;
	}

	private String getProperty(String key) {
		return prop.getProperty(key);
	}

	public static synchronized VcloudClient getVcloudClient()
			throws VMwareException {
		Environment evn = Environment.instance();
		try {
			if (client == null) {
				client = new VcloudClient(evn.getProperty(VMWARE_VCLOUD_URL),
						Version.valueOf(evn.getProperty(VMWARE_VCLOUD_VERSION)));
				VcloudClient.setLogLevel(Level.parse(evn
						.getProperty(VMWARE_VCLOUD_LOG)));
				client.registerScheme("https", 443,
						FakeSSLSocketFactory.getInstance());
			}

			if (!client.extendSession()) {
				System.out.println("login to "
						+ evn.getProperty(VMWARE_VCLOUD_URL) + "...");
				client.login(evn.getProperty(VMWARE_VCLOUD_USERNAME),
						evn.getProperty(VMWARE_VCLOUD_PASSWORD));
			}
		} catch (Exception e) {
			throw new VMwareException(ErrorCode.ENV_ERR_002, e);
		}
		return client;
	}

	public static String getOrganization() throws VMwareException {
		Environment evn = Environment.instance();
		return evn.getProperty("vmware.vcloud.organization.name");
	}

	public static String getCatalog() throws VMwareException {
		Environment evn = Environment.instance();
		return evn.getProperty("vmware.vcloud.catalog.name");
	}

	public static synchronized void destory() {
		if (client != null && client.extendSession()) {
			try {
				client.logout();
			} catch (VCloudException e) {
				e.printStackTrace();
			}
		}
		client = null;
	}
}
