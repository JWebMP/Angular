package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.interfaces.*;
import com.guicedee.logger.*;
import com.jwebmp.core.base.angular.client.services.*;
import com.jwebmp.core.base.angular.services.compiler.*;

import java.util.logging.*;

import static com.jwebmp.core.base.angular.implementations.AngularTSPostStartup.*;


public class AngularTSPreStartup implements IGuicePreStartup<AngularTSPreStartup>
{
	private static final Logger log = LogFactory.getLog(AngularTSPreStartup.class);
	
	@Override
	public void onStartup()
	{
		if (loadTSOnStartup)
		{
			log.info("Pre Startup - Scanning for TS Annotations");
			try
			{
				AnnotationsMap.loadAllClasses();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
