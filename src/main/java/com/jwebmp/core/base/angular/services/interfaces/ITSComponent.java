package com.jwebmp.core.base.angular.services.interfaces;

import com.fasterxml.jackson.core.*;
import com.google.common.base.*;
import com.jwebmp.core.base.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.functions.*;
import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.base.angular.services.annotations.structures.*;
import com.jwebmp.core.base.angular.services.compiler.*;

import java.io.*;
import java.lang.annotation.*;
import java.nio.file.*;
import java.util.*;

import static com.jwebmp.core.base.angular.services.compiler.AnnotationsMap.*;

public interface ITSComponent<J extends ITSComponent<J>> extends IComponent<J>
{
	String importString = "import { %s } from '%s';\n";
	String importPlainString = "import %s from '%s';\n";
	
	String componentString = "@Component({\n" +
	                         "\tselector:'%s',\n" +
	                         "\ttemplateUrl:'%s',\n" +
	                         "\tstyles: [%s],\n" +
	                         "\tstyleUrls:[%s],\n" +
	                         "\tviewProviders:[%s],\n" +
	                         "\tanimations:[%s],\n" +
	                         "\tproviders:[%s],\n" +
	                         "\tpreserveWhitespaces:true,\n" +
	                         "\thost:%s\n" +
	                         "" +
	                         "})";
	
	String moduleString = "@NgModule({\n" +
	                      "\timports:[%s],\n" +
	                      "\tdeclarations:[%s],\n" +
	                      "\tproviders: [%s],\n" +
	                      "\texports:[%s],\n" +
	                      "\tbootstrap:%s,\n" +
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
	
	default StringBuilder renderImports()
	{
		StringBuilder sb = new StringBuilder();
		Map<String, String> out = new LinkedHashMap<>();
		processReferenceAnnotations(out);
		out.forEach((key, value) -> {
			if (!key.startsWith("!"))
			{
				sb.append(String.format(importString, key, value));
			}
			else
			{
				sb.append(String.format(importPlainString, key.substring(1), value));
			}
		});
		return sb;
	}
	
	default void processReferenceAnnotations(Map<String, String> out)
	{
		var reference = getAnnotations(getClass(), NgSourceDirectoryReference.class);
		List<NgComponentReference> moduleRefs = getAnnotations(getClass(), NgComponentReference.class);
		moduleRefs.addAll(getComponentReferences());
		for (NgComponentReference moduleRef : moduleRefs)
		{
			putRelativeLinkInMap(getClass(), out, moduleRef);
		}
		
		List<NgDataTypeReference> dataTypeReferences = getAnnotations(getClass(), NgDataTypeReference.class);
		for (NgDataTypeReference moduleRef : dataTypeReferences)
		{
			putRelativeLinkInMap(getClass(), out, getNgComponentReference(moduleRef.value()));
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
		
		getImportsFromTypes().forEach(out::putIfAbsent);
		//get rid of myself
		out.remove(getTsFilename(getClass()));
	}
	
	default void putRelativeLinkInMap(Class<?> clazz, Map<String, String> out, NgComponentReference moduleRef)
	{
		var baseDir = JWebMPTypeScriptCompiler.getCurrentAppFile();
		try
		{
			File me = new File(getFileReference(baseDir.get()
			                                           .getCanonicalPath(), clazz));
			File destination = new File(getFileReference(baseDir.get()
			                                                    .getCanonicalPath(), moduleRef.value()));
			out.putIfAbsent(getTsFilename(moduleRef.value()), getRelativePath(me, destination, null));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	default Map<String, String> getImportsFromTypes()
	{
		Map<String, String> out = new HashMap<>();
		var imps = imports();
		if (getClass().isAnnotationPresent(NgComponent.class) && this instanceof ComponentHierarchyBase && this instanceof INgComponent)
		{
			ComponentHierarchyBase chb = (ComponentHierarchyBase) this;
			chb.toString(0);
			Set childrenHierarchy = chb.getChildrenHierarchy();
			for (Object o : childrenHierarchy)
			{
				ComponentHierarchyBase chb1 = (ComponentHierarchyBase) o;
				if (!chb1.getClass()
				         .isAnnotationPresent(NgComponent.class) && chb1 instanceof INgComponent)
				{
					INgComponent ngComp = (INgComponent) chb1;
					imps.putAll(ngComp.imports());
				}
			}
		}
		
		for (Map.Entry<String, String> entry : imps
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
		return out;
	}
	
	default List<NgComponentReference> getComponentReferences()
	{
		return List.of();
	}
	
	static String getTsVarName(Class<?> clazz)
	{
		String tsName = getTsFilename(clazz);
		tsName = tsName.substring(0, 1)
		               .toLowerCase() +
		         tsName.substring(1);
		return tsName;
	}
	
	static String getTsFilename(Class<?> clazz)
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
	
	
	static String getTsFilename(INgApp<?> clazz)
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
	
	static String getClassDirectory(Class<?> clazz)
	{
		return clazz.getPackageName()
		            .replaceAll("\\.", "/");
	}
	
	
	static String getClassLocationDirectory(Class<?> clazz)
	{
		return getClassDirectory(clazz) + "/" + getTsFilename(clazz) + "/";
	}
	
	static String getRelativePath(File absolutePath1, File absolutePath2, String extension)
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
	
	default String getFileReference(String baseDirectory, Class<?> clazz, String... extension)
	{
		String classLocationDirectory = getClassLocationDirectory(clazz);
		classLocationDirectory = classLocationDirectory.replaceAll("\\\\", "/");
		String baseLocation = baseDirectory;
		baseLocation.replaceAll("\\\\", "/");
		baseLocation += "/src/app/";
		classLocationDirectory = baseLocation + classLocationDirectory + getTsFilename(clazz) + (extension.length > 0 ? extension[0] : "");
		
		return classLocationDirectory;
	}
	
	@Deprecated
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
	
	default File getFile(String baseDirectory, Class<?> classPath, String... extension)
	{
		String baseDir = getFileReference(baseDirectory, classPath, extension);
		File file = new File(baseDir);
		return file;
	}
	
	
	default boolean exportsClass()
	{
		return true;
	}
	
	default boolean includeInBootModule()
	{
		return false;
	}
	
	/**
	 * Renders the body of .ts file, excluding import statements
	 *
	 * @return
	 * @throws JsonProcessingException
	 */
	default StringBuilder renderClassTs() throws IOException
	{
		StringBuilder out = new StringBuilder();
		out.append(renderImports());
		@SuppressWarnings("unchecked")
		J component = (J) this;
		
		if (!Strings.isNullOrEmpty(component.renderBeforeClass()))
		{
			out.append(component.renderBeforeClass());
		}
		
		for (String globalField : globalFields())
		{
			out.append(globalField)
			   .append("\n");
		}
		
		for (String decorator : componentDecorators())
		{
			out.append(decorator)
			   .append("\n");
		}
		for (String decorator : decorators())
		{
			out.append(decorator)
			   .append("\n");
		}
		
		out.append(renderClassDefinition());
		
		if (!Strings.isNullOrEmpty(component.renderAfterClass()))
		{
			out.append(";")
			   .append(component.renderAfterClass());
		}
		
		return out;
	}
	
	default StringBuilder renderClassDefinition()
	{
		StringBuilder out = new StringBuilder();
		out.append(exportsClass() ? "export " : "");
		
		List<NgDataType> cType = getAnnotations(getClass(), NgDataType.class);
		if (!cType.isEmpty())
		{
			out.append(cType.get(0)
			                .value()
			                .description())
			   .append(" ");
		}
		else
		{
			out.append("class ");
		}
		out.append(getTsFilename(getClass()));
		
		if (!Strings.isNullOrEmpty(ofType()))
		{
			out.append(" ")
			   .append(ofType());
		}
		
		out.append(renderInterfaces());
		
		out.append("\n");
		
		out.append(renderClassBody());
		
		return out;
	}
	
	default StringBuilder renderClassBody()
	{
		StringBuilder out = new StringBuilder();
		out.append("{\n");
		
		out.append(renderFields());
		out.append(renderConstructor());
		out.append(renderMethods());
		
		out.append("}\n");
		
		return out;
	}
	
	default StringBuilder renderInterfaces()
	{
		StringBuilder out = new StringBuilder();
		Set<String> ints = new HashSet<>(interfaces());
		List<NgInterface> interfacs = getAnnotations(getClass(), NgInterface.class);
		for (NgInterface interfac : interfacs)
		{
			if (interfac.onSelf())
			{
				ints.add(interfac.value());
			}
		}
		ints.addAll(componentInterfaces());
		
		if (!ints.isEmpty())
		{
			StringBuilder sbInterfaces = new StringBuilder();
			sbInterfaces.append(" implements ");
			for (String interf : ints)
			{
				sbInterfaces.append(interf)
				            .append(",");
			}
			sbInterfaces.deleteCharAt(sbInterfaces.length() - 1);
			out.append(sbInterfaces);
		}
		return out;
	}
	
	default StringBuilder renderFields()
	{
		StringBuilder out = new StringBuilder();
		Set<String> fStrings = new LinkedHashSet<>();
		List<NgField> fAnno = getAnnotations(getClass(), NgField.class);
		for (NgField ngField : fAnno)
		{
			if (ngField.onSelf())
			{
				fStrings.add(ngField.value());
			}
		}
		fStrings.addAll(componentFields());
		fStrings.addAll(fields());
		
		if (getClass().isAnnotationPresent(NgComponent.class) && this instanceof ComponentHierarchyBase && this instanceof INgComponent)
		{
			ComponentHierarchyBase chb = (ComponentHierarchyBase) this;
			for (Object o : chb.getChildrenHierarchy())
			{
				ComponentHierarchyBase chb1 = (ComponentHierarchyBase) o;
				if (!chb1.getClass()
				         .isAnnotationPresent(NgComponent.class) && chb1 instanceof INgComponent)
				{
					INgComponent ngComp = (INgComponent) chb1;
					fStrings.addAll(ngComp.fields());
				}
			}
		}
		
		for (String field : fStrings)
		{
			out.append(field)
			   .append("\n");
		}
		return out;
	}
	
	default StringBuilder renderConstructor()
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
		
		if (getClass().isAnnotationPresent(NgComponent.class) && this instanceof ComponentHierarchyBase && this instanceof INgComponent)
		{
			ComponentHierarchyBase chb = (ComponentHierarchyBase) this;
			chb.toString(0);
			Set childrenHierarchy = chb.getChildrenHierarchy();
			for (Object o : childrenHierarchy)
			{
				ComponentHierarchyBase chb1 = (ComponentHierarchyBase) o;
				if (!chb1.getClass()
				         .isAnnotationPresent(NgComponent.class) && chb1 instanceof INgComponent)
				{
					INgComponent ngComp = (INgComponent) chb1;
					constructorParameters.addAll(ngComp.constructorParameters());
				}
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
		
		if (getClass().isAnnotationPresent(NgComponent.class) && this instanceof ComponentHierarchyBase && this instanceof INgComponent)
		{
			ComponentHierarchyBase chb = (ComponentHierarchyBase) this;
			chb.toString(0);
			Set childrenHierarchy = chb.getChildrenHierarchy();
			for (Object o : childrenHierarchy)
			{
				ComponentHierarchyBase chb1 = (ComponentHierarchyBase) o;
				if (!chb1.getClass()
				         .isAnnotationPresent(NgComponent.class) && chb1 instanceof INgComponent)
				{
					INgComponent ngComp = (INgComponent) chb1;
					constructorBodies.addAll(ngComp.constructorBody());
				}
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
	
	default StringBuilder renderMethods()
	{
		StringBuilder out = new StringBuilder();
		Set<String> fStrings = new LinkedHashSet<>();
		
		List<NgOnInit> fInit = getAnnotations(getClass(), NgOnInit.class);
		if (!fInit.isEmpty())
		{
			StringBuilder fInitOut = new StringBuilder();
			fInitOut.append("ngOnInit(){\n");
			for (NgOnInit ngField : fInit)
			{
				for (String s : ngField.onInit())
				{
					fInitOut.append(s)
					        .append("\n");
				}
			}
			fInitOut.append("}\n");
			fStrings.add(fInitOut.toString());
		}
		
		List<NgOnDestroy> fDestroy = getAnnotations(getClass(), NgOnDestroy.class);
		if (!fDestroy.isEmpty())
		{
			StringBuilder fDestroyOut = new StringBuilder();
			Set<String> destroys = new LinkedHashSet<>();
			fDestroyOut.append("ngOnDestroy(){\n");
			for (NgOnDestroy ngField : fDestroy)
			{
				destroys.addAll(Arrays.asList(ngField.onDestroy()));
			}
			for (String destroy : destroys)
			{
				fDestroyOut.append(destroy)
				           .append("\n");
			}
			fDestroyOut.append("}\n");
			fStrings.add(fDestroyOut.toString());
		}
		
		List<NgMethod> fAnno = getAnnotations(getClass(), NgMethod.class);
		for (NgMethod ngField : fAnno)
		{
			if (ngField.onSelf())
			{
				fStrings.add(ngField.value());
			}
		}
		
		fStrings.addAll(componentMethods());
		fStrings.addAll(methods());
		
		for (String field : fStrings)
		{
			out.append(field)
			   .append("\n");
		}
		return out;
	}
	
	default NgComponentReference getNgComponentReference(Class<? extends ITSComponent<?>> aClass)
	{
		NgComponentReference componentReference = new NgComponentReference()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return NgComponentReference.class;
			}
			
			@Override
			public Class<? extends ITSComponent<?>> value()
			{
				return aClass;
			}
			
			@Override
			public boolean provides()
			{
				return false;
			}
			
			@Override
			public boolean onParent()
			{
				return false;
			}
			
			@Override
			public boolean onSelf()
			{
				return true;
			}
		};
		return componentReference;
	}
	
	default NgComponentReference getNgComponentReferenceOnParent(Class<? extends ITSComponent<?>> aClass)
	{
		NgComponentReference componentReference = new NgComponentReference()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return NgComponentReference.class;
			}
			
			@Override
			public Class<? extends ITSComponent<?>> value()
			{
				return aClass;
			}
			
			@Override
			public boolean provides()
			{
				return false;
			}
			
			@Override
			public boolean onParent()
			{
				return true;
			}
			
			@Override
			public boolean onSelf()
			{
				return false;
			}
		};
		return componentReference;
	}
}
