package com.jwebmp.core.base.angular.services;

import com.guicedee.guicedinjection.*;
import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.html.interfaces.*;
import com.jwebmp.core.base.html.interfaces.events.*;
import com.jwebmp.core.base.interfaces.*;
import com.jwebmp.core.htmlbuilder.javascript.events.commandevent.*;
import com.jwebmp.core.services.*;

/**
 * Fires the on connect method in page, configured like any other event
 */
public class FirePageOnConnectEvent extends OnComponentLoadedEvent<FirePageOnConnectEvent>
		implements BodyEvents<GlobalFeatures, FirePageOnConnectEvent>
{
	
	public FirePageOnConnectEvent()
	{
		this(null);
	}
	
	public FirePageOnConnectEvent(IComponentHierarchyBase<?, ?> component)
	{
		super(component);
		returnVariable("jw.pageClass");
	}
	
	@Override
	public void perform(AjaxCall<?> call, AjaxResponse<?> response)
	{
		IPage<?> page = GuiceContext.get(IPage.class);
		page.onConnect(call, response);
	}
}
