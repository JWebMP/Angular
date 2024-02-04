package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.interfaces.IGuiceConfig;
import com.guicedee.guicedinjection.interfaces.IGuiceConfigurator;

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
