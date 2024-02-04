package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.interfaces.IGuicePreStartup;
import com.jwebmp.core.base.angular.client.services.AnnotationsMap;
import lombok.extern.java.Log;

import static com.jwebmp.core.base.angular.implementations.AngularTSPostStartup.loadTSOnStartup;

@Log
public class AngularTSPreStartup implements IGuicePreStartup<AngularTSPreStartup>
{
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
