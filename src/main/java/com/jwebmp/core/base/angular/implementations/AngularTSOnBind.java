package com.jwebmp.core.base.angular.implementations;

import com.jwebmp.core.base.html.Input;
import com.jwebmp.core.base.html.Paragraph;
import com.jwebmp.core.base.interfaces.IComponentDataBindingBase;
import com.jwebmp.core.base.interfaces.IComponentHTMLAttributeBase;
import com.jwebmp.core.databind.IOnDataBind;
import jakarta.validation.constraints.NotNull;

public class AngularTSOnBind
        implements IOnDataBind<AngularTSOnBind>
{
    @Override
    public void onBind(@NotNull IComponentDataBindingBase<?> component, String bindingValue)
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
            IComponentHTMLAttributeBase.class.cast(component)
                                             .addAttribute("[(ngModel)]", bindingValue);
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
        input.addAttribute("#" + input.getID(), "ngModel");
    }
}
