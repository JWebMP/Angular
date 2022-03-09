package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.*;
import com.jwebmp.core.databind.*;

import java.io.*;
import java.util.*;

public interface INgDirective<J extends INgDirective<J>> extends ITSComponent<J>
{
	default List<String> declarations()
	{
		Set<String> out = new HashSet<>();
		out.add(getClass().getSimpleName());
		return new ArrayList<>(out);
	}
	
	default Set<String> styleUrls()
	{
		return new HashSet<>();
	}
	
	default Set<String> providers()
	{
		return Set.of();
	}
	
}
