package com.clicktracker.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by klemen.
 */
public class SettingsHelper
{
	public static final String SETTING_KEY_ADMIN_USERNAME = "api.admin.credentials.username";
	public static final String SETTING_KEY_ADMIN_PASSWORD_MD5 = "api.admin.credentials.password_md5";

	public static final String SETTING_KEY_TRACKER_INVALID_CLICK_REDIRECT_URL = "api.tracker.invalid_click.redirect_url";

	public static final String SETTING_KEY_MAX_COUNTER_SHARDS = "api.tracker.max_counter_shards";

	private static final String SETTING_PROPERTIES_PATH = "settings.properties";

	public static String getSetting(String key)
	{
		return getProperties(SETTING_PROPERTIES_PATH).getProperty(key);
	}

	private static Properties getProperties(String propertiesPath)
	{
		Properties prop = new Properties();
		ClassLoader loader = SettingsHelper.class.getClassLoader();
		InputStream stream = loader.getResourceAsStream(propertiesPath);
		if (stream==null)
		{
			throw new IllegalArgumentException("Cannot read file named " + propertiesPath);
		}
		else
		{
			try
			{
				prop.load(stream);
			}
			catch (IOException e)
			{
				throw new IllegalArgumentException("Cannot read file named " + propertiesPath);
			}
		}
		return prop;
	}
}
