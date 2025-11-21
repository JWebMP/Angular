package com.jwebmp.core.base.angular.services;

import com.fasterxml.jackson.annotation.*;
import com.guicedee.services.jsonrepresentation.IJsonRepresentation;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@JsonAutoDetect(fieldVisibility = ANY,
        getterVisibility = NONE,
        setterVisibility = NONE)
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefinedRoute<J extends DefinedRoute<J>> implements IJsonRepresentation<J>
{
    private String path;

    private String componentName;

    private String redirectTo;

    private String pathMatch;

    @JsonIgnore
    private boolean renderComponent;

    public String getRedirectTo()
    {
        return redirectTo;
    }

    public DefinedRoute<J> setRedirectTo(String redirectTo)
    {
        this.redirectTo = redirectTo;
        return this;
    }

    public String getPathMatch()
    {
        return pathMatch;
    }

    public DefinedRoute<J> setPathMatch(String pathMatch)
    {
        this.pathMatch = pathMatch;
        return this;
    }

    @JsonIgnore
    private Class<? extends IComponent<?>> component;

    private List<DefinedRoute<?>> children;

    public List<DefinedRoute<?>> getChildren()
    {
        if (children == null)
        {
            children = new ArrayList<>();
        }
        return children;
    }

    @SuppressWarnings("unchecked")
    public J addChild(J route)
    {
        getChildren().add(route);
        return (J) this;
    }

    public String getPath()
    {
        return path;
    }

    @SuppressWarnings("unchecked")
    public @org.jspecify.annotations.NonNull J setPath(String path)
    {
        this.path = path;
        return (J) this;
    }

    @JsonRawValue
    @JsonProperty("component")
    public String getComponentName()
    {
        if (isRenderComponent())
        {
            return componentName;
        }
        else
        {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public @org.jspecify.annotations.NonNull J setComponentName(String componentName)
    {
        this.componentName = componentName;
        return (J) this;
    }

    public Class<? extends IComponent<?>> getComponent()
    {
        return component;
    }

    @SuppressWarnings("unchecked")
    public @org.jspecify.annotations.NonNull J setComponent(Class<? extends IComponent<?>> component)
    {
        this.component = component;
        return (J) this;
    }

    public boolean isRenderComponent()
    {
        return renderComponent;
    }

    public DefinedRoute<J> setRenderComponent(boolean renderComponent)
    {
        this.renderComponent = renderComponent;
        return this;
    }

    public DefinedRoute<J> setChildren(List<DefinedRoute<?>> children)
    {
        this.children = children;
        return this;
    }
}
