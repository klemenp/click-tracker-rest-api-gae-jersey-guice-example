package com.clicktracker.rest.services;

import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Clicks;
import com.clicktracker.rest.model.Platform;
import com.clicktracker.rest.services.dao.CampaignDAO;
import com.clicktracker.rest.services.exception.*;

import java.util.List;
import java.util.Set;

/**
 * Created by klemen.
 */
public interface MainService
{
	void createCampaign(Campaign campaign) throws InvalidObjectException;
	void updateCampaign(Campaign campaign)
		throws StaleUpdateException, NotFoundException,
		InvalidObjectException;
	void deleteCampaign(String campaignId) throws NotFoundException;
	Campaign getCampaign(String id) throws NotFoundException;
	List<Campaign> getCampaignList(Set<Platform> withPlatforms);
	Clicks getNumberOfClicks(Campaign campagn, Platform platform);
	Campaign recordClick(String patformId, String campaignId)
		throws InvalidCampaignException, InvalidPlatformException,
		ServiceErrorException;
}
