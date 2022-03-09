package com.jwebmp.core.base.angular.services;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.google.common.base.*;
import com.guicedee.guicedinjection.*;
import com.guicedee.guicedinjection.interfaces.*;
import com.guicedee.logger.*;
import com.jwebmp.core.*;
import com.jwebmp.core.base.*;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.modules.services.base.*;
import com.jwebmp.core.base.angular.services.annotations.NgModule;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.angular.typescript.JWebMP.*;
import com.jwebmp.core.base.html.*;
import com.jwebmp.core.base.interfaces.*;
import com.jwebmp.core.base.references.*;
import com.jwebmp.core.base.servlets.enumarations.*;
import com.jwebmp.core.htmlbuilder.css.composer.*;
import com.mangofactory.typescript.*;
import io.github.classgraph.*;
import org.apache.commons.io.*;
import org.apache.commons.lang3.*;

import java.io.*;
import java.lang.reflect.*;
import java.math.*;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;
import static com.jwebmp.core.base.angular.services.interfaces.ITSComponent.*;
import static java.nio.charset.StandardCharsets.*;

public class JWebMPTypeScriptCompiler
{
	private static final Logger log = LogFactory.getLog(JWebMPTypeScriptCompiler.class);
	
	private Map<File, String> tsFiles = new HashMap<>();
	
	
	public static File baseUserDirectory;
	
	private File srcDirectory;
	private File appDirectory;
	private File appBaseDirectory;
	private File assetsDirectory;
	private File environmentDirectory;
	private File mainTsFile;
	
	private final Set<String> assets = new LinkedHashSet<>();
	private final Set<String> stylesGlobal = new LinkedHashSet<>();
	private final Set<String> scripts = new LinkedHashSet<>();
	
	private NgApp ngApp;
	private INgApp<?> app;
	
	public static final Map<INgApp, File> appDirectories = new HashMap<>();
	
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
		this.app = app;
		this.ngApp = app.getClass()
		                .getAnnotation(NgApp.class);
		try
		{
			appBaseDirectory = new File(baseUserDirectory.getCanonicalPath() + "/" + ngApp.value());
			if (!appBaseDirectory.exists())
			{
				FileUtils.forceMkdirParent(appBaseDirectory);
				FileUtils.forceMkdir(appBaseDirectory);
			}
			appDirectories.put(app, appBaseDirectory);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		log.info("Application [" + ngApp.value() + "] is compiling to " + baseUserDirectory.getPath() + ". Change with env property \"jwebmp\"");
	}
	
	public String renderAssetsList()
	{
		StringBuilder assetString = new StringBuilder();
		for (String asset : assets)
		{
			assetString.append(asset)
			           .append(",");
		}
		if (!assets.isEmpty())
		{
			assetString.deleteCharAt(assetString.length() - 1);
		}
		return assetString.toString();
	}
	
	public String renderStylesGlobalList()
	{
		StringBuilder assetString = new StringBuilder();
		for (String asset : stylesGlobal)
		{
			assetString.append(asset)
			           .append(",");
		}
		if (!stylesGlobal.isEmpty())
		{
			assetString.deleteCharAt(assetString.length() - 1);
		}
		return assetString.toString();
	}
	
	public String renderScriptsList()
	{
		StringBuilder assetString = new StringBuilder();
		for (String asset : scripts)
		{
			assetString.append(asset)
			           .append(",");
		}
		if (!scripts.isEmpty())
		{
			assetString.deleteCharAt(assetString.length() - 1);
		}
		return assetString.toString();
	}
	
	public static String renderJavaScript()
	{
		TypescriptCompiler compiler = new TypescriptCompiler();
		compiler.setEcmaScriptVersion(EcmaScriptVersion.ES5);
		String output = compiler.compile("class Greeter { greeting: string; }");
		return output;
	}
	
	public JWebMPTypeScriptCompiler addPageConfig(Page<?> page)
	{
		for (CSSReference cssReference : page.getCssReferencesAll())
		{
			stylesGlobal.add(cssReference.toString());
		}
		for (JavascriptReference javascriptReference : page.getJavascriptReferencesAll())
		{
			scripts.add(javascriptReference.toString());
		}
		
		return this;
	}
	
	public JWebMPTypeScriptCompiler addComponent(IComponentHierarchyBase<?, ?> component)
	{
		File file = renderTypescriptFile(component);
		
		return this;
	}
	
	public File renderTypescriptFile(IComponentHierarchyBase<?, ?> component)
	{
		Path path = Path.of("site", component.getClass()
		                                     .getPackageName(), component.getClass()
		                                                                 .getSimpleName(), ".ts");
		return path.toFile();
	}
	
	
	public File renderAngularJsonFile()
	{
		Path path = Path.of("site", "angular", ".json");
		return path.toFile();
	}
	
	public List<ComponentHierarchyBase> getAllComponents()
	{
		List<ComponentHierarchyBase> out = new ArrayList<>();
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgComponent.class))
		{
			if (classInfo.isAbstract() || classInfo.isInterfaceOrAnnotation())
			{
				continue;
			}
			
			System.out.println("Generating @Component (" + getTsFilename(classInfo.loadClass()) + ") in folder " + getClassDirectory(classInfo.loadClass()));
			
		}
		return out;
	}
	
	public static List<INgModule<?>> getAllModules()
	{
		List<INgModule<?>> out = new ArrayList<>();
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(com.jwebmp.core.base.angular.services.annotations.NgModule.class))
		{
			if (classInfo.isAbstract() || classInfo.isInterfaceOrAnnotation())
			{
				continue;
			}
			
			try
			{
				out.add((INgModule<?>) classInfo.loadClass()
				                                .getConstructor()
				                                .newInstance());
			}
			catch (ClassCastException e)
			{
				System.out.println("Class [" + classInfo.getSimpleName() + "] is annotated as a NG Module, but does not implement INgModule");
			}
			catch (InstantiationException e)
			{
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
			catch (InvocationTargetException e)
			{
				e.printStackTrace();
			}
			catch (NoSuchMethodException e)
			{
				e.printStackTrace();
			}
			
		}
		return out;
	}
	
	public List<ComponentHierarchyBase> getAllDirectives()
	{
		List<ComponentHierarchyBase> out = new ArrayList<>();
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgDirective.class))
		{
			if (classInfo.isAbstract() || classInfo.isInterfaceOrAnnotation())
			{
				continue;
			}
			System.out.println("Generating @Directive (" + getTsFilename(classInfo.loadClass()) + ") in folder " + getClassDirectory(classInfo.loadClass()));
			
		}
		return out;
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
	
	public List<ComponentHierarchyBase> getAllProviders(String packageName)
	{
		List<ComponentHierarchyBase> out = new ArrayList<>();
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgProvider.class))
		{
			if (classInfo.isAbstract() || classInfo.isInterfaceOrAnnotation())
			{
				continue;
			}
			System.out.println("Generating @Directive (" + getTsFilename(classInfo.loadClass()) + ") in folder " + getClassDirectory(classInfo.loadClass()));
			
		}
		return out;
	}
	
	
	public StringBuilder renderProviderTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgProvider<?> component, Class<?> requestingClass) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		
		StringBuilder imports = new StringBuilder();
		imports = renderImports(component, component.renderImports(), requestingClass);
		sb.append(imports);
		sb.append("\n");
		sb.append(renderClassTs(component, requestingClass));
		return sb;
	}
	
	
	public StringBuilder renderServiceTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgDataService<?> component, Class<?> requestingClass) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		
		StringBuilder imports = new StringBuilder();
		imports = renderImports(component, component.renderImports(), requestingClass);
		sb.append(imports);
		sb.append("\n");
		sb.append(renderClassTs(component, requestingClass));
		return sb;
	}
	
	public StringBuilder renderDirectiveTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgDirective<?> component, Class<?> requestingClass) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		
		StringBuilder imports = new StringBuilder();
		StringBuilder selector = new StringBuilder();
		StringBuilder styles = new StringBuilder();
		StringBuilder template = new StringBuilder();
		StringBuilder styleUrls = new StringBuilder();
		StringBuilder providers = new StringBuilder();
		
		NgDirective ngComponent = component.getClass()
		                                   .getAnnotation(NgDirective.class);
		selector.append(ngComponent.selector());
		
		component.providers()
		         .forEach((key) -> {
			         providers.append(key)
			                  .append(",")
			                  .append("\n");
		         });
		if (!component.providers()
		              .isEmpty())
		{
			providers.deleteCharAt(providers.length() - 2);
		}
		
		String componentString = String.format(ITSComponent.directiveString, selector, providers);
		
		imports = renderImports(component, component.renderImports(), requestingClass);
		sb.append(imports);
		
		sb.append(componentString);
		sb.append("\n");
		
		sb.append(renderClassTs(component, requestingClass));
		return sb;
	}
	
	public StringBuilder renderComponentTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgComponent<?> component, Class<?> requestingClass) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		
		StringBuilder imports = new StringBuilder();
		StringBuilder selector = new StringBuilder();
		StringBuilder template = new StringBuilder();
		StringBuilder styles = new StringBuilder();
		StringBuilder styleUrls = new StringBuilder();
		StringBuilder viewProviders = new StringBuilder();
		StringBuilder animations = new StringBuilder();
		StringBuilder providers = new StringBuilder();
		
		NgComponent ngComponent = component.getClass()
		                                   .getAnnotation(NgComponent.class);
		ComponentHierarchyBase chb = (ComponentHierarchyBase) component;
		selector.append(ngComponent.value());
		
		StringBuilder templateUrls = new StringBuilder();
		String templateHtml = chb.toString(0);
		
		templateUrls.append("./")
		            .append(getTsFilename(component.getClass()))
		            .append(".html");
		File htmlFile = getFile(component.getClass(), ".html");
		FileUtils.writeStringToFile(htmlFile, templateHtml, UTF_8);
		
		styleUrls.append("'./")
		         .append(getTsFilename(component.getClass()))
		         .append(".css")
		         .append("',\n");
		for (String styleUrl : component.styleUrls())
		{
			styleUrls.append("'")
			         .append(styleUrl)
			         .append("',\n");
		}
		if (styleUrls.length() > 0)
		{
			styleUrls.deleteCharAt(styleUrls.length() - 2);
		}
		
		CSSComposer cssComposer = new CSSComposer();
		cssComposer.addComponent(chb);
		//styles.append("\"" + cssComposer.toString() + "\"");
		File cssFile = getFile(component.getClass(), ".css");
		FileUtils.writeStringToFile(cssFile, cssComposer.toString(), UTF_8);
		
		component.providers()
		         .forEach((key) -> {
			         providers.append(key)
			                  .append(",")
			                  .append("\n");
		         });
		if (!component.providers()
		              .isEmpty())
		{
			providers.deleteCharAt(providers.length() - 2);
		}
		
		String componentString = String.format(ITSComponent.componentString, selector, template, templateUrls, styles, styleUrls,
				"", //viewProviders
				"", //Animations
				providers //Directive Providers
		);
		
		imports = renderImports(component, component.renderImports(), requestingClass);
		sb.append(imports);
		
		sb.append(componentString);
		
		StringBuilder czzz = renderClassTs(component, getClass());
		
		sb.append(czzz);
	/*	sb.append("\n");
		sb.append("export class ")
		  .append(getTsFilename(component.getClass()))
		  .append("\n");
		sb.append("{")
		  .append("\n");
		sb.append("}")
		  .append("\n");
		*/
		return sb;
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
		
		StringBuilder imports = new StringBuilder();
		StringBuilder declarations = new StringBuilder();
		StringBuilder providers = new StringBuilder();
		StringBuilder exports = new StringBuilder();
		StringBuilder bootstrap = new StringBuilder();
		StringBuilder schemas = new StringBuilder();
		
		imports = renderImports(component, component.renderImports(), requestingClass);
		
		
		component.declarations()
		         .forEach(a -> {
			         declarations.append(a)
			                     .append(",")
			                     .append("\n");
		         });
		if (declarations.length() > 1)
		{
			declarations.deleteCharAt(declarations.length() - 2);
		}
		
		component.providers()
		         .forEach((key) -> {
			         providers.append(key)
			                  .append(",")
			                  .append("\n");
		         });
		
		if (providers.length() > 1)
		{
			providers.deleteCharAt(providers.length() - 2);
		}
		
		
		component.exports()
		         .forEach((key) -> {
			         exports.append(key)
			                .append(",")
			                .append("\n");
		         });
		if (exports.length() > 1)
		{
			exports.deleteCharAt(exports.length() - 2);
		}
		
		bootstrap.append(component.bootstrap());
		
		
		component.schemas()
		         .forEach((key) -> {
			         schemas.append(key)
			                .append(",")
			                .append("\n");
		         });
		if (schemas.length() > 1)
		{
			schemas.deleteCharAt(schemas.length() - 2);
		}
		
		sb.append(imports);
		
		for (String s : component.renderBeforeNgModuleDecorator())
		{
			sb.append(s);
		}
		StringBuilder importNames = new StringBuilder();
		
		Arrays.stream(component.moduleImports()
		                       .toArray())
		      .forEach((key) -> {
			
			      importNames.append(key)
			                 .append(",")
			                 .append("\n");
		      });
		
		if (importNames.length() > 1)
		{
			importNames.deleteCharAt(importNames.length() - 2);
		}
		
		sb.append(String.format(moduleString, importNames, declarations, providers, exports, bootstrap, schemas));
		sb.append("\n");
		
		StringBuilder classRender = renderClassTs(component, getClass());
		String result = StringUtils.trim(classRender.toString());
		if (!result.startsWith("export"))
		{
			classRender.insert(0, " export ");
		}
		sb.append(classRender);
		
		/*
		sb.append("export class ")
		  .append(getTsFilename(component.getClass()))
		  .append(" {}\n");
		*/
		return sb;
	}
	
	// if unable to get parent, try substring to get the parent folder.
	private static String getParentPath(File file)
	{
		if (file.getParent() == null)
		{
			String absolutePath = file.getAbsolutePath();
			return absolutePath.substring(0,
					absolutePath.lastIndexOf(File.separator));
		}
		else
		{
			return file.getParent();
		}
	}
	
	
	private void loadClassFilePaths(INgApp<?> app) throws IOException
	{
		ngApp = app.getClass()
		           .getAnnotation(NgApp.class);
		
		//======================================================================
		buildFolderStructure(appBaseDirectory);
		//======================================================================
		String mainFileFilename = ngApp.value() + ".ts";
		mainTsFile = new File((srcDirectory.getCanonicalPath() + "/" + mainFileFilename).replaceAll("\\\\", "/"));
	}
	
	public StringBuilder renderAppTS(INgApp<?> app) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		//render the main.ts file
		loadClassFilePaths(app);
		
		ScanResult scan = getNgPackageFilterScanResult(app.getClass(), app.getClass()
		                                                                  .getAnnotation(NgApp.class));
		
		AngularAppBootModule appBootModule = new AngularAppBootModule();
		appBootModule.setBootModule(ngApp.bootComponent());
		
		GuiceContext.get(EnvironmentModule.class)
		            .getEnvironmentOptions()
		            .setAppClass(app.getClass()
		                            .toString());
		GuiceContext.get(EnvironmentModule.class)
		            .getEnvironmentOptions()
		            .setProduction(((Page<?>) app).getRunningEnvironment()
		                                          .equals(DevelopmentEnvironments.Production));
		
		String relativePath = getRelativePath(mainTsFile, getFile(AngularAppBootModule.class), null);
		String relativePath2 = getRelativePath(mainTsFile, getFile(EnvironmentModule.class), null);
		
		sb.append(renderImportStatement("AngularAppBootModule", relativePath));
		sb.append(renderImportStatement("EnvironmentModule", relativePath2));
		
		File packageJsonFile = new File(appBaseDirectory.getCanonicalPath() + "/package.json");
		String packageTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("package.json")), UTF_8);
		
		Map<String, String> dependencies = new HashMap<>();
		Map<String, String> devDependencies = new HashMap<>();
		
		scan
				.getClassesWithAnnotation(TsDependency.class)
				.stream()
				.forEach(a -> {
							TsDependency annotation = a.loadClass()
							                           .getAnnotation(TsDependency.class);
							if (annotation != null)
							{
								dependencies.put(annotation.value(), annotation.version());
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
									dependencies.put(annotation.value(), annotation.version());
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
		packageTemplate = packageTemplate.replace("/*appName*/", ngApp.value());
		packageTemplate = packageTemplate.replace("/*dependencies*/", om.writerWithDefaultPrettyPrinter()
		                                                                .writeValueAsString(dependencies));
		packageTemplate = packageTemplate.replace("/*devDependencies*/", om.writerWithDefaultPrettyPrinter()
		                                                                   .writeValueAsString(devDependencies));
		
		FileUtils.writeStringToFile(packageJsonFile, packageTemplate, UTF_8, false);
		
		File tsConfigFile = new File(appBaseDirectory.getCanonicalPath() + "/tsconfig.app.json");
		String tsConfigTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("tsconfig.app.json")), UTF_8);
		FileUtils.writeStringToFile(tsConfigFile, tsConfigTemplate, UTF_8, false);
		
		File tsConfigFileAbs = new File(appBaseDirectory.getCanonicalPath() + "/tsconfig.json");
		String tsConfigTemplateAbs = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("tsconfig.json")), UTF_8);
		FileUtils.writeStringToFile(tsConfigFileAbs, tsConfigTemplateAbs, UTF_8, false);
		
		File polyfillFile = new File(srcDirectory.getCanonicalPath() + "/polyfills.ts");
		String polyfillTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("polyfills.ts")), UTF_8);
		FileUtils.writeStringToFile(polyfillFile, polyfillTemplate, UTF_8, false);
		
		//	File scrFile = new File(srcDirectory.getCanonicalPath() + "/app/jwscr.ts");
		//String scrTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("jwscr.ts")), UTF_8);
		
		
		//	FileUtils.writeStringToFile(scrFile, scrTemplate, UTF_8, false);
		
		File angularFile = new File(appBaseDirectory.getCanonicalPath() + "/angular.json");
		
		
		String angularTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("angular.json")), UTF_8);
		angularTemplate = angularTemplate.replace("/*BuildAssets*/", om.writerWithDefaultPrettyPrinter()
		                                                               .writeValueAsString(ngApp.assets()));
		angularTemplate = angularTemplate.replace("/*BuildStylesSCSS*/", om.writerWithDefaultPrettyPrinter()
		                                                                   .writeValueAsString(ngApp.stylesheets()));
		angularTemplate = angularTemplate.replace("/*BuildScripts*/", om.writerWithDefaultPrettyPrinter()
		                                                                .writeValueAsString(ngApp.scripts()));
		angularTemplate = angularTemplate.replace("/*MainTSFile*/", om.writerWithDefaultPrettyPrinter()
		                                                              .writeValueAsString("src/" + mainTsFile.getName()
		                                                              )
		);
		FileUtils.writeStringToFile(angularFile, angularTemplate, UTF_8, false);
		
		
		Map<String, String> importsMap = app.imports();
		sb.append(renderImports(app, importsMap, app.getClass()));
		
		
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
		
		File finalSrcDirectory = srcDirectory;
		scan
				.getClassesWithAnnotation(NgModule.class)
				.stream()
				.filter(a -> !(a.isInterface() || a.isAbstract()))
				.forEach(a -> {
					Class<?> aClass = a.loadClass();
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
					
				});
		
		scan
				.getClassesWithAnnotation(NgComponent.class)
				.stream()
				.filter(a -> !(a.isInterface() || a.isAbstract()))
				.forEach(a -> {
					Class<?> aClass = a.loadClass();
					File classFile = null;
					classFile = getFile(aClass, ".ts");
					try
					{
						FileUtils.forceMkdirParent(classFile);
						INgComponent<?> modd = (INgComponent<?>) GuiceContext.get(aClass);
						ComponentHierarchyBase chb2 = (ComponentHierarchyBase) modd;
						chb2.addConfiguration(new NgComponentModule());
						FileUtils.write(classFile, renderComponentTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					
				});
		
		scan.getClassesWithAnnotation(NgDirective.class)
		    .stream()
		    .filter(a -> !(a.isInterface() || a.isAbstract()))
		    .forEach(a -> {
			    Class<?> aClass = a.loadClass();
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
		    });
		
		scan.getClassesWithAnnotation(NgDataService.class)
		    .stream()
		    .filter(a -> !(a.isInterface() || a.isAbstract()))
		    .forEach(a -> {
			    Class<?> aClass = a.loadClass();
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
		    });
		
		
		scan.getClassesWithAnnotation(NgProvider.class)
		    .stream()
		    .filter(a -> !(a.isInterface() || a.isAbstract()))
		    .forEach(a -> {
			    Class<?> aClass = a.loadClass();
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
		    });
		
		scan.getClassesWithAnnotation(NgDataType.class)
		    .stream()
		    .filter(a -> !(a.isInterface() || a.isAbstract()))
		    .forEach(a -> {
			    Class<?> aClass = a.loadClass();
			    File classFile = null;
			    classFile = getFile(aClass, ".ts");
			    try
			    {
				    FileUtils.forceMkdirParent(classFile);
				    INgDataType modd = (INgDataType) GuiceContext.get(aClass);
				    FileUtils.write(classFile, renderClassTs(modd, modd.getClass()), UTF_8, false);
			    }
			    catch (IOException e)
			    {
				    e.printStackTrace();
			    }
		    });
		
		
		System.out.println("Installing node-modules...");
		installDependencies(appBaseDirectory);
		System.out.println("Building Angular Client App...");
		installAngular(appBaseDirectory);
		//System.out.println("Starting Local Angular Client...");
		//serveAngular(appBaseDirectory);
		return sb;
	}
	
	public static ScanResult getNgPackageFilterScanResult(Class<?> baseClass, NgApp app)
	{
		Set<String> packages = getAngularPackageScans(baseClass, app);
		
		ClassGraph classGraph = GuiceContext.instance()
		                                    .getScanner()
		                                    .acceptPackages(packages.toArray(new String[]{}));
		ScanResult scan = classGraph.scan();
		return scan;
	}
	
	private StringBuilder renderImports(ITSComponent<?> app, Map<String, String> importsMap, Class<?> requestingClass) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		writeImportsForMap(app, sb, importsMap, requestingClass);
		if (sb.length() > 1)
		{
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("\n");
		return sb;
	}
	
	private void writeImportsForMap(ITSComponent<?> app, StringBuilder sb, Map<String, String> importsMap, Class<?> requestingFromClass) throws IOException
	{
		for (Map.Entry<String, String> entry : importsMap
				.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.equals(app.getClass()
			                  .getSimpleName()))
			{
				continue;
			}
			
			if (!value.startsWith("@"))
			{
				File referenceModuleFile = getFile(value);
				File sourceModuleFile = getFile(requestingFromClass);
				String relPath = getRelativePath(sourceModuleFile.toPath(), referenceModuleFile.toPath(), null);
				sb.append(renderImportStatement(key,
						relPath));
			}
			else if (value.startsWith("!"))
			{
				sb.append((renderImportStatement(key,
						value.substring(1))));
			}
			else
			{
				sb.append((renderImportStatement(key,
						value)));
			}
		}
	}
	
	public static Set<String> getAngularPackageScans(Class<?> baseClass, NgApp ngApp)
	{
		
		Set<String> packages = new HashSet<>(Set.of(ngApp.includePackages()));
		if (packages.isEmpty())
		{
			System.out.println("NgApp " + ngApp.value() + " is using the default package, itself and all children");
		}
		packages.add(baseClass.getPackage()
		                      .getName());
		packages.add(com.jwebmp.core.base.angular.modules.services.angular.NgModule.class.getPackageName());
		
		Set<AngularScanPackages> packageScan = IDefaultService.loaderToSet(ServiceLoader.load(AngularScanPackages.class));
		for (AngularScanPackages angularScanPackages : packageScan)
		{
			packages.addAll(angularScanPackages.packages());
		}
		return packages;
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
	
	/**
	 * Renders the body of .ts file, excluding import statements
	 *
	 * @param component
	 * @return
	 * @throws JsonProcessingException
	 */
	public <T extends INgDataType<T>> StringBuilder renderClassTs(T component, boolean renderImports, Class<?> requestingClass) throws IOException
	{
		StringBuilder out = new StringBuilder();
		NgDataType dt = component.getClass()
		                         .getAnnotation(NgDataType.class);
		
		
		if (renderImports)
		{
			out.append(renderImports(component, component.imports(), requestingClass));
		}
		
		if (!Strings.isNullOrEmpty(component.renderBeforeClass()))
		{
			out.append(";")
			   .append(component.renderBeforeClass());
		}
		
		out.append(dt.exports() ? "export " : "")
		   .append(dt.value()
		             .description())
		   .append(" ")
		   .append(getTsFilename(component.getClass()));
		
		if (!Strings.isNullOrEmpty(component.ofType()))
		{
			out.append(" ")
			   .append(component.ofType());
		}
		
		ObjectMapper om = GuiceContext.get(JavascriptObjectMapper);
		
		
		out.append(om.writerWithDefaultPrettyPrinter()
		             .writeValueAsString(component));
		
		if (!Strings.isNullOrEmpty(component.renderAfterClass()))
		{
			out.append(";")
			   .append(component.renderAfterClass());
		}
		
		return out;
	}
	
	public StringBuilder renderClassTs(ITSComponent<?> configuration, Class<?> requestingClass) throws IOException
	{
		StringBuilder out = new StringBuilder();
		
		
		if (!Strings.isNullOrEmpty(configuration.renderBeforeClass()))
		{
			out.append(";")
			   .append(configuration.renderBeforeClass());
		}
		
		
		for (String globalField : configuration.globalFields())
		{
			out.append(globalField)
			   .append("\n");
		}
		
		for (String decorator : configuration.decorators())
		{
			out.append(decorator)
			   .append("\n");
		}
		
		boolean isDataType = configuration.getClass()
		                                  .isAnnotationPresent(NgDataType.class);
		if (isDataType)
		{
			INgDataType tt = (INgDataType<?>) configuration;
			NgDataType dt = tt.getClass()
			                  .getAnnotation(NgDataType.class);
			if (dt.value()
			      .equals(NgDataType.DataTypeClass.Const))
			{
				return renderClassTs(tt, false, requestingClass);
			}
		}
		//default class object
		out.append("export class ")
		   .append(getTsFilename(configuration.getClass()));
		
		
		if (!Strings.isNullOrEmpty(configuration.ofType()))
		{
			out.append(" ")
			   .append(configuration.ofType());
		}
		
		if (!configuration.interfaces()
		                  .isEmpty())
		{
			StringBuilder sbInterfaces = new StringBuilder();
			sbInterfaces.append(" implements ");
			for (String interf : configuration.interfaces())
			{
				sbInterfaces.append(interf)
				            .append(",");
			}
			sbInterfaces.deleteCharAt(sbInterfaces.length() - 1);
			out.append(sbInterfaces);
		}
		
		out.append("\n");
		out.append("{" + "\n");
		out.append("\n");
		
		
		if (isDataType)
		{
			//load all the fields
			for (Field declaredField : configuration.getClass()
			                                        .getDeclaredFields())
			{
				String fieldName = declaredField.getName();
				String fieldType = null;
				if (Number.class.isAssignableFrom(declaredField.getType()))
				{
					out.append(" public " + fieldName + "? : number = 0;");
				}
				else if (BigDecimal.class.isAssignableFrom(declaredField.getType()))
				{
					out.append(" public " + fieldName + "? : number = 0;");
				}
				else if (BigInteger.class.isAssignableFrom(declaredField.getType()))
				{
					out.append(" public " + fieldName + "? : number = 0;");
				}
				else if (String.class.isAssignableFrom(declaredField.getType()))
				{
					out.append(" public " + fieldName + "? : string = '';");
				}
				else if (Boolean.class.isAssignableFrom(declaredField.getType()))
				{
					out.append(" public " + fieldName + "? : boolean =false;");
				}
				else if (INgDataType.class.isAssignableFrom(declaredField.getType()))
				{
					out.append(" public " + fieldName + "? : " + getTsFilename(declaredField.getType()) + " = {};");
				}
				else if (Collection.class.isAssignableFrom(declaredField.getType()))
				{
					out.append(" public " + fieldName + "? : " + getTsFilename(declaredField.getType()) + " = [];");
				}
				else if (Object.class.isAssignableFrom(declaredField.getType()))
				{
					out.append(" public " + fieldName + "? : any;");
				}
			}
		}
		
		if (INgDataService.class.isAssignableFrom(configuration.getClass()))
		{
			//create data references...
			INgDataService<?> data = (INgDataService<?>) configuration;
			out.append(" public data : " + getTsFilename(data.dataTypeReturned()) + " = {};");
			
			//add ajax data fetch from component
		}
		
		for (String field : configuration.fields())
		{
			out.append(field)
			   .append("\n");
		}
		
		out.append("\n");
		
		out.append("constructor(  ");
		
		StringBuilder constructorParameters = new StringBuilder();
		
		
		if (!(configuration instanceof AngularAppBootModule))
		{
			if (configuration.getClass()
			                 .isAnnotationPresent(NgProviderReferences.class))
			{
				NgProviderReferences annotation = configuration.getClass()
				                                               .getAnnotation(NgProviderReferences.class);
				for (NgProviderReference ngProviderReference : annotation.value())
				{
					String nameCamel = getTsFilename(ngProviderReference.value()).substring(0, 1)
					                                                             .toLowerCase() +
					                   getTsFilename(ngProviderReference.value()).substring(1);
					constructorParameters.append(" public ")
					                     .append(nameCamel)
					                     .append(" : ")
					                     .append(getTsFilename(ngProviderReference.value()))
					                     .append(", ");
				}
			}
			if (configuration.getClass()
			                 .isAnnotationPresent(NgProviderReference.class))
			{
				
				NgProviderReference ngProviderReference = configuration.getClass()
				                                                       .getAnnotation(NgProviderReference.class);
				String nameCamel = getTsFilename(ngProviderReference.value()).substring(0, 1)
				                                                             .toLowerCase() +
				                   getTsFilename(ngProviderReference.value()).substring(1);
				constructorParameters.append(" public ")
				                     .append(nameCamel)
				                     .append(" : ")
				                     .append(getTsFilename(ngProviderReference.value()))
				                     .append(", ");
			}
			
			
			if (configuration.getClass()
			                 .isAnnotationPresent(NgServiceReferences.class))
			{
				NgServiceReferences annotation = configuration.getClass()
				                                              .getAnnotation(NgServiceReferences.class);
				for (NgServiceReference ngServiceReference : annotation.value())
				{
					String nameCamel = getTsFilename(ngServiceReference.value()).substring(0, 1)
					                                                            .toLowerCase() +
					                   getTsFilename(ngServiceReference.value()).substring(1);
					constructorParameters.append(" public ")
					                     .append(nameCamel)
					                     .append(" : ")
					                     .append(getTsFilename(ngServiceReference.value()))
					                     .append(", ");
				}
			}
			if (configuration.getClass()
			                 .isAnnotationPresent(NgServiceReference.class))
			{
				NgServiceReference ngServiceReference = configuration.getClass()
				                                                     .getAnnotation(NgServiceReference.class);
				String nameCamel = getTsFilename(ngServiceReference.value()).substring(0, 1)
				                                                            .toLowerCase() +
				                   getTsFilename(ngServiceReference.value()).substring(1);
				constructorParameters.append(" public ")
				                     .append(nameCamel)
				                     .append(" : ")
				                     .append(getTsFilename(ngServiceReference.value()))
				                     .append(", ");
			}
		}
		
		
		for (String constructorParameter : configuration.constructorParameters())
		{
			constructorParameters.append(constructorParameter + ", ");
		}
		if (constructorParameters.length() > 1)
		{
			constructorParameters.deleteCharAt(constructorParameters.lastIndexOf(", "));
		}
		out.append(constructorParameters);
		
		out.append(")");
		
		out.append("{" + "\n");
		for (String body : configuration.constructorBody())
		{
			out.append(body);
		}
		
		out.append("}" + "\n");
		
		
		for (String method : configuration.methods())
		{
			out.append(method)
			   .append("\n");
		}
		
		out.append("}");
		
		if (!Strings.isNullOrEmpty(configuration.renderAfterClass()))
		{
			out.append(";")
			   .append(configuration.renderAfterClass());
		}
		
		return out;
	}
	
	public StringBuilder renderBootIndexHtml(INgApp<?> app, AngularAppBootModule appBootModule)
	{
		Page<?> p = (Page) app;
		StringBuilder sb = new StringBuilder();
		Body body = p.getBody();
		List<ComponentHierarchyBase> comps = new ArrayList<>(body.getChildren());
		
		body.getChildren()
		    .clear();
		body.add(new DivSimple<>().setTag(appBootModule.getBootModule()
		                                               .getAnnotation(NgComponent.class)
		                                               .value()));
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
	}
	
	public static void serveAngular(File appBaseDirectory)
	{
		if (SystemUtils.IS_OS_WINDOWS)
		{
			try
			{
				ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", FileUtils.getUserDirectory() + "/AppData/Roaming/npm/ng.cmd serve");
				processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
				processBuilder
						.environment()
						.putAll(System.getenv());
				processBuilder = processBuilder.directory(appBaseDirectory);
				Process p = processBuilder.start();
				//p.waitFor();
				Runtime.getRuntime()
				       .addShutdownHook(new Thread()
				       {
					       @Override
					       public void run()
					       {
						       p.destroy();
					       }
				       });
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
