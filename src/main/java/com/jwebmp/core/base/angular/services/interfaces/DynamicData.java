package com.jwebmp.core.base.angular.services.interfaces;

import com.fasterxml.jackson.annotation.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
                getterVisibility = JsonAutoDetect.Visibility.NONE,
                setterVisibility = JsonAutoDetect.Visibility.NONE)
@NgDataType
public final class DynamicData implements INgDataType<DynamicData>
{
	@Override
	public List<String> fields()
	{
		return List.of("public out? : any[] = [];");
	}
	
	private List<INgDataType<?>> out = new ArrayList<>();
	
	public DynamicData addData(INgDataType<?>...out)
	{
		this.out.addAll(Arrays.asList(out));
		return this;
	}
	
}
