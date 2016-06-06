package com.clicktracker.rest.services.dao;

import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Counter;
import com.clicktracker.rest.model.Platform;

import java.util.List;
import java.util.Set;

/**
 * Created by klemen.
 */
public interface CounterDAO extends BaseDAO
{
	Counter increaseOrCreateCounter(final long counterShard, final Campaign campaign, final Platform platform);
	void deleteCounter(final String id);
	Counter getCounter(String id);
	List<Counter> getCounterList(Set<Platform> withPlatforms, Campaign campaign);
}
