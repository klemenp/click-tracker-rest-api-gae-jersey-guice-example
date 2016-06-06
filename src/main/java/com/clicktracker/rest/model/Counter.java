package com.clicktracker.rest.model;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

import java.io.Serializable;

/**
 * Created by klemen.
 */
@Cache
@Entity
public class Counter implements Serializable
{
	@Id
	private String counterId;

	private long          counterShard;
	@Index
	private Ref<Campaign> campaign;
	@Index
	private Platform      platform;
	private long          numberOfClicks;

	public Counter(Campaign campaign, Platform platform, long counterShard)
	{
		this.numberOfClicks = 0;
		this.campaign = Ref.create(campaign);
		this.platform = platform;
		this.counterShard = counterShard;
		this.counterId = createId(campaign, platform, counterShard);
	}

	public Counter()
	{
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof Counter))
			return false;

		Counter counter = (Counter) o;

		return getCounterId().equals(counter.getCounterId());

	}

	@Override
	public int hashCode()
	{
		return getCounterId().hashCode();
	}

	public static String createId(Campaign campaign, Platform platform, long counterShard)
	{
		return campaign.getCampaignId() + "_" + platform.toString() + "_" + counterShard;
	}

	public void increaseNumberOfClicks()
	{
		this.numberOfClicks++;
	}

	public String getCounterId()
	{
		return counterId;
	}

	public void setCounterId(String counterId)
	{
		this.counterId = counterId;
	}

	public long getCounterShard()
	{
		return counterShard;
	}

	public void setCounterShard(long counterShard)
	{
		this.counterShard = counterShard;
	}

	public Platform getPlatform()
	{
		return platform;
	}

	public void setPlatform(Platform platform)
	{
		this.platform = platform;
	}

	public long getNumberOfClicks()
	{
		return numberOfClicks;
	}

	public void setNumberOfClicks(long numberOfClicks)
	{
		this.numberOfClicks = numberOfClicks;
	}

	public Ref<Campaign> getCampaign()
	{
		return campaign;
	}

	public void setCampaign(Ref<Campaign> campaign)
	{
		this.campaign = campaign;
	}
}
