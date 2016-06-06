package com.clicktracker.rest.services.dao;

import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Counter;
import com.clicktracker.rest.model.Platform;
import com.clicktracker.rest.services.BaseTest;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by klemen.
 */
public class CounterDAOTest extends BaseTest
{
	@Test
	public void testCreateCounter()
	{
		log.info("Running testCreateCounter");

		Campaign campaign = new Campaign();
		campaign.setRedirectUrl("http://localhost/redirect");
		campaign.getPlatforms().add(Platform.ANDROID);
		campaignDAO.createCampaign(campaign);

		counterDAO.increaseOrCreateCounter(1, campaign, Platform.ANDROID);

		Counter storedCounter = counterDAO.getCounter(Counter.createId(campaign,Platform.ANDROID, 1));
		assertNotNull(storedCounter);
		assertEquals(1, storedCounter.getNumberOfClicks());

		counterDAO.increaseOrCreateCounter(1, campaign, Platform.ANDROID);

		storedCounter = counterDAO.getCounter(Counter.createId(campaign,Platform.ANDROID, 1));
		assertNotNull(storedCounter);
		assertEquals(2, storedCounter.getNumberOfClicks());

	}

	@Test
	public void testGetCounterList()
	{
		log.info("Running testGetCounterList");

		// Wait, lists are eventually consistent
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		List<Counter> counterList  = counterDAO.getCounterList(null, null);
		assertEquals(0, counterList.size());

		Campaign campaign = new Campaign();
		campaign.setRedirectUrl("http://localhost/redirect");
		campaign.getPlatforms().add(Platform.ANDROID);
		campaign.getPlatforms().add(Platform.IPHONE);
		campaign.getPlatforms().add(Platform.WINDOWSPHONE);
		campaignDAO.createCampaign(campaign);

		Campaign campaign2 = new Campaign();
		campaign2.setRedirectUrl("redirect url");
		campaign2.getPlatforms().add(Platform.ANDROID);
		campaign2.getPlatforms().add(Platform.IPHONE);
		campaign2.getPlatforms().add(Platform.WINDOWSPHONE);
		campaignDAO.createCampaign(campaign2);

		counterDAO.increaseOrCreateCounter(1, campaign, Platform.ANDROID);
		counterDAO.increaseOrCreateCounter(1, campaign, Platform.ANDROID);


		// Wait, lists are eventually consistent
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		counterList  = counterDAO.getCounterList(null, null);
		assertEquals(1, counterList.size());

		counterDAO.increaseOrCreateCounter(1, campaign, Platform.IPHONE);

		// Wait, lists are eventually consistent
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		counterList  = counterDAO.getCounterList(null, null);
		assertEquals(2, counterList.size());

		counterDAO.increaseOrCreateCounter(1, campaign2, Platform.WINDOWSPHONE);

		// Wait, lists are eventually consistent
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		counterList  = counterDAO.getCounterList(null, null);
		assertEquals(3, counterList.size());

		counterDAO.increaseOrCreateCounter(1, campaign, Platform.WINDOWSPHONE);
		// Wait, lists are eventually consistent
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		counterList  = counterDAO.getCounterList(null, null);
		assertEquals(4, counterList.size());

		counterList  = counterDAO.getCounterList(null, campaign);
		assertEquals(3, counterList.size());

		counterList  = counterDAO.getCounterList(null, campaign2);
		assertEquals(1, counterList.size());

		Set<Platform> platforms = new HashSet<>();
		platforms.add(Platform.IPHONE);
		counterList  = counterDAO.getCounterList(platforms, campaign);
		assertEquals(1, counterList.size());

		counterList  = counterDAO.getCounterList(platforms, campaign2);
		assertEquals(0, counterList.size());


	}

	@Test
	public void testDeleteCounter()
	{
		log.info("Running testDeleteCounter");

		Campaign campaign = new Campaign();
		campaign.setRedirectUrl("http://localhost/redirect");
		campaign.getPlatforms().add(Platform.ANDROID);
		campaignDAO.createCampaign(campaign);

		counterDAO.increaseOrCreateCounter(1, campaign, Platform.ANDROID);

		Counter storedCounter = counterDAO.getCounter(Counter.createId(campaign,Platform.ANDROID, 1));
		assertNotNull(storedCounter);

		counterDAO.deleteCounter(storedCounter.getCounterId());

		Counter deletedCounter = counterDAO.getCounter(storedCounter.getCounterId());
		assertNull(deletedCounter);
	}
}
