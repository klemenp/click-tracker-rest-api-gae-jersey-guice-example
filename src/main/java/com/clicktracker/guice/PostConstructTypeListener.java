package com.clicktracker.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by klemen.
 */
public class PostConstructTypeListener implements TypeListener
{
	private final String packagePrefix;

	public PostConstructTypeListener(String packagePrefix)
	{
		this.packagePrefix = packagePrefix;
	}

	@Override
	public <I> void hear(final TypeLiteral<I> typeLiteral,
		TypeEncounter<I> typeEncounter)
	{
		Class<? super I> clz = typeLiteral.getRawType();
		if (packagePrefix != null && !clz.getName().startsWith(packagePrefix))
		{
			return;
		}

		final Method method = getPostConstructMethod(clz);
		if (method != null)
		{
			typeEncounter.register(new InjectionListener<I>()
			{
				@Override
				public void afterInjection(Object i)
				{
					try
					{
						// call the @PostConstruct annotated method after all dependencies have been injected
						method.invoke(i);
					}
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
					{
						throw new RuntimeException(e);
					}
				}
			});
		}
	}

	private Method getPostConstructMethod(Class<?> clz)
	{
		for (Method method : clz.getDeclaredMethods())
		{
			if (method.getAnnotation(PostConstruct.class) != null
				&& isPostConstructEligible(method))
			{
				method.setAccessible(true);
				return method;
			}
		}
		Class<?> superClz = clz.getSuperclass();
		return (superClz == Object.class || superClz == null) ?
			null :
			getPostConstructMethod(superClz);
	}

	private boolean isPostConstructEligible(final Method method)
	{
		return (method.getReturnType() == void.class) && (
			method.getParameterTypes().length == 0) && (
			method.getExceptionTypes().length == 0);
	}
}
