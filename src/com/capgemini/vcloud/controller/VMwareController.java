package com.capgemini.vcloud.controller;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import org.apache.log4j.Logger;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;

public class VMwareController implements ServletContextAware, ServletConfigAware {

	protected static Logger logger = Logger.getLogger(VMwareController.class);

	private ServletContext servletContext;

	public ServletContext getServletContext() {
		return servletContext;
	}

	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	private ServletConfig servletConfig;

	@Override
	public void setServletConfig(ServletConfig config) {
		this.servletConfig = config;
	}

	@Override
	public void setServletContext(ServletContext context) {
		this.servletContext = context;
	}

}
