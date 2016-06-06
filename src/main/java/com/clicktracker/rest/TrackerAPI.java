package com.clicktracker.rest;

import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.services.MainService;
import com.clicktracker.rest.services.exception.InvalidCampaignException;
import com.clicktracker.rest.services.exception.InvalidPlatformException;
import com.clicktracker.util.SettingsHelper;
import com.google.inject.Singleton;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Created by klemen.
 */
@Singleton
@Path(TrackerAPI.TRACKER_ENDPOINT_PATH)
public class TrackerAPI extends BaseAPI
{
	private static final String ALLOWED_ORIGIN  = "*";
	private static final String ALLOWED_HEADERS = "Content-Type, Accept";
	private static final String EXPOSED_HEADERS = "Location";
	private static final String ALLOWED_METHODS = "GET, OPTIONS";

	public static final String TRACKER_ENDPOINT_PATH = "/tracker";
	public static final String CLICK_ENDPOINT_PATH = "/click";

	private static final Logger log = Logger.getLogger(TrackerAPI.class.getName());

	@Inject
	private MainService mainService;

	public static final String PARAM_PLATFORM				= "platform";
	public static final String PARAM_CAMPAIGN				= "campaign";

	private URI invalidClickRedirectUri;

	@GET
	@Path(CLICK_ENDPOINT_PATH)
	public Response click(
		@QueryParam(PARAM_PLATFORM) String platformId,
		@QueryParam(PARAM_CAMPAIGN) String campaignId)
	{
		try
		{
			log.info("GET request");

			if (platformId==null)
			{
				log.fine("Missing platform");
				return Response.seeOther(invalidClickRedirectUri).build();
			}
			if (campaignId==null)
			{
				log.fine("Missing campaign");
				return Response.seeOther(invalidClickRedirectUri).build();
			}

			log.fine("click request paltform: " + platformId + " campaign: " + campaignId);

			Campaign campaign = mainService.recordClick(platformId, campaignId);

			URI campaignRedirectUri = URI.create(campaign.getRedirectUrl());
			return Response.seeOther(campaignRedirectUri).build();
		}
		catch (InvalidCampaignException e)
		{
			log.fine(e.getMessage());
			return Response.seeOther(invalidClickRedirectUri).build();
		}
		catch (InvalidPlatformException e)
		{
			log.fine(e.getMessage());
			return Response.seeOther(invalidClickRedirectUri).build();
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return Response.seeOther(invalidClickRedirectUri).build();
		}
	}

	@OPTIONS
	@Path(CLICK_ENDPOINT_PATH)
	public Response clickOptions()
	{
		try
		{
			log.info("OPTIONS request");
			Response.ResponseBuilder responseBuilder = Response.ok();

			responseBuilder.header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, ALLOWED_ORIGIN);
			responseBuilder.header(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, ALLOWED_HEADERS);
			responseBuilder.header(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, EXPOSED_HEADERS);
			responseBuilder.header(ACCESS_CONTROL_ALLOW_METHODS_HEADER, ALLOWED_METHODS);
			responseBuilder.header(ALLOW_HEADER, ALLOWED_METHODS);

			return responseBuilder.build();
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return Response.ok().build();
		}
	}

	@PostConstruct
	public void postConstruct()
	{
		invalidClickRedirectUri = URI.create(SettingsHelper.getSetting(SettingsHelper.SETTING_KEY_TRACKER_INVALID_CLICK_REDIRECT_URL));
	}

}
