package com.clicktracker.rest.services.dao;

import com.clicktracker.rest.model.Campaign;
import com.clicktracker.rest.model.Platform;

import java.util.List;
import java.util.Set;

/**
 * Created by klemen.
 */
public interface CampaignDAO extends BaseDAO
{
	void createCampaign(final Campaign campaign);
	void updateCampaign(final Campaign campaign) throws StaleUpdateException, NoDataException;
	void deleteCampaign(final String id);
	Campaign getCampaign(String id);
	List<Campaign> getCampaignList(Set<Platform> withPlatforms);
}
