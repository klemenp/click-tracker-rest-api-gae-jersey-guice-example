package com.clicktracker.guice;

import com.clicktracker.rest.AdminAPI;
import com.clicktracker.rest.TrackerAPI;
import com.clicktracker.rest.services.MainService;
import com.clicktracker.rest.services.cache.CacheService;
import com.clicktracker.rest.services.cache.impl.CacheServiceImpl;
import com.clicktracker.rest.services.dao.CampaignDAO;
import com.clicktracker.rest.services.dao.CounterDAO;
import com.clicktracker.rest.services.dao.impl.CampaignDAOImpl;
import com.clicktracker.rest.services.dao.impl.CounterDAOImpl;
import com.clicktracker.rest.services.impl.MainServiceImpl;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.ServletModule;
import com.googlecode.objectify.ObjectifyFilter;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by klemen.
 */
public class GuiceRestModule extends JerseyServletModule
{
	private static final Logger log = Logger.getLogger(GuiceRestModule.class.getName());

	private String rootPackage;
	private String servingUrl;

	public GuiceRestModule(String rootPackage, String servingUrl)
	{
		super();
		this.rootPackage = rootPackage;
		this.servingUrl = servingUrl;
	}

	@Override
	protected void configureServlets()
	{
		log.finer("Configuring servlets");
		super.configureServlets();
		bindListener(Matchers.any(), new PostConstructTypeListener(null));

		Map<String, String> initParams = new HashMap<>();

		initParams.put("jersey.config.server.provider.packages", getRootPackage());
		initParams.put("jersey.config.server.provider.classnames","org.glassfish.jersey.server.gae.GaeFeature");

		// GZIP
		initParams.put("com.sun.jersey.spi.container.ContainerRequestFilters","com.sun.jersey.api.container.filter.GZIPContentEncodingFilter");
		initParams.put("com.sun.jersey.spi.container.ContainerResponseFilters","com.sun.jersey.api.container.filter.GZIPContentEncodingFilter");

		serve(getServingUrl()).with(GuiceContainer.class, initParams);

		bind(TrackerAPI.class);
		bind(AdminAPI.class);

		bind(CacheService.class).to(CacheServiceImpl.class);

		bind(MainService.class).to(MainServiceImpl.class);

		bind(CampaignDAO.class).to(CampaignDAOImpl.class);
		bind(CounterDAO.class).to(CounterDAOImpl.class);

        bind(JacksonJsonProvider.class).in(Singleton.class);

		filter("/*").through(ObjectifyFilter.class);
		bind(ObjectifyFilter.class).in(Singleton.class);

		log.finer("Servlets configurecd");
	}

	private String getServingUrl()
	{
		return servingUrl;
	}

	private String getRootPackage()
	{
		return rootPackage;
	}
}
