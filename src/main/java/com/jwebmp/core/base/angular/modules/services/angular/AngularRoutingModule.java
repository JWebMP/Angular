package com.jwebmp.core.base.angular.modules.services.angular;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgModule;
import com.jwebmp.core.base.angular.client.annotations.boot.NgBootModuleImport;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.routing.NgRoutable;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDependency;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.DefinedRoute;
import com.jwebmp.core.base.interfaces.IComponentHTMLAttributeBase;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;
import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.getNgComponentReference;
import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.getTsFilename;


@NgImportReference(value = "RouterModule, ParamMap,Router", reference = "@angular/router")
@NgImportReference(value = "Routes", reference = "@angular/router")

@NgBootModuleImport("RoutingModule")
@TsDependency(value = "@angular/router", version = "^19.2.0")

@NgModule
public class AngularRoutingModule implements INgModule<AngularRoutingModule>
{
    private INgApp<?> app;
    private List<DefinedRoute<?>> definedRoutesList;
    private static List<DefinedRoute<?>> routes = new ArrayList<>();

    private static RoutingModuleOptions options;

    public static RoutingModuleOptions getOptions()
    {
        if (options == null)
        {
            options = new RoutingModuleOptions();
        }
        return options;
    }

    public static List<DefinedRoute<?>> getRoutes(INgApp<?> app)
    {
        if (routes.isEmpty())
        {
            try
            {
                new AngularRoutingModule().setApp(app)
                        .buildRoutes();
            }
            catch (Throwable T)
            {

            }
        }

        return routes;
    }

    @Override
    public Set<String> moduleImports()
    {
        if (options == null)
        {
            return Set.of("RouterModule.forRoot(routes)");
        }
        else
        {
            try
            {
                return Set.of("RouterModule.forRoot(routes," + new ObjectMapper()
                        .disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES)
                        .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                        .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                        .writeValueAsString(options) + ")");
            }
            catch (JsonProcessingException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public AngularRoutingModule setApp(INgApp<?> app)
    {
        this.app = app;
        return this;
    }

    @Override
    public List<String> exports()
    {
        return List.of("RouterModule");
    }

    @Override
    public List<NgImportReference> getAllImportAnnotations()
    {
        List<NgImportReference> out = INgModule.super.getAllImportAnnotations();
        if (definedRoutesList == null)
        {
            buildRoutes();
        }
        for (DefinedRoute<?> definedRoute : definedRoutesList)
        {
            NgComponentReference ngComponentReference = getNgComponentReference(definedRoute.getComponent());
            out.addAll(putRelativeLinkInMap(getClass(), ngComponentReference));
            buildImportReferenceNest(out, definedRoute);
        }
        return out;
    }

    private void buildImportReferenceNest(List<NgImportReference> out, DefinedRoute<?> definedRoute)
    {
        List<DefinedRoute<?>> dChildren = definedRoute.getChildren();
        for (DefinedRoute<?> dChild : dChildren)
        {
            out.addAll(addImportToMap(dChild));
            buildImportReferenceNest(out, dChild);
        }
    }

    public void buildRoutes()
    {
        ScanResult scan = IGuiceContext.instance()
                .getScanResult();

        Map<Class<? extends IComponent<?>>, String> baseRoutes = new LinkedHashMap<>();
        definedRoutesList = new ArrayList<>();
        scan
                .getClassesWithAnnotation(NgRoutable.class)
                .stream()
                .sorted(Comparator.comparingInt(o -> o.loadClass()
                        .getAnnotation(NgRoutable.class)
                        .sortOrder()))
                .forEach(a -> {
                            Class<? extends INgComponent<?>> aClass = (Class<? extends INgComponent<?>>) a.loadClass();
                            var component = IGuiceContext.get(aClass);
                            this.app.getRoutes()
                                    .add((IComponentHierarchyBase<?, ?>) component);
                            NgRoutable annotation = aClass.getAnnotation(NgRoutable.class);
                            if (annotation != null)
                            {
                                if (annotation.parent().length == 0)
                                {
                                    baseRoutes.put(aClass, annotation.path());
                                }
                            }
                        }
                );
        baseRoutes.forEach((aClass, aName) -> {
            DefinedRoute<?> dr = getDefinedRoute(aClass);
            buildRoutePathway(dr, aClass, true);
            definedRoutesList.add(dr);
            routes.add(dr);
        });
        for (DefinedRoute<?> definedRoute : definedRoutesList)
        {
            if (definedRoute.getComponent() != null)
            {
                List<NgImportReference> refs = new ArrayList<>();
                buildRoutePathwayImports(definedRoute, refs);
            }
        }
    }

    private DefinedRoute<?> getDefinedRoute(Class<? extends IComponent<?>> aClass)
    {
        DefinedRoute<?> dr = new DefinedRoute<>();
        NgRoutable annotation = aClass.getAnnotation(NgRoutable.class);
        dr.setPath(annotation.path());
        dr.setRenderComponent(!annotation.ignoreComponent());
        dr.setComponent(aClass);
        dr.setComponentName(getTsFilename(aClass));

        if (!Strings.isNullOrEmpty(annotation.redirectTo()))
        {
            dr.setRedirectTo(annotation.redirectTo());
        }
        if (!Strings.isNullOrEmpty(annotation.pathMatch()))
        {
            dr.setPathMatch(annotation.pathMatch());
        }
        return dr;
    }

    private List<NgImportReference> addImportToMap(DefinedRoute<?> definedRoute)
    {
        NgComponentReference reference = getNgComponentReference(definedRoute.getComponent());
        if (definedRoute.getComponent() != null)
        {
            return putRelativeLinkInMap(getClass(), reference);
        }
        return new ArrayList<>();
    }

    private List<NgImportReference> buildRoutePathwayImports(DefinedRoute<?> definedRoute, List<NgImportReference> out)
    {
        for (DefinedRoute<?> child : definedRoute.getChildren())
        {
            if (child.getComponent() != null)
            {
                out.addAll(addImportToMap(child));
                out.addAll(buildRoutePathwayImports(child, out));
            }
        }

        return out;
    }

    private void buildRoutePathway(DefinedRoute parentRoute,
                                   Class<? extends IComponent<?>> aClass, boolean first)
    {
        DefinedRoute<?> innerDr = getDefinedRoute(aClass);
        if (!first)
        {
            parentRoute.addChild(innerDr);
        }
        else
        {
            innerDr = parentRoute;
        }

        first = false;
        for (Class<? extends IComponent<?>> componentsWithParentClass : componentsWithParenClassAs(aClass))
        {
            buildRoutePathway(innerDr, componentsWithParentClass, first);
        }
    }

    List<Class<? extends IComponent<?>>> componentsWithParenClassAs(Class<? extends IComponent<?>> clazz)
    {
        List<Class<? extends IComponent<?>>> out = new ArrayList<>();

        for (ClassInfo classInfo : IGuiceContext.instance()
                .getScanResult()
                .getClassesWithAnnotation(NgRoutable.class))
        {
            if (classInfo.isAbstract() || classInfo.isInterface())
            {
                continue;
            }
            NgRoutable routable = classInfo.loadClass()
                    .getAnnotation(NgRoutable.class);
            for (Class<? extends IComponent<?>> aClass : routable.parent())
            {
                if (aClass.equals(clazz))
                {
                    out.add((Class<? extends IComponent<?>>) classInfo.loadClass());
                }
            }
        }
        return out;
    }

    static <K, V> Map<K, V> filterByValue(Map<K, V> map, Predicate<V> predicate)
    {
        return map.entrySet()
                .stream()
                .filter(entry -> predicate.test(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    @Override
    public String renderBeforeClass()
    {
        //render the const class
        ObjectMapper om = IGuiceContext.get(DefaultObjectMapper);
        try
        {
            String routesOutput = om.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(definedRoutesList);
            routesOutput = "export const routes: Routes = " + routesOutput + ";\n";
            return routesOutput;
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }
        return "";
    }

}
