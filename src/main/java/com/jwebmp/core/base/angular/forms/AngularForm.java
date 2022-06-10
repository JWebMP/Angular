package com.jwebmp.core.base.angular.forms;

import com.jwebmp.core.base.angular.client.annotations.boot.*;
import com.jwebmp.core.base.angular.client.annotations.components.*;
import com.jwebmp.core.base.angular.client.annotations.constructors.*;
import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.client.annotations.structures.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.html.*;

import java.util.List;

import static com.jwebmp.core.base.angular.client.services.AnnotationsMap.*;
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
		
		StringBuilder sendDataString = new StringBuilder();
		
		sendDataString.append("onSubmit() {\n");
		
		List<NgInput> inputList = getAnnotations(getClass(), NgInput.class);
		StringBuilder inputs = new StringBuilder();
		for (NgInput ngInput : inputList)
		{
			if (ngInput.additionalData())
			{
				inputs.append("\t\tthis." + getFormDataProvider().getAnnotation()
				                                                 .referenceName() +
				              ".additionalData." + ngInput.value() + " = this." + ngInput.value() + ";\n");
			}
		}
		sendDataString.append(inputs.toString());
		
		
		sendDataString.append("" +
		                      "" +
		                      "" +
		                      "" + " this." + getFormDataProvider().getAnnotation()
		                                                           .referenceName() +
		                      ".sendData(this." + getFormDataProvider().getAnnotation()
		                                                               .referenceName() + "." + getFormDataProvider().getAnnotation()
		                                                                                                             .variableName() + ");  \n" + "}\n");
		out.add(sendDataString.toString());
		
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
