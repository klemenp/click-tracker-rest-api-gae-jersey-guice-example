package com.clicktracker.util;

import java.security.MessageDigest;

/**
 * Created by klemen.
 */
public class MD5Helper
{
	public static String compute(String md5)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i)
			{
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return sb.toString();
		}
		catch (java.security.NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}
}
