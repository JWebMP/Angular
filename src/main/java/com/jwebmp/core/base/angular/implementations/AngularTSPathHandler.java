package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.*;
import com.guicedee.guicedservlets.undertow.services.*;
import com.jwebmp.core.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import io.github.classgraph.*;
import io.undertow.*;
import io.undertow.predicate.*;
import io.undertow.server.*;
import io.undertow.server.handlers.*;
import io.undertow.server.handlers.resource.*;
import org.apache.commons.io.*;

import java.io.*;

public class AngularTSPathHandler implements UndertowPathHandler<AngularTSPathHandler>
{
	@Override
	public HttpHandler registerPathHandler(HttpHandler incoming)
	{
		if (incoming == null)
		{
			for (ClassInfo classInfo : GuiceContext.instance()
			                                       .getScanResult()
			                                       .getClassesWithAnnotation(NgApp.class))
			{
				Class<?> loadClass = classInfo.loadClass();
				NgApp app = loadClass.getAnnotation(NgApp.class);
				PageConfiguration pc = loadClass.getAnnotation(PageConfiguration.class);
				File file = new File(FileUtils.getUserDirectory() + "/jwebmp/" + app.value() + "/dist/jwebmp/");
				incoming = Handlers.predicate(
						Predicates.suffixes(".png", ".css", ".jpg", ".svg", "*.js", "*.html"),
						Handlers.resource(new PathResourceManager(file.toPath())), incoming);
				;
			}
			
		}
		return incoming;
	}
}
