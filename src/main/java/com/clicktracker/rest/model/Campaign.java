package com.clicktracker.rest.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

import java.io.Serializable;
import java.util.*;

/**
 * Created by klemen.
 */
@Cache
@Entity
public class Campaign implements Serializable
{
	@Id
	private String        campaignId;
	@Index
	private Set<Platform> platforms;
	private long          version;
	private String        redirectUrl;
	private Date          dateCreated;
	private Date          dateModified;

	public String getCampaignId()
	{
		return campaignId;
	}

	public void setCampaignId(String campaignId)
	{
		this.campaignId = campaignId;
	}

	public String getRedirectUrl()
	{
		return redirectUrl;
	}

	public Campaign()
	{
		version = 0;
		platforms = new HashSet<>();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof Campaign))
			return false;

		Campaign campaign = (Campaign) o;

		return getCampaignId().equals(campaign.getCampaignId());

	}

	@Override
	public int hashCode()
	{
		return getCampaignId().hashCode();
	}

	public void setRedirectUrl(String redirectUrl)
	{
		this.redirectUrl = redirectUrl;
	}

	public long getVersion()
	{
		return version;
	}

	public void setVersion(long version)
	{
		this.version = version;
	}

	public Set<Platform> getPlatforms()
	{
		return platforms;
	}

	public void setPlatforms(Set<Platform> platforms)
	{
		this.platforms = platforms;
	}

	public Date getDateCreated()
	{
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated;
	}

	public Date getDateModified()
	{
		return dateModified;
	}

	public void setDateModified(Date dateModified)
	{
		this.dateModified = dateModified;
	}
}
