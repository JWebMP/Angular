package com.jwebmp.core.base.angular.modules.services.angular;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.guicedee.guicedinjection.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.angular.typescript.JWebMP.*;
import com.jwebmp.core.base.interfaces.*;
import io.github.classgraph.*;

import java.io.*;
import java.util.*;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;
import static com.jwebmp.core.base.angular.services.JWebMPTypeScriptCompiler.*;
import static com.jwebmp.core.base.angular.services.annotations.NgSourceDirectoryReference.SourceDirectories.*;
import static com.jwebmp.core.base.angular.services.interfaces.ITSComponent.*;


@NgSourceDirectoryReference(value = App)
@NgModuleReference(com.jwebmp.core.base.angular.modules.services.angular.NgModule.class)
@NgModuleReference(NgRouterModule.class)
@NgModuleReference(NgRoutesModule.class)
@com.jwebmp.core.base.angular.services.annotations.NgModule
public class RoutingModule implements INgModule<RoutingModule>
{
	private INgApp<?> app;
	private List<DefinedRoute> definedRoutesList;
	
	@Override
	public Set<String> moduleImports()
	{
		return Set.of("RouterModule.forRoot(routes)");
	}
	
	@Override
	public RoutingModule setApp(INgApp<?> app)
	{
		this.app = app;
		return this;
	}
	
	@Override
	public Set<String> exports()
	{
		return Set.of("RouterModule");
	}
	
	@Override
	public Map<String, String> renderImports(File... srcRelative)
	{
		Map<String, String> out = new HashMap<>();
		
		if (getClass()
				.isAnnotationPresent(NgModuleReferences.class))
		{
			NgModuleReferences references = getClass()
					.getAnnotation(NgModuleReferences.class);
			for (NgModuleReference ngModuleReference : references.value())
			{
				INgModule<?> module = GuiceContext.get(ngModuleReference.value());
				module.imports()
				      .forEach((key, value) -> {
					      out.putIfAbsent(key, value);
				      });
			}
		}
		NgModuleReference ngModuleReference = getClass()
				.getAnnotation(NgModuleReference.class);
		if (ngModuleReference != null)
		{
			INgModule<?> module = GuiceContext.get(ngModuleReference.value());
			module.imports()
			      .forEach((key, value) -> {
				      out.putIfAbsent(key, value);
			      });
		}
		
		
		ScanResult scan = getNgPackageFilterScanResult(app.getClass(), app.getClass()
		                                                                  .getAnnotation(NgApp.class));
		
		Map<String, String> componentReferencePair = new HashMap<>();
		
		definedRoutesList = new ArrayList<>();
		scan
				.getClassesWithAnnotation(NgRoutable.class)
				.stream()
				.forEach(a -> {
							NgRoutable annotation = a.loadClass()
							                         .getAnnotation(NgRoutable.class);
							if (annotation != null)
							{
								DefinedRoute dr = new DefinedRoute();
								dr.setPath(annotation.path());
								dr.setComponentName(getTsFilename(a.loadClass()));
								dr.setComponent((Class<? extends INgComponent<?>>) a.loadClass());
								String referencePath = getFileReference(appDirectories.get(app)
								                                                      .getPath(), a.loadClass());
								componentReferencePair.put(dr.getComponentName(), referencePath);
								definedRoutesList.add(dr);
							}
						}
				);
		
		StringBuilder imports = new StringBuilder();
		for (DefinedRoute definedRoute : definedRoutesList)
		{
			String key = definedRoute.getComponentName();
			String value = definedRoute.getPath();
			File routingFilePath = ITSComponent.getFile(appDirectories.get(app)
			                                                          .getPath(), getClass());
			File routeFilePath = ITSComponent.getFile(appDirectories.get(app)
			                                                        .getPath(), definedRoute.getComponent());
			if (!value.startsWith("@"))
			{
				String relPathhed = getRelativePath(routingFilePath, routeFilePath, null);
				out.put(key, routeFilePath.getPath());
				//	out.add((renderImportStatement(key,
				//		relPathhed)));
			}
			
			else if (value.startsWith("!"))
			{
				out.put(key, value.substring(1));
				//	imports.add((renderImportStatement(key,
				//	value.substring(1))));
			}
			else
			{
				out.put(key, value);
				//imports.add((renderImportStatement(key,
				//		value)));
			}
		}
		
		return out;
	}
	
	@Override
	public Set<String> renderBeforeNgModuleDecorator()
	{
		//render the const class
		ObjectMapper om = GuiceContext.get(DefaultObjectMapper);
		try
		{
			String routesOutput = om.writerWithDefaultPrettyPrinter()
			                        .writeValueAsString(definedRoutesList);
			routesOutput = "const routes: Routes = " + routesOutput + ";\n";
			return Set.of(routesOutput);
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}
		return Set.of();
	}
	
	public static void applyRoute(IComponentHTMLAttributeBase<?, ?> component,
	                              String pathRoute,
	                              String variablePath)
	{
		component.addAttribute("[routerLink]", "['" + pathRoute + "'," +
		                                       variablePath + "]");
	}
}
