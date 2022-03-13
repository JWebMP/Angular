package com.jwebmp.core.base.angular.services.interfaces;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface INgDirective<J extends INgDirective<J>> extends ITSComponent<J> {
    default List<String> declarations() {
        Set<String> out = new HashSet<>();
        out.add(getClass().getSimpleName());
        return new ArrayList<>(out);
    }

    default List<String> styleUrls() {
        return List.of();
    }

    default List<String> providers() {
        return List.of();
    }

    default List<String> inputs()
    {
        return List.of();
    }

    default List<String> outputs()
    {
        return List.of();
    }

    default List<String> host()
    {
        return List.of();
    }
}
