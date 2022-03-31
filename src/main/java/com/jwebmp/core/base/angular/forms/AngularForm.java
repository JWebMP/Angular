package com.jwebmp.core.base.angular.forms;

import com.jwebmp.core.base.angular.modules.services.angular.forms.*;
import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.html.*;

import java.util.List;

@NgBootModuleImport("FormsModule")
public class AngularForm<J extends AngularForm<J>> extends Form<J>
		implements INgComponent<J>
{
	private FormDataService<?> formDataService;
	
	public AngularForm(FormDataService<?> formDataService)
	{
		this.formDataService = formDataService;
	}
	
	@Override
	public void init()
	{
		addAttribute("#" + getID(), "ngForm");
		super.init();
	}
	
	public FormDataService<?> getFormDataService()
	{
		return formDataService;
	}
	
	public String getServiceName()
	{
		if (formDataService == null)
		{
			return "formDataService";
		}
		String name = ITSComponent.getTsFilename(formDataService.getClass());
		name = name.substring(0, 1)
		           .toLowerCase() + name.substring(1);
		return name;
	}
	
	@Override
	public java.util.List<String> methods()
	{
		if (formDataService != null)
		{
			String name = getServiceName();
			return java.util.List.of("onSubmit() {\n" +
			                         "  \n" +
			                         "}\n" +
			                         "" +
			                         ""
			);
		}
		else
		{
			return List.of();
		}
	}
}
