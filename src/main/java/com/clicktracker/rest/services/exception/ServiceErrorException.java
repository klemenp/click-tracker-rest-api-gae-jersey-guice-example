package com.clicktracker.rest.services.exception;

/**
 * Created by klemen.
 */
public class ServiceErrorException extends Exception
{
	public ServiceErrorException(String message)
	{
		super(message);
	}

	public ServiceErrorException(String message, Exception cause)
	{
		super(message, cause);
	}
}
