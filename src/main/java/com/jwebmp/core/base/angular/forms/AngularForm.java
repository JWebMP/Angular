package com.jwebmp.core.base.angular.forms;

import com.jwebmp.core.base.angular.modules.services.angular.forms.*;
import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.base.angular.services.annotations.structures.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.html.*;

import java.util.*;
import java.util.List;

@NgBootModuleImport("FormsModule")
@NgImportReference(name = "AfterViewInit", reference = "@angular/core")
@NgImportReference(name = "FormBuilder, FormGroup, Validators, FormControl, FormArray, ControlValueAccessor ", reference = "@angular/forms")
@NgConstructorParameter("private formBuilder : FormBuilder")
public class AngularForm<J extends AngularForm<J>> extends Form<J>
		implements INgComponent<J>
{
	private FormDataService<?> formDataService;
	
	AngularForm()
	{
	
	}
	
	public AngularForm(String id, FormDataService<?> formDataService)
	{
		setID(id);
		this.formDataService = formDataService;
	}
	
	@Override
	public void init()
	{
		addAttribute("#" + getID(), "ngForm");
		super.init();
	}
	
	public List<String> methods()
	{
		if (formDataService == null)
		{
			return List.of();
		}
		List<String> output = new ArrayList<>();
		String name = getServiceName();
		output.add(
				"onSubmit() {\n" +
				" this." + name + ".sendData(this.data);  \n" +
				"}\n" +
				"" +
				""
		);
		return output;
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
	public List<NgComponentReference> getComponentReferences()
	{
		return List.of(getNgComponentReference((Class<? extends ITSComponent<?>>) formDataService.getClass()));
	}
	
	@Override
	public List<String> componentFields()
	{
		return List.of("    data?: any = {};\n" +
		               "    private updated: boolean = false;");
	}
	
	@Override
	public List<String> componentMethods()
	{
		if (formDataService != null)
		{
			String name = getServiceName();
			return List.of("ngAfterViewInit(): void {\n" +
			               "        this." + name + ".data.subscribe((dd) => {\n" +
			               "            this.data = dd;\n" +
			               "            this.updated = true;\n" +
			               "        });\n" +
			               "    }");
		}
		else
		{
			return List.of();
		}
	}
	
	@Override
	public List<String> componentInterfaces()
	{
		return List.of("AfterViewInit");
	}
	
	@Override
	public List<String> componentConstructorParameters()
	{
		List<String> out = new ArrayList<>();
		if (formDataService != null)
		{
			out.add("public " + getServiceName() + " : " + formDataService.getClass()
			                                                              .getSimpleName());
		}
		return out;
	}
	
}
