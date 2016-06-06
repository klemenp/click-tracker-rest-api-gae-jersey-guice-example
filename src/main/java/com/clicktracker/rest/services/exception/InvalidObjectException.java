package com.clicktracker.rest.services.exception;

/**
 * Created by klemen.
 */
public class InvalidObjectException extends Exception
{
	public InvalidObjectException(String message)
	{
		super(message);
	}

	public InvalidObjectException(String message, Exception cause)
	{
		super(message, cause);
	}
}
