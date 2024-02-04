package com.jwebmp.core.base.angular;

import com.guicedee.services.jsonrepresentation.IJsonRepresentation;
import com.jwebmp.core.base.angular.client.annotations.angular.NgDataType;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDataType;

@NgDataType
public class DataComponentData
		implements INgDataType<DataComponentData>, IJsonRepresentation<DataComponentData>
{
	private String name = "";
	public DataComponentData setName(String name)
	{
		this.name = name;
		return this;
	}
}
