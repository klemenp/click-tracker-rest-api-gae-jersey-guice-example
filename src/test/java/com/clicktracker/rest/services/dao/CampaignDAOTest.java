package com.clicktracker.rest.services.dao;

import com.clicktracker.rest.BaseAPI;
import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Platform;
import com.clicktracker.rest.services.BaseTest;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by klemen.
 */
public class CampaignDAOTest extends BaseTest
{
	@Test
	public void testCreateCampaign()
	{
		log.info("Running testCreateCampaign");

		Campaign campaign = new Campaign();
		campaign.setRedirectUrl("http://localhost/redirect");
		campaign.getPlatforms().add(Platform.ANDROID);
		campaignDAO.createCampaign(campaign);

		Campaign storedCampaign = campaignDAO.getCampaign(campaign.getCampaignId());
		assertNotNull(storedCampaign);
		assertEquals(campaign.getCampaignId(), storedCampaign.getCampaignId());
		assertEquals(campaign.getRedirectUrl(), storedCampaign.getRedirectUrl());
	}

	@Test
	public void testGetCampaignList()
	{
		log.info("Running testGetCampaignList");

		List<Campaign> campaignList  = campaignDAO.getCampaignList(null);
		assertEquals(0, campaignList.size());

		Campaign campaign = new Campaign();
		campaign.setRedirectUrl("http://localhost/redirect");
		campaign.getPlatforms().add(Platform.ANDROID);
		campaignDAO.createCampaign(campaign);
		Campaign storedCampaign = campaignDAO.getCampaign(campaign.getCampaignId());
		assertNotNull(storedCampaign);
		// Wait, lists are eventually consistent
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		List<Campaign> newCampaignList  = campaignDAO.getCampaignList(null);

		assertEquals(1,newCampaignList.size()-campaignList.size());

		campaign = new Campaign();
		campaign.setRedirectUrl("http://localhost/redirect");
		campaign.getPlatforms().add(Platform.ANDROID);
		campaignDAO.createCampaign(campaign);
		// Wait, lists are eventually consistent
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		List<Campaign> newCampaignList2  = campaignDAO.getCampaignList(null);

		assertEquals(2,newCampaignList2.size()-campaignList.size());
	}

	@Test
	public void testUpdateCampaign() throws Exception
	{
		log.info("Running testUpdateCampaign");

		Campaign campaign = new Campaign();
		campaign.setRedirectUrl("http://localhost/redirect");
		campaign.getPlatforms().add(Platform.ANDROID);
		long oldVersion = campaign.getVersion();
		campaignDAO.createCampaign(campaign);

		Campaign storedCampaign = campaignDAO.getCampaign(campaign.getCampaignId());
		assertNotNull(storedCampaign);
		storedCampaign.setRedirectUrl("http://localhost/redirect2");

		campaignDAO.updateCampaign(storedCampaign);

		// Create stale data
		Campaign staleCampaign = new Campaign();
		staleCampaign.setCampaignId(campaign.getCampaignId());
		staleCampaign.setRedirectUrl("redirect url again");
		staleCampaign.getPlatforms().add(Platform.ANDROID);
		staleCampaign.setVersion(oldVersion);

		// test stale update prevention
		try
		{
			campaignDAO.updateCampaign(staleCampaign);
			fail("Stale data, Exception should be thrown.");
		}
		catch (Exception e)
		{

		}

		// test update with correct version
		staleCampaign.setVersion(storedCampaign.getVersion());
		campaignDAO.updateCampaign(staleCampaign);

		Campaign freshStoredCampaign = campaignDAO.getCampaign(campaign.getCampaignId());
		assertNotNull(freshStoredCampaign);
		assertEquals(staleCampaign.getRedirectUrl(), freshStoredCampaign.getRedirectUrl());

		// Create non existant data
		Campaign nonExistantCampaign = new Campaign();
		nonExistantCampaign.setCampaignId(UUID.randomUUID().toString());
		nonExistantCampaign.setRedirectUrl("http://localhost/redirect");
		nonExistantCampaign.getPlatforms().add(Platform.ANDROID);

		// test stale update prevention
		try
		{
			campaignDAO.updateCampaign(nonExistantCampaign);
			fail("Non existant data, Exception should be thrown.");
		}
		catch (Exception e)
		{

		}
	}

	@Test
	public void testDeleteCampaign()
	{
		log.info("Running testDeleteCampaign");

		Campaign campaign = new Campaign();
		campaign.setRedirectUrl("http://localhost/redirect");
		campaign.getPlatforms().add(Platform.ANDROID);
		campaignDAO.createCampaign(campaign);

		Campaign storedCampaign = campaignDAO.getCampaign(campaign.getCampaignId());
		assertNotNull(storedCampaign);

		campaignDAO.deleteCampaign(storedCampaign.getCampaignId());

		Campaign deletedCampaign = campaignDAO.getCampaign(campaign.getCampaignId());
		assertNull(deletedCampaign);
	}
}
