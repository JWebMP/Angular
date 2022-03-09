package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.interfaces.*;
import com.jwebmp.core.events.click.*;

public class ServerClickEvent extends ClickAdapter<ServerClickEvent>
{
	public ServerClickEvent()
	{
	}
	
	public ServerClickEvent(IComponentHierarchyBase<?, ?> component)
	{
		super(component);
	}
	
	@Override
	public void onClick(AjaxCall<?> call, AjaxResponse<?> response)
	{
		System.out.println("Clicked!");
		response.addReaction(new AjaxResponseReaction<>("/products", ReactionType.RedirectUrl));
		
	}
}
