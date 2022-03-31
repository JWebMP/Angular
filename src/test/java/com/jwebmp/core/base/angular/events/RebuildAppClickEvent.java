package com.jwebmp.core.base.angular.events;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.AngularApp;
import com.jwebmp.core.base.angular.services.compiler.JWebMPTypeScriptCompiler;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.events.click.ClickAdapter;

import java.io.IOException;

public class RebuildAppClickEvent extends ClickAdapter<RebuildAppClickEvent>
{
	public RebuildAppClickEvent()
	{
	}
	
	public RebuildAppClickEvent(IComponentHierarchyBase<?, ?> component)
	{
		super(component);
	}
	
	@Override
	public void onClick(AjaxCall<?> call, AjaxResponse<?> response)
	{
		try
		{
			new JWebMPTypeScriptCompiler(new AngularApp()).renderAppTS();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		response.addReaction(new AjaxResponseReaction<>("/", ReactionType.RedirectUrl));
	}

}
