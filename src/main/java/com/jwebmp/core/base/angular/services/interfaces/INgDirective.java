package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.references.*;

import java.util.*;

import static com.jwebmp.core.base.angular.services.compiler.AnnotationsMap.*;

@NgImportReference(name = "Directive", reference = "@angular/core")
public interface INgDirective<J extends INgDirective<J>> extends ITSComponent<J>
{
	default List<String> declarations()
	{
		Set<String> out = new HashSet<>();
		out.add(getClass().getSimpleName());
		return new ArrayList<>(out);
	}
	
	@Override
	default List<String> componentDecorators()
	{
		List<String> list =ITSComponent.super.componentDecorators();
		if (list == null)
		{
			list = new ArrayList<>();
		}
		StringBuilder selector = new StringBuilder();
		StringBuilder styles = new StringBuilder();
		StringBuilder template = new StringBuilder();
		StringBuilder styleUrls = new StringBuilder();
		StringBuilder providers = new StringBuilder();
		
		NgDirective ngComponent = getAnnotations(getClass(), NgDirective.class).get(0);
		
		selector.append(ngComponent.selector());
		
		providers()
				.forEach((key) -> {
					providers.append(key)
					         .append(",")
					         .append("\n");
				});
		if (!providers()
				.isEmpty())
		{
			providers.deleteCharAt(providers.length() - 2);
		}
		
		String componentString = String.format(ITSComponent.directiveString, selector, providers);
		list.add(componentString);
		return list;
	}
	
	default List<String> styleUrls()
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
}
