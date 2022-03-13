package com.jwebmp.core.base.angular.data;

import com.guicedee.guicedinjection.representations.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

@NgDataType
public class DataComponentData
		implements INgDataType<DataComponentData>, IJsonRepresentation<DataComponentData>
{
	private String name = "";
	
	public String getName()
	{
		return name;
	}
	
	public DataComponentData setName(String name)
	{
		this.name = name;
		return this;
	}
}
