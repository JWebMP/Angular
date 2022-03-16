package com.jwebmp.core.base.angular.services.interfaces;

import com.google.common.base.*;
import com.guicedee.guicedinjection.*;
import com.jwebmp.core.base.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.angularconfig.*;

import java.io.*;
import java.lang.annotation.*;
import java.nio.file.*;
import java.util.*;

import static com.jwebmp.core.base.angular.services.TypescriptAnnotationHandler.*;
import static com.jwebmp.core.base.angular.services.annotations.NgSourceDirectoryReference.SourceDirectories.*;

public interface ITSComponent<J extends ITSComponent<J>> extends IComponent<J>
{
	String importString = "import %s from '%s';\n";
	String componentString = "@Component({\n" +
	                         "\tselector:'%s',\n" +
	                         "\ttemplateUrl:'%s',\n" +
	                         "\tstyles: [%s],\n" +
	                         "\tstyleUrls:[%s],\n" +
	                         "\tviewProviders:[%s],\n" +
	                         "\tanimations:[%s],\n" +
	                         "\tproviders:[%s],\n" +
	                         "\tpreserveWhitespaces:true\n" +
	                         "" +
	                         "})";
	
	String moduleString = "@NgModule({\n" +
	                      "\timports:[%s],\n" +
	                      "\tdeclarations:[%s],\n" +
	                      "\tproviders: [%s],\n" +
	                      "\texports:[%s],\n" +
	                      "\tbootstrap:[%s],\n" +
	                      "\tschemas:[%s]\n" +
	                      "" +
	                      "})";
	
	String directiveString = "@Directive({\n" +
	                         "\tselector:'%s',\n" +
	                         "\tproviders:[%s]\n" +
	                         "})";
	
	default String renderBeforeClass()
	{
		return "";
	}
	
	default String renderAfterClass()
	{
		return "";
	}
	
	default Map<String, String> imports()
	{
		return imports(new File[]{});
	}
	
	
	default Map<String, String> importSelf(File... srcRelative)
	{
		Map<String, String> out = new java.util.HashMap<>(
				Map.of(getTsFilename(getClass()),
						getClassLocationDirectory(getClass()) + getTsFilename(getClass()))
		);
		return out;
	}
	
	default Map<String, String> imports(File... srcRelative)
	{
		Map<String, String> out = new java.util.HashMap<>(importSelf());
		return out;
	}
	
	default Map<String, String> renderImports()
	{
		return renderImports(new File[]{});
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	default Map<String, String> renderImports(File... srcRelative)
	{
		Map<String, String> out = new java.util.HashMap<>();
		if (this instanceof ComponentHierarchyBase)
		{
			ComponentHierarchyBase chb = (ComponentHierarchyBase) this;
			Set<INgModule<?>> modules = chb.getConfigurations(INgModule.class);
			for (INgModule<?> module : modules)
			{
				for (Map.Entry<String, String> entry : module.imports()
				                                             .entrySet())
				{
					String key = entry.getKey();
					String value = entry.getValue();
					out.putIfAbsent(key, value);
				}
			}
			
			Set<INgComponent<?>> components = chb.getConfigurations(INgComponent.class);
			for (INgComponent<?> module : components)
			{
				for (Map.Entry<String, String> entry : module.imports()
				                                             .entrySet())
				{
					String key = entry.getKey();
					String value = entry.getValue();
					out.putIfAbsent(key, value);
				}
			}
			
			Set<INgDirective<?>> directives = chb.getConfigurations(INgDirective.class);
			for (INgDirective<?> module : directives)
			{
				for (Map.Entry<String, String> entry : module.imports()
				                                             .entrySet())
				{
					String key = entry.getKey();
					String value = entry.getValue();
					out.putIfAbsent(key, value);
				}
			}
			
			Set<INgProvider<?>> providers = chb.getConfigurations(INgProvider.class);
			for (INgProvider<?> module : providers)
			{
				for (Map.Entry<String, String> entry : module.imports()
				                                             .entrySet())
				{
					String key = entry.getKey();
					String value = entry.getValue();
					out.putIfAbsent(key, value);
				}
			}
		}
		processReferenceAnnotations(out);
		
		getImportsFromAnnotations().forEach((key, value) -> {
			out.putIfAbsent(key, value);
		});
		
		return out;
	}
	
	static <A extends Annotation> A getAnnotation(Class<?> classType, final Class<A> annotationClass)
	{
		final Class classy = classType;
		while (!classType.getName()
		                 .equals(Object.class.getName()))
		{
			if (classType.isAnnotationPresent(annotationClass))
			{
				return classType.getAnnotation(annotationClass);
			}
			Class classyInterface = classType;
			for (Class<?> anInterface : classyInterface.getInterfaces())
			{
				classyInterface = anInterface;
				while (classyInterface != null && !classyInterface.getName()
				                                      .equals(Object.class.getName()))
				{
					if (classyInterface.isAnnotationPresent(annotationClass))
					{
						return (A) classyInterface.getAnnotation(annotationClass);
					}
					classyInterface = anInterface.getSuperclass();
				}
			}
			classType = classType.getSuperclass();
		}
		Class classyInterface = classy;
		for (Class<?> anInterface : classyInterface.getInterfaces())
		{
			classyInterface = anInterface;
			while (classyInterface != null && !classyInterface.getName()
			                                                  .equals(Object.class.getName()))
			{
				if (classyInterface.isAnnotationPresent(annotationClass))
				{
					return (A)classyInterface.getAnnotation(annotationClass);
				}
				classyInterface = anInterface.getSuperclass();
			}
		}
		return null;
	}
	
	static <A extends Annotation> boolean isAnnotationPresent(Class<?> classType, final Class<A> annotationClass)
	{
		final Class classy = classType;
		while (!classType.getName()
		                 .equals(Object.class.getName()))
		{
			if (classType.isAnnotationPresent(annotationClass))
			{
				return true;
			}
			Class classyInterface = classType;
			for (Class<?> anInterface : classyInterface.getInterfaces())
			{
				classyInterface = anInterface;
				while (classyInterface != null && !classyInterface.getName()
				                                                  .equals(Object.class.getName()))
				{
					if (classyInterface.isAnnotationPresent(annotationClass))
					{
						return true;
					}
					classyInterface = anInterface.getSuperclass();
				}
			}
			classType = classType.getSuperclass();
		}
		Class classyInterface = classy;
		for (Class<?> anInterface : classyInterface.getInterfaces())
		{
			classyInterface = anInterface;
			while (classyInterface != null && !classyInterface.getName()
			                                                  .equals(Object.class.getName()))
			{
				if (classyInterface.isAnnotationPresent(annotationClass))
				{
					return true;
				}
				classyInterface = anInterface.getSuperclass();
			}
		}
		return false;
	}
	
	default void processReferenceAnnotations(Map<String, String> out)
	{
		if (isAnnotationPresent(getClass(), NgModuleReferences.class))
		{
			NgModuleReferences references = getAnnotation(getClass(), NgModuleReferences.class);
			for (NgModuleReference ngModuleReference : references.value())
			{
				INgModule<?> module = GuiceContext.get(ngModuleReference.value());
				module.imports()
				      .forEach((key, value) -> {
					      out.putIfAbsent(key, value);
				      });
			}
		}
		NgModuleReference ngModuleReference = getAnnotation(getClass(), NgModuleReference.class);
		if (ngModuleReference != null)
		{
			INgModule<?> module = GuiceContext.get(ngModuleReference.value());
			module.imports()
			      .forEach((key, value) -> {
				      out.putIfAbsent(key, value);
			      });
		}
		
		if (isAnnotationPresent(getClass(), NgDataTypeReferences.class))
		{
			NgDataTypeReferences references = getAnnotation(getClass(), NgDataTypeReferences.class);
			for (NgDataTypeReference ngDataTypeReference : references.value())
			{
				INgDataType<?> module = GuiceContext.get(ngDataTypeReference.value());
				module.imports()
				      .forEach((key, value) -> {
					      out.putIfAbsent(key, value);
				      });
			}
		}
		NgDataTypeReference ngDataTypeReference = getAnnotation(getClass(), NgDataTypeReference.class);
		if (ngDataTypeReference != null)
		{
			INgDataType<?> module = GuiceContext.get(ngDataTypeReference.value());
			module.imports()
			      .forEach((key, value) -> {
				      out.putIfAbsent(key, value);
			      });
		}
		if (isAnnotationPresent(getClass(), NgProviderReferences.class))
		{
			NgProviderReferences references = getAnnotation(getClass(), NgProviderReferences.class);
			for (NgProviderReference ngProviderReference : references.value())
			{
				INgProvider<?> module = GuiceContext.get(ngProviderReference.value());
				module.imports()
				      .forEach((key, value) -> {
					      out.putIfAbsent(key, value);
				      });
			}
		}
		NgProviderReference ngProviderReference = getAnnotation(getClass(), NgProviderReference.class);
		if (ngProviderReference != null)
		{
			INgProvider<?> module = GuiceContext.get(ngProviderReference.value());
			module.imports()
			      .forEach((key, value) -> {
				      out.putIfAbsent(key, value);
			      });
		}
		
		List<NgServiceReference> serviceRefs = getListOfAnnotations(getClass(), NgServiceReference.class, NgServiceReferences.class);
		for (NgServiceReference moduleRef : serviceRefs)
		{
			INgDataService<?> module = GuiceContext.get(moduleRef.value());
			module.imports()
			      .forEach((key, value) -> {
				      out.putIfAbsent(key, value);
			      });
		}
		
		List<NgModuleImportReference> moduleRefs = getListOfAnnotations(getClass(), NgModuleImportReference.class, NgModuleImportReferences.class);
		for (NgModuleImportReference moduleRef : moduleRefs)
		{
			out.putIfAbsent(moduleRef.name(), moduleRef.reference());
		}
		
		getImportsFromAnnotations().forEach((key, value) -> {
			out.putIfAbsent(key, value);
		});
	}
	
	default Map<String, String> getImportsFromAnnotations()
	{
		Map<String, String> out = new HashMap<>();
		if (this instanceof INgComponent)
		{
			INgComponent<?> ng = (INgComponent) this;
			for (Map.Entry<String, String> entry : ng.imports()
			                                         .entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				out.putIfAbsent(key, value);
			}
			
		}
		else if (this instanceof INgModule)
		{
			INgModule<?> ng = (INgModule) this;
			for (Map.Entry<String, String> entry : ng.imports()
			                                         .entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				out.putIfAbsent(key, value);
			}
		}
		else if (this instanceof INgProvider)
		{
			INgProvider<?> ng = (INgProvider) this;
			for (Map.Entry<String, String> entry : ng.imports()
			                                         .entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if (key.equals(getTsFilename(getClass())))
				{
					continue;
				}
				out.putIfAbsent(key, value);
			}
		}
		else if (this instanceof INgDataType)
		{
			INgDataType<?> ng = (INgDataType) this;
			for (Map.Entry<String, String> entry : ng.imports()
			                                         .entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if (key.equals(getTsFilename(getClass())))
				{
					continue;
				}
				out.putIfAbsent(key, value);
			}
		}
		else if (this instanceof INgDirective)
		{
			INgDirective<?> ng = (INgDirective) this;
			for (Map.Entry<String, String> entry : ng.imports()
			                                         .entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if (key.equals(getTsFilename(getClass())))
				{
					continue;
				}
				out.putIfAbsent(key, value);
			}
		}
		else if (this instanceof INgDataService)
		{
			INgDataService<?> ng = (INgDataService) this;
			for (Map.Entry<String, String> entry : ng.imports()
			                                         .entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if (key.equals(getTsFilename(getClass())))
				{
					continue;
				}
				out.putIfAbsent(key, value);
			}
		}
		
		return out;
	}
	
	public static String getTsFilename(Class<?> clazz)
	{
		if (clazz.isAnnotationPresent(NgSourceDirectoryReference.class))
		{
			NgSourceDirectoryReference ref = clazz.getAnnotation(NgSourceDirectoryReference.class);
			if (!Strings.isNullOrEmpty(ref.name()))
			{
				return ref.name();
			}
		}
		return clazz.getSimpleName();
	}
	
	
	public static String getTsFilename(INgApp<?> clazz)
	{
		NgApp app;
		if (!clazz.getClass()
		          .isAnnotationPresent(NgApp.class))
		{
			System.out.println("Ng App Interface without NgApp Annotation? - " + clazz.getClass()
			                                                                          .getCanonicalName());
			throw new RuntimeException("Unable to build application without base metadata");
		}
		return clazz.name();
	}
	
	public static String getClassDirectory(Class<?> clazz)
	{
		return clazz.getPackageName()
		            .replaceAll("\\.", "/");
	}
	
	
	public static String getClassLocationDirectory(Class<?> clazz)
	{
		return getClassDirectory(clazz) + "/" + getTsFilename(clazz) + "/";
	}
	
	public static String getRelativePath(File absolutePath1, File absolutePath2, String extension)
	{
		return getRelativePath(absolutePath1.toPath(), absolutePath2.toPath(), extension);
	}
	
	static String getRelativePath(Path absolutePath1, Path absolutePath2, String extension)
	{
		//get the directories of each to compare them
		File original = new File(absolutePath1.toString());
		File requestedForPath = new File(absolutePath2.toString());
		if (absolutePath2.toString()
		                 .contains("!"))
		{
			String result = absolutePath2.toString()
			                             .substring(absolutePath2.toString()
			                                                     .indexOf('!') + 1);
			return result.replace('\\', '/');
		}
		
		try
		{
			if (!original.isDirectory())
			{
				original = original.getParentFile();
			}
			String path = original.toPath()
			                      .relativize(requestedForPath.toPath())
			                      .toString()
			                      .replaceAll("\\\\", "/");
			if (!path.startsWith("..") && !path.startsWith("./") && !path.startsWith("/"))
			{
				path = "./" + path;
			}
			return path;
		}
		catch (Exception e)
		{
			e.getStackTrace();
		}
		try
		{
			requestedForPath = absolutePath2.toFile();
			return requestedForPath.getCanonicalPath();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	static String getFileReference(String baseDirectory, Class<?> clazz, String... extension)
	{
		String classLocationDirectory = getClassLocationDirectory(clazz);
		classLocationDirectory = classLocationDirectory.replaceAll("\\\\", "/");
		
		NgSourceDirectoryReference.SourceDirectories sourceRef = Self;
		if (clazz.isAnnotationPresent(NgSourceDirectoryReference.class))
		{
			sourceRef = clazz.getAnnotation(NgSourceDirectoryReference.class)
			                 .value();
		}
		String baseLocation = baseDirectory;
		baseLocation.replaceAll("\\\\", "/");
		baseLocation += "/src/app/";
		classLocationDirectory = baseLocation + classLocationDirectory + getTsFilename(clazz) + (extension.length > 0 ? extension[0] : "");
		
		return classLocationDirectory;
	}
	
	static String renderImportStatement(String name, String from)
	{
		if (name.startsWith("!"))
		{
			return String.format(importString, name.substring(1), from);
		}
		else
		{
			return String.format(importString, "{" + name + "}", from);
		}
	}
	
	static File getFile(String baseDirectory, Class<?> classPath, String... extension)
	{
		String baseDir = getFileReference(baseDirectory, classPath, extension);
		File file = new File(baseDir);
		return file;
	}
	
	default boolean includeInBootModule()
	{
		return false;
	}
	
}
