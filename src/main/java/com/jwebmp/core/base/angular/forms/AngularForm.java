package com.jwebmp.core.base.angular.forms;

import com.jwebmp.core.base.angular.client.annotations.boot.*;
import com.jwebmp.core.base.angular.client.annotations.constructors.*;
import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.client.annotations.structures.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.html.*;

import java.util.List;

import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.*;

@NgBootModuleImport("FormsModule")
@NgComponentReference(FormRegexProvider.class)
@NgImportReference(value = "FormBuilder, FormGroup, Validators, FormControl, FormArray, ControlValueAccessor ", reference = "@angular/forms")
@NgConstructorParameter("private formBuilder : FormBuilder")
@NgField("regex = FormRegexProvider;")

public class AngularForm<J extends AngularForm<J>> extends Form<J> implements INgComponent<J>
{
	private INgServiceProvider<?> formDataProvider;
	
	AngularForm()
	{
	
	}
	
	@Override
	public List<String> afterViewInit()
	{
		List<String> out = INgComponent.super.afterViewInit();
		return out;
	}
	
	public AngularForm(String id, INgServiceProvider<?> formDataProvider)
	{
		setID(id);
		this.formDataProvider = formDataProvider;
	}
	
	@Override
	public void init()
	{
		addAttribute("#" + getID(), "ngForm");
		super.init();
	}
	
	@Override
	public List<String> componentMethods()
	{
		List<String> out = INgComponent.super.componentMethods();
		if (formDataProvider == null)
		{
			return out;
		}
		out.add("onSubmit() {\n" + " this." + getFormDataProvider().getAnnotation()
		                                                           .referenceName() +
		        ".sendData(this." + getFormDataProvider().getAnnotation()
		                                                 .referenceName() + "." + getFormDataProvider().getAnnotation()
		                                                                                               .variableName()  + ");  \n" + "}\n");
		
		return out;
	}
	
	public INgServiceProvider<?> getFormDataProvider()
	{
		return formDataProvider;
	}
	
	public String getServiceName()
	{
		if (formDataProvider == null)
		{
			return "formDataProvider";
		}
		return getFormDataProvider().getAnnotation()
		                            .referenceName();
	}
	
	@Override
	public List<NgComponentReference> getComponentReferences()
	{
		List<NgComponentReference> out = INgComponent.super.getComponentReferences();
		out.add(getNgComponentReference((Class<? extends IComponent<?>>) formDataProvider.getClass()));
		return out;
	}
	
}
