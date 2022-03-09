package com.jwebmp.core.base.angular.typescript;

import com.guicedee.guicedinjection.*;
import com.guicedee.guicedservlets.undertow.*;
import com.guicedee.logger.*;
import com.jwebmp.core.base.angular.services.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.logging.*;

import static com.jwebmp.core.base.angular.services.interfaces.ITSComponent.*;

public class JWebMPTypeScriptCompilerTest
{
	
	@Test
	public void testBoot() throws Exception
	{
		LogFactory.configureConsoleColourOutput(Level.FINE);
		GuicedUndertow.boot("localhost", 6523);
	}
	
	public static void main(String[] args) throws Exception
	{
		LogFactory.configureConsoleColourOutput(Level.FINE);
		GuicedUndertow.boot("localhost", 6523);
	}
	
	
	@Test
	public void testAppSearch() throws IOException
	{
		GuiceContext.inject();
		for (INgApp<?> app : JWebMPTypeScriptCompiler.getAllApps())
		{
			JWebMPTypeScriptCompiler compiler = new JWebMPTypeScriptCompiler(app);
			
			System.out.println("Generating @NgApp (" + getTsFilename(app.getClass()) + ") " +
			                   "in folder " + getClassDirectory(app.getClass()));
			System.out.println("================");
			//	compiler.renderAppTS(app);
			System.out.println("================");
		}
	}
}