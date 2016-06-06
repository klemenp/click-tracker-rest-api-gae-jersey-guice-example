package com.clicktracker.rest;

import com.clicktracker.guice.GuiceRestModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;

/**
 * Created by klemen.
 */
public class RestServletConfig extends GuiceServletContextListener
{
	private static final Logger log                 = Logger.getLogger(RestServletConfig.class.getName());

	private static final String URL_PATTERN         = "/*";
	private static final String JERSEY_ROOT_PACKAGE = RestServletConfig.class
		.getPackage().getName();

	protected static Injector injector;

	@Override
	protected Injector getInjector()
	{
		log.finer("Creating injector");
		injector = Guice.createInjector(new GuiceRestModule(JERSEY_ROOT_PACKAGE, URL_PATTERN));
		return injector;
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent)
	{
		super.contextInitialized(servletContextEvent);
		log.finer("Context inititalized");
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent)
	{
		super.contextDestroyed(servletContextEvent);
		log.finer("Context destroyed");
	}
}
