package com.clicktracker.rest.services.dao.impl;

import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Counter;
import com.clicktracker.rest.model.Platform;
import com.clicktracker.rest.services.dao.CounterDAO;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.inject.Singleton;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by klemen.
 */
@Singleton
public class CounterDAOImpl implements CounterDAO
{
	private static final Logger log = Logger.getLogger(CounterDAOImpl.class.getName());

	@Override
	public Counter increaseOrCreateCounter(final long counterShard, final Campaign campaign, final
		Platform platform)
	{
		return ObjectifyService.ofy().transactNew(new Work<Counter>()
		{
			public Counter run()
			{
				Counter counter = ObjectifyService.ofy().load().type(Counter.class).id(Counter.createId(campaign, platform, counterShard)).now();
				if (counter != null)
				{
					counter.increaseNumberOfClicks();
				}
				else
				{
					counter = new Counter(campaign, platform, counterShard);
					counter.increaseNumberOfClicks();
				}
				ObjectifyService.ofy().save().entity(counter).now();
				return counter;
			}
		});
	}

	@Override
	public void deleteCounter(final String id)
	{
		ObjectifyService.ofy().transactNew(new VoidWork()
		{
			public void vrun()
			{
				Counter storedCounter = ObjectifyService.ofy().load().type(Counter.class).id(id)
					.now();
				if (storedCounter != null)
				{
					ObjectifyService.ofy().delete().entity(storedCounter).now();
				}
			}
		});
	}

	@Override
	public Counter getCounter(String id)
	{
		return ObjectifyService.ofy().load().type(Counter.class).id(id).now();
	}

	@Override
	public List<Counter> getCounterList(Set<Platform> withPlatforms, Campaign campaign)
	{
		Query<Counter> query = ObjectifyService.ofy().consistency(
			ReadPolicy.Consistency.STRONG).load().type(Counter.class);
		if (withPlatforms!=null)
		{
			query = query.filter("platform in", withPlatforms);
		}
		if (campaign!=null)
		{
			query = query.filter("campaign", campaign);
		}
		List<Counter> ccounterList = query.list();
		return ccounterList;
	}

	@PostConstruct
	public void postConstruct()
	{
		log.finer("Registering entities ... ");
		ObjectifyService.register(Counter.class);
	}

	private static class StaleUpdateException extends RuntimeException
	{
		public StaleUpdateException(String message)
		{
			super(message);
		}
	}

	private static class DuplicateException extends RuntimeException
	{
		public DuplicateException(String message)
		{
			super(message);
		}
	}
}
