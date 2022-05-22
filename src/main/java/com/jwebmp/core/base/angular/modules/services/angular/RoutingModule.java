package com.jwebmp.core.base.angular.modules.services.angular;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.google.common.base.*;
import com.guicedee.guicedinjection.*;
import com.jwebmp.core.base.angular.services.DefinedRoute;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.interfaces.*;
import io.github.classgraph.*;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;
import static com.jwebmp.core.base.angular.services.compiler.AnnotationsMap.*;
import static com.jwebmp.core.base.angular.services.compiler.JWebMPTypeScriptCompiler.*;
import static com.jwebmp.core.base.angular.services.interfaces.ITSComponent.*;


@NgImportReference(name = "RouterModule, ParamMap,Router",reference="@angular/router")
@NgImportReference(name = "Routes",reference="@angular/router")

@NgBootModuleImport("RoutingModule")
@TsDependency(value = "@angular/router",version = "^13.3.1")

@NgModule
public class RoutingModule implements INgModule<RoutingModule>
{
	private INgApp<?> app;
	private List<DefinedRoute<?>> definedRoutesList;
	private static List<DefinedRoute<?>> routes = new ArrayList<>();
	
	public static List<DefinedRoute<?>> getRoutes()
	{
		if(routes.isEmpty())
		{
			try
			{
				new RoutingModule().renderImports();
			}catch (Throwable T)
			{
			
			}
		}
		
		return routes;
	}
	
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
	public StringBuilder renderImports()
	{
		Map<String, String> out = new HashMap<>();
		
		List<NgImportReference> refs = getAnnotations(getClass(), NgImportReference.class);
		for (NgImportReference ref : refs)
		{
			out.putIfAbsent(ref.name(), ref.reference());
		}
		
		ScanResult scan = GuiceContext.instance()
		                              .getScanResult();
		
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
			if (!Strings.isNullOrEmpty(annotation.redirectTo()))
			{
				dr.setRedirectTo(annotation.redirectTo());
			}
			if (!Strings.isNullOrEmpty(annotation.pathMatch()))
			{
				dr.setPathMatch(annotation.pathMatch());
			}
			
			buildRoutePathway(filterByValue(childParentMappings, value -> value.equals(aClass)), aClass, dr);
			definedRoutesList.add(dr);
			routes.add(dr);
		});
		
		StringBuilder imports = new StringBuilder();
		List<DefinedRoute<?>> drs = definedRoutesList;
		for (DefinedRoute<?> definedRoute : drs)
		{
			List<DefinedRoute<?>> children = definedRoute.getChildren();
			addImportToMap(out, definedRoute);
			buildRouteActualPathway(out, definedRoute, children);
		}
		out.forEach((key,value)->{
			if (!key.startsWith("!"))
			{
				imports.append(String.format(importString, key, value));
			}
			else
			{
				imports.append(String.format(importPlainString, key.substring(1), value));
			}
		});
		
		return imports;
	}
	
	private void addImportToMap(Map<String, String> out, DefinedRoute<?> definedRoute)
	{
		NgComponentReference reference = getNgComponentReference(definedRoute.getComponent());
		putRelativeLinkInMap(getClass(), out, reference);
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
	public String renderBeforeClass()
	{
		//render the const class
		ObjectMapper om = GuiceContext.get(DefaultObjectMapper);
		try
		{
			String routesOutput = om.writerWithDefaultPrettyPrinter()
			                        .writeValueAsString(definedRoutesList);
			routesOutput = "const routes: Routes = " + routesOutput + ";\n";
			return routesOutput;
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}
		return "";
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
