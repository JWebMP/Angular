package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.*;
import com.guicedee.guicedinjection.interfaces.*;

public class SearchPathConfigurator implements IGuiceConfigurator
{
	@Override
	public GuiceConfig configure(GuiceConfig config)
	{
		config.setAnnotationScanning(true);
		config.setClasspathScanning(true);
		return config;
	}
}
