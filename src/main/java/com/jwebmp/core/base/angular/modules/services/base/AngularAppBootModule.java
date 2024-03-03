package com.jwebmp.core.base.angular.modules.services.base;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgPolyfill;
import com.jwebmp.core.base.angular.client.annotations.boot.*;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorBody;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDependency;
import com.jwebmp.core.base.angular.client.services.AnnotationHelper;
import com.jwebmp.core.base.angular.client.services.SocketClientService;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.modules.services.angular.RoutingModule;
import com.jwebmp.core.base.html.DivSimple;
import com.jwebmp.core.databind.IConfiguration;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.extern.java.Log;

import java.util.*;
import java.util.logging.Level;

import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.*;

@TsDependency(value = "@angular/platform-browser", version = "^17.2.0", overrides = true)
@NgImportReference(value = "BrowserModule", reference = "@angular/platform-browser")
@NgBootModuleImport("BrowserModule")

@NgImportReference(value = "FormsModule", reference = "@angular/forms")
@NgBootModuleImport("FormsModule")
//@NgBootModuleImport("ReactiveFormsModule")

@NgImportReference(value = "CommonModule", reference = "@angular/common")
@NgBootModuleImport("CommonModule")

@NgPolyfill("zone.js")

@TsDependency(value = "@angular/platform-browser-dynamic", version = "^17.2.0")
@NgComponentReference(SocketClientService.class)
@NgComponentReference(RoutingModule.class)
@Log
public class AngularAppBootModule extends DivSimple<AngularAppBootModule> implements INgModule<AngularAppBootModule>
{
    private Class<? extends INgComponent<?>> bootModule;
    private INgApp<?> app;

    public AngularAppBootModule()
    {
    }

    @Override
    public AngularAppBootModule setApp(INgApp<?> app)
    {
        this.app = app;
        return this;
    }

    public Class<?> getBootModule()
    {
        return bootModule;
    }

    public AngularAppBootModule setBootModule(Class<? extends INgComponent<?>> bootModule)
    {
        this.bootModule = bootModule;
        return this;
    }

    @Override
    public List<String> bootstrap()
    {
        var ngApp = app.getAnnotation();
        Class<? extends INgComponent<?>> aClass = ngApp.bootComponent();
        return List.of(getTsFilename(aClass));
    }

    @Override
    public List<String> moduleImports()
    {
        List<String> out = new ArrayList<>();
        for (NgBootModuleImport allAnnotation : IGuiceContext.get(AnnotationHelper.class)
                                                             .getGlobalAnnotations(NgBootModuleImport.class))
        {
            out.add(allAnnotation.value());
        }
        return out.stream()
                  .distinct()
                  .toList();
    }

    @Override
    public List<NgImportReference> getAllImportAnnotations()
    {
        List<NgImportReference> refs = INgModule.super.getAllImportAnnotations();
        List<NgImportReference> bootImportReferences = renderImportsMap();
        //List<NgImportReference> importAnnotations = getAllImportAnnotations();

        refs.addAll(bootImportReferences);
        refs.addAll(listAllBootImportReferences());
        refs.addAll(listAllProviderImports());
        refs.addAll(listAllModuleImports());
        refs.addAll(listAllDirectiveImports());
        refs.addAll(listAllComponentImports());

        refs = clean(refs);
        return refs;
    }

    @Override
    public List<NgConstructorParameter> getAllConstructorParameters()
    {
        List<NgConstructorParameter> out = INgModule.super.getAllConstructorParameters();
        List<NgBootConstructorParameter> params = IGuiceContext.get(AnnotationHelper.class)
                                                               .getGlobalAnnotations(NgBootConstructorParameter.class);
        for (NgBootConstructorParameter param : params)
        {
            out.add(getNgConstructorParameter(param.value()));
        }
        return out;
    }

    @Override
    public List<NgConstructorBody> getAllConstructorBodies()
    {
        List<NgConstructorBody> out = INgModule.super.getAllConstructorBodies();
        List<NgBootConstructorBody> params = IGuiceContext.get(AnnotationHelper.class)
                                                          .getGlobalAnnotations(NgBootConstructorBody.class);
        for (NgBootConstructorBody param : params)
        {
            out.add(getNgConstructorBody(param.value()));
        }
        return out;
    }


    private List<NgImportReference> listAllBootImportReferences()
    {
        List<NgImportReference> out = new ArrayList<>();
        List<NgBootImportReference> refs = IGuiceContext.get(AnnotationHelper.class)
                                                        .getGlobalAnnotations(NgBootImportReference.class);
        for (NgBootImportReference ref : refs)
        {
            if (ref.overrides())
            {
                out.removeIf(a -> a.value()
                                   .equals(ref.value()));
                out.add(AnnotationUtils.getNgImportReference(ref.value(), ref.reference()));
            }
            else
            {
                out.add(AnnotationUtils.getNgImportReference(ref.value(), ref.reference()));
            }

        }

        return out;
    }

    private List<NgImportReference> listAllModuleImports()
    {
        List<NgImportReference> out = new ArrayList<>();
        var scan = IGuiceContext.instance()
                                .getScanResult();
        for (ClassInfo classInfo : scan.getClassesWithAnnotation(NgModule.class))
        {
            Set<Class<? extends IComponent<?>>> classes = new HashSet<>();
            var a = classInfo;
            if (a.isInterface() || a.isAbstract())
            {
                for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                {
                    if (!subclass.isAbstract() && !subclass.isInterface())
                    {
                        classes.add((Class<? extends IComponent<?>>) subclass.loadClass());
                    }
                }
            }
            else
            {
                Class<?> aClass = a.loadClass();
                classes.add((Class<? extends IComponent<?>>) aClass);
            }
            for (Class<? extends IComponent<?>> aClass : classes)
            {
                if (aClass.equals(getClass()))
                {
                    continue;
                }
                var annos = IGuiceContext.get(AnnotationHelper.class)
                                         .getAnnotationFromClass(aClass, NgModule.class);
                for (NgModule anno : annos)
                {
                    if (anno != null)
                    {
                        NgComponentReference componentReference = getNgComponentReference(aClass);
                        List<NgImportReference> ngImportReferences = putRelativeLinkInMap(getClass(), componentReference);
                        out.addAll(ngImportReferences);
                    }
                }
            }
        }
        return out;
    }

    private List<NgImportReference> listAllProviderImports()
    {
        List<NgImportReference> out = new ArrayList<>();
        var scan = IGuiceContext.instance()
                                .getScanResult();
        for (ClassInfo classInfo : scan.getClassesWithAnnotation(NgProvider.class))
        {
            Set<Class<? extends IComponent<?>>> classes = new HashSet<>();
            var a = classInfo;
            if (a.isInterface() || a.isAbstract())
            {
                for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                {
                    if (!subclass.isAbstract() && !subclass.isInterface())
                    {
                        classes.add((Class<? extends IComponent<?>>) subclass.loadClass());
                    }
                }
            }
            else
            {
                Class<?> aClass = a.loadClass();
                classes.add((Class<? extends IComponent<?>>) aClass);
            }
            for (Class<? extends IComponent<?>> aClass : classes)
            {
                if (aClass.equals(getClass()))
                {
                    continue;
                }
                var annos = IGuiceContext.get(AnnotationHelper.class)
                                         .getAnnotationFromClass(aClass, NgProvider.class);
                for (NgProvider anno : annos)
                {
                    if (anno != null)
                    {
                        NgComponentReference componentReference = getNgComponentReference(aClass);
                        List<NgImportReference> ngImportReferences = putRelativeLinkInMap(getClass(), componentReference);
                        out.addAll(ngImportReferences);
                    }
                }
            }
        }

        for (ClassInfo classInfo : scan.getClassesWithAnnotation(NgServiceProvider.class))
        {
            Set<Class<? extends IComponent<?>>> classes = new HashSet<>();
            var a = classInfo;
            if (a.isInterface() || a.isAbstract())
            {
                for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                {
                    if (!subclass.isAbstract() && !subclass.isInterface())
                    {
                        classes.add((Class<? extends IComponent<?>>) subclass.loadClass());
                    }
                }
            }
            else
            {
                Class<?> aClass = a.loadClass();
                classes.add((Class<? extends IComponent<?>>) aClass);
            }
            for (Class<? extends IComponent<?>> aClass : classes)
            {
                if (aClass.equals(getClass()))
                {
                    continue;
                }
                var annos = IGuiceContext.get(AnnotationHelper.class)
                                         .getAnnotationFromClass(aClass, NgServiceProvider.class);
                for (NgServiceProvider anno : annos)
                {
                    if (anno != null && anno.singleton())
                    {
                        NgComponentReference componentReference = getNgComponentReference(aClass);
                        List<NgImportReference> ngImportReferences = putRelativeLinkInMap(getClass(), componentReference);
                        out.addAll(ngImportReferences);
                    }
                }
            }
        }

        return out;
    }

    private List<NgImportReference> listAllDirectiveImports()
    {
        List<NgImportReference> out = new ArrayList<>();
        var scan = IGuiceContext.instance()
                                .getScanResult();
        for (ClassInfo classInfo : scan.getClassesWithAnnotation(NgDirective.class))
        {
            Set<Class<? extends IComponent<?>>> classes = new HashSet<>();
            var a = classInfo;
            if (a.isInterface() || a.isAbstract())
            {
                for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                {
                    if (!subclass.isAbstract() && !subclass.isInterface())
                    {
                        classes.add((Class<? extends IComponent<?>>) subclass.loadClass());
                    }
                }
            }
            else
            {
                Class<?> aClass = a.loadClass();
                classes.add((Class<? extends IComponent<?>>) aClass);
            }
            for (Class<? extends IComponent<?>> aClass : classes)
            {
                if (aClass.equals(getClass()))
                {
                    continue;
                }
                try
                {
                    var annos = IGuiceContext.get(AnnotationHelper.class)
                                             .getAnnotationFromClass(aClass, NgDirective.class);
                    for (NgDirective anno : annos)
                    {
                        if (anno != null)
                        {
                            NgComponentReference componentReference = getNgComponentReference(aClass);
                            List<NgImportReference> ngImportReferences = putRelativeLinkInMap(getClass(), componentReference);
                            out.addAll(ngImportReferences);
                        }
                    }
                }
                catch (Exception e)
                {
                    log.log(Level.WARNING, "Unable to render directive - " + aClass.getCanonicalName(), e);
                }
            }
        }
        return out;
    }

    private List<NgImportReference> listAllComponentImports()
    {
        List<NgImportReference> out = new ArrayList<>();
        var scan = IGuiceContext.instance()
                                .getScanResult();
        for (ClassInfo classInfo : scan.getClassesWithAnnotation(NgComponent.class))
        {
            Set<Class<? extends IComponent<?>>> classes = new HashSet<>();
            var a = classInfo;
            if (a.isInterface() || a.isAbstract())
            {
                for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                {
                    if (!subclass.isAbstract() && !subclass.isInterface())
                    {
                        classes.add((Class<? extends IComponent<?>>) subclass.loadClass());
                    }
                }
            }
            else
            {
                Class<?> aClass = a.loadClass();
                classes.add((Class<? extends IComponent<?>>) aClass);
            }
            for (Class<? extends IComponent<?>> aClass : classes)
            {
                if (aClass.equals(getClass()))
                {
                    continue;
                }
                var annos = IGuiceContext.get(AnnotationHelper.class)
                                         .getAnnotationFromClass(aClass, NgComponent.class);
                for (NgComponent anno : annos)
                {
                    if (anno != null)
                    {
                        NgComponentReference componentReference = getNgComponentReference(aClass);
                        List<NgImportReference> ngImportReferences = putRelativeLinkInMap(getClass(), componentReference);
                        out.addAll(ngImportReferences);
                    }
                }
            }
        }
        return out;
    }


    public List<NgImportReference> renderImportsMap()
    {
        List<NgImportReference> out = new ArrayList<>();
        for (IConfiguration configuration : getConfigurations(INgModule.class))
        {
            INgModule<?> module = (INgModule<?>) configuration;
            for (Map.Entry<String, String> entry : module.imports()
                                                         .entrySet())
            {
                String key = entry.getKey();
                String value = entry.getValue();
                out.add(AnnotationUtils.getNgImportReference(key, value));
            }
        }
        return out;
    }

    @Override
    public List<String> providers()
    {
        List<String> out = INgModule.super.providers();
        ScanResult scan = IGuiceContext.instance()
                                       .getScanResult();
        for (ClassInfo classInfo : scan
                .getClassesWithAnnotation(NgBootProvider.class))
        {
            if (classInfo.isInterface() || classInfo.isAbstract())
            {
                continue;
            }
            Class<? extends INgProvider<?>> aClass = (Class<? extends INgProvider<?>>) classInfo.loadClass();
            INgProvider<?> component = IGuiceContext.get(aClass);
            var annos = IGuiceContext.get(AnnotationHelper.class)
                                     .getGlobalAnnotations(NgBootProvider.class);
            for (NgBootProvider anno : annos)
            {
                if (anno != null)
                {
                    if (anno.onSelf())
                    {
                        out.add(anno.value());
                    }
                    for (String key : component.providers())
                    {
                        out.add(key);
                    }
                }
            }
        }
        for (ClassInfo classInfo : scan.getClassesWithAnnotation(NgServiceProvider.class))
        {
            Set<Class<? extends IComponent<?>>> classes = new HashSet<>();
            var a = classInfo;
            if (a.isInterface() || a.isAbstract())
            {
                for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                {
                    if (!subclass.isAbstract() && !subclass.isInterface())
                    {
                        classes.add((Class<? extends IComponent<?>>) subclass.loadClass());
                    }
                }
            }
            else
            {
                Class<?> aClass = a.loadClass();
                classes.add((Class<? extends IComponent<?>>) aClass);
            }
            for (Class<? extends IComponent<?>> aClass : classes)
            {
                if (aClass.equals(getClass()))
                {
                    continue;
                }
                var annos = IGuiceContext.get(AnnotationHelper.class)
                                         .getAnnotationFromClass(aClass, NgServiceProvider.class);
                for (NgServiceProvider anno : annos)
                {
                    if (anno != null && anno.singleton())
                    {
                        out.add(getTsFilename(aClass));
                    }
                }
            }
        }

        return out;
    }

    @Override
    public List<String> declarations()
    {
        List<String> out = INgModule.super.declarations();
        out.add(bootModule.getSimpleName());
        var scan = IGuiceContext.instance()
                                .getScanResult();

        for (ClassInfo classInfo : scan.getClassesWithAnnotation(NgComponent.class))
        {
            Set<Class<? extends IComponent<?>>> classes = new HashSet<>();
            var a = classInfo;
            if (a.isInterface() || a.isAbstract())
            {
                for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                {
                    if (!subclass.isAbstract() && !subclass.isInterface())
                    {
                        classes.add((Class<? extends IComponent<?>>) subclass.loadClass());
                    }
                }
            }
            else
            {
                Class<?> aClass = a.loadClass();
                classes.add((Class<? extends IComponent<?>>) aClass);
            }
            for (Class<? extends IComponent<?>> aClass : classes)
            {
                if (aClass.equals(getClass()))
                {
                    continue;
                }
                try
                {
                    var annos = IGuiceContext.get(AnnotationHelper.class)
                                             .getAnnotationFromClass(aClass, NgComponent.class);
                    for (NgComponent anno : annos)
                    {
                        if (anno != null)
                        {
                            out.add(aClass.getSimpleName());
                        }
                    }
                }
                catch (Exception e)
                {
                    log.log(Level.WARNING, "Unable to render directive - " + aClass.getCanonicalName(), e);
                }
            }
        }

        for (ClassInfo classInfo : scan.getClassesWithAnnotation(NgDirective.class))
        {
            Set<Class<? extends IComponent<?>>> classes = new HashSet<>();
            var a = classInfo;
            if (a.isInterface() || a.isAbstract())
            {
                for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                {
                    if (!subclass.isAbstract() && !subclass.isInterface())
                    {
                        classes.add((Class<? extends IComponent<?>>) subclass.loadClass());
                    }
                }
            }
            else
            {
                Class<?> aClass = a.loadClass();
                classes.add((Class<? extends IComponent<?>>) aClass);
            }
            for (Class<? extends IComponent<?>> aClass : classes)
            {
                if (aClass.equals(getClass()))
                {
                    continue;
                }
                try
                {
                    var annos = IGuiceContext.get(AnnotationHelper.class)
                                             .getAnnotationFromClass(aClass, NgDirective.class);
                    for (NgDirective anno : annos)
                    {
                        if (anno != null)
                        {
                            out.add(aClass.getSimpleName());
                        }
                    }
                }
                catch (Exception e)
                {
                    log.log(Level.WARNING, "Unable to render directive - " + aClass.getCanonicalName(), e);
                }
            }
        }


        return new ArrayList<>(out);
    }


    @Override
    public List<String> globalFields()
    {
        List<String> gf = new ArrayList<>();
        List<NgBootGlobalField> bootDy = IGuiceContext.get(AnnotationHelper.class)
                                                      .getGlobalAnnotations(NgBootGlobalField.class);
        for (NgBootGlobalField globalFields : bootDy)
        {
            if (globalFields.onSelf())
            {
                gf.add(globalFields.value());
            }
        }
        gf.addAll(INgModule.super.globalFields());
        return gf;
    }

    @Override
    public List<String> schemas()
    {
        List<String> gf = new ArrayList<>();
        List<NgBootModuleSchema> bootDy = IGuiceContext.get(AnnotationHelper.class)
                                                       .getGlobalAnnotations(NgBootModuleSchema.class);
        for (NgBootModuleSchema globalFields : bootDy)
        {
            gf.add(globalFields.value());
        }
        gf.addAll(INgModule.super.schemas());
        return gf;
    }
}
