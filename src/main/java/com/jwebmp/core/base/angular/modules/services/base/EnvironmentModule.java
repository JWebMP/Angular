package com.jwebmp.core.base.angular.modules.services.base;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.google.inject.*;
import com.guicedee.guicedinjection.*;

import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;

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
	public StringBuilder renderClassBody()
	{
		StringBuilder sb = new StringBuilder();
		ObjectMapper om = GuiceContext.get(DefaultObjectMapper);
		try
		{
			sb.append(om.writerWithDefaultPrettyPrinter()
			            .writeValueAsString(environmentOptions));
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}
		return sb;
	}
	
	@Override
	public String renderBeforeClass()
	{
		if (!environmentOptions.isProduction())
		{
			return "import 'zone.js/dist/zone-error';\n";
		}
		return "";
	}
}
