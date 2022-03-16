package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.interfaces.*;
import com.guicedee.logger.*;
import com.jwebmp.core.base.angular.services.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.io.*;
import java.util.logging.*;

import static com.jwebmp.core.base.angular.services.interfaces.ITSComponent.*;


public class AngularTSPostStartup implements IGuicePostStartup<AngularTSPostStartup>
{
	private static final Logger log = LogFactory.getLog(AngularTSPostStartup.class);
	
	@Override
	public void postLoad()
	{
		for (INgApp<?> app : JWebMPTypeScriptCompiler.getAllApps())
		{
			JWebMPTypeScriptCompiler compiler = new JWebMPTypeScriptCompiler(app);
			log.info("Post Startup - Generating @NgApp (" + getTsFilename(app.getClass()) + ") " +
			         "in folder " + getClassDirectory(app.getClass()));
			try
			{
				compiler.renderAppTS();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
