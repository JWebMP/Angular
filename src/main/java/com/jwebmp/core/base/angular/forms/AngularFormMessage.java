package com.jwebmp.core.base.angular.forms;

import com.jwebmp.core.base.angular.forms.enumerations.*;
import com.jwebmp.core.base.html.*;

public class AngularFormMessage<J extends AngularFormMessage<J>> extends DivSimple<J>
{
	private final InputErrorValidations validations;
	private final String messageString;
	private String fieldName;
	private InputErrorValidations error;
	private String errorMessage;
	
	public AngularFormMessage(InputErrorValidations validations, String messageString, String fieldName)
	{
		this.validations = validations;
		this.messageString = messageString;
		this.fieldName = fieldName;
	}
	
	public InputErrorValidations getError()
	{
		return error;
	}
	
	public J setError(InputErrorValidations error)
	{
		this.error = error;
		return (J) this;
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}
	
	public J setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
		return (J) this;
	}
	
	@Override
	public void init()
	{
		addAttribute("*ngIf", "" + fieldName + ".errors?.['" + error.toString() + "']");
		add(new Paragraph<>().setText(messageString)
		                     .setTextOnly(true));
		super.init();
	}
}
