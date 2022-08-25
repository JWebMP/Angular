package com.jwebmp.core.base.angular.modules.services.angular;

public enum SameUrlNavigationType
{
	Reload,
	Ignore,
	
	;
	
	@Override
	public String toString()
	{
		return name().toLowerCase();
	}
}
