package com.clicktracker.rest.services;

import com.clicktracker.guice.PostConstructTypeListener;
import com.clicktracker.rest.services.cache.CacheService;
import com.clicktracker.rest.services.cache.impl.CacheServiceImpl;
import com.clicktracker.rest.services.dao.CampaignDAO;
import com.clicktracker.rest.services.dao.CounterDAO;
import com.clicktracker.rest.services.dao.impl.CampaignDAOImpl;
import com.clicktracker.rest.services.dao.impl.CounterDAOImpl;
import com.clicktracker.rest.services.impl.MainServiceImpl;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import org.junit.After;
import org.junit.Before;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by klemen.
 */
public abstract class BaseTest
{
	private LocalServiceTestHelper helper;

	protected static final Logger log = Logger.getLogger(BaseTest.class.getName());

	protected Injector injector;

	@Inject
	protected MainService mainService;

	@Inject
	protected CampaignDAO campaignDAO;

	@Inject
	protected CounterDAO counterDAO;

	protected Closeable session;

	@Before
	public void setUp()
	{
		injector = Guice.createInjector(new AbstractModule()
		{
			@Override
			protected void configure()
			{
				bindListener(Matchers.any(), new PostConstructTypeListener(null));

				bind(MainService.class).to(MainServiceImpl.class);
				bind(CampaignDAO.class).to(CampaignDAOImpl.class);
				bind(CounterDAO.class).to(CounterDAOImpl.class);
				bind(CacheService.class).to(CacheServiceImpl.class);

			}
		});

		injector.injectMembers(this);

		session = ObjectifyService.begin();

		helper =
			new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(1));
		helper.setUp();

	}

	@After
	public void tearDown()
	{
		helper.tearDown();
		session.close();
	}
}
