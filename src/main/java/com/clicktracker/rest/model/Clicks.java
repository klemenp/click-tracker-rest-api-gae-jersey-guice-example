package com.clicktracker.rest.model;

import java.io.Serializable;

/**
 * Created by klemen.
 */
public class Clicks implements Serializable
{
	private long numberOfClicks;

	public long getNumberOfClicks()
	{
		return numberOfClicks;
	}

	public void setNumberOfClicks(long numberOfClicks)
	{
		this.numberOfClicks = numberOfClicks;
	}
}
