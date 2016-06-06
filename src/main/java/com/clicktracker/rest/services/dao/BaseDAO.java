package com.clicktracker.rest.services.dao;

/**
 * Created by klemen.
 */
public interface BaseDAO
{
	class StaleUpdateException extends Exception
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

	class NoDataException extends Exception
	{
		public NoDataException(String message)
		{
			super(message);
		}

		public NoDataException(String message, Exception cause)
		{
			super(message, cause);
		}
	}
}
