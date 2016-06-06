package com.clicktracker.rest.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by klemen.
 */
public enum Platform implements Serializable
{
	ANDROID("Android"),
	IPHONE("IPhone"),
	WINDOWSPHONE("WindowsPhone");

	private final String value;
	private static Map<String, Platform> constants = new HashMap<String, Platform>();

	static
	{
		for (Platform c: Platform.values())
		{
			constants.put(c.value, c);
		}
	}

	private Platform(String value)
	{
		this.value = value;
	}

	@JsonValue
	@Override
	public String toString() {
		return this.value;
	}

	@JsonCreator
	public static Platform fromValue(String value)
	{
		Platform constant = constants.get(value);
		if (constant == null)
		{
			throw new IllegalArgumentException(value);
		}
		else
		{
			return constant;
		}
	}
}
