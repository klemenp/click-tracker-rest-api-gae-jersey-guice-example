package com.clicktracker.rest.services.cache.impl;

import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.services.cache.CacheService;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.inject.Singleton;
import com.googlecode.objectify.ObjectifyService;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by klemen.
 */
@Singleton
public class CacheServiceImpl implements CacheService
{
	private static final Logger log = Logger.getLogger(CacheServiceImpl.class.getName());

	private MemcacheService syncCache;

	public Object get(String key)
	{
		return syncCache.get(key);
	}

	public Map<String,Object> getAll(Set<String> keys)
	{
		return  syncCache.getAll(keys);
	}


	public boolean delete(String key)
	{
		return syncCache.delete(key);
	}

	public void put(String key, Object value)
	{
		syncCache.put(key, value);
	}

	@PostConstruct
	public void postConstruct()
	{
		log.finer("Configuring cache ... ");
		syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(
			Level.INFO));
	}
}
