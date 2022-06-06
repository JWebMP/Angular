package com.jwebmp.core.base.angular.services.compiler;

import com.fasterxml.jackson.databind.*;
import com.google.common.base.*;
import com.guicedee.guicedinjection.*;
import com.guicedee.logger.*;
import com.jwebmp.core.*;
import com.jwebmp.core.base.*;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.*;
import com.jwebmp.core.base.angular.client.annotations.typescript.*;
import com.jwebmp.core.base.angular.client.services.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.modules.services.base.*;

import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.angular.typescript.JWebMP.*;
import com.jwebmp.core.base.html.*;
import com.jwebmp.core.base.servlets.enumarations.*;
import io.github.classgraph.*;
import io.github.classgraph.Resource;
import org.apache.commons.io.*;
import org.apache.commons.lang3.*;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;
import static com.jwebmp.core.base.angular.client.services.AnnotationsMap.*;
import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.*;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.*;
import static com.jwebmp.core.base.angular.client.services.interfaces.ImportsStatementsComponent.*;
import static com.jwebmp.core.base.angular.implementations.AngularTSPostStartup.*;
import static java.nio.charset.StandardCharsets.*;

public class JWebMPTypeScriptCompiler
{
	private static final Logger log = LogFactory.getLog(JWebMPTypeScriptCompiler.class);
	
	private Map<File, String> tsFiles = new HashMap<>();
	private static JWebMPTypeScriptCompiler instance;
	
	public static File baseUserDirectory;
	
	private File srcDirectory;
	private File appDirectory;
	private File appBaseDirectory;
	private File assetsDirectory;
	private File environmentDirectory;
	private File mainTsFile;
	
	private final Set<String> assetStringBuilder = new LinkedHashSet<>();
	private final Set<String> stylesGlobal = new LinkedHashSet<>();
	private final Set<String> scripts = new LinkedHashSet<>();
	
	private NgApp ngApp;
	private INgApp<?> app;
	
	public static final Map<INgApp, File> appDirectories = new HashMap<>();
	
	public static ThreadLocal<File> getCurrentAppFile()
	{
		return IComponent.getCurrentAppFile();
	}
	
	static
	{
		String userDir = System.getProperty("jwebmp", FileUtils.getUserDirectory()
		                                                       .getPath());
		baseUserDirectory = new File(userDir.replaceAll("\\\\", "/") + "/jwebmp/");
		try
		{
			if (!baseUserDirectory.exists())
			{
				FileUtils.forceMkdirParent(baseUserDirectory);
				FileUtils.forceMkdir(baseUserDirectory);
			}
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE, "Unable to create base directory for creating typescript! - " + userDir);
		}
		log.info("TypeScript is compiling to " + baseUserDirectory.getPath() + ". Change with env property \"jwebmp\"");
		
	}
	
	public JWebMPTypeScriptCompiler(INgApp<?> app)
	{
		instance = this;
		this.app = app;
		this.ngApp = getAnnotations(app.getClass(), NgApp.class).get(0);
		try
		{
			appBaseDirectory = new File(baseUserDirectory.getCanonicalPath() + "/" + ngApp.name());
			if (!appBaseDirectory.exists())
			{
				FileUtils.forceMkdirParent(appBaseDirectory);
				FileUtils.forceMkdir(appBaseDirectory);
			}
			appDirectories.put(app, appBaseDirectory);
			currentAppFile.set(appBaseDirectory);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		log.info("Application [" + ngApp.name() + "] is compiling to " + baseUserDirectory.getPath() + ". Change with env property \"jwebmp\"");
	}
	
	public static Set<INgApp<?>> getAllApps()
	{
		Set<INgApp<?>> out = new LinkedHashSet<>();
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgApp.class))
		{
			if (classInfo.isAbstract() || classInfo.isInterfaceOrAnnotation())
			{
				continue;
			}
			try
			{
				INgApp<?> clazz = (INgApp<?>) GuiceContext.get(classInfo.loadClass());
				System.out.println("Generating Angular Application - (" + getTsFilename(clazz) + ") in folder " + getClassDirectory(classInfo.loadClass()));
				out.add(clazz);
			}
			catch (ClassCastException e)
			{
				System.out.println("Cannot render app - " + classInfo.getSimpleName() + " / Annotated @NgApp does not implement INgApp");
			}
		}
		return out;
	}
	
	public StringBuilder renderDataTypeTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgDataType<?> component, Class<?> requestingClass) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append(component.renderClassTs());
		return sb;
	}
	
	public StringBuilder renderServiceProviderTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgServiceProvider<?> component, Class<?> requestingClass) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append(component.renderClassTs());
		return sb;
	}
	
	public StringBuilder renderProviderTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgProvider<?> component, Class<?> requestingClass) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append(component.renderClassTs());
		return sb;
	}
	
	
	public StringBuilder renderServiceTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgDataService<?> component, Class<?> requestingClass) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append(component.renderClassTs());
		return sb;
	}
	
	public StringBuilder renderDirectiveTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgDirective<?> component, Class<?> requestingClass) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append(component.renderClassTs());
		return sb;
	}
	
	public StringBuilder renderComponentTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgComponent<?> component, Class<?> requestingClass) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append(component.renderClassTs());
		return sb;
	}
	
	public String getFileReference(String baseDirectory, Class<?> clazz, String... extension)
	{
		String classLocationDirectory = getClassLocationDirectory(clazz);
		classLocationDirectory = classLocationDirectory.replaceAll("\\\\", "/");
		String baseLocation = baseDirectory;
		baseLocation.replaceAll("\\\\", "/");
		baseLocation += "/src/app/";
		classLocationDirectory = baseLocation + classLocationDirectory + getTsFilename(clazz) + (extension.length > 0 ? extension[0] : "");
		
		return classLocationDirectory;
	}
	
	public File getFile(Class<?> clazz, String... extension)
	{
		//	ITSComponent.getFile(appBaseDirectory.getPath(), clazz, extension);
		String baseDir = getFileReference(appBaseDirectory.getPath(), clazz, extension);
		File file = new File(baseDir);
		return file;
	}
	
	public File getFile(String filename, String... extension) throws IOException
	{
		String baseDir = appBaseDirectory.getCanonicalPath()
		                                 .replaceAll("\\\\", "/")
		                 + "/src/app/"
				                 .replaceAll("\\\\", "/");
		return new File(FilenameUtils.concat(baseDir, filename)
		                + (extension.length > 0 ? extension[0] : ""));
	}
	
	public StringBuilder renderModuleTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgModule<?> component, Class<?> requestingClass) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		if (component instanceof AngularAppBootModule)
		{
			component = appBootModule;
		}
		component.setApp(app);
		sb.append(component.renderClassTs());
		return sb;
	}
	
	private void loadClassFilePaths(INgApp<?> app) throws IOException
	{
		ngApp = getAnnotations(app.getClass(), NgApp.class).get(0);
		
		//======================================================================
		buildFolderStructure(appBaseDirectory);
		//======================================================================
		//String mainFileFilename = ngApp.value() + ".ts";
		String mainFileFilename = "main.ts";
		mainTsFile = new File((srcDirectory.getCanonicalPath() + "/" + mainFileFilename).replaceAll("\\\\", "/"));
	}
	
	public StringBuilder renderAppTS() throws IOException
	{
		StringBuilder sb = new StringBuilder();
		//render the main.ts file
		loadClassFilePaths(app);
		
		log.config("Loading resources from assets directory");
		ScanResult assetsResults = new ClassGraph().acceptPaths("app/")
		                                           .acceptPaths("src/")
		                                           .scan();
		for (Resource allResource : assetsResults.getAllResources())
		{
			//System.out.println("Resource Found : " + allResource.getPath());
			String assetLocation = allResource.getPathRelativeToClasspathElement();
			File assetFile = new File(appBaseDirectory.getCanonicalPath() + "/" + assetLocation);
			FileUtils.forceMkdirParent(assetFile);
			IOUtils.copy(allResource.getURL(), assetFile);
		}
		
		
		ScanResult scan = GuiceContext.instance()
		                              .getScanResult();
		
		AngularAppBootModule appBootModule = new AngularAppBootModule();
		appBootModule.setBootModule(ngApp.bootComponent());
		
		GuiceContext.get(EnvironmentModule.class)
		            .getEnvironmentOptions()
		            .setAppClass(app.getClass()
		                            .getCanonicalName());
		GuiceContext.get(EnvironmentModule.class)
		            .getEnvironmentOptions()
		            .setProduction(app.getRunningEnvironment()
		                              .equals(DevelopmentEnvironments.Production));
		
		File packageJsonFile = new File(appBaseDirectory.getCanonicalPath() + "/package.json");
		String packageTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("package.json")), UTF_8);
		
		Map<String, String> dependencies = new HashMap<>();
		Map<String, String> devDependencies = new HashMap<>();
		Map<String, String> overrideDependencies = new HashMap<>();
		
		scan
				.getClassesWithAnnotation(TsDependency.class)
				.stream()
				.forEach(a -> {
							TsDependency annotation = a.loadClass()
							                           .getAnnotation(TsDependency.class);
							if (annotation != null)
							{
								var annotation1 = getNamedTSDependency(annotation);
								dependencies.putIfAbsent(annotation1.value(), annotation1.version());
								if (annotation1.overrides())
								{
									overrideDependencies.put(annotation1.value(), annotation1.version());
								}
							}
						}
				);
		scan
				.getClassesWithAnnotation(TsDevDependency.class)
				.stream()
				.forEach(a -> {
							TsDevDependency annotation = a.loadClass()
							                              .getAnnotation(TsDevDependency.class);
							if (annotation != null)
							{
								devDependencies.put(annotation.value(), annotation.version());
							}
						}
				);
		scan
				.getClassesWithAnnotation(TsDependencies.class)
				.stream()
				.forEach(a -> {
							TsDependencies annotations = a.loadClass()
							                              .getAnnotation(TsDependencies.class);
							if (annotations != null)
							{
								for (TsDependency annotation : annotations.value())
								{
									var annotation1 = getNamedTSDependency(annotation);
									dependencies.putIfAbsent(annotation1.value(), annotation1.version());
									if (annotation1.overrides())
									{
										overrideDependencies.put(annotation1.value(), annotation1.version());
									}
								}
							}
						}
				);
		scan
				.getClassesWithAnnotation(TsDevDependencies.class)
				.stream()
				.forEach(a -> {
							TsDevDependencies annotations = a.loadClass()
							                                 .getAnnotation(TsDevDependencies.class);
							if (annotations != null)
							{
								for (TsDevDependency annotation : annotations.value())
								{
									devDependencies.put(annotation.value(), annotation.version());
								}
							}
						}
				);
		
		
		ObjectMapper om = GuiceContext.get(DefaultObjectMapper);
		packageTemplate = packageTemplate.replace("/*appName*/", app.name());
		packageTemplate = packageTemplate.replace("/*dependencies*/", om.writerWithDefaultPrettyPrinter()
		                                                                .writeValueAsString(dependencies));
		packageTemplate = packageTemplate.replace("/*devDependencies*/", om.writerWithDefaultPrettyPrinter()
		                                                                   .writeValueAsString(devDependencies));
		packageTemplate = packageTemplate.replace("/*overrideDependencies*/", om.writerWithDefaultPrettyPrinter()
		                                                                        .writeValueAsString(overrideDependencies));
		
		
		FileUtils.writeStringToFile(packageJsonFile, packageTemplate, UTF_8, false);
		
		File tsConfigFile = new File(appBaseDirectory.getCanonicalPath() + "/tsconfig.app.json");
		String tsConfigTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("tsconfig.app.json")), UTF_8);
		FileUtils.writeStringToFile(tsConfigFile, tsConfigTemplate, UTF_8, false);
		
		File tsConfigFileAbs = new File(appBaseDirectory.getCanonicalPath() + "/tsconfig.json");
		String tsConfigTemplateAbs = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("tsconfig.json")), UTF_8);
		FileUtils.writeStringToFile(tsConfigFileAbs, tsConfigTemplateAbs, UTF_8, false);
		
		File polyfillFile = new File(srcDirectory.getCanonicalPath() + "/polyfills.ts");
		StringBuilder polyfills = new StringBuilder();
		for (ClassInfo classInfo : scan.getClassesWithAnnotation(NgPolyfill.class))
		{
			Class<?> aClass = classInfo.loadClass();
			NgPolyfill fill = aClass.getAnnotation(NgPolyfill.class);
			String newString = fill.value();
			polyfills.append("import \"" + newString + "\";\n");
		}
		FileUtils.writeStringToFile(polyfillFile, polyfills.toString(), UTF_8, false);
		
		File angularFile = new File(appBaseDirectory.getCanonicalPath() + "/angular.json");
		
		Map<String, String> namedAssets = new HashMap<>();
		List<NgAsset> assets = AnnotationsMap.getAllAnnotations(NgAsset.class);
		for (NgAsset ngAsset : assets)
		{
			String name = ngAsset.name();
			if (Strings.isNullOrEmpty(ngAsset.name()))
			{
				name = ngAsset.value();
			}
			namedAssets.put(name, ngAsset.value());
		}
		for (NgAsset ngAsset : assets)
		{
			String name = ngAsset.name();
			if (ngAsset.replaces().length > 0)
			{
				for (String replace : ngAsset.replaces())
				{
					namedAssets.put(replace, ngAsset.value());
				}
			}
		}
		
		for (String value : namedAssets.values())
		{
			assetStringBuilder.add(value);
		}
		
		for (String asset : app.assets())
		{
			assetStringBuilder.add("src/assets/" + asset);
		}
		
		Map<String, String> namedStylesheets = new LinkedHashMap<>();
		List<NgStyleSheet> ngStyleSheets = getAllAnnotations(NgStyleSheet.class);
		ngStyleSheets.sort(new Comparator<NgStyleSheet>()
		{
			@Override
			public int compare(NgStyleSheet o1, NgStyleSheet o2)
			{
				return Integer.compare(o1.sortOrder(), o2.sortOrder());
			}
		});
		for (NgStyleSheet ngAsset : ngStyleSheets)
		{
			String name = ngAsset.name();
			if (Strings.isNullOrEmpty(ngAsset.name()))
			{
				name = ngAsset.value();
			}
			namedStylesheets.put(name, ngAsset.value());
		}
		for (NgStyleSheet ngAsset : ngStyleSheets)
		{
			String name = ngAsset.name();
			if (ngAsset.replaces().length > 0)
			{
				for (String replace : ngAsset.replaces())
				{
					namedStylesheets.put(replace, ngAsset.value());
				}
			}
		}
		
		for (String value : namedStylesheets.values())
		{
			stylesGlobal.add(value);
		}
		
		for (String stylesheet : app.stylesheets())
		{
			stylesGlobal.add("src/assets/" + stylesheet);
		}
		
		Map<String, String> namedScripts = new LinkedHashMap<>();
		
		List<NgScript> allAnnotations = getAllAnnotations(NgScript.class);
		allAnnotations.sort(new Comparator<NgScript>()
		{
			@Override
			public int compare(NgScript o1, NgScript o2)
			{
				return Integer.compare(o1.sortOrder(), o2.sortOrder());
			}
		});
		for (NgScript ngAsset : allAnnotations)
		{
			String name = ngAsset.name();
			if (Strings.isNullOrEmpty(ngAsset.name()))
			{
				name = ngAsset.value();
			}
			namedScripts.put(name, ngAsset.value());
		}
		for (NgScript ngAsset : allAnnotations)
		{
			String name = ngAsset.name();
			if (ngAsset.replaces().length > 0)
			{
				for (String replace : ngAsset.replaces())
				{
					namedScripts.put(replace, ngAsset.value());
				}
			}
		}
		
		for (String value : namedScripts.values())
		{
			scripts.add(value);
		}
		
		for (String stylesheet : app.scripts())
		{
			scripts.add("src/assets/" + stylesheet);
		}
		
		String angularTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("angular.json")), UTF_8);
		angularTemplate = angularTemplate.replace("/*BuildAssets*/", om.writerWithDefaultPrettyPrinter()
		                                                               .writeValueAsString(assetStringBuilder));
		angularTemplate = angularTemplate.replace("/*BuildStylesSCSS*/", om.writerWithDefaultPrettyPrinter()
		                                                                   .writeValueAsString(stylesGlobal));
		angularTemplate = angularTemplate.replace("/*BuildScripts*/", om.writerWithDefaultPrettyPrinter()
		                                                                .writeValueAsString(scripts));
		angularTemplate = angularTemplate.replace("/*MainTSFile*/", om.writerWithDefaultPrettyPrinter()
		                                                              .writeValueAsString("src/" + mainTsFile.getName()
		                                                              )
		);
		FileUtils.writeStringToFile(angularFile, angularTemplate, UTF_8, false);
		sb.append(app.renderImports());
		
		sb.append("if(EnvironmentModule.production) {\n" + " enableProdMode();\n" + "}\n" + "" + "platformBrowserDynamic().bootstrapModule(")
		  .append(appBootModule.getClass()
		                       .getSimpleName())
		  .append(")\n")
		  .append(".catch(err => console.error(err));\n");
		
		
		System.out.println("Writing out angular boot file - " + mainTsFile.getCanonicalPath());
		try
		{
			FileUtils.writeStringToFile(mainTsFile, sb.toString(), UTF_8, false);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		File mainIndexHtmlTsFile = new File(srcDirectory.getCanonicalPath() + "/" + "index.html");
		System.out.println("Writing out index.html - " + mainIndexHtmlTsFile.getCanonicalPath());
		try
		{
			FileUtils.writeStringToFile(mainIndexHtmlTsFile, renderBootIndexHtml(app, appBootModule).toString(), UTF_8, false);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		for (Resource resource : scan.getResourcesMatchingWildcard("assets/*"))
		{
			try
			{
				FileUtils.copyFile(new File(FilenameUtils.concat(resource.getClasspathElementFile()
				                                                         .getPath(), resource.getPath())),
						new File(FilenameUtils.concat(instance.srcDirectory.getPath(),
								resource.getPath())));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
		}
		
		File finalSrcDirectory = srcDirectory;
		
		scan
				.getClassesWithAnnotation(NgModule.class)
				.stream()
				.forEach(a -> {
					Set<Class<?>> classes = new HashSet<>();
					if (a.isInterface() || a.isAbstract())
					{
						for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
						{
							if (!subclass.isAbstract() && !subclass.isInterface())
							{
								classes.add(subclass.loadClass());
							}
						}
					}
					else
					{
						Class<?> aClass = a.loadClass();
						classes.add(aClass);
					}
					for (Class<?> aClass : classes)
					{
						File classFile = null;
						classFile = getFile(aClass, ".ts");
						try
						{
							FileUtils.forceMkdirParent(classFile);
							INgModule<?> modd = (INgModule<?>) GuiceContext.get(aClass);
							modd.setApp(app);
							FileUtils.write(classFile, renderModuleTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				});
		
		scan
				.getClassesWithAnnotation(NgComponent.class)
				.stream()
				// .filter(a -> !(a.isInterface() || a.isAbstract()))
				.forEach(a -> {
					Set<Class<?>> classes = new HashSet<>();
					if (a.isInterface() || a.isAbstract())
					{
						for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
						{
							if (!subclass.isAbstract() && !subclass.isInterface())
							{
								classes.add(subclass.loadClass());
							}
						}
					}
					else
					{
						Class<?> aClass = a.loadClass();
						classes.add(aClass);
					}
					for (Class<?> aClass : classes)
					{
						
						File classFile = null;
						classFile = getFile(aClass, ".ts");
						try
						{
							FileUtils.forceMkdirParent(classFile);
							INgComponent<?> modd = (INgComponent<?>) GuiceContext.get(aClass);
							ComponentHierarchyBase chb2 = (ComponentHierarchyBase) modd;
							FileUtils.write(classFile, renderComponentTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				});
		
		scan.getClassesWithAnnotation(NgDirective.class)
		    .stream()
		    // .filter(a -> !(a.isInterface() || a.isAbstract()))
		    .forEach(a -> {
			    Set<Class<?>> classes = new HashSet<>();
			    if (a.isInterface() || a.isAbstract())
			    {
				    for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
				    {
					    if (!subclass.isAbstract() && !subclass.isInterface())
					    {
						    classes.add(subclass.loadClass());
					    }
				    }
			    }
			    else
			    {
				    Class<?> aClass = a.loadClass();
				    classes.add(aClass);
			    }
			    for (Class<?> aClass : classes)
			    {
				    File classFile = null;
				    classFile = getFile(aClass, ".ts");
				
				    try
				    {
					    FileUtils.forceMkdirParent(classFile);
					    INgDirective<?> modd = (INgDirective<?>) GuiceContext.get(aClass);
					    FileUtils.write(classFile, renderDirectiveTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
				    }
				    catch (IOException e)
				    {
					    e.printStackTrace();
				    }
			    }
			
			
		    });
		
		scan.getClassesWithAnnotation(NgDataService.class)
		    .stream()
		    //    .filter(a -> !(a.isInterface() || a.isAbstract()))
		    .forEach(a -> {
			    Set<Class<?>> classes = new HashSet<>();
			    if (a.isInterface() || a.isAbstract())
			    {
				    for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
				    {
					    if (!subclass.isAbstract() && !subclass.isInterface())
					    {
						    classes.add(subclass.loadClass());
					    }
				    }
			    }
			    else
			    {
				    Class<?> aClass = a.loadClass();
				    classes.add(aClass);
			    }
			    for (Class<?> aClass : classes)
			    {
				    File classFile = null;
				    classFile = getFile(aClass, ".ts");
				    try
				    {
					    FileUtils.forceMkdirParent(classFile);
					    INgDataService modd = (INgDataService) GuiceContext.get(aClass);
					    FileUtils.write(classFile, renderServiceTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
				    }
				    catch (IOException e)
				    {
					    e.printStackTrace();
				    }
			    }
		    });
		
		
		scan.getClassesWithAnnotation(NgProvider.class)
		    .stream()
		    //.filter(a -> !(a.isInterface() || a.isAbstract()))
		    .forEach(a -> {
			    Set<Class<?>> classes = new HashSet<>();
			    if (a.isInterface() || a.isAbstract())
			    {
				    for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
				    {
					    if (!subclass.isAbstract() && !subclass.isInterface())
					    {
						    classes.add(subclass.loadClass());
					    }
				    }
			    }
			    else
			    {
				    Class<?> aClass = a.loadClass();
				    classes.add(aClass);
			    }
			    for (Class<?> aClass : classes)
			    {
				    File classFile = null;
				    classFile = getFile(aClass, ".ts");
				    try
				    {
					    FileUtils.forceMkdirParent(classFile);
					    INgProvider<?> modd = (INgProvider<?>) GuiceContext.get(aClass);
					    FileUtils.write(classFile, renderProviderTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
				    }
				    catch (IOException e)
				    {
					    e.printStackTrace();
				    }
			    }
		    });
		
		scan.getClassesWithAnnotation(NgDataType.class)
		    .stream()
		    //   .filter(a -> !(a.isInterface() || a.isAbstract()))
		    .forEach(a -> {
			    Set<Class<?>> classes = new HashSet<>();
			    if (a.isInterface() || a.isAbstract())
			    {
				    for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
				    {
					    if (!subclass.isAbstract() && !subclass.isInterface())
					    {
						    classes.add(subclass.loadClass());
					    }
				    }
			    }
			    else
			    {
				    Class<?> aClass = a.loadClass();
				    classes.add(aClass);
			    }
			    for (Class<?> aClass : classes)
			    {
				    File classFile = null;
				    classFile = getFile(aClass, ".ts");
				    try
				    {
					    FileUtils.forceMkdirParent(classFile);
					    INgDataType modd = (INgDataType) GuiceContext.get(aClass);
					    FileUtils.write(classFile, renderDataTypeTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
				    }
				    catch (IOException e)
				    {
					    e.printStackTrace();
				    }
			    }
		    });
		
		scan.getClassesWithAnnotation(NgServiceProvider.class)
		    .stream()
		    //   .filter(a -> !(a.isInterface() || a.isAbstract()))
		    .forEach(a -> {
			    Set<Class<?>> classes = new HashSet<>();
			    if (a.isInterface() || a.isAbstract())
			    {
				    for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
				    {
					    if (!subclass.isAbstract() && !subclass.isInterface())
					    {
						    classes.add(subclass.loadClass());
					    }
				    }
			    }
			    else
			    {
				    Class<?> aClass = a.loadClass();
				    classes.add(aClass);
			    }
			    for (Class<?> aClass : classes)
			    {
				    File classFile = null;
				    classFile = getFile(aClass, ".ts");
				    try
				    {
					    FileUtils.forceMkdirParent(classFile);
					    INgServiceProvider<?> modd = (INgServiceProvider<?>) GuiceContext.get(aClass);
					    FileUtils.write(classFile, renderServiceProviderTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
				    }
				    catch (IOException e)
				    {
					    e.printStackTrace();
				    }
			    }
		    });
		
		if (buildApp)
		{
			System.out.println("Installing node-modules...");
			installDependencies(appBaseDirectory);
			System.out.println("Building Angular Client App...");
			installAngular(appBaseDirectory);
		}
		//System.out.println("Starting Local Angular Client...");
		//serveAngular(appBaseDirectory);
		return sb;
	}
	
	private File buildFolderStructure(File appBaseDirectory) throws IOException
	{
		
		srcDirectory = new File(appBaseDirectory.getCanonicalPath() + "/src");
		if (!srcDirectory.exists())
		{
			FileUtils.forceMkdirParent(srcDirectory);
			FileUtils.forceMkdir(srcDirectory);
		}
		
		appDirectory = new File(srcDirectory.getCanonicalPath() + "/app");
		if (!appDirectory.exists())
		{
			FileUtils.forceMkdirParent(appDirectory);
			FileUtils.forceMkdir(appDirectory);
		}
		
		
		assetsDirectory = new File(srcDirectory.getCanonicalPath() + "/assets");
		if (!assetsDirectory.exists())
		{
			FileUtils.forceMkdirParent(assetsDirectory);
			FileUtils.forceMkdir(assetsDirectory);
		}
		
		environmentDirectory = new File(srcDirectory.getCanonicalPath() + "/environment");
		if (!environmentDirectory.exists())
		{
			FileUtils.forceMkdirParent(environmentDirectory);
			FileUtils.forceMkdir(environmentDirectory);
		}
		return srcDirectory;
	}
	
	
	public StringBuilder renderBootIndexHtml(INgApp<?> app, AngularAppBootModule appBootModule)
	{
		Page<?> p = (Page) app;
		StringBuilder sb = new StringBuilder();
		Body body = p.getBody();
		List<ComponentHierarchyBase> comps = new ArrayList<>(body.getChildren());
		
		body.getChildren()
		    .clear();
		List<NgComponent> annotations = getAnnotations(appBootModule.getBootModule(), NgComponent.class);
		if (annotations.isEmpty())
		{
			throw new RuntimeException("No components found to render for boot index, the boot module specified does not have a @NgComponent");
		}
		else
		{
			body.add(new DivSimple<>().setTag(annotations.get(0)
			                                             .value()));
		}
		p.setBody(body);
		sb.append(p.toString(0));
		
		return sb;
	}
	
	public static void installDependencies(File appBaseDirectory)
	{
		if (SystemUtils.IS_OS_WINDOWS)
		{
			try
			{
				ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", FileUtils.getUserDirectory() + "/AppData/Roaming/npm/npm.cmd install");
				//processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				//processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
				processBuilder.inheritIO();
				processBuilder
						.environment()
						.putAll(System.getenv());
				processBuilder = processBuilder.directory(appBaseDirectory);
				Process p = processBuilder.start();
				p.waitFor();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (SystemUtils.IS_OS_LINUX)
		{
			try
			{
				ProcessBuilder processBuilder = new ProcessBuilder("npm install");
				processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
				processBuilder
						.environment()
						.putAll(System.getenv());
				processBuilder = processBuilder.directory(appBaseDirectory);
				Process p = processBuilder.start();
				p.waitFor();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void installAngular(File appBaseDirectory)
	{
		if (SystemUtils.IS_OS_WINDOWS)
		{
			try
			{
				ProcessBuilder processBuilder =
						new ProcessBuilder("cmd.exe", "/c", FileUtils.getUserDirectory() +
						                                    "/AppData/Roaming/npm/ng.cmd build " +
						                                    (GuiceContext.get(EnvironmentModule.class)
						                                                 .getEnvironmentOptions()
						                                                 .isProduction() ? "--configuration production" : ""));
				processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
				processBuilder
						.environment()
						.putAll(System.getenv());
				processBuilder = processBuilder.directory(appBaseDirectory);
				Process p = processBuilder.start();
				p.waitFor(15, TimeUnit.SECONDS);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		else if (SystemUtils.IS_OS_LINUX)
		{
			try
			{
				ProcessBuilder processBuilder = new ProcessBuilder("ng build");
				processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
				processBuilder
						.environment()
						.putAll(System.getenv());
				processBuilder = processBuilder.directory(appBaseDirectory);
				Process p = processBuilder.start();
				p.waitFor();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private static final Map<String, TsDependency> namedDependencies = new HashMap<>();
	
	private TsDependency getNamedTSDependency(TsDependency dependency)
	{
		String name = dependency.value();
		if (!Strings.isNullOrEmpty(dependency.name()))
		{
			name = dependency.name();
		}
		
		if (dependency.overrides())
		{
			namedDependencies.put(name, dependency);
		}
		else
		{
			namedDependencies.putIfAbsent(name, dependency);
		}
		TsDependency dep = namedDependencies.get(name);
		return dep;
	}
	
}
