package com.jwebmp.core.base.angular.services;

import com.jwebmp.core.*;
import com.jwebmp.core.base.angular.modules.services.base.*;
import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.base.angular.services.compiler.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.html.*;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.*;

@NgImportReference(name = "platformBrowserDynamic", reference = "@angular/platform-browser-dynamic")
@NgImportReference(name = "enableProdMode", reference = "@angular/core")
@NgComponentReference(EnvironmentModule.class)
@NgComponentReference(AngularAppBootModule.class)
public class NGApplication<J extends NGApplication<J>> extends Page<J> implements ITSComponent<J>, INgApp<J>
{
	private List<String> renderAfterImports;
	
	public NGApplication()
	{
		getHead()
				.add(new Meta(Meta.MetadataFields.Charset, "utf-8"));
		getHead()
				.add(new Meta(Meta.MetadataFields.ViewPort, "width=device-width, initial-scale=1"));
		getOptions().setBase(new Base<>("/"));
	}
	
	public List<String> getRenderAfterImports()
	{
		if (renderAfterImports == null)
		{
			renderAfterImports = new ArrayList<>();
		}
		return renderAfterImports;
	}
	
	public NGApplication setRenderAfterImports(List<String> renderAfterImports)
	{
		this.renderAfterImports = renderAfterImports;
		return this;
	}
	
	@Override
	public void putRelativeLinkInMap(Class<?> clazz, Map<String, String> out, NgComponentReference moduleRef)
	{
		var baseDir = JWebMPTypeScriptCompiler.getCurrentAppFile();
		try
		{
			File me = new File(baseDir.get().getCanonicalPath().replace('\\','/') + "/src");
			File destination = new File(getFileReference(baseDir.get()
			                                                    .getCanonicalPath(), moduleRef.value()));
			out.putIfAbsent(ITSComponent.getTsFilename(moduleRef.value()), ITSComponent.getRelativePath(me, destination, null));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
