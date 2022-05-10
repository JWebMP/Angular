package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.databind.*;
import com.jwebmp.core.htmlbuilder.css.composer.*;
import org.apache.commons.io.*;

import java.io.*;
import java.util.*;

import static com.jwebmp.core.base.angular.services.compiler.AnnotationsMap.*;
import static com.jwebmp.core.base.angular.services.compiler.JWebMPTypeScriptCompiler.*;
import static com.jwebmp.core.base.angular.services.interfaces.ITSComponent.*;
import static java.nio.charset.StandardCharsets.*;

@NgImportReference(name = "Component", reference = "@angular/core")
public interface INgComponent<J extends INgComponent<J>>
		extends ITSComponent<J>, IConfiguration
{
	default List<String> componentDecorators()
	{
		List<String> list = ITSComponent.super.componentDecorators();
		if (list == null)
		{
			list = new ArrayList<>();
		}
		StringBuilder selector = new StringBuilder();
		StringBuilder template = new StringBuilder();
		StringBuilder styles = new StringBuilder();
		StringBuilder styleUrls = new StringBuilder();
		StringBuilder viewProviders = new StringBuilder();
		StringBuilder animations = new StringBuilder();
		StringBuilder providers = new StringBuilder();
		StringBuilder hosts = new StringBuilder();
		
		if (!getClass().isAnnotationPresent(NgComponent.class))
		{
			System.out.println("This one doesn't have a ng component");
		}
		
		NgComponent ngComponent = getAnnotations(getClass(), NgComponent.class).get(0);
		
		ComponentHierarchyBase chb = (ComponentHierarchyBase) this;
		selector.append(ngComponent.value());
		
		StringBuilder templateUrls = new StringBuilder();
		String templateHtml = chb.toString(0);
		
		templateUrls.append("./")
		            .append(getTsFilename(getClass()))
		            .append(".html");
		File htmlFile = getFile(getClass(), ".html");
		try
		{
			FileUtils.writeStringToFile(htmlFile, templateHtml, UTF_8);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		styleUrls.append("'./")
		         .append(getTsFilename(getClass()))
		         .append(".css")
		         .append("',\n");
		for (String styleUrl : styleUrls())
		{
			styleUrls.append("'")
			         .append(styleUrl)
			         .append("',\n");
		}
		if (styleUrls.length() > 0)
		{
			styleUrls.deleteCharAt(styleUrls.length() - 2);
		}
		
		for (String style : styles())
		{
			styles.append("'")
			      .append(style)
			      .append("',\n");
		}
		if (styles.length() > 0)
		{
			styles.deleteCharAt(styles.length() - 2);
		}
		
		CSSComposer cssComposer = new CSSComposer();
		cssComposer.addComponent(chb);
		//styles.append("\"" + cssComposer.toString() + "\"");
		File cssFile = getFile(getClass(), ".css");
		try
		{
			FileUtils.writeStringToFile(cssFile, cssComposer.toString(), UTF_8);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		providers()
				.forEach((key) -> {
					providers.append(key)
					         .append(",")
					         .append("\n");
				});
		
		List<NgImportProvider> refs = getAnnotations(getClass(), NgImportProvider.class);
		for (NgImportProvider ref : refs)
		{
			providers.append(ref.value())
			         .append(",")
			         .append("\n");
		}
		List<NgComponentReference> compRefs = getAnnotations(getClass(), NgComponentReference.class);
		for (NgComponentReference compRef : compRefs)
		{
			if (compRef.provides())
			{
				providers.append(getTsFilename(compRef.value()))
				         .append(",")
				         .append("\n");
			}
		}
		
		if (providers.length() > 1)
		{
			providers.deleteCharAt(providers.length() - 2);
		}
		
		if(!host().isEmpty())
		for (String s : host())
		{
			hosts.append(s);
		}
		else hosts.append("{}");
		
		String componentString = String.format(ITSComponent.componentString, selector, templateUrls, styles, styleUrls,
				"", //viewProviders
				"", //Animations
				providers, //Directive Providers,
				hosts //hosts entry
		);
		
		list.add(componentString);
		return list;
	}
	
	@Override
	default List<String> constructorParameters()
	{
		List<String> parms = new ArrayList<>();
		List<NgComponentReference> compRefs = getAnnotations(getClass(), NgComponentReference.class);
		for (NgComponentReference compRef : compRefs)
		{
			if (compRef.provides() && compRef.onSelf())
			{
				parms.add("public " + getTsVarName(compRef.value()) + " : " + getTsFilename(compRef.value()) + "");
			}
		}
		return parms;
	}
	
	default List<String> styleUrls()
	{
		return List.of();
	}
	
	default List<String> styles()
	{
		return List.of();
	}
	
	default List<String> animations()
	{
		return List.of();
	}
	
	default List<String> providers()
	{
		return List.of();
	}
	
	default List<String> inputs()
	{
		return List.of();
	}
	
	default List<String> outputs()
	{
		return List.of();
	}
	
	default List<String> host()
	{
		return List.of();
	}
	
	default File getFile(Class<?> clazz, String... extension)
	{
		String baseDir = getFileReference(getCurrentAppFile().get()
		                                                     .getPath(), clazz, extension);
		File file = new File(baseDir);
		return file;
	}
	
}
