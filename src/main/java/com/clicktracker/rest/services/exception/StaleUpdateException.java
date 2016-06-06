package com.clicktracker.rest.services.exception;

/**
 * Created by klemen.
 */
public class StaleUpdateException extends Exception
{
	public StaleUpdateException(String message)
	{
		super(message);
	}

	public StaleUpdateException(String message, Exception cause)
	{
		super(message, cause);
	}
}
