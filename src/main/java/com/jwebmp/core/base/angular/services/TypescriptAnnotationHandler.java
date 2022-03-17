package com.jwebmp.core.base.angular.services;

import com.guicedee.guicedinjection.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.services.annotations.references.NgImportReferences;
import io.github.classgraph.*;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import static com.jwebmp.core.base.angular.services.interfaces.ITSComponent.*;

public class TypescriptAnnotationHandler
{
	private static final Class<? extends Annotation>[] groupImportableAnnotations = new Class[]{
			//NgModuleReferences.class,
			NgImportReferences.class,
			NgProviderReferences.class,
			NgDataTypeReferences.class,
			NgServiceReferences.class
	};
	private static final Class<? extends Annotation>[] importableAnnotations = new Class[]{
			//	NgModuleReference.class,
			NgImportReference.class,
			NgProviderReference.class,
			NgDataTypeReference.class,
			NgServiceReference.class
	};
	
	public Map<String, String> renderImports(Class<?> anyClass)
	{
		Map<String, String> out = new HashMap<>();
		scanClassForImportAnnotations(anyClass, out);
		return out;
	}
	
	private void scanClassForImportAnnotations(Class<?> anyClass, Map<String, String> out)
	{
		for (Class<? extends Annotation> importableAnnotation : groupImportableAnnotations)
		{
			out.putAll(checkForImportAnnotations(anyClass, importableAnnotation));
		}
		
		for (Class<? extends Annotation> importableAnnotation : importableAnnotations)
		{
			out.putAll(checkForImportAnnotation(anyClass, importableAnnotation));
		}
	}
	
	public Map<String, String> checkForImportAnnotation(Class<?> clazz, Class<? extends Annotation> annotation)
	{
		Map<String, String> out = new HashMap<>();
		if (isAnnotationPresent(clazz, annotation))
		{
			Annotation refAnnotation = getAnnotation(clazz, annotation);
			try
			{
				Method valueMethod = refAnnotation.getClass()
				                                  .getDeclaredMethod("value");
				Class result = (Class) valueMethod.invoke(refAnnotation);
				
				
				//extract import registration
				
				//check the found class for any nested references
				scanClassForImportAnnotations(result, out);
			}
			catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		return out;
	}
	
	public Map<String, String> checkForImportAnnotations(Class<?> clazz, Class<? extends Annotation> annotation)
	{
		Map<String, String> out = new HashMap<>();
		if (isAnnotationPresent(clazz, annotation))
		{
			Annotation refAnnotation = getAnnotation(clazz, annotation);
			try
			{
				Method valueMethod = refAnnotation.getClass()
				                                  .getDeclaredMethod("value");
				Annotation[] result = (Annotation[]) valueMethod.invoke(refAnnotation);
				for (Annotation annotationIndex : result)
				{
					try
					{
						Method loopedValueMethod = annotationIndex.getClass()
						                                          .getDeclaredMethod("value");
						Class loopedValueResult = (Class) loopedValueMethod.invoke(annotationIndex);
						//check the found class for any nested references
						scanClassForImportAnnotations(loopedValueResult, out);
					}
					catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
					{
						e.printStackTrace();
					}
				}
			}
			catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		
		return out;
	}
	
	private String getRelativeReference(Class<?> requestingClass, Class<?> destinationClass)
	{
		return "";
	}
	
	private String getRelativeReference(File requestingClass, File destinationClass)
	{
		return getRelativePath(requestingClass.toPath(), destinationClass.toPath(), null);
	}
	
	private String cleanImportReference(String key, String value)
	{
		String cleanedValue = "";
		if (value.startsWith("!"))
		{
			cleanedValue = value.substring(1);
		}
		else
		{
			cleanedValue = value;
		}
		return cleanedValue;
	}
	
	
	public static <T> Set<Class<T>> findClassesImplementing(Class<T> clazz)
	{
		Set<Class<T>> classes = new HashSet<>();
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesImplementing(clazz))
		{
			classes.add((Class<T>) classInfo.loadClass());
		}
		return classes;
	}
	
	public static <T extends Annotation> List<T> getListOfAnnotations(Class<?> clazz, Class<T> singularAnnotation, Class<? extends Annotation> multipleAnnotation)
	{
		List<T> out = new ArrayList<>();
		if (isAnnotationPresent(clazz, multipleAnnotation))
		{
			Annotation refAnnotation = getAnnotation(clazz, multipleAnnotation);
			try
			{
				Method valueMethod = refAnnotation.getClass()
				                                  .getDeclaredMethod("value");
				Annotation[] result = (Annotation[]) valueMethod.invoke(refAnnotation);
				for (Annotation annotation : result)
				{
					out.add((T) annotation);
				}
			}
			catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		if(isAnnotationPresent(clazz,singularAnnotation))
		{
			out.add(getAnnotation(clazz, singularAnnotation));
		}
		return out;
	}
}
