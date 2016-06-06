package com.clicktracker.rest.services.exception;

/**
 * Created by klemen.
 */
public class NotFoundException extends Exception
{
	public NotFoundException(String message)
	{
		super(message);
	}

	public NotFoundException(String message, Exception cause)
	{
		super(message, cause);
	}
}
