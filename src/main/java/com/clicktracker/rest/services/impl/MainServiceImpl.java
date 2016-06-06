package com.clicktracker.rest.services.impl;

import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Clicks;
import com.clicktracker.rest.model.Counter;
import com.clicktracker.rest.model.Platform;
import com.clicktracker.rest.services.MainService;
import com.clicktracker.rest.services.cache.CacheService;
import com.clicktracker.rest.services.dao.BaseDAO;
import com.clicktracker.rest.services.dao.CampaignDAO;
import com.clicktracker.rest.services.dao.CounterDAO;
import com.clicktracker.rest.services.exception.*;
import com.clicktracker.util.SettingsHelper;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import org.apache.commons.validator.routines.UrlValidator;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by klemen.
 */
@Singleton
public class MainServiceImpl implements MainService
{
	private static final Logger log = Logger.getLogger(MainServiceImpl.class.getName());

	private static final int CAMPAIGN_REDIRECT_URL_MAX_LENGTH = 2000;

	@Inject
	private CampaignDAO campaignDAO;

	@Inject
	private CounterDAO counterDAO;

	@Inject
	private CacheService cacheService;

	@Override
	public void createCampaign(Campaign campaign) throws InvalidObjectException
	{
		validateCampaignForCreation(campaign);
		campaignDAO.createCampaign(campaign);
		cacheService.put(campaign.getCampaignId(), campaign);
	}

	@Override
	public void updateCampaign(Campaign campaign)
		throws StaleUpdateException, NotFoundException,
		InvalidObjectException
	{
		validateCampaignForUpdate(campaign);
		try
		{
			campaignDAO.updateCampaign(campaign);
		}
		catch (BaseDAO.StaleUpdateException e)
		{
			throw new StaleUpdateException(e.getMessage(), e);
		}
		catch (BaseDAO.NoDataException e)
		{
			throw new NotFoundException(e.getMessage(), e);
		}
		cacheService.put(campaign.getCampaignId(), campaign);
	}

	@Override
	public void deleteCampaign(String campaignId) throws NotFoundException
	{
		Campaign campaign = campaignDAO.getCampaign(campaignId);
		if (campaign!=null)
		{
			// Delete all counters
			List<Counter> counters = counterDAO.getCounterList(null, campaign);
			for (Counter counter : counters)
			{
				counterDAO.deleteCounter(counter.getCounterId());
			}
			campaignDAO.deleteCampaign(campaignId);
			cacheService.delete(campaignId);
		}
		else
		{
			throw new NotFoundException("no campaign with id " + campaignId);
		}
	}

	@Override
	public Campaign getCampaign(String id) throws NotFoundException
	{
		Campaign campaign = (Campaign) cacheService.get(id);
		if (campaign==null)
		{
			campaign = campaignDAO.getCampaign(id);
			if (campaign==null)
			{
				throw new NotFoundException("No campaign with id " + id);
			}
			cacheService.put(id, campaign);
		}
		return campaign;
	}

	@Override
	public List<Campaign> getCampaignList(Set<Platform> withPlatforms)
	{
		return campaignDAO.getCampaignList(withPlatforms);
	}

	@Override
	public Clicks getNumberOfClicks(Campaign campaign, Platform platform)
	{
		Set<Platform> platforms = null;
		if (platform!=null)
		{
			platforms = new HashSet<Platform>();
			platforms.add(platform);
		}
		List<Counter> counters = counterDAO.getCounterList(platforms, campaign);
		long sumClicks = 0;
		for (Counter counter : counters)
		{
			sumClicks += counter.getNumberOfClicks();
		}
		Clicks clicks = new Clicks();
		clicks.setNumberOfClicks(sumClicks);
		return clicks;
	}

	private void validateCampaignForCreation(Campaign campaign) throws InvalidObjectException
	{
		if (campaign.getRedirectUrl()==null)
		{
			throw new InvalidObjectException("Redirect url is required");
		}
		if (campaign.getRedirectUrl().length()>CAMPAIGN_REDIRECT_URL_MAX_LENGTH)
		{
			throw new InvalidObjectException("Redirect url too long. Max " + CAMPAIGN_REDIRECT_URL_MAX_LENGTH + " supported.");
		}
		if (campaign.getCampaignId()!=null)
		{
			throw new InvalidObjectException("Id should not be set for new objects");
		}
		if (campaign.getVersion()!=0)
		{
			throw new InvalidObjectException("Version should not be modified for new objects");
		}
		if (campaign.getPlatforms()==null)
		{
			throw new InvalidObjectException("At least one platform must be enabled for campaign");
		}
		if (campaign.getPlatforms().size()<1)
		{
			throw new InvalidObjectException("At least one platform must be enabled for campaign");
		}
		String[] schemes = {"http","https"};
		UrlValidator urlValidator = new UrlValidator(schemes,UrlValidator.ALLOW_LOCAL_URLS);
		if (!urlValidator.isValid(campaign.getRedirectUrl()))
		{
			throw new InvalidObjectException("Invalid redirect URL " + campaign.getRedirectUrl());
		}
	}

	private void validateCampaignForUpdate(Campaign campaign) throws InvalidObjectException
	{
		if (campaign.getRedirectUrl()==null)
		{
			throw new InvalidObjectException("Redirect url is required");
		}
		if (campaign.getPlatforms()==null)
		{
			throw new InvalidObjectException("At least one platform must be enabled for campaign");
		}
		if (campaign.getPlatforms().size()<1)
		{
			throw new InvalidObjectException("At least one platform must be enabled for campaign");
		}
		String[] schemes = {"http","https"};
		UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);
		if (!urlValidator.isValid(campaign.getRedirectUrl()))
		{
			throw new InvalidObjectException("Invalid redirect URL " + campaign.getRedirectUrl());
		}
	}

	@Override
	public Campaign recordClick(String platformId, String campaignId)
		throws InvalidCampaignException, InvalidPlatformException,
		ServiceErrorException
	{
		log.finer("Recording click ...");

		Campaign campaign = null;
		try
		{
			campaign = getCampaign(campaignId);
		}
		catch (NotFoundException e)
		{
			throw new InvalidCampaignException(e.getMessage(), e);
		}
		if (campaign==null)
		{
			throw new InvalidCampaignException("Campaign does not exist");
		}
		Platform platform;
		try
		{
			platform = Platform.fromValue(platformId);
		}
		catch (IllegalArgumentException e)
		{
			throw new InvalidPlatformException("Platform does not exist", e);
		}

		if (!campaign.getPlatforms().contains(platform))
		{
			throw new InvalidPlatformException("Platform not enabled on given campaign");
		}

		counterDAO.increaseOrCreateCounter(getRandomShardNumber(), campaign, platform);

		log.finer("Click recorded");

		return campaign;
	}

	private long getRandomShardNumber()
	{
		long maxShards = Long.valueOf(SettingsHelper.getSetting(SettingsHelper.SETTING_KEY_MAX_COUNTER_SHARDS));
		return  ThreadLocalRandom.current().nextLong(maxShards);
	}

	@PostConstruct
	public void postConstruct()
	{
		log.finer("Some startup tasks ... ");
		ObjectifyService.begin();

	}
}
