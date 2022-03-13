package com.jwebmp.core.base.angular.modules.services.angular;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.guicedee.guicedinjection.*;
import com.jwebmp.core.base.angular.services.DefinedRoute;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.angularconfig.NgBootModuleImportReference;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.interfaces.*;
import io.github.classgraph.*;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;
import static com.jwebmp.core.base.angular.services.JWebMPTypeScriptCompiler.*;
import static com.jwebmp.core.base.angular.services.annotations.NgSourceDirectoryReference.SourceDirectories.*;
import static com.jwebmp.core.base.angular.services.interfaces.ITSComponent.*;


@NgSourceDirectoryReference(value = App)
@NgModuleReference(com.jwebmp.core.base.angular.modules.services.angular.NgModule.class)
@NgModuleReference(NgRouterModule.class)
@NgModuleReference(NgRoutesModule.class)
@NgBootModuleImportReference(name = "RoutingModule", reference = "")
@com.jwebmp.core.base.angular.services.annotations.NgModule
public class RoutingModule implements INgModule<RoutingModule>
{
	private INgApp<?> app;
	private List<DefinedRoute<?>> definedRoutesList;
	
	@Override
	public List<String> moduleImports()
	{
		return List.of("RouterModule.forRoot(routes)");
	}
	
	@Override
	public RoutingModule setApp(INgApp<?> app)
	{
		this.app = app;
		return this;
	}
	
	@Override
	public List<String> exports()
	{
		return List.of("RouterModule");
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
		
		
		ScanResult scan = getNgPackageFilterScanResult(app.getClass(), app.getAnnotation());
		
		Map<String, String> componentReferencePair = new HashMap<>();
		
		Map<Class<? extends INgComponent<?>>, String> baseRoutes = new LinkedHashMap<>();
		Map<Class<? extends INgComponent<?>>, Class<? extends INgComponent<?>>> childParentMappings = new LinkedHashMap<>();
		Map<Class<? extends INgComponent<?>>, String> nestedRoutes = new LinkedHashMap<>();
		
		definedRoutesList = new ArrayList<>();
		scan
				.getClassesWithAnnotation(NgRoutable.class)
				.stream()
				.forEach(a -> {
							Class<? extends INgComponent<?>> aClass = (Class<? extends INgComponent<?>>) a.loadClass();
							NgRoutable annotation = aClass.getAnnotation(NgRoutable.class);
							if (annotation != null)
							{
								if (annotation.parent().length == 0)
								{
									//baseRoutes
									baseRoutes.put(aClass, annotation.path());
								}
								else
								{
									nestedRoutes.put(aClass, annotation.path());
									childParentMappings.put(aClass, annotation.parent()[0]);
								}
							}
						}
				);
		baseRoutes.forEach((aClass, aName) -> {
			DefinedRoute<?> dr = new DefinedRoute<>();
			NgRoutable annotation = aClass.getAnnotation(NgRoutable.class);
			dr.setPath(annotation.path());
			dr.setComponentName(getTsFilename(aClass));
			dr.setComponent(aClass);
			
			buildRoutePathway(filterByValue(childParentMappings, value -> value.equals(aClass)), aClass, dr);
			definedRoutesList.add(dr);
		});
		
		StringBuilder imports = new StringBuilder();
		List<DefinedRoute<?>> drs = definedRoutesList;
		for (DefinedRoute<?> definedRoute : drs)
		{
			List<DefinedRoute<?>> children = definedRoute.getChildren();
			addImportToMap(out, definedRoute);
			buildRouteActualPathway(out, definedRoute, children);
		}
		
		return out;
	}
	
	private void addImportToMap(Map<String, String> out, DefinedRoute<?> definedRoute)
	{
		String key = definedRoute.getComponentName();
		String value = definedRoute.getPath();
		File routingFilePath = ITSComponent.getFile(appDirectories.get(app)
		                                                          .getPath(), getClass());
		File routeFilePath = ITSComponent.getFile(appDirectories.get(app)
		                                                        .getPath(),
				definedRoute.getComponent());
		
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
	
	private void buildRouteActualPathway(Map<String, String> out, DefinedRoute<?> definedRoute, List<DefinedRoute<?>> children)
	{
		while (!children.isEmpty())
		{
			for (DefinedRoute<?> child : children)
			{
				addImportToMap(out, child);
				buildRouteActualPathway(out, child, child.getChildren());
				
			}
			children = new ArrayList<>();
		}
	}
	
	private void buildRoutePathway(Map<Class<? extends INgComponent<?>>, Class<? extends INgComponent<?>>> childParentMappings, Class<? extends INgComponent<?>> aClass, DefinedRoute<?> dr)
	{
		DefinedRoute currentDr = dr;
		Map<Class<? extends INgComponent<?>>, Class<? extends INgComponent<?>>> currentRoute = childParentMappings;
		while (!currentRoute.isEmpty())
		{
			for (Map.Entry<Class<? extends INgComponent<?>>, Class<? extends INgComponent<?>>> entry : currentRoute.entrySet())
			{
				Class<? extends INgComponent<?>> routeClass = entry.getKey();
				Class<? extends INgComponent<?>> routeParent = entry.getValue();
				NgRoutable innerAnnotation = routeClass.getAnnotation(NgRoutable.class);
				DefinedRoute<?> innerDr = new DefinedRoute<>();
				innerDr.setPath(innerAnnotation.path());
				innerDr.setComponentName(getTsFilename(routeClass));
				innerDr.setComponent(routeClass);
				currentDr.addChild(innerDr);
				buildRoutePathway(filterByValue(childParentMappings, value -> value.equals(routeClass)), routeClass, innerDr);
			}
			currentRoute.clear();
		}
	}
	
	static <K, V> Map<K, V> filterByValue(Map<K, V> map, Predicate<V> predicate)
	{
		return map.entrySet()
		          .stream()
		          .filter(entry -> predicate.test(entry.getValue()))
		          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	
	@Override
	public List<String> renderBeforeNgModuleDecorator()
	{
		//render the const class
		ObjectMapper om = GuiceContext.get(DefaultObjectMapper);
		try
		{
			String routesOutput = om.writerWithDefaultPrettyPrinter()
			                        .writeValueAsString(definedRoutesList);
			routesOutput = "const routes: Routes = " + routesOutput + ";\n";
			return List.of(routesOutput);
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}
		return List.of();
	}
	
	public static void applyRoute(IComponentHTMLAttributeBase<?, ?> component,
	                              String pathRoute,
	                              String variablePath)
	{
		component.addAttribute("[routerLink]", "['" + pathRoute + "'," +
		                                       variablePath + "]");
	}
	
	public static void applyRoute(IComponentHTMLAttributeBase<?, ?> component,
	                              Class<? extends INgComponent<?>> pathRoute)
	{
		applyRoute(component, pathRoute, "");
	}
	
	public static void applyRoute(IComponentHTMLAttributeBase<?, ?> component,
	                              Class<? extends INgComponent<?>> pathRoute,
	                              String variablePath)
	{
		component.addAttribute("[routerLink]", "['" + pathRoute.getAnnotation(NgRoutable.class)
		                                                       .path() + "'," +
		                                       variablePath + "]");
	}
}
