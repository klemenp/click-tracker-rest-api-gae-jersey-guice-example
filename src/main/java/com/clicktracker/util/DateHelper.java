package com.clicktracker.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class DateHelper
{
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static String dateToDateString(Date date)
	{
		SimpleDateFormat format = new SimpleDateFormat();
		format.applyPattern(DATE_FORMAT);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format.format(date);
	}

	public static Date timestampStringToTimestamp(String date) throws ParseException
	{
		SimpleDateFormat format = new SimpleDateFormat();
		format.applyPattern(DATE_FORMAT);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return new Date(format.parse(date).getTime());
	}
}
