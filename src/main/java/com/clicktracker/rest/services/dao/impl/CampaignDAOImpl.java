package com.clicktracker.rest.services.dao.impl;

import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Platform;
import com.clicktracker.rest.services.dao.CampaignDAO;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.inject.Singleton;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.cmd.LoadType;
import com.googlecode.objectify.cmd.Query;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by klemen.
 */
@Singleton
public class CampaignDAOImpl implements CampaignDAO
{
	private static final Logger log = Logger.getLogger(CampaignDAOImpl.class.getName());

	private static final int MAX_ID_GENERATINO_TRIALS = 5;

	@Override
	public void createCampaign(final Campaign campaign)
	{
		ObjectifyService.ofy().transactNew(new VoidWork()
		{
			public void vrun()
			{
				String newId;
				int trials = 0;
				do
				{
					trials++;
					if (trials == MAX_ID_GENERATINO_TRIALS)
					{
						throw new RuntimeException("Cannot geenrate unique id");
					}
					newId = UUID.randomUUID().toString();
				}
				while (ObjectifyService.ofy().load().type(Campaign.class).id(newId).now() != null);
				campaign.setCampaignId(newId);
				campaign.setVersion(campaign.getVersion() + 1);
				campaign.setDateModified(new Date());
				campaign.setDateCreated(new Date());
				ObjectifyService.ofy().save().entity(campaign).now();
			}
		});
	}

	@Override
	public void updateCampaign(final Campaign campaign)
		throws CampaignDAO.StaleUpdateException, CampaignDAO.NoDataException
	{
		try
		{
			ObjectifyService.ofy().transactNew(1, new VoidWork()
			{
				public void vrun()
				{
					Campaign storedCampaign = ObjectifyService.ofy().load().type(Campaign.class).id(campaign.getCampaignId())
						.now();
					if (storedCampaign == null)
					{
						throw new CampaignDAOImpl.NoDataException("Does not exist yet");
					}
					if (storedCampaign.getVersion() != campaign.getVersion())
					{
						throw new CampaignDAOImpl.StaleUpdateException("Cannot update stale data");
					}
					campaign.setVersion(campaign.getVersion() + 1);
					campaign.setDateModified(new Date());
					ObjectifyService.ofy().save().entity(campaign).now();
				}
			});
		}
		catch (StaleUpdateException | ConcurrentModificationException e)
		{
			throw new CampaignDAO.StaleUpdateException("Cannot update stale data", e);
		}
		catch (NoDataException e)
		{
			throw new CampaignDAO.NoDataException("No data", e);
		}
	}

	@Override
	public void deleteCampaign(final String id)
	{
		ObjectifyService.ofy().transactNew(new VoidWork()
		{
			public void vrun()
			{
				Campaign storedCampaign = ObjectifyService.ofy().load().type(Campaign.class).id(id)
					.now();
				if (storedCampaign != null)
				{
					ObjectifyService.ofy().delete().entity(storedCampaign).now();
				}
			}
		});
	}

	@Override
	public Campaign getCampaign(String id)
	{
		return ObjectifyService.ofy().load().type(Campaign.class).id(id).now();
	}

	@Override
	public List<Campaign> getCampaignList(Set<Platform> withPlatforms)
	{
		Query<Campaign> query = ObjectifyService.ofy().consistency(
		ReadPolicy.Consistency.STRONG).load().type(Campaign.class);
		if (withPlatforms!=null)
		{
			query = query.filter("platforms in", withPlatforms);
		}
		List<Campaign> campaignList = query.list();
		return campaignList;
	}

	@PostConstruct
	public void postConstruct()
	{
		log.finer("Registering entities ... ");
		ObjectifyService.register(Campaign.class);
	}

	private static class StaleUpdateException extends RuntimeException
	{
		public StaleUpdateException(String message)
		{
			super(message);
		}
	}

	private static class NoDataException extends RuntimeException
	{
		public NoDataException(String message)
		{
			super(message);
		}
	}
}
