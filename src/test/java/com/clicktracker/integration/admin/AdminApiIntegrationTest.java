package com.clicktracker.integration.admin;

import com.clicktracker.integration.BaseIntegrationTest;
import com.clicktracker.rest.TrackerAPI;
import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Clicks;
import com.clicktracker.rest.model.Platform;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by klemen.
 */
public class AdminApiIntegrationTest extends BaseIntegrationTest
{
	@Test
	public void testCampaign()
	{
		log.info("Running testCampaign");

		try
		{
			// List
			HttpResponse response = executeHttpGetRequest(getTestProperty(
				API_ADMIN_URL_KEY), null, null, getBasicAdminCredentials(), true);
			int oldSize = createObjectListFromJson(response.getEntity().getContent(), Campaign.class).size();

			// Create
			Campaign campaign = new Campaign();
			campaign.getPlatforms().add(Platform.IPHONE);
			campaign.setRedirectUrl("http://www.google.com");
			String postBody = objectMapper.writeValueAsString(campaign);

			response = executeHttpPostRequest(getTestProperty(
				API_ADMIN_URL_KEY), "application/json", null, getBasicAdminCredentials(), postBody, true);
			assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

			assertNotNull(response.getFirstHeader("Location"));

			String campaignLocation = response.getFirstHeader("Location").getValue();

			response = executeHttpGetRequest(campaignLocation, null, null, getBasicAdminCredentials(), true);
			assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			assertNotNull(response.getFirstHeader("Content-Type"));
			assertEquals( "application/json",response.getFirstHeader("Content-Type").getValue());
			Campaign object = createObjectFromJson(response.getEntity().getContent(), Campaign.class);
			assertNotNull(object);
			assertEquals(campaign.getRedirectUrl(), object.getRedirectUrl());

			// Wait, lists are eventually consistent
			Thread.sleep(5000);

			response = executeHttpGetRequest(getTestProperty(
				API_ADMIN_URL_KEY), null, null, getBasicAdminCredentials(), true);
			int newSize = createObjectListFromJson(response.getEntity().getContent(), Campaign.class).size();

			assertEquals(1, newSize-oldSize);

			// Update
			object.setRedirectUrl("http://localhost");
			String putBody = objectMapper.writeValueAsString(object);
			response = executeHttpPutRequest(campaignLocation, "application/json", null, getBasicAdminCredentials(), putBody, true);
			assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			Campaign updatedObject = createObjectFromJson(response.getEntity().getContent(), Campaign.class);
			assertNotNull(object);
			assertEquals(object.getRedirectUrl(), updatedObject.getRedirectUrl());


		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testClickCount()
	{
		log.info("Running testClickCount");

		try
		{
			// Create campaign
			Campaign campaign = new Campaign();
			campaign.getPlatforms().add(Platform.IPHONE);
			campaign.setRedirectUrl("http://www.google.com");
			String postBody = objectMapper.writeValueAsString(campaign);
			HttpResponse response = executeHttpPostRequest(getTestProperty(
				API_ADMIN_URL_KEY), "application/json", null, getBasicAdminCredentials(), postBody, true);
			assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
			assertNotNull(response.getFirstHeader("Location"));
			String campaignLocation = response.getFirstHeader("Location").getValue();
			response = executeHttpGetRequest(campaignLocation, null, null, getBasicAdminCredentials(), true);
			assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			assertNotNull(response.getFirstHeader("Content-Type"));
			assertEquals( "application/json",response.getFirstHeader("Content-Type").getValue());
			campaign = createObjectFromJson(response.getEntity().getContent(), Campaign.class);
			assertNotNull(campaign);



			// Execute clicks
			int numClicks = 1000;
			for (int i = 0; i<numClicks; i++)
			{
				Map<String, String> queryParams = new HashMap<>();
				queryParams.put(TrackerAPI.PARAM_CAMPAIGN, campaign.getCampaignId());
				queryParams.put(TrackerAPI.PARAM_PLATFORM, Platform.IPHONE.toString());
				response = executeHttpGetRequest(getTestProperty(API_TRACKER_CLICK_URL_KEY), null,
					queryParams, null, false);
			}

			// Wait, lists are eventually consistent
			Thread.sleep(10000);

			Map<String, String> queryParams = new HashMap<>();
			queryParams.put(TrackerAPI.PARAM_CAMPAIGN, campaign.getCampaignId());
			queryParams.put(TrackerAPI.PARAM_PLATFORM, Platform.IPHONE.toString());
			response = executeHttpGetRequest(getTestProperty(
				API_CLICKS_URL_KEY), null, queryParams, getBasicAdminCredentials(), true);
			assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			assertEquals( "application/json",response.getFirstHeader("Content-Type").getValue());
			Clicks clicks = createObjectFromJson(response.getEntity().getContent(), Clicks.class);
			assertEquals(numClicks, clicks.getNumberOfClicks());

			queryParams = new HashMap<>();
			queryParams.put(TrackerAPI.PARAM_CAMPAIGN, campaign.getCampaignId());
			queryParams.put(TrackerAPI.PARAM_PLATFORM, Platform.ANDROID.toString());
			response = executeHttpGetRequest(getTestProperty(
				API_CLICKS_URL_KEY), null, queryParams, getBasicAdminCredentials(), true);
			assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			assertEquals( "application/json",response.getFirstHeader("Content-Type").getValue());
			clicks = createObjectFromJson(response.getEntity().getContent(), Clicks.class);
			assertEquals(0, clicks.getNumberOfClicks());

			queryParams = new HashMap<>();
			queryParams.put(TrackerAPI.PARAM_PLATFORM, Platform.ANDROID.toString());
			response = executeHttpGetRequest(getTestProperty(
				API_CLICKS_URL_KEY), null, queryParams, getBasicAdminCredentials(), true);
			assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			assertEquals( "application/json",response.getFirstHeader("Content-Type").getValue());
			clicks = createObjectFromJson(response.getEntity().getContent(), Clicks.class);
			assertEquals(0, clicks.getNumberOfClicks());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
