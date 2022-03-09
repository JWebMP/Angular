package com.jwebmp.core.base.angular.modules.listeners;

import com.jwebmp.core.*;
import com.jwebmp.core.events.click.*;

public class OnClickListener implements IOnClickService<OnClickListener>
{
	@Override
	public void onCreate(Event<?, ?> e)
	{
		if (e.getComponent() != null)
		{
			e.getComponent()
			 .asAttributeBase()
			 .addAttribute("className", e.getClass()
			                             .getCanonicalName());
		}
	}
	
	@Override
	public void onCall(Event<?, ?> e)
	{
	
	}
}
