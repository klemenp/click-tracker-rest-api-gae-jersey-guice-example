package com.clicktracker.rest.services.cache;

import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Platform;
import com.clicktracker.rest.services.dao.CampaignDAO;
import com.clicktracker.rest.services.exception.InvalidObjectException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by klemen.
 */
public interface CacheService
{
	Object get(String key);
	Map<String,Object> getAll(Set<String> keys);
	void put(String key, Object value);
	boolean delete(String key);
}
