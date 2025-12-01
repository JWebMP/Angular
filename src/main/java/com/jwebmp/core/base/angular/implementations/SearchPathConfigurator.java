package com.jwebmp.core.base.angular.implementations;

import com.guicedee.client.services.IGuiceConfig;
import com.guicedee.client.services.lifecycle.IGuiceConfigurator;

public class SearchPathConfigurator implements IGuiceConfigurator
{
	@Override
	public IGuiceConfig<?> configure(IGuiceConfig<?> config)
	{
		config.setAnnotationScanning(true);
		config.setClasspathScanning(true);
		config.setPathScanning(true);
		config.setAllowPaths(true);
		return config;
	}
}
