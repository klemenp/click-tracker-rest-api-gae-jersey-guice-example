package com.clicktracker.rest.services;

import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Platform;
import com.clicktracker.rest.services.exception.NotFoundException;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/**
 * Created by klemen.
 */
public class MainServiceTest extends BaseTest
{
	@Test
	public void testCreateCampaign() throws Exception
	{
		log.info("Running testCreateCampaign");

		Campaign campaign = new Campaign();
		campaign.setRedirectUrl("http://www.somedomain.com/redirect");
		campaign.getPlatforms().add(Platform.ANDROID);
		mainService.createCampaign(campaign);

		Campaign storedCampaign = mainService.getCampaign(campaign.getCampaignId());
		assertNotNull(storedCampaign);
		assertEquals(campaign.getCampaignId(), storedCampaign.getCampaignId());
		assertEquals(campaign.getRedirectUrl(), storedCampaign.getRedirectUrl());
	}

	@Test
	public void testGetCampaignList() throws Exception
	{
		log.info("Running testGetCampaignList");

		// Wait, lists are eventually consistent
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		List<Campaign> campaignList  = mainService.getCampaignList(null);
		assertEquals(0, campaignList.size());

		Campaign campaign = new Campaign();
		campaign.setRedirectUrl("http://www.somedomain.com/redirect");
		campaign.getPlatforms().add(Platform.ANDROID);
		mainService.createCampaign(campaign);
		Campaign storedCampaign = mainService.getCampaign(campaign.getCampaignId());
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
		List<Campaign> newCampaignList  = mainService.getCampaignList(null);

		assertEquals(1,newCampaignList.size());

		campaign = new Campaign();
		campaign.setRedirectUrl("http://www.somedomain.com/redirect");
		campaign.getPlatforms().add(Platform.IPHONE);
		mainService.createCampaign(campaign);

		campaign = new Campaign();
		campaign.setRedirectUrl("http://www.somedomain.com/redirect");
		campaign.getPlatforms().add(Platform.IPHONE);
		mainService.createCampaign(campaign);

		// Wait, lists are eventually consistent
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		List<Campaign> newCampaignList2  = mainService.getCampaignList(null);

		assertEquals(3,newCampaignList2.size());

		Set<Platform> platforms = new HashSet<>();
		platforms.add(Platform.IPHONE);
		List<Campaign> newCampaignList3  = campaignDAO.getCampaignList(platforms);

		assertEquals(2,newCampaignList3.size());

		platforms = new HashSet<>();
		platforms.add(Platform.IPHONE);
		platforms.add(Platform.ANDROID);
		List<Campaign> newCampaignList4  = campaignDAO.getCampaignList(platforms);

		assertEquals(3,newCampaignList4.size());

		platforms = new HashSet<>();
		platforms.add(Platform.ANDROID);
		List<Campaign> newCampaignList5  = campaignDAO.getCampaignList(platforms);
		assertEquals(1,newCampaignList5.size());
	}

	@Test
	public void testUpdateCampaign() throws Exception
	{
		log.info("Running testUpdateCampaign");

		Campaign campaign = new Campaign();
		campaign.setRedirectUrl("http://www.somedomain.com/redirect");
		campaign.getPlatforms().add(Platform.ANDROID);
		long oldVersion = campaign.getVersion();
		mainService.createCampaign(campaign);

		Campaign storedCampaign = mainService.getCampaign(campaign.getCampaignId());
		assertNotNull(storedCampaign);
		storedCampaign.setRedirectUrl("http://www.somedomain.com/redirect");

		mainService.updateCampaign(storedCampaign);

		// Create stale data
		Campaign staleCampaign = new Campaign();
		staleCampaign.setCampaignId(campaign.getCampaignId());
		staleCampaign.setRedirectUrl("http://www.somedomain.com/redirect");
		staleCampaign.getPlatforms().add(Platform.ANDROID);
		staleCampaign.setVersion(oldVersion);

		// test stale update prevention
		try
		{
			mainService.updateCampaign(staleCampaign);
			fail("Stale data, Exception should be thrown.");
		}
		catch (Exception e)
		{

		}

		// test update with correct version
		staleCampaign.setVersion(storedCampaign.getVersion());
		mainService.updateCampaign(staleCampaign);

		Campaign freshStoredCampaign = mainService.getCampaign(campaign.getCampaignId());
		assertNotNull(freshStoredCampaign);
		assertEquals(staleCampaign.getRedirectUrl(), freshStoredCampaign.getRedirectUrl());

		// Create non existant data
		Campaign nonExistantCampaign = new Campaign();
		nonExistantCampaign.setCampaignId(UUID.randomUUID().toString());
		nonExistantCampaign.setRedirectUrl("http://www.somedomain.com/redirect");
		nonExistantCampaign.getPlatforms().add(Platform.ANDROID);

		// test stale update prevention
		try
		{
			mainService.updateCampaign(nonExistantCampaign);
			fail("Non existant data, Exception should be thrown.");
		}
		catch (Exception e)
		{

		}
	}

	@Test
	public void testDeleteCampaign() throws Exception
	{
		log.info("Running testDeleteCampaign");

		Campaign campaign = new Campaign();
		campaign.setRedirectUrl("http://www.somedomain.com/redirect");
		campaign.getPlatforms().add(Platform.ANDROID);
		mainService.createCampaign(campaign);

		Campaign storedCampaign = mainService.getCampaign(campaign.getCampaignId());
		assertNotNull(storedCampaign);

		mainService.deleteCampaign(storedCampaign.getCampaignId());

		try
		{
			Campaign deletedCampaign = mainService.getCampaign(campaign.getCampaignId());
			fail("Exception should be thrown");
		}
		catch (NotFoundException e)
		{}
	}

	@Test
	public void testRecordClick()
	{
		log.info("Running testRecordClick");
		try
		{
			mainService.recordClick("", "");
			fail("Exception should be thrown");
		}
		catch (Exception e)
		{

		}
	}
}
