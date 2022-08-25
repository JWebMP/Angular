package com.jwebmp.core.base.angular.services;

import com.guicedee.guicedinjection.interfaces.*;

import java.util.*;

public interface RenderedAssets<J extends RenderedAssets<J>> extends IDefaultService<J>
{
	Set<String> assets();
}
