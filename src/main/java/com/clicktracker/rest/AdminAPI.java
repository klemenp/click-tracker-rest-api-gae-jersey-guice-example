package com.clicktracker.rest;

import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Clicks;
import com.clicktracker.rest.model.Platform;
import com.clicktracker.rest.services.MainService;
import com.clicktracker.rest.services.exception.InvalidObjectException;
import com.clicktracker.rest.services.exception.NotFoundException;
import com.clicktracker.rest.services.exception.StaleUpdateException;
import com.google.inject.Singleton;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by klemen.
 */
@Singleton
@Path(AdminAPI.ADMIN_ENDPOINT_PATH)
public class AdminAPI extends BaseAPI
{
	private static final String ALLOWED_ORIGIN  = "*";
	private static final String ALLOWED_HEADERS = "Content-Type, Accept";
	private static final String ROOT_ALLOWED_METHODS = "GET, POST, OPTIONS";
	private static final String EXPOSED_HEADERS = "Location";
	private static final String CAMPAIGN_ALLOWED_METHODS = "GET, PUT, DELETE, POST, OPTIONS";
	private static final String CAMPAIGN_CLICKS_ALLOWED_METHODS = "GET, OPTIONS";

	public static final String ADMIN_ENDPOINT_PATH = "/admin";
	public static final String CAMPAIGN_ENDPOINT_PATH = "/campaign";
	public static final String CLICKS_ENDPOINT_PATH = "/clicks";

	public static final String PARAM_PLATFORM				= "platform";
	public static final String PARAM_CAMPAIGN				= "campaign";


	private static final Logger log = Logger.getLogger(TrackerAPI.class.getName());

	@Inject
	private MainService mainService;

	private Response authorizationRequiredResponse()
	{
		log.fine("Unauthorized request - missing credentials");
		Response.ResponseBuilder responseBuilder = Response.status(UNAUTHORIZED_STATUS_CODE);
		responseBuilder.header("WWW-Authenticate", "Basic realm=\"Basic credentials needed\"");
		return responseBuilder.build();
	}

	@GET
	@Path(CAMPAIGN_ENDPOINT_PATH + "/{id}")
	@Produces("application/json")
	public Response getCampaign(@HeaderParam("Authorization") String authorization, @PathParam("id") String campaignId)
	{
		try
		{
			log.info("GET request");
			if (authorization==null)
			{
				return authorizationRequiredResponse();
			}
			else if (authenticate(authorization))
			{
				Campaign campaign;
				try
				{
					campaign = mainService.getCampaign(campaignId);
				}
				catch (NotFoundException e)
				{
					log.fine("Campaign does not exist " + campaignId);
					return Response.status(Response.Status.NOT_FOUND).build();
				}
				String responseString;
				try
				{
					responseString = objectMapper.writeValueAsString(campaign);
					log.finer("Response: " + responseString);
				}
				catch (JsonProcessingException e)
				{
					log.severe(e.getMessage());
					e.printStackTrace();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.build();
				}
				return Response.ok(responseString).build();
			}
			else
			{
				log.fine("Unauthorized request");
				return Response.status(UNAUTHORIZED_STATUS_CODE).build();
			}
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@GET
	@Path(CLICKS_ENDPOINT_PATH)
	@Produces("application/json")
	public Response getClicks(@HeaderParam("Authorization") String authorization, @QueryParam(PARAM_CAMPAIGN) String campaignId, @QueryParam(PARAM_PLATFORM) String platformId)
	{
		try
		{
			log.info("GET request");
			if (authorization==null)
			{
				return authorizationRequiredResponse();
			}
			else if (authenticate(authorization))
			{
				Campaign campaign = null;
				if (campaignId!=null && campaignId.length()>0)
				{
					try
					{
						campaign = mainService.getCampaign(campaignId);
					}
					catch (NotFoundException e)
					{
						log.fine("Campaign does not exist " + campaignId);
						return Response.status(Response.Status.NOT_FOUND).build();
					}
				}
				Platform platform = null;
				if (platformId!=null && platformId.length()>0)
				{
					try
					{
						platform = Platform.fromValue(platformId);
					}
					catch (IllegalArgumentException e)
					{
						log.fine("Bad request");
						return Response.status(Response.Status.BAD_REQUEST).build();
					}
				}

				Clicks clicks = mainService.getNumberOfClicks(campaign, platform);
				String responseString;
				try
				{
					responseString = objectMapper.writeValueAsString(clicks);
					log.finer("Response: " + responseString);
				}
				catch (JsonProcessingException e)
				{
					log.severe(e.getMessage());
					e.printStackTrace();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.build();
				}

				return Response.ok(responseString).build();
			}
			else
			{
				log.fine("Unauthorized request");
				return Response.status(UNAUTHORIZED_STATUS_CODE).build();
			}
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@DELETE
	@Path(CAMPAIGN_ENDPOINT_PATH + "/{id}")
	public Response deleteCampaign(@HeaderParam("Authorization") String authorization, @PathParam("id") String campaignId)
	{
		try
		{
			log.info("DELETE request");

			if (authorization==null)
			{
				return authorizationRequiredResponse();
			}
			else if (authenticate(authorization))
			{
				try
				{
					mainService.deleteCampaign(campaignId);
					return Response.noContent().build();
				}
				catch (NotFoundException e)
				{
					log.fine("Campaign does not exist " + campaignId);
					return Response.status(Response.Status.NOT_FOUND).build();
				}
			}
			else
			{
				log.fine("Unauthorized request");
				return Response.status(UNAUTHORIZED_STATUS_CODE).build();
			}
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@GET
	@Path(CAMPAIGN_ENDPOINT_PATH)
	@Produces("application/json")
	public Response getCampaignList(@HeaderParam("Authorization") String authorization, @QueryParam(PARAM_PLATFORM) String platform)
	{
		try
		{
			log.info("GET request");

			if (authorization==null)
			{
				return authorizationRequiredResponse();
			}
			else if (authenticate(authorization))
			{
				Set<Platform> platforms = null;
				if (platform!=null && platform.length()>0)
				{
					platforms = new HashSet<>();
					try
					{
						platforms.add(Platform.fromValue(platform));
					}
					catch (IllegalArgumentException e)
					{
						log.fine("Bad request");
						return Response.status(Response.Status.BAD_REQUEST).build();
					}
				}
				List<Campaign> campaignList = mainService.getCampaignList(platforms);
				String responseString;
				try
				{
					responseString = objectMapper.writeValueAsString(campaignList);
					log.finer("Response: " + responseString);
				}
				catch (JsonProcessingException e)
				{
					log.severe(e.getMessage());
					e.printStackTrace();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.build();
				}

				return Response.ok(responseString).build();
			}
			else
			{
				log.fine("Unauthorized request");
				return Response.status(UNAUTHORIZED_STATUS_CODE).build();
			}
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@POST
	@Path(CAMPAIGN_ENDPOINT_PATH)
	@Consumes("application/json")
	public Response createCampaign(@Context UriInfo uriInfo, @HeaderParam("Content-Type") String contentType, @HeaderParam("Authorization") String authorization, InputStream requestBodyInputStream)
	{
		try
		{
			log.info("POST request");
			if (authorization==null)
			{
				return authorizationRequiredResponse();
			}
			else if (authenticate(authorization))
			{
				try
				{
					Campaign campaign = objectMapper.readValue(requestBodyInputStream, Campaign.class);
					mainService.createCampaign(campaign);
					Response.ResponseBuilder responseBuilder = Response.status(Response.Status.CREATED);
					String link = generateCampaignLocationHeader(uriInfo, campaign.getCampaignId());
					responseBuilder = responseBuilder.header(HttpHeaders.LOCATION, link);
					return responseBuilder.build();
				}
				catch (InvalidObjectException e)
				{
					log.fine("BAD_REQUEST: " + e.getMessage());
					return Response.status(Response.Status.BAD_REQUEST).build();
				}
				catch (JsonParseException e)
				{
					log.fine("BAD_REQUEST: " + e.getMessage());
					return Response.status(Response.Status.BAD_REQUEST).build();
				}
				catch (JsonMappingException e)
				{
					log.fine("BAD_REQUEST: " + e.getMessage());
					return Response.status(Response.Status.BAD_REQUEST).build();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					log.severe(e.getMessage());
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
				}
			}
			else
			{
				log.fine("Unauthorized request");
				return Response.status(UNAUTHORIZED_STATUS_CODE).build();
			}
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@PUT
	@Path(CAMPAIGN_ENDPOINT_PATH + "/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response updateCampaign(@Context UriInfo uriInfo, @HeaderParam("Authorization") String authorization, @PathParam("id") String campaignId, InputStream requestBodyInputStream)
	{
		try
		{
			log.info("PUT request");

			if (authorization==null)
			{
				return authorizationRequiredResponse();
			}
			else if (authenticate(authorization))
			{
				Campaign newValuesCampaign;
				try
				{
					newValuesCampaign = objectMapper.readValue(requestBodyInputStream, Campaign.class);

				}
				catch (JsonParseException e)
				{
					log.fine("BAD_REQUEST: " + e.getMessage());
					return Response.status(Response.Status.BAD_REQUEST).build();
				}
				catch (JsonMappingException e)
				{
					log.fine("BAD_REQUEST: " + e.getMessage());
					return Response.status(Response.Status.BAD_REQUEST).build();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					log.severe(e.getMessage());
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
				}

				if (newValuesCampaign.getCampaignId()!=null && !newValuesCampaign.getCampaignId().equals(campaignId))
				{
					log.fine("BAD_REQUEST: Campaign ids must match");
					return Response.status(Response.Status.BAD_REQUEST).build();
				}

				Campaign oldCampaign;
				try
				{
					oldCampaign = mainService.getCampaign(campaignId);
				}
				catch (NotFoundException e)
				{
					log.fine("Campaign does not exist " + campaignId);
					return Response.status(Response.Status.NOT_FOUND).build();
				}

				copyValuesForUpdate(newValuesCampaign, oldCampaign);

				try
				{
					mainService.updateCampaign(oldCampaign);
				}
				catch (NotFoundException e)
				{
					log.fine("NOT FOUND: Campaign does not exist "  + e.getMessage());
					return Response.status(Response.Status.NOT_FOUND).build();
				}
				catch (StaleUpdateException e)
				{
					log.fine("CONFLCI: Version conflict. Updating stale data" + e.getMessage());
					return Response.status(Response.Status.CONFLICT).build();
				}
				catch (InvalidObjectException e)
				{
					log.fine("BAD_REQUEST: " + e.getMessage());
					return Response.status(Response.Status.BAD_REQUEST).build();
				}
				String responseString;
				try
				{
					responseString = objectMapper.writeValueAsString(oldCampaign);
					log.finer("Response" + responseString);
				}
				catch (JsonProcessingException e)
				{
					log.severe(e.getMessage());
					e.printStackTrace();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.build();
				}
				return Response.ok(responseString).build();
			}
			else
			{
				log.fine("Unauthorized request");
				return Response.status(UNAUTHORIZED_STATUS_CODE).build();
			}
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	private void copyValuesForUpdate(Campaign origin, Campaign target)
	{
		target.setRedirectUrl(origin.getRedirectUrl());
		target.setVersion(origin.getVersion());
		target.setPlatforms(origin.getPlatforms());
	}

	@OPTIONS
	@Path(CAMPAIGN_ENDPOINT_PATH + "/{id}")
	public Response getCampaignOptions(@PathParam("id") String campaignId)
	{
		try
		{
			log.info("OPTIONS request");

			Response.ResponseBuilder responseBuilder = Response.ok();
			responseBuilder.header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, ALLOWED_ORIGIN);
			responseBuilder.header(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, ALLOWED_HEADERS);
			responseBuilder.header(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, EXPOSED_HEADERS);
			responseBuilder.header(ACCESS_CONTROL_ALLOW_METHODS_HEADER, CAMPAIGN_ALLOWED_METHODS);
			responseBuilder.header(ALLOW_HEADER, CAMPAIGN_ALLOWED_METHODS);

			return responseBuilder.build();
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@OPTIONS
	@Path(CLICKS_ENDPOINT_PATH)
	public Response getClickSOptions(@PathParam("id") String campaignId)
	{
		try
		{
			log.info("OPTIONS request");

			Response.ResponseBuilder responseBuilder = Response.ok();
			responseBuilder.header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, ALLOWED_ORIGIN);
			responseBuilder.header(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, ALLOWED_HEADERS);
			responseBuilder.header(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, EXPOSED_HEADERS);
			responseBuilder.header(ACCESS_CONTROL_ALLOW_METHODS_HEADER, CAMPAIGN_CLICKS_ALLOWED_METHODS);
			responseBuilder.header(ALLOW_HEADER, CAMPAIGN_CLICKS_ALLOWED_METHODS);

			return responseBuilder.build();
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@OPTIONS
	@Path(CAMPAIGN_ENDPOINT_PATH)
	public Response getCampaignListOptions()
	{
		try
		{
			log.info("OPTIONS request");

			Response.ResponseBuilder responseBuilder = Response.ok();
			responseBuilder.header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, ALLOWED_ORIGIN);
			responseBuilder.header(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, ALLOWED_HEADERS);
			responseBuilder.header(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, EXPOSED_HEADERS);
			responseBuilder.header(ACCESS_CONTROL_ALLOW_METHODS_HEADER, ROOT_ALLOWED_METHODS);
			responseBuilder.header(ALLOW_HEADER, ROOT_ALLOWED_METHODS);

			return responseBuilder.build();
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return Response.serverError().build();
		}
	}



	private String generateCampaignLocationHeader(UriInfo requestUriInfo, String id)
	{
		URI base = requestUriInfo.getBaseUri();
		String path = requestUriInfo.getPath();
		UriBuilder uriBuilder = UriBuilder.fromUri(base);
		uriBuilder.path(path);
		if (id != null)
		{
			uriBuilder.path(id);
		}
		String locationHeader = uriBuilder.build().toString();
		return locationHeader;
	}
}
