package com.clicktracker.rest.services.exception;

/**
 * Created by klemen.
 */
public class InvalidCampaignException extends Exception
{
	public InvalidCampaignException(String message)
	{
		super(message);
	}

	public InvalidCampaignException(String message, Exception cause)
	{
		super(message, cause);
	}
}
