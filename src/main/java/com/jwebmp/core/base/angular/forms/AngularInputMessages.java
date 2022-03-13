/*
 * Copyright (C) 2017 GedMarc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jwebmp.core.base.angular.forms;

import com.guicedee.guicedinjection.json.StaticStrings;
import com.jwebmp.core.base.ComponentHierarchyBase;
import com.jwebmp.core.base.angular.AngularAttributes;
import com.jwebmp.core.base.angular.forms.enumerations.InputErrorValidations;
import com.jwebmp.core.base.html.*;
import com.jwebmp.core.base.html.attributes.GlobalAttributes;
import jakarta.validation.constraints.NotNull;

import static com.guicedee.guicedinjection.json.StaticStrings.*;
import static com.jwebmp.core.base.angular.AngularAttributes.*;

/**
 * Default angular messages div
 * <p>
 * https://docs.angularjs.org/api/ngMessages/directive/ngMessages
 *
 * @param <J>
 */
@SuppressWarnings("MissingClassJavaDoc")
public class AngularInputMessages<J extends AngularInputMessages<J>>
		extends DivSimple<J>
{
	private final Form<?> formToApply;
	private final Input<?, ?> formInputName;
	
	/**
	 * Constructs a new angular block for the given inputFields and Names
	 *
	 * @param formToApply   The form to apply to
	 * @param formInputName The name applied to the input field (attribute name)
	 */
	public AngularInputMessages(Form<?> formToApply, Input<?, ?> formInputName)
	{
		this.formToApply = formToApply;
		this.formInputName = formInputName;
		addAttribute(AngularAttributes.ngMessages.getAttributeName(),
		             formToApply.getFormID() + StaticStrings.STRING_DOT + formInputName.getAttribute(GlobalAttributes.Name) + StaticStrings.STRING_DOT + "$error");
		addAttribute("role", "alert");
		setRenderIDAttribute(false);
	}
	
	/**
	 * Adds a message for the given error form
	 *
	 * @param message The message to display
	 * @param inline  If must be a div or span, in line or next line
	 * @return J
	 */
	@SuppressWarnings({"unchecked", "WeakerAccess"})
	@NotNull
	public J addMessageDefault(String message, boolean inline)
	{
		ComponentHierarchyBase comp;
		if (inline)
		{
			comp = new Span<>();
		}
		else
		{
			comp = new Div<>();
		}
		comp.setRenderIDAttribute(false)
		    .addAttribute(ngMessageDefault, STRING_EMPTY);
		comp.setText(message);
		add(comp);
		return (J) this;
	}
	
	/**
	 * Shows after a form field has been edited, instead of on open
	 *
	 * @param showOnEdit
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public J setShowOnEdit(boolean showOnEdit)
	{
		if (showOnEdit)
		{ addAttribute(ngShow.getAttributeName(), formToApply.getID() + StaticStrings.STRING_DOT + formInputName.getAttribute(GlobalAttributes.Name) + StaticStrings.STRING_DOT + "$dirty"); }
		else
		{ removeAttribute(ngShow.getAttributeName()); }
		return (J) this;
	}
	
	/**
	 * Adds a message for the given error form
	 *
	 * @param forError The error type the message is for
	 * @param message  The message or raw html to display
	 * @return J
	 */
	@SuppressWarnings("unchecked")
	@NotNull
	public J addMessage(@NotNull InputErrorValidations forError, String message, boolean inline)
	{
		ComponentHierarchyBase comp;
		if (inline)
		{
			comp = new Span<>();
		}
		else
		{
			comp = new Div<>();
		}
		comp.setRenderIDAttribute(false)
		    .addAttribute(ngMessage, forError.toString());
		comp.setText(message);
		add(comp);
		if (getChildren().size() > 1 && getAttribute(ngMessagesMultiple.getAttributeName()) == null)
		{
			addAttribute(ngMessagesMultiple.getAttributeName(),STRING_EMPTY);
		}
		return (J) this;
	}
}
