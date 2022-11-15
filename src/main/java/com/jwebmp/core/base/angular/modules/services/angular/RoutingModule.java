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

import com.jwebmp.core.base.interfaces.*;
import io.github.classgraph.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;
import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.*;


@NgImportReference(value = "RouterModule, ParamMap,Router", reference = "@angular/router")
@NgImportReference(value = "Routes", reference = "@angular/router")

@NgBootModuleImport("RoutingModule")
@TsDependency(value = "@angular/router", version = "^13.3.0")

@NgModule
public class RoutingModule implements INgModule<RoutingModule>
{
	private INgApp<?> app;
	private List<DefinedRoute<?>> definedRoutesList;
	private static List<DefinedRoute<?>> routes = new ArrayList<>();
	
	private static RoutingModuleOptions options;
	
	public static RoutingModuleOptions getOptions()
	{
		if (options == null)
		{
			options = new RoutingModuleOptions();
		}
		return options;
	}
	
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
		if (options == null)
		{
			return List.of("RouterModule.forRoot(routes)");
		}
		else
		{
			try
			{
				return List.of("RouterModule.forRoot(routes," + new ObjectMapper()
				                                                            .disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES)
				                                                            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
				                                                            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
				                                                            .writeValueAsString(options) + ")");
			}
			catch (JsonProcessingException e)
			{
				throw new RuntimeException(e);
			}
		}
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
							}
						}
				);
		baseRoutes.forEach((aClass, aName) -> {
			DefinedRoute<?> dr = getDefinedRoute(aClass);
			buildRoutePathway(dr, aClass, true);
			definedRoutesList.add(dr);
			routes.add(dr);
		});
		for (DefinedRoute<?> definedRoute : definedRoutesList)
		{
			if (definedRoute.getComponent() != null)
			{
				List<NgImportReference> refs = new ArrayList<>();
				buildRoutePathwayImports(definedRoute, refs);
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
	
	private List<NgImportReference> buildRoutePathwayImports(DefinedRoute<?> definedRoute, List<NgImportReference> out)
	{
		for (DefinedRoute<?> child : definedRoute.getChildren())
		{
			if (child.getComponent() != null)
			{
				out.addAll(addImportToMap(child));
				out.addAll(buildRoutePathwayImports(child, out));
			}
		}
		
		return out;
	}
	
	private void buildRoutePathway(DefinedRoute parentRoute,
	                               Class<? extends IComponent<?>> aClass, boolean first)
	{
		DefinedRoute<?> innerDr = getDefinedRoute(aClass);
		if (!first)
		{
			parentRoute.addChild(innerDr);
		}
		else
		{
			innerDr = parentRoute;
		}
		
		first = false;
		for (Class<? extends IComponent<?>> componentsWithParentClass : componentsWithParenClassAs(aClass))
		{
			buildRoutePathway(innerDr, componentsWithParentClass, first);
		}
	}
	
	List<Class<? extends IComponent<?>>> componentsWithParenClassAs(Class<? extends IComponent<?>> clazz)
	{
		List<Class<? extends IComponent<?>>> out = new ArrayList<>();
		
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgRoutable.class))
		{
			if (classInfo.isAbstract() || classInfo.isInterface())
			{
				continue;
			}
			NgRoutable routable = classInfo.loadClass()
			                               .getAnnotation(NgRoutable.class);
			for (Class<? extends IComponent<?>> aClass : routable.parent())
			{
				if (aClass.equals(clazz))
				{
					out.add((Class<? extends IComponent<?>>) classInfo.loadClass());
				}
			}
		}
		return out;
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
