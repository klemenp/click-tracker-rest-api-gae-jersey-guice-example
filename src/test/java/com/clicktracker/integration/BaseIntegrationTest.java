package com.clicktracker.integration;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.junit.After;
import org.junit.Before;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by klemen.
 */
public abstract class BaseIntegrationTest
{
	protected static final String ADMIN_USERNAME_KEY             = "api.admin.credentials.username";
	protected static final String ADMIN_PASSWORD_KEY             = "api.admin.credentials.password";
	protected static final String INVALID_CLICK_REDIRECT_URL_KEY = "api.tracker.invalid_click.redirect_url";
	protected static final String API_TRACKER_CLICK_URL_KEY      = "api.tracker.click.url";
	protected static final String API_ADMIN_URL_KEY              = "api.admin.url";
	protected static final String API_CLICKS_URL_KEY              = "api.clicks.url";

	private static final String TEST_PROPERTIES_PATH = "test.properties";

	protected static final Logger log = Logger.getLogger(BaseIntegrationTest.class.getName());

	protected ObjectMapper objectMapper;

	protected String getTestProperty(String key)
	{
		Properties prop = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream stream = loader.getResourceAsStream(TEST_PROPERTIES_PATH);
		if (stream==null)
		{
			throw new IllegalArgumentException("Cannot read file named " + TEST_PROPERTIES_PATH);
		}
		else
		{
			try
			{
				prop.load(stream);
			}
			catch (IOException e)
			{
				throw new IllegalArgumentException("Cannot read file named " + TEST_PROPERTIES_PATH, e);
			}
		}
		return prop.getProperty(key);
	}
	protected InputStream doGetRequest(String url, String mimeType, Map<String, String> queryParams, UsernamePasswordCredentials basicCredentials, int checkStatus) throws
		ClientProtocolException,
		IOException
	{
		HttpResponse response = executeHttpGetRequest(url, mimeType, queryParams, basicCredentials, false);
		assertEquals(checkStatus, response.getStatusLine().getStatusCode());
		return response.getEntity().getContent();
	}

	protected HttpResponse executeHttpGetRequest(String url, String mimeType, Map<String, String> queryParams, UsernamePasswordCredentials basicCredentials, boolean handleRedirects) throws
		IOException
	{
		log.info("Executing GET request to " + url);

		if (queryParams != null)
		{
			if (queryParams.size()>0)
			{
				url += "?";
				for (Map.Entry<String,String> queryParam : queryParams.entrySet())
				{
					log.info("Query param: " + queryParam.getKey() + " = " + queryParam.getValue());
					url += URLEncoder.encode(queryParam.getKey(),"UTF-8") + "="+URLEncoder.encode(queryParam.getValue(),"UTF-8") + "&";
				}
			}
		}

		// Make request
		DefaultHttpClient client = new DefaultHttpClient();
	 	if (basicCredentials!=null)
		{
			log.info("Using basic credentials");
			CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY, basicCredentials);
			client.setCredentialsProvider(provider);
		}
		if (!handleRedirects)
		{
			client.getParams()
				.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
		}

		HttpGet request = new HttpGet(url);
		if (mimeType != null)
		{
			request.addHeader(HttpHeaders.ACCEPT, mimeType);
		}
		return client.execute(request);
	}

	protected HttpResponse executeHttpPostRequest(String url, String mimeType, Map<String, String> queryParams, UsernamePasswordCredentials basicCredentials, String postBody, boolean handleRedirects) throws
		IOException
	{
		log.info("Executing POST request to " + url);

		if (queryParams != null)
		{
			if (queryParams.size()>0)
			{
				url += "?";
				for (Map.Entry<String,String> queryParam : queryParams.entrySet())
				{
					log.info("Query param: " + queryParam.getKey() + " = " + queryParam.getValue());
					url += URLEncoder.encode(queryParam.getKey(),"UTF-8") + "="+URLEncoder.encode(queryParam.getValue(),"UTF-8") + "&";
				}
			}
		}

		// Make request
		DefaultHttpClient client = new DefaultHttpClient();
		if (basicCredentials!=null)
		{
			CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY, basicCredentials);
			client.setCredentialsProvider(provider);
		}
		if (!handleRedirects)
		{
			client.getParams()
				.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
		}
		HttpPost request = new HttpPost(url);
		request.setEntity(new StringEntity(postBody, "UTF-8"));
		if (mimeType!=null)
		{
			log.info("Setting Content-Type " + mimeType);
			request.addHeader(HttpHeaders.CONTENT_TYPE, mimeType);
		}
		return client.execute(request);
	}

	protected HttpResponse executeHttpPutRequest(String url, String mimeType, Map<String, String> queryParams, UsernamePasswordCredentials basicCredentials, String postBody, boolean handleRedirects) throws
		IOException
	{
		log.info("Executing POST request to " + url);

		if (queryParams != null)
		{
			if (queryParams.size()>0)
			{
				url += "?";
				for (Map.Entry<String,String> queryParam : queryParams.entrySet())
				{
					log.info("Query param: " + queryParam.getKey() + " = " + queryParam.getValue());
					url += URLEncoder.encode(queryParam.getKey(),"UTF-8") + "="+URLEncoder.encode(queryParam.getValue(),"UTF-8") + "&";
				}
			}
		}

		// Make request
		DefaultHttpClient client = new DefaultHttpClient();
		if (basicCredentials!=null)
		{
			CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY, basicCredentials);
			client.setCredentialsProvider(provider);
		}
		if (!handleRedirects)
		{
			client.getParams()
				.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
		}
		HttpPut request = new HttpPut(url);
		request.setEntity(new StringEntity(postBody, "UTF-8"));
		if (mimeType!=null)
		{
			log.info("Setting Content-Type " + mimeType);
			request.addHeader(HttpHeaders.CONTENT_TYPE, mimeType);
			request.addHeader(HttpHeaders.ACCEPT, mimeType);
		}
		return client.execute(request);
	}

	@Before
	public void setUp()
	{
		objectMapper =  new ObjectMapper();
	}

	protected String createJsonFromObject(Object object) throws IOException
	{
		return objectMapper.writeValueAsString(object);
	}

	protected <T> T createObjectFromJson(InputStream json,  Class<T> type) throws Exception
	{
		return objectMapper.readValue(json, type );
	}

	protected  <T> List<T> createObjectListFromJson(InputStream json, Class<T> clazz) throws Exception
	{
		JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, clazz);
		return objectMapper.readValue(json, javaType);
	}

	@After
	public void tearDown()
	{

	}

	protected UsernamePasswordCredentials getBasicAdminCredentials()
	{
		String user = getTestProperty(ADMIN_USERNAME_KEY);
		String password = getTestProperty(ADMIN_PASSWORD_KEY);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
		return credentials;
	}

}
