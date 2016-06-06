package com.clicktracker.integration.tracker;


import com.clicktracker.integration.BaseIntegrationTest;
import com.clicktracker.rest.TrackerAPI;
import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Platform;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by klemen.
 */
public class TrackerApiIntegrationTest extends BaseIntegrationTest
{
	@Test
	public void testInvalidClickNoParams()
	{
		log.info("Running testInvalidClickNoParams");

		try
		{
			HttpResponse response = executeHttpGetRequest(getTestProperty(
				API_TRACKER_CLICK_URL_KEY), null, null, null, false);
			assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatusLine().getStatusCode());
			assertNotNull(response.getFirstHeader("Location"));
			assertEquals(getTestProperty(INVALID_CLICK_REDIRECT_URL_KEY),
				response.getFirstHeader("Location").getValue());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testInvalidClickInvalidParams()
	{
		log.info("Running testInvalidClickInvalidParams");
		try
		{
			Map<String, String> queryParams = new HashMap<>();
			queryParams.put(TrackerAPI.PARAM_CAMPAIGN, "InvalidCampaign");
			queryParams.put(TrackerAPI.PARAM_PLATFORM, "InvalidPlatform");

			HttpResponse response = executeHttpGetRequest(getTestProperty(
				API_TRACKER_CLICK_URL_KEY), null, queryParams, null, false);
			assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatusLine().getStatusCode());
			assertNotNull(response.getFirstHeader("Location"));
			assertEquals(getTestProperty(INVALID_CLICK_REDIRECT_URL_KEY),
				response.getFirstHeader("Location").getValue());

			queryParams = new HashMap<>();
			queryParams.put(TrackerAPI.PARAM_CAMPAIGN, "InvalidCampaign");
			queryParams.put(TrackerAPI.PARAM_PLATFORM, "Android");

			response = executeHttpGetRequest(getTestProperty(
				API_TRACKER_CLICK_URL_KEY), null, queryParams, null, false);
			assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatusLine().getStatusCode());
			assertNotNull(response.getFirstHeader("Location"));
			assertEquals(getTestProperty(INVALID_CLICK_REDIRECT_URL_KEY),
				response.getFirstHeader("Location").getValue());

			queryParams = new HashMap<>();
			queryParams.put(TrackerAPI.PARAM_CAMPAIGN, "Campaign1");
			queryParams.put(TrackerAPI.PARAM_PLATFORM, "Android");

			response = executeHttpGetRequest(getTestProperty(
				API_TRACKER_CLICK_URL_KEY), null, queryParams, null, false);
			assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatusLine().getStatusCode());
			assertNotNull(response.getFirstHeader("Location"));
			assertEquals(getTestProperty(INVALID_CLICK_REDIRECT_URL_KEY),
				response.getFirstHeader("Location").getValue());

		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testValidClickValidParams()
	{
		log.info("Running testValidClickValidParams");

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

			Map<String, String> queryParams = new HashMap<>();
			queryParams.put(TrackerAPI.PARAM_CAMPAIGN, campaign.getCampaignId());
			queryParams.put(TrackerAPI.PARAM_PLATFORM, Platform.IPHONE.toString());

			response = executeHttpGetRequest(getTestProperty(
				API_TRACKER_CLICK_URL_KEY), null, queryParams, null, false);
			assertEquals(HttpStatus.SC_SEE_OTHER, response.getStatusLine().getStatusCode());
			assertNotNull(response.getFirstHeader("Location"));
			assertEquals(campaign.getRedirectUrl(),
				response.getFirstHeader("Location").getValue());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
