package com.drupal.poc.core.servlets;

import java.io.IOException;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.day.cq.wcm.api.WCMException;
import com.drupal.poc.core.utils.RESTUtils;

@Component(service = Servlet.class, property = {
		Constants.SERVICE_DESCRIPTION + "=Content Migration Servlet",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.resourceTypes="
				+ "drupalpocsite/components/structure/page",
		"sling.servlet.extensions=" + "migrate" })
public class ContentMigrationServlet extends SlingSafeMethodsServlet {
	@Override
	protected void doGet(final SlingHttpServletRequest req,
			final SlingHttpServletResponse resp) throws ServletException,
			IOException {
		final Resource resource = req.getResource();
		try {
			List<String> pageList = RESTUtils.createConent(req);
			resp.setContentType("text/plain");
			for (String path : pageList) {
				resp.getWriter().write(
						"**-----Page created with path:" + path
								+ "-----**\r\n");
			}
		} catch (WCMException | RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
