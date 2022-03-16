package com.jwebmp.core.base.angular.implementations;

import com.jwebmp.core.base.*;
import com.jwebmp.core.base.html.*;
import com.jwebmp.core.databind.*;
import jakarta.validation.constraints.*;

public class AngularTSOnBind
		implements IOnDataBind<AngularTSOnBind>
{
	@Override
	public void onBind(@NotNull ComponentDataBindingBase component, String bindingValue)
	{
		if (Paragraph.class.isAssignableFrom(component.getClass()))
		{
			configureForParagraph((Paragraph) component, bindingValue);
		}
		else if (Input.class.isAssignableFrom(component.getClass()))
		{
			configureForInput((Input) component, bindingValue);
		}
		else
		{
			component.addAttribute("[(ngModel)]", bindingValue);
		}
	}
	
	private void configureForParagraph(Paragraph paragraph, String bindingValue)
	{
		if (bindingValue.contains("{{"))
		{
			paragraph.setText(paragraph.getText(0) + bindingValue);
		}
		else
		{
			paragraph.setText(paragraph.getText(0) + "{{" + bindingValue + "}}");
		}
	}
	
	private void configureForInput(Input input, String bindingValue)
	{
		
		if (bindingValue != null)
		{
			input.addAttribute("[(ngModel)]", bindingValue);
			if (input.getAttribute("name") == null)
			{
				input.addAttribute("name", input.getID());
			}
		}
		else
		{
			input.removeAttribute("[(ngModel)]");
		}
	}
}
