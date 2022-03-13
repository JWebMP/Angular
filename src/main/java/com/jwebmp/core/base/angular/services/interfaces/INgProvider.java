package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.services.annotations.*;

import java.util.*;

@NgProvider()
@NgSourceDirectoryReference()
public interface INgProvider<J extends INgProvider<J>> extends ITSComponent<J> {
    default List<String> declarations() {
        return new ArrayList<>();
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

    default List<String> schemas() {
        return new ArrayList<>();
    }

    default List<String> providers() {
        return List.of();
    }


}
