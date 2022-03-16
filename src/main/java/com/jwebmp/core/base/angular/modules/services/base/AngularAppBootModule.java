package com.jwebmp.core.base.angular.modules.services.base;

import com.guicedee.guicedinjection.*;
import com.jwebmp.core.base.angular.modules.services.*;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.services.annotations.NgModule;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.angularconfig.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.html.*;
import com.jwebmp.core.databind.*;
import io.github.classgraph.*;

import java.util.List;
import java.util.Map;
import java.util.*;

import static com.jwebmp.core.base.angular.services.annotations.NgSourceDirectoryReference.SourceDirectories.*;

/**
 * Internal use only - for mapping the annotation boot class into an imports clause for the base application
 */
@NgSourceDirectoryReference(value = App, inPackage = false)
@NgModule
@NgModuleReference(PlatformBrowserModule.class)
@NgModuleReference(com.jwebmp.core.base.angular.modules.services.angular.NgModule.class)
@NgModuleReference(RoutingModule.class)
@NgProviderReference(SocketClientService.class)
@NgModuleReference(HttpClientModule.class)
@NgPolyfill("zone.js")
public class AngularAppBootModule extends DivSimple<AngularAppBootModule> implements INgModule<AngularAppBootModule>
{
	private Class<? extends INgComponent<?>> bootModule;
	private INgApp<?> app;
	
	public AngularAppBootModule()
	{
	}
	
	@Override
	public AngularAppBootModule setApp(INgApp<?> app)
	{
		this.app = app;
		return this;
	}
	
	public Class<?> getBootModule()
	{
		return bootModule;
	}
	
	public AngularAppBootModule setBootModule(Class<? extends INgComponent<?>> bootModule)
	{
		this.bootModule = bootModule;
		return this;
	}
	
	@Override
	public List<String> bootstrap()
	{
		var ngApp = app.getAnnotation();
		Class<? extends INgComponent<?>> aClass = ngApp.bootComponent();
		return List.of(ITSComponent.getTsFilename(aClass));
	}
	
	@Override
	public List<String> moduleImports()
	{
		List<String> out = new ArrayList<>();
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgBootModuleImportReference.class))
		{
			NgBootModuleImportReference reference = classInfo.loadClass()
			                                                 .getAnnotation(NgBootModuleImportReference.class);
			out.add(reference.name());
		}
		return out;
	}
	
	@Override
	public List<String> fields()
	{
		return List.of("");
	}
	
	@Override
	public List<String> constructorParameters()
	{
		return List.of("");
	}
	
	
	@Override
	public Map<String, String> renderImports()
	{
		Map<String, String> out = new java.util.HashMap<>(Map.of(getClass().getSimpleName(), "./" + getClass().getPackageName()
		                                                                                                      .replaceAll("\\.", "\\/")
		                                                                                     + "/" + getClass().getSimpleName()));
		
		for (IConfiguration configuration : getConfigurations(INgModule.class))
		{
			INgModule<?> module = (INgModule<?>) configuration;
			for (Map.Entry<String, String> entry : module.imports()
			                                             .entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				out.putIfAbsent(key, value);
			}
		}
		
		processReferenceAnnotations(out);
		
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgComponent.class))
		{
			if (classInfo.isInterface() || classInfo.isAbstract())
			{
				continue;
			}
			Class<? extends INgComponent<?>> aClass = (Class<? extends INgComponent<?>>) classInfo.loadClass();
			INgComponent<?> component = GuiceContext.get(aClass);
			NgComponent anno = aClass.getAnnotation(NgComponent.class);
			for (Map.Entry<String, String> entry : component.imports()
			                                                .entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				out.putIfAbsent(key, value);
			}
		}
		
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgModule.class))
		{
			if (classInfo.isInterface() || classInfo.isAbstract())
			{
				continue;
			}
			Class<? extends INgModule<?>> aClass = (Class<? extends INgModule<?>>) classInfo.loadClass();
			INgModule<?> component = GuiceContext.get(aClass);
			NgModule anno = aClass.getAnnotation(NgModule.class);
			if (anno != null)
			{
				component.imports()
				         .forEach((key, value) -> {
					         out.put(key, value);
				         });
			}
		}
		
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgDirective.class))
		{
			if (classInfo.isInterface() || classInfo.isAbstract())
			{
				continue;
			}
			Class<? extends INgDirective<?>> aClass = (Class<? extends INgDirective<?>>) classInfo.loadClass();
			INgDirective<?> component = GuiceContext.get(aClass);
			NgDirective anno = aClass.getAnnotation(NgDirective.class);
			if (anno != null)
			{
				component.imports()
				         .forEach((key, value) -> {
					         out.put(key, value);
				         });
			}
		}
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgProvider.class))
		{
			if (classInfo.isInterface() || classInfo.isAbstract())
			{
				continue;
			}
			Class<? extends INgProvider<?>> aClass = (Class<? extends INgProvider<?>>) classInfo.loadClass();
			INgProvider<?> component = GuiceContext.get(aClass);
			NgProvider anno = aClass.getAnnotation(NgProvider.class);
			if (anno != null)
			{
				component.imports()
				         .forEach((key, value) -> {
					         out.put(key, value);
				         });
			}
		}
		
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgBootModuleImportReference.class))
		{
			/*if (classInfo.isInterface() || classInfo.isAbstract())
			{
				continue;
			}*/
			Class<?> aClass = classInfo.loadClass();
			if (ITSComponent.isAnnotationPresent(aClass, NgBootModuleImportReference.class))
			{
				NgBootModuleImportReference ref = ITSComponent.getAnnotation(aClass, NgBootModuleImportReference.class);
				out.putIfAbsent(ref.name(), ref.reference());
			}
		}
		
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgBootModuleImportReferences.class))
		{
			/*if (classInfo.isInterface() || classInfo.isAbstract())
			{
				continue;
			}*/
			Class<?> aClass = classInfo.loadClass();
			if (ITSComponent.isAnnotationPresent(aClass, NgBootModuleImportReferences.class))
			{
				NgBootModuleImportReferences refs = ITSComponent.getAnnotation(aClass, NgBootModuleImportReferences.class);
				for (NgBootModuleImportReference ref : refs.value())
				{
					out.putIfAbsent(ref.name(), ref.reference());
				}
			}
		}
		
		
		return out;
	}
	
	@Override
	public List<String> providers()
	{
		List<String> out = new ArrayList<>();
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgProvider.class))
		{
			if (classInfo.isInterface() || classInfo.isAbstract())
			{
				continue;
			}
			Class<? extends INgProvider<?>> aClass = (Class<? extends INgProvider<?>>) classInfo.loadClass();
			INgProvider<?> component = GuiceContext.get(aClass);
			NgProvider anno = aClass.getAnnotation(NgProvider.class);
			if (anno != null)
			{
				if (anno.singleton())
				{
					for (String key : component.providers())
					{
						out.add(key);
					}
				}
			}
		}
		return out;
	}
	
	@Override
	public List<String> declarations()
	{
		Set<String> out = new HashSet<>();
		out.add(bootModule.getSimpleName());
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgComponent.class))
		{
			if (classInfo.isInterface() || classInfo.isAbstract())
			{
				continue;
			}
			Class<? extends INgComponent<?>> aClass = (Class<? extends INgComponent<?>>) classInfo.loadClass();
			INgComponent<?> component = GuiceContext.get(aClass);
			NgComponent anno = aClass.getAnnotation(NgComponent.class);
			component.imports()
			         .forEach((key, value) -> {
				         out.add(key);
			         });
		}
		
		
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgDirective.class))
		{
			if (classInfo.isInterface() || classInfo.isAbstract())
			{
				continue;
			}
			Class<? extends INgDirective<?>> aClass = (Class<? extends INgDirective<?>>) classInfo.loadClass();
			INgDirective<?> component = GuiceContext.get(aClass);
			NgDirective anno = aClass.getAnnotation(NgDirective.class);
			if (anno != null)
			{
				component.declarations()
				         .forEach((key) -> {
					         if (anno.includeADeclaration())
					         {
						         out.add(key);
					         }
				         });
			}
		}
		
		return new ArrayList<>(out);
	}
}
