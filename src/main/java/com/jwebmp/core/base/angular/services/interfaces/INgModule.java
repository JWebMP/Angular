package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.databind.*;

import java.util.*;

@NgSourceDirectoryReference()
public interface INgModule<J extends INgModule<J>>
        extends ITSComponent<J>, IConfiguration {
    default List<String> renderBeforeNgModuleDecorator() {
        return List.of();
    }

    default List<String> declarations() {
        return new ArrayList<>();
    }

    default List<String> providers() {
        return List.of();
    }

    default List<String> bootstrap() {
        return new ArrayList<>();
    }

    default List<String> assets() {
        return new ArrayList<>();
    }

    default List<String> exports() {
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    default J setApp(INgApp<?> app) {
        return (J) this;
    }

    default List<String> moduleImports() {
        return new ArrayList<>();
    }

    default List<String> schemas() {
        return new ArrayList<>();
    }
}
