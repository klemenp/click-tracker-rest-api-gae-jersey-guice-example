package com.clicktracker.rest;

import com.clicktracker.util.DateHelper;
import com.clicktracker.util.MD5Helper;
import com.clicktracker.util.SettingsHelper;
import com.sun.jersey.core.util.Base64;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Created by klemen.
 */
public abstract class BaseAPI
{
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER   = "Access-Control-Allow-Origin";
	public static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER  = "Access-Control-Allow-Headers";
	public static final String ACCESS_CONTROL_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";
	public static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER	= "Access-Control-Allow-Methods";
	public static final String ALLOW_HEADER							= "Allow";

	private static final   Logger log                      = Logger.getLogger(TrackerAPI.class.getName());

	protected static final int    UNAUTHORIZED_STATUS_CODE = 401;

	protected ObjectMapper objectMapper;

	protected boolean authenticate(String authorizationHeader)
	{
		log.finer("Authenticating with headers ...");

		if (authorizationHeader != null && !authorizationHeader.isEmpty())
		{
			final String schema = "Basic";
			authorizationHeader = authorizationHeader.replaceFirst(schema, "").trim();
			// base64 decode and then explode
			String usernameAndPassword = new String(Base64.base64Decode(authorizationHeader));
			if (usernameAndPassword != null)
			{
				final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
				final String username = tokenizer.nextToken();
				final String password = tokenizer.nextToken();

				if (username!=null && !username.equals(SettingsHelper.getSetting(SettingsHelper.SETTING_KEY_ADMIN_USERNAME)))
				{
					log.finer("Authentication failed. Invalid username.");
					// Invalid username
					return false;
				}
				else if (password!=null && !MD5Helper.compute(password).equals(SettingsHelper.getSetting(SettingsHelper.SETTING_KEY_ADMIN_PASSWORD_MD5)))
				{
					// Invalid username
					log.finer("Authentication failed. Invalid password.");
					return false;
				}
				else
				{
					log.finer("Authentication succeeded");
					return true;
				}
			}
			log.finer("Authentication failed. Invalid authorisation header.");
			return false;
		}
		log.finer("Authentication failed. No authorisation header or empty. " + authorizationHeader);
		return false;
	}

	@PostConstruct
	public void postConstruct()
	{
		log.finer("Instantiating jackson object mapper");
		objectMapper =  new ObjectMapper();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		objectMapper.setDateFormat(df);
	}
}
