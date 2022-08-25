package com.jwebmp.core.base.angular.modules.services.angular;

public class RoutingModuleOptions
{
	private SameUrlNavigationType onSameUrlNavigation;
	
	
	public SameUrlNavigationType getOnSameUrlNavigation()
	{
		return onSameUrlNavigation;
	}
	
	public RoutingModuleOptions setOnSameUrlNavigation(SameUrlNavigationType onSameUrlNavigation)
	{
		this.onSameUrlNavigation = onSameUrlNavigation;
		return this;
	}
}
