package com.clicktracker.rest.services.exception;

/**
 * Created by klemen.
 */
public class InvalidPlatformException extends Exception
{
	public InvalidPlatformException(String message)
	{
		super(message);
	}

	public InvalidPlatformException(String message, Exception cause)
	{
		super(message, cause);
	}
}
