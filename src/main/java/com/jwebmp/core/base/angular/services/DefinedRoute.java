package com.jwebmp.core.base.angular.services;

import com.fasterxml.jackson.annotation.*;
import com.guicedee.guicedinjection.representations.IJsonRepresentation;
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
@JsonPropertyOrder({"component","path","redirectTo","pathMatch","children"})
public class DefinedRoute<J extends DefinedRoute<J>> implements IJsonRepresentation<J> {
    private String path;
    @JsonRawValue
    @JsonProperty("component")
    private String componentName;
    
    private String redirectTo;
    
    private String pathMatch;
    
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
    private Class<? extends INgComponent<?>> component;

    private List<DefinedRoute<?>> children;

    public List<DefinedRoute<?>> getChildren() {
        if (children == null) {
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

    public String getPath() {
        return path;
    }

    @SuppressWarnings("unchecked")
    public J setPath(String path) {
        this.path = path;
        return (J) this;
    }

    public String getComponentName() {
        return componentName;
    }

    @SuppressWarnings("unchecked")
    public J setComponentName(String componentName) {
        this.componentName = componentName;
        return (J) this;
    }

    public Class<? extends INgComponent<?>> getComponent() {
        return component;
    }

    @SuppressWarnings("unchecked")
    public J setComponent(Class<? extends INgComponent<?>> component) {
        this.component = component;
        return (J) this;
    }
}
