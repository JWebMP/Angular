package com.jwebmp.core.base.angular.modules.services.base;

import com.fasterxml.jackson.annotation.*;
import com.google.inject.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import static com.jwebmp.core.base.angular.services.annotations.NgSourceDirectoryReference.SourceDirectories.*;

@NgSourceDirectoryReference(value = Environment)
@Singleton
@NgDataType(value = NgDataType.DataTypeClass.Const)
public class EnvironmentModule implements INgDataType<EnvironmentModule>
{
	private EnvironmentOptions<?> environmentOptions;
	
	@Override
	public String ofType()
	{
		return " = ";
	}
	
	public EnvironmentModule()
	{
		environmentOptions = new EnvironmentOptions<>();
	}
	
	@JsonValue
	public EnvironmentOptions<?> getEnvironmentOptions()
	{
		return environmentOptions;
	}
	
	public EnvironmentModule setEnvironmentOptions(EnvironmentOptions<?> environmentOptions)
	{
		this.environmentOptions = environmentOptions;
		return this;
	}
	
	@Override
	public String renderBeforeClass()
	{
		if (!environmentOptions.isProduction())
		{
			return "import 'zone.js/dist/zone-error';  ";
		}
		return "";
	}
}
