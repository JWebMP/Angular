package com.jwebmp.core.base.angular.modules.services.angular;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.google.common.base.*;
import com.guicedee.guicedinjection.*;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.boot.*;
import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.client.annotations.typescript.*;
import com.jwebmp.core.base.angular.client.annotations.routing.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.DefinedRoute;

import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.interfaces.*;
import io.github.classgraph.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;
import static com.jwebmp.core.base.angular.client.services.AnnotationsMap.*;
import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.*;


@NgImportReference(value = "RouterModule, ParamMap,Router", reference = "@angular/router")
@NgImportReference(value = "Routes", reference = "@angular/router")

@NgBootModuleImport("RoutingModule")
@TsDependency(value = "@angular/router", version = "^13.3.1")

@NgModule
public class RoutingModule implements INgModule<RoutingModule>
{
	private INgApp<?> app;
	private List<DefinedRoute<?>> definedRoutesList;
	private static List<DefinedRoute<?>> routes = new ArrayList<>();
	
	public static List<DefinedRoute<?>> getRoutes()
	{
		if (routes.isEmpty())
		{
			try
			{
				new RoutingModule().buildRoutes();
			}
			catch (Throwable T)
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
	public List<NgImportReference> getAllImportAnnotations()
	{
		List<NgImportReference> out = INgModule.super.getAllImportAnnotations();
		if (definedRoutesList == null)
		{
			buildRoutes();
		}
		for (DefinedRoute<?> definedRoute : definedRoutesList)
		{
			NgComponentReference ngComponentReference = getNgComponentReference(definedRoute.getComponent());
			out.addAll(putRelativeLinkInMap(getClass(), ngComponentReference));
			buildImportReferenceNest(out, definedRoute);
		}
		return out;
	}
	
	private void buildImportReferenceNest(List<NgImportReference> out, DefinedRoute<?> definedRoute)
	{
		List<DefinedRoute<?>> dChildren = definedRoute.getChildren();
		for (DefinedRoute<?> dChild : dChildren)
		{
			out.addAll(addImportToMap(dChild));
			buildImportReferenceNest(out, dChild);
		}
	}
	
	public void buildRoutes()
	{
		ScanResult scan = GuiceContext.instance()
		                              .getScanResult();
		
		Map<Class<? extends IComponent<?>>, String> baseRoutes = new LinkedHashMap<>();
		Map<Class<? extends IComponent<?>>, Class<? extends IComponent<?>>> childParentMappings = new LinkedHashMap<>();
		Map<Class<? extends IComponent<?>>, String> nestedRoutes = new LinkedHashMap<>();
		
		definedRoutesList = new ArrayList<>();
		scan
				.getClassesWithAnnotation(NgRoutable.class)
				.stream()
				.sorted(Comparator.comparingInt(o -> o.loadClass()
				                                      .getAnnotation(NgRoutable.class)
				                                      .sortOrder()))
				.forEach(a -> {
							Class<? extends INgComponent<?>> aClass = (Class<? extends INgComponent<?>>) a.loadClass();
							NgRoutable annotation = aClass.getAnnotation(NgRoutable.class);
							if (annotation != null)
							{
								if (annotation.parent().length == 0)
								{
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
			DefinedRoute<?> dr = getDefinedRoute(aClass);
			
			buildRoutePathway(filterByValue(childParentMappings, value -> value.equals(aClass)), aClass, dr);
			definedRoutesList.add(dr);
			routes.add(dr);
		});
		for (DefinedRoute<?> definedRoute : definedRoutesList)
		{
			if (definedRoute.getComponent() != null)
			{
				List<DefinedRoute<?>> children = definedRoute.getChildren();
				buildRouteActualPathway(definedRoute, children);
			}
		}
	}
	
	private DefinedRoute<?> getDefinedRoute(Class<? extends IComponent<?>> aClass)
	{
		DefinedRoute<?> dr = new DefinedRoute<>();
		NgRoutable annotation = aClass.getAnnotation(NgRoutable.class);
		dr.setPath(annotation.path());
		dr.setRenderComponent(!annotation.ignoreComponent());
		dr.setComponent(aClass);
		dr.setComponentName(getTsFilename(aClass));
		
		if (!Strings.isNullOrEmpty(annotation.redirectTo()))
		{
			dr.setRedirectTo(annotation.redirectTo());
		}
		if (!Strings.isNullOrEmpty(annotation.pathMatch()))
		{
			dr.setPathMatch(annotation.pathMatch());
		}
		return dr;
	}
	
	private List<NgImportReference> addImportToMap(DefinedRoute<?> definedRoute)
	{
		NgComponentReference reference = getNgComponentReference(definedRoute.getComponent());
		if (definedRoute.getComponent() != null)
		{
			return putRelativeLinkInMap(getClass(), reference);
		}
		return new ArrayList<>();
	}
	
	private List<NgImportReference> buildRouteActualPathway(DefinedRoute<?> definedRoute, List<DefinedRoute<?>> children)
	{
		List<NgImportReference> out = new ArrayList<>();
		
		while (!children.isEmpty())
		{
			for (DefinedRoute<?> child : children)
			{
				if (child.getComponent() != null)
				{
					if (child.getComponent() != null)
					{
						out.addAll(addImportToMap(child));
					}
					out.addAll(buildRouteActualPathway(child, child.getChildren()));
				}
			}
			children = new ArrayList<>();
		}
		return out;
	}
	
	private void buildRoutePathway(Map<Class<? extends IComponent<?>>, Class<? extends IComponent<?>>> childParentMappings, Class<? extends IComponent<?>> aClass, DefinedRoute<?> dr)
	{
		DefinedRoute currentDr = dr;
		Map<Class<? extends IComponent<?>>, Class<? extends IComponent<?>>> currentRoute = childParentMappings;
		while (!currentRoute.isEmpty())
		{
			for (Map.Entry<Class<? extends IComponent<?>>, Class<? extends IComponent<?>>> entry : currentRoute.entrySet())
			{
				Class<? extends IComponent<?>> routeClass = entry.getKey();
				Class<? extends IComponent<?>> routeParent = entry.getValue();
				NgRoutable innerAnnotation = routeClass.getAnnotation(NgRoutable.class);
				DefinedRoute<?> innerDr = getDefinedRoute(routeClass);
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
