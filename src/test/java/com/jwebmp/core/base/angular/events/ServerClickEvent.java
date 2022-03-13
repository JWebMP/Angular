package com.jwebmp.core.base.angular.events;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.data.DataComponentData;
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
		response.addReaction(new AjaxResponseReaction<>("/products", ReactionType.RedirectUrl));
		response.addDataResponse("updateDataComponent", new DataComponentData().setName("This was updated from an event!"));
	}

}
