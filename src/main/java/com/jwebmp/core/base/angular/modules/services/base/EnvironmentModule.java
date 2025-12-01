package com.jwebmp.core.base.angular.modules.services.base;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgDataType;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDataType;

import static com.guicedee.client.implementations.ObjectBinderKeys.DefaultObjectMapper;

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
        ObjectMapper om = IGuiceContext.get(DefaultObjectMapper);
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
}
