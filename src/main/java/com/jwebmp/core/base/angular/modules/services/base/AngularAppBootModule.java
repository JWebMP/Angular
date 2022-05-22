package com.jwebmp.core.base.angular.modules.services.base;

import com.google.common.base.*;
import com.guicedee.guicedinjection.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.angularconfig.*;
import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.base.angular.services.annotations.structures.*;
import com.jwebmp.core.base.angular.services.compiler.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.html.*;
import com.jwebmp.core.databind.*;
import io.github.classgraph.*;

import java.util.List;
import java.util.Map;
import java.util.*;

import static com.jwebmp.core.base.angular.services.compiler.AnnotationsMap.*;

@TsDependency(value = "@angular/platform-browser", version = "^13.3.4", overrides = true)
@NgImportReference(name = "BrowserModule", reference = "@angular/platform-browser")
@NgBootModuleImport("BrowserModule")

@TsDependency(value = "@angular/forms", version = "^13.3.4", overrides = true)
@NgImportReference(name = "FormsModule, ReactiveFormsModule", reference = "@angular/forms")
@NgBootModuleImport("FormsModule")
@NgBootModuleImport("ReactiveFormsModule")

@TsDependency(value = "@angular/common", version = "^13.3.4", overrides = true)
@NgImportReference(name = "CommonModule", reference = "@angular/common")
@NgBootModuleImport("CommonModule")

//@NgImportReference(name = "HttpClient, HttpResponse, HttpHeaders,HttpParams,HttpErrorResponse", reference = "@angular/common/http")

@NgPolyfill("zone.js")

@TsDependency(value = "@angular/platform-browser-dynamic", version = "^13.3.4")
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
		                                       .getClassesWithAnnotation(NgBootModuleImports.class))
		{
			NgBootModuleImports reference = classInfo.loadClass()
			                                         .getAnnotation(NgBootModuleImports.class);
			for (NgBootModuleImport ngBootModuleImport : reference.value())
			{
				out.add(ngBootModuleImport.value());
			}
		}
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgBootModuleImport.class))
		{
			NgBootModuleImport reference = classInfo.loadClass()
			                                        .getAnnotation(NgBootModuleImport.class);
			if (reference == null)
			{
				continue;
			}
			out.add(reference.value());
		}
		
		return out.stream()
		          .distinct()
		          .toList();
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
	public StringBuilder renderImports()
	{
		StringBuilder sb = new StringBuilder();
		Map<String, String> out = renderImportsMap();
		processReferenceAnnotations(out);
		for (Map.Entry<String, String> entry : out.entrySet())
		{
			String key = entry.getKey();
			if (key.equals("AngularAppBootModule"))
			{
				continue;
			}
			String value = entry.getValue();
			if (!key.startsWith("!"))
			{
				sb.append(String.format(importString, key, value));
			}
			else
			{
				sb.append(String.format(importPlainString, key.substring(1), value));
			}
		}
		return sb;
	}
	
	public Map<String, String> renderImportsMap()
	{
		ScanResult scan = GuiceContext.instance()
		                              .getScanResult();
		
		
		Map<String, String> out = new LinkedHashMap<>();
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
		
		List<NgImportReference> importRefs = getAnnotations(getClass(), NgImportReference.class);
		for (NgImportReference moduleRef : importRefs)
		{
			//these are fixed locations
			if (moduleRef.onSelf())
			{
				out.putIfAbsent(moduleRef.name(), moduleRef.reference());
			}
		}
		
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgBootImportReference.class))
		{
			List<NgBootImportReference> refs = AnnotationsMap.getAnnotations(classInfo.loadClass(), NgBootImportReference.class);
			for (NgBootImportReference ref : refs)
			{
				if (ref.overrides())
				{
					out.put(ref.name(), ref.reference());
				}
				else
				{
					out.putIfAbsent(ref.name(), ref.reference());
				}
			}
		}
		
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgComponent.class))
		{
			Set<Class<? extends ITSComponent<?>>> classes = new HashSet<>();
			var a = classInfo;
			if (a.isInterface() || a.isAbstract())
			{
				for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
				{
					if (!subclass.isAbstract() && !subclass.isInterface())
					{
						classes.add((Class<? extends ITSComponent<?>>) subclass.loadClass());
					}
				}
			}
			else
			{
				Class<?> aClass = a.loadClass();
				classes.add((Class<? extends ITSComponent<?>>) aClass);
			}
			for (Class<? extends ITSComponent<?>> aClass : classes)
			{
				INgComponent<?> component = (INgComponent<?>) GuiceContext.get(aClass);
				var annos = getAnnotations(aClass, NgComponent.class);
				for (NgComponent anno : annos)
				{
					NgComponentReference componentReference = getNgComponentReference(aClass);
					putRelativeLinkInMap(getClass(), out, componentReference);
					for (Map.Entry<String, String> entry : component.imports()
					                                                .entrySet())
					{
						String key = entry.getKey();
						String value = entry.getValue();
						out.putIfAbsent(key, value);
					}
				}
			}
		}
		
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgModule.class))
		{
			Set<Class<? extends ITSComponent<?>>> classes = new HashSet<>();
			var a = classInfo;
			if (a.isInterface() || a.isAbstract())
			{
				for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
				{
					if (!subclass.isAbstract() && !subclass.isInterface())
					{
						classes.add((Class<? extends ITSComponent<?>>) subclass.loadClass());
					}
				}
			}
			else
			{
				Class<?> aClass = a.loadClass();
				classes.add((Class<? extends ITSComponent<?>>) aClass);
			}
			for (Class<? extends ITSComponent<?>> aClass : classes)
			{
				if (aClass.equals(getClass()))
				{
					continue;
				}
				INgModule<?> component = (INgModule<?>) GuiceContext.get(aClass);
				var annos = getAnnotations(aClass, NgModule.class);
				for (NgModule anno : annos)
				{
					if (anno != null)
					{
						NgComponentReference componentReference = getNgComponentReference(aClass);
						putRelativeLinkInMap(getClass(), out, componentReference);
						for (Map.Entry<String, String> entry : component.imports()
						                                                .entrySet())
						{
							String key = entry.getKey();
							String value = entry.getValue();
							out.putIfAbsent(key, value);
						}
					}
				}
			}
		}
		
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgDirective.class))
		{
			Set<Class<? extends ITSComponent<?>>> classes = new HashSet<>();
			var a = classInfo;
			if (a.isInterface() || a.isAbstract())
			{
				for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
				{
					if (!subclass.isAbstract() && !subclass.isInterface())
					{
						classes.add((Class<? extends ITSComponent<?>>) subclass.loadClass());
					}
				}
			}
			else
			{
				Class<?> aClass = a.loadClass();
				classes.add((Class<? extends ITSComponent<?>>) aClass);
			}
			for (Class<? extends ITSComponent<?>> aClass : classes)
			{
				INgDirective<?> component = (INgDirective<?>) GuiceContext.get(aClass);
				var annos = getAnnotations(aClass, NgDirective.class);
				for (NgDirective anno : annos)
				{
					if (anno != null)
					{
						NgComponentReference componentReference = getNgComponentReference(aClass);
						putRelativeLinkInMap(getClass(), out, componentReference);
						for (Map.Entry<String, String> entry : component.imports()
						                                                .entrySet())
						{
							String key = entry.getKey();
							String value = entry.getValue();
							out.putIfAbsent(key, value);
						}
					}
				}
			}
		}
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgProvider.class))
		{
			Set<Class<? extends ITSComponent<?>>> classes = new HashSet<>();
			var a = classInfo;
			if (a.isInterface() || a.isAbstract())
			{
				for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
				{
					if (!subclass.isAbstract() && !subclass.isInterface())
					{
						classes.add((Class<? extends ITSComponent<?>>) subclass.loadClass());
					}
				}
			}
			else
			{
				Class<?> aClass = a.loadClass();
				classes.add((Class<? extends ITSComponent<?>>) aClass);
			}
			for (Class<? extends ITSComponent<?>> aClass : classes)
			{
				INgProvider<?> component = (INgProvider<?>) GuiceContext.get(aClass);
				var annos = getAnnotations(aClass, NgProvider.class);
				for (NgProvider anno : annos)
				{
					NgComponentReference componentReference = getNgComponentReference(aClass);
					putRelativeLinkInMap(getClass(), out, componentReference);
					for (Map.Entry<String, String> entry : component.imports()
					                                                .entrySet())
					{
						String key = entry.getKey();
						String value = entry.getValue();
						out.putIfAbsent(key, value);
					}
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
		                                       .getClassesWithAnnotation(NgBootProvider.class))
		{
			if (classInfo.isInterface() || classInfo.isAbstract())
			{
				continue;
			}
			Class<? extends INgProvider<?>> aClass = (Class<? extends INgProvider<?>>) classInfo.loadClass();
			INgProvider<?> component = GuiceContext.get(aClass);
			var annos = getAnnotations(aClass, NgBootProvider.class);
			for (NgBootProvider anno : annos)
			{
				if (anno != null)
				{
					if (anno.onSelf())
					{
						out.add(anno.value());
					}
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
	
	@Override
	public StringBuilder renderConstructor()
	{
		StringBuilder out = new StringBuilder();
		Set<String> constructorParameters = new LinkedHashSet<>();
		Set<Class<? extends ITSComponent<?>>> aClasses = new HashSet<>();
		aClasses.add((Class<? extends ITSComponent<?>>) getClass());
		
		List<NgComponentReference> componentReferences = getAnnotations(getClass(), NgComponentReference.class);
		for (NgComponentReference componentReference : componentReferences)
		{
			Class<? extends ITSComponent> aClass = componentReference.value();
			List<NgConstructorParameter> constructorParams = getAnnotations(aClass, NgConstructorParameter.class);
			for (NgConstructorParameter constructorParam : constructorParams)
			{
				if (constructorParam.onParent())
				{
					constructorParameters.add(constructorParam.value());
				}
			}
		}
		
		List<NgConstructorParameter> constructorParams = getAnnotations(getClass(), NgConstructorParameter.class);
		for (NgConstructorParameter constructorParam : constructorParams)
		{
			if (constructorParam.onSelf())
			{
				constructorParameters.add(constructorParam.value());
			}
		}
		
		List<NgBootConstructorParameter> constructorBootParams = getAllAnnotations(NgBootConstructorParameter.class);
		for (NgBootConstructorParameter constructorBootParam : constructorBootParams)
		{
			if (constructorBootParam.onSelf())
			{
				constructorParameters.add(constructorBootParam.value());
			}
		}
		
		for (String constructorParameter : componentConstructorParameters())
		{
			constructorParameters.add(constructorParameter);
		}
		for (String constructorParameter : constructorParameters())
		{
			constructorParameters.add(constructorParameter);
		}
		
		StringBuilder constructorParametersString = new StringBuilder();
		if (!constructorParameters.isEmpty())
		{
			for (String constructorParameter : constructorParameters)
			{
				constructorParametersString.append(constructorParameter + ", ");
			}
			if (constructorParametersString.length() > 1)
			{
				constructorParametersString.deleteCharAt(constructorParametersString.lastIndexOf(", "));
			}
		}
		
		StringBuilder cBodyBuilder = new StringBuilder();
		Set<String> constructorBodies = new LinkedHashSet<>();
		List<NgConstructorBody> bDy = getAnnotations(getClass(), NgConstructorBody.class);
		for (NgConstructorBody ngConstructorBody : bDy)
		{
			if (ngConstructorBody.onSelf())
			{
				constructorBodies.add(ngConstructorBody.value());
			}
		}
		
		List<NgBootConstructorBody> bootDy = getAllAnnotations(NgBootConstructorBody.class);
		bootDy.sort(Comparator.comparingInt(NgBootConstructorBody::sortOrder));
		for (NgBootConstructorBody ngConstructorBody : bootDy)
		{
			if (ngConstructorBody.onSelf())
			{
				constructorBodies.add(ngConstructorBody.value());
			}
		}
		
		constructorBodies.addAll(componentConstructorBody());
		constructorBodies.addAll(constructorBody());
		
		for (String body : constructorBodies)
		{
			cBodyBuilder.append(body)
			            .append("\n");
		}
		
		if (!Strings.isNullOrEmpty(constructorParametersString.toString()) || !constructorBodies.isEmpty())
		{
			out.append("constructor( ");
			out.append(constructorParametersString);
			out.append(")\n");
			
			out.append("{\n");
			out.append(cBodyBuilder);
			out.append("}\n");
		}
		
		return out;
	}
	
	@Override
	public List<String> globalFields()
	{
		List<String> gf = new ArrayList<>();
		List<NgBootGlobalField> bootDy = getAllAnnotations(NgBootGlobalField.class);
		for (NgBootGlobalField globalFields : bootDy)
		{
			if (globalFields.onSelf())
			{
				gf.add(globalFields.value());
			}
		}
		gf.addAll(INgModule.super.globalFields());
		return gf;
	}
}
