package com.jwebmp.core.base.angular.implementations.configurations;

import com.google.common.base.Strings;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.angular.NgDirective;
import com.jwebmp.core.base.angular.client.annotations.angular.NgServiceProvider;
import com.jwebmp.core.base.angular.client.annotations.components.NgInput;
import com.jwebmp.core.base.angular.client.annotations.components.NgOutput;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorBody;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.annotations.functions.*;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportProvider;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.*;
import com.jwebmp.core.base.angular.client.services.ComponentConfiguration;
import com.jwebmp.core.base.angular.client.services.any;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.html.Input;
import com.jwebmp.core.base.html.interfaces.GlobalChildren;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.databind.IConfiguration;
import com.jwebmp.core.databind.IOnComponentConfigured;

import java.util.List;

public class ConfigureImportReferences implements IOnComponentConfigured<ConfigureImportReferences>
{
    private final ComponentConfiguration<?> compConfig = new ComponentConfiguration<>();
    private String componentString;

    @Override
    public void onComponentConfigured(IComponentHierarchyBase<GlobalChildren, ?> parent, IComponentHierarchyBase<GlobalChildren, ?> component)
    {
        compConfig.setRootComponent(component);
        this.componentString = component.toString(0);
        onComponentConfigured(parent, component, false);
    }

    private void onComponentConfigured(IComponentHierarchyBase<GlobalChildren, ?> parent, IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        Class<?> componentClass = component.getClass();
        if (componentClass.getSimpleName().equals("ProductDetailPart"))
        {
            System.out.println("adfasdf");
        }

        processClassToComponent(componentClass, component, checkForParent);

        for (GlobalChildren child : component.getChildren())
        {
            IComponentHierarchyBase<?, ?> childComponent = (IComponentHierarchyBase<?, ?>) child;
            if (!checkForParent)
            {
                if (INgComponent.class.isAssignableFrom(child.getClass()) && child.getClass().getDeclaredAnnotationsByType(NgComponent.class).length > 0)
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("inject", "@angular/core"));
                    var cRef = AnnotationUtils.getNgComponentReference((Class<? extends IComponent<?>>) child.getClass());
                    if (compConfig.getRootComponent() instanceof ImportsStatementsComponent<?> imp)
                    {
                        List<NgImportReference> ngImportReferences = imp.putRelativeLinkInMap(((INgComponent<?>) compConfig.getRootComponent()).getClass(), cRef);
                        for (NgImportReference ngImportReference : ngImportReferences)
                        {
                            compConfig.getImportReferences().add((AnnotationUtils.getNgImportReference(ngImportReference.value(), ngImportReference.reference())));
                        }
                        onComponentConfigured(component, (IComponentHierarchyBase<GlobalChildren, ?>) child, true);
                    }
                }
                else
                {
                    ((IComponentHierarchyBase) compConfig.getRootComponent()).getConfigurations()
                            .addAll(childComponent.getConfigurations());
                    onComponentConfigured(component, (IComponentHierarchyBase<GlobalChildren, ?>) child, false);
                }
            }
            else
            {
                ((IComponentHierarchyBase) compConfig.getRootComponent()).getConfigurations()
                        .addAll(childComponent.getConfigurations());
                onComponentConfigured(component, (IComponentHierarchyBase<GlobalChildren, ?>) child, false);
            }
        }

        if (component.getClass().getSimpleName().equalsIgnoreCase("FontAwesome"))
        {
            System.out.println("asdfff");
        }
        configureAngularLifeCycleMethods(componentClass, component, checkForParent);
        processComponentConfigurations(component, checkForParent);

        if (parent == null)
        {
            //finished
            component.asBase().getProperties().put("AngularConfiguration", compConfig);
        }
    }

    private void processComponentConfigurations(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        for (IConfiguration configuration : component.getConfigurations())
        {
            if (configuration instanceof NgComponentReference ngComponentReference && component instanceof ImportsStatementsComponent<?> imp)
            {
                if ((ngComponentReference.onSelf() && !checkForParent) || (ngComponentReference.onParent() && checkForParent))
                {
                    List<NgImportReference> ngImportReferences = imp.putRelativeLinkInMap(((INgComponent<?>) compConfig.getRootComponent()).getClass(), ngComponentReference);
                    for (NgImportReference ngImportReference : ngImportReferences)
                    {
                        compConfig.getImportReferences().add((AnnotationUtils.getNgImportReference(ngImportReference.value(), ngImportReference.reference())));
                    }
                }
            }
            else if (configuration instanceof NgImportReference ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference(ngComponentReference.value(), ngComponentReference.reference()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference(ngComponentReference.value(), ngComponentReference.reference()));
                }
            }
            else if (configuration instanceof NgField ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getFields().add(AnnotationUtils.getNgField(ngComponentReference.value()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getFields().add(AnnotationUtils.getNgField(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgMethod ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getMethods().add(AnnotationUtils.getNgMethod(ngComponentReference.value()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getMethods().add(AnnotationUtils.getNgMethod(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgInterface ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface(ngComponentReference.value()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgOnInit ngComponentReference)
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnInit", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnInit"));
                compConfig.getOnInit().add(AnnotationUtils.getNgOnInit(ngComponentReference.value()));
            }
            else if (configuration instanceof NgOnDestroy ngComponentReference)
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnDestroy", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnDestroy"));
                compConfig.getOnDestroy().add(AnnotationUtils.getNgOnDestroy(ngComponentReference.value()));
            }
            else if (configuration instanceof NgAfterViewInit ngComponentReference)
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewInit", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewInit"));
                compConfig.getAfterViewInit().add(AnnotationUtils.getNgAfterViewInit(ngComponentReference.value()));
            }
            else if (configuration instanceof NgAfterViewChecked ngComponentReference)
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewChecked", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewChecked"));
                compConfig.getAfterViewChecked().add(AnnotationUtils.getNgAfterViewChecked(ngComponentReference.value()));
            }
            else if (configuration instanceof NgAfterContentInit ngComponentReference)
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentInit", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentInit"));
                compConfig.getAfterContentInit().add(AnnotationUtils.getNgAfterContentInit(ngComponentReference.value()));
            }
            else if (configuration instanceof NgAfterContentChecked ngComponentReference)
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentChecked", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentChecked"));
                compConfig.getAfterContentChecked().add(AnnotationUtils.getNgAfterContentChecked(ngComponentReference.value()));
            }
            else if (configuration instanceof NgInput ngComponentReference)
            {
                compConfig.getInputs().add(
                        AnnotationUtils.getNgInput(ngComponentReference.value(),
                                ngComponentReference.mandatory(),
                                ngComponentReference.type(),
                                ngComponentReference.attributeReference(),
                                ngComponentReference.renderAttributeReference(),
                                ngComponentReference.additionalData()
                        ));
            }
            else if (configuration instanceof NgOutput ngComponentReference)
            {
                compConfig.getOutputs().add(AnnotationUtils.getNgOutput(ngComponentReference.value(), ngComponentReference.parentMethodName()));
            }
            else if (configuration instanceof NgInterface ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface(ngComponentReference.value()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgImportModule ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getImportModules().add(AnnotationUtils.getNgImportModule(ngComponentReference.value()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getImportModules().add(AnnotationUtils.getNgImportModule(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgImportProvider ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getImportProviders().add(AnnotationUtils.getNgImportProvider(ngComponentReference.value()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getImportProviders().add(AnnotationUtils.getNgImportProvider(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgGlobalField ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getGlobalFields().add(AnnotationUtils.getNgGlobalField(ngComponentReference.value()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getGlobalFields().add(AnnotationUtils.getNgGlobalField(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgConstructorBody ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getConstructorBodies().add(AnnotationUtils.getNgConstructorBody(ngComponentReference.value()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getConstructorBodies().add(AnnotationUtils.getNgConstructorBody(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgConstructorParameter ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getConstructorParameters().add(AnnotationUtils.getNgConstructorParameter(ngComponentReference.value()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getConstructorParameters().add(AnnotationUtils.getNgConstructorParameter(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgInject ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("inject", "@angular/core"));
                    compConfig.getInjects().add(AnnotationUtils.getNgInject(ngComponentReference.referenceName(), ngComponentReference.value()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("inject", "@angular/core"));
                    compConfig.getInjects().add(AnnotationUtils.getNgInject(ngComponentReference.referenceName(), ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgModal ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("modal", "@angular/core"));
                    compConfig.getModals().add(AnnotationUtils.getNgModal(ngComponentReference.value(), ngComponentReference.referenceName()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("modal", "@angular/core"));
                    compConfig.getModals().add(AnnotationUtils.getNgModal(ngComponentReference.value(), ngComponentReference.referenceName()));
                }
            }
            else if (configuration instanceof NgSignal ngComponentReference)
            {
                if (ngComponentReference.onSelf() && !checkForParent)
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("signal", "@angular/core"));
                    compConfig.getSignals().add(AnnotationUtils.getNgSignal(ngComponentReference.referenceName(), ngComponentReference.value()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("signal", "@angular/core"));
                    compConfig.getSignals().add(AnnotationUtils.getNgSignal(ngComponentReference.referenceName(), ngComponentReference.value()));
                }
            }
        }
    }


    private void unwrapMethods(IComponentHierarchyBase<GlobalChildren, ?> comp, boolean checkForParent)
    {
        if (comp instanceof IComponent<?> component)
        {
            for (String componentMethod : component.interfaces())
            {
                if (!Strings.isNullOrEmpty(componentMethod))
                {
                    var ng = AnnotationUtils.getNgInterface(componentMethod);
                    if ((ng.onSelf() && !checkForParent) || (ng.onParent() && checkForParent))
                    {
                        compConfig.getInterfaces().add(ng);
                    }
                }
            }

            for (String componentMethod : component.methods())
            {
                if (!Strings.isNullOrEmpty(componentMethod))
                {
                    var ng = AnnotationUtils.getNgComponentMethod(componentMethod);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                    {
                        compConfig.getMethods().add(ng);
                    }
                }
            }

            for (String componentField : component.globalFields())
            {
                if (!Strings.isNullOrEmpty(componentField))
                {
                    var ng = AnnotationUtils.getNgGlobalField(componentField);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                    {
                        compConfig.getGlobalFields().add(ng);
                    }
                }
            }

            for (String componentField : component.fields())
            {
                if (!Strings.isNullOrEmpty(componentField))
                {
                    var ng = AnnotationUtils.getNgField(componentField);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                    {
                        compConfig.getFields().add(ng);
                    }
                }
            }

            for (String componentField : component.moduleImports())
            {
                if (!Strings.isNullOrEmpty(componentField))
                {
                    var ng = AnnotationUtils.getNgImportModule(componentField);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                    {
                        compConfig.getImportModules().add(ng);
                    }
                }
            }

            for (String onInit : component.onInit())
            {
                if (!Strings.isNullOrEmpty(onInit))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnInit", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnInit"));
                    compConfig.getOnInit().add(AnnotationUtils.getNgOnInit(onInit));
                }
            }
            for (String onDestroy : component.onDestroy())
            {
                if (!Strings.isNullOrEmpty(onDestroy))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnDestroy", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnDestroy"));
                    compConfig.getOnDestroy().add(AnnotationUtils.getNgOnDestroy(onDestroy));
                }
            }

            for (String afterContentChecked : component.afterContentChecked())
            {
                if (!Strings.isNullOrEmpty(afterContentChecked))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentChecked", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentChecked"));
                    compConfig.getAfterContentChecked().add(AnnotationUtils.getNgAfterContentChecked(afterContentChecked));
                }
            }
            for (String afterContentInit : component.afterContentInit())
            {
                if (!Strings.isNullOrEmpty(afterContentInit))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentInit", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentInit"));
                    compConfig.getAfterContentInit().add(AnnotationUtils.getNgAfterContentInit(afterContentInit));
                }
            }

            for (String afterViewInit : component.afterViewInit())
            {
                if (!Strings.isNullOrEmpty(afterViewInit))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewInit", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewInit"));
                    compConfig.getAfterViewInit().add(AnnotationUtils.getNgAfterViewInit(afterViewInit));
                }
            }
            for (String afterViewChecked : component.afterViewChecked())
            {
                if (!Strings.isNullOrEmpty(afterViewChecked))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewChecked", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewChecked"));
                    compConfig.getAfterViewChecked().add(AnnotationUtils.getNgAfterViewChecked(afterViewChecked));
                }
            }
            for (String componentConstructorParameter : component.constructorParameters())
            {
                if (!Strings.isNullOrEmpty(componentConstructorParameter))
                {
                    var ng = AnnotationUtils.getNgConstructorParameter(componentConstructorParameter);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                    {
                        compConfig.getConstructorParameters().add(AnnotationUtils.getNgConstructorParameter(ng.value()));
                    }
                }
            }
            for (String componentConstructorParameter : component.constructorBody())
            {
                if (!Strings.isNullOrEmpty(componentConstructorParameter))
                {
                    var ng = AnnotationUtils.getNgConstructorBody(componentConstructorParameter);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                    {
                        compConfig.getConstructorBodies().add(AnnotationUtils.getNgConstructorBody(ng.value()));
                    }
                }
            }
        }

        if (comp instanceof INgComponent<?> component)
        {
            for (String provider : component.providers())
            {
                if (!Strings.isNullOrEmpty(provider))
                {
                    var ng = AnnotationUtils.getNgImportProvider(provider);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                    {
                        compConfig.getImportProviders().add(AnnotationUtils.getNgImportProvider(ng.value()));
                    }
                }
            }
            for (String input : component.inputs())
            {
                if (!Strings.isNullOrEmpty(input))
                {
                    var ng = AnnotationUtils.getNgInput(input);
                    compConfig.getInputs().add(AnnotationUtils.getNgInput(ng.value()));
                }
            }
        }
    }


    private void addFormsImports(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        if (component.asAttributeBase().getAttributes().containsValue("ngModel") || component.asAttributeBase().getAttributes().containsValue("ngForm"))
        {
            compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("FormsModule", "@angular/forms"));
            compConfig.getImportModules().add(AnnotationUtils.getNgImportModule("FormsModule"));
        }
        if (component instanceof Input input)
        {
            if (!input.getProperties().containsKey("noName") && input.getAttributes().containsKey("[(ngModel)]"))
            {
                input.addAttribute("#" + (Strings.isNullOrEmpty(input.getName()) ? input.getID() : input.getName()), "ngModel");
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("FormsModule", "@angular/forms"));
                compConfig.getImportModules().add(AnnotationUtils.getNgImportModule("FormsModule"));
            }
        }
        if (component.asAttributeBase().getAttributes().containsKey("ngClass"))
        {
            compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("NgClass", "@angular/common"));
            compConfig.getImportModules().add(AnnotationUtils.getNgImportModule("NgClass"));
        }
        if (component.asAttributeBase().getAttributes().containsKey("ngStyle"))
        {
            compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("NgStyle", "@angular/common"));
            compConfig.getImportModules().add(AnnotationUtils.getNgImportModule("NgStyle"));
        }

    }


    private void addConstructorParameters(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgConstructorParameter.class).forEach(constructorParameter -> {
            if ((constructorParameter.onSelf() && !checkForParent) || (constructorParameter.onParent() && checkForParent))
            {
                compConfig.getConstructorParameters().add(AnnotationUtils.getNgConstructorParameter(constructorParameter.value()));
            }
        });
    }

    private void addConstructorBodies(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgConstructorBody.class).forEach(ngConstructorBody -> {
            if ((ngConstructorBody.onSelf() && !checkForParent) || (ngConstructorBody.onParent() && checkForParent))
            {
                compConfig.getConstructorBodies().add(AnnotationUtils.getNgConstructorBody(ngConstructorBody.value()));
            }
        });
    }

    private void addFields(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgField.class).forEach(ngField -> {
            if ((ngField.onSelf() && !checkForParent) || (ngField.onParent() && checkForParent))
            {
                compConfig.getFields().add(AnnotationUtils.getNgField(ngField.value()));
            }
        });
    }

    private void addGlobalFields(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgGlobalField.class).forEach(ngField -> {
            if ((ngField.onSelf() && !checkForParent) || (ngField.onParent() && checkForParent))
            {
                compConfig.getGlobalFields().add(AnnotationUtils.getNgGlobalField(ngField.value()));
            }
        });
    }

    private void addMethods(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgMethod.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getMethods().add(AnnotationUtils.getNgComponentMethod(ngMethod.value()));
            }
        });
    }

    private void addInterfaces(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgInterface.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface(ngMethod.value()));
            }
        });
    }

    private void addInjects(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgInject.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getInjects().add(AnnotationUtils.getNgInject(ngMethod.referenceName(), ngMethod.value()));
            }
        });
    }

    private void addImportModules(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgImportModule.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getImportModules().add(AnnotationUtils.getNgImportModule(ngMethod.value()));
            }
        });
    }

    private void addImportProviders(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgImportProvider.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getImportProviders().add(AnnotationUtils.getNgImportProvider(ngMethod.value()));
            }
        });
    }

    private void addModals(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgModal.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getModals().add(AnnotationUtils.getNgModal(ngMethod.referenceName(), ngMethod.value()));
            }
        });
    }

    private void addSignals(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgSignal.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getSignals().add(AnnotationUtils.getNgSignal(ngMethod.referenceName(), ngMethod.value()));
            }
        });
    }

    private void addInputs(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgInput.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getInputs().add(AnnotationUtils.getNgInput(
                        ngMethod.value(),
                        ngMethod.mandatory(),
                        ngMethod.type(),
                        ngMethod.attributeReference(),
                        ngMethod.renderAttributeReference(),
                        ngMethod.additionalData()
                ));
            }
        });
    }

    private void addOutputs(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgOutput.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getOutputs().add(AnnotationUtils.getNgOutput(ngMethod.value(), ngMethod.parentMethodName()));
            }
        });
    }

    private void addOnInit(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgOnInit.class).forEach(ngMethod -> {
            compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnInit", "@angular/core"));
            compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnInit"));
            compConfig.getOnInit().add(AnnotationUtils.getNgOnInit(ngMethod.value().trim()));
        });
    }

    private void addOnDestroy(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgOnDestroy.class).forEach(ngMethod -> {
            compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnDestroy", "@angular/core"));
            compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnDestroy"));
            compConfig.getOnDestroy().add(AnnotationUtils.getNgOnDestroy(ngMethod.value()));
        });
    }

    private void addAfterViewInit(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgAfterViewInit.class).forEach(ngMethod -> {
            compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewInit", "@angular/core"));
            compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewInit"));
            compConfig.getAfterViewInit().add(AnnotationUtils.getNgAfterViewInit(ngMethod.value()));
        });
    }

    private void addAfterViewChecked(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgAfterViewChecked.class).forEach(ngMethod -> {
            compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewChecked", "@angular/core"));
            compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewChecked"));
            compConfig.getAfterViewChecked().add(AnnotationUtils.getNgAfterViewChecked(ngMethod.value()));
        });
    }

    private void addAfterContentChecked(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgAfterContentChecked.class).forEach(ngMethod -> {
            compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentChecked", "@angular/core"));
            compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentChecked"));
            compConfig.getAfterContentChecked().add(AnnotationUtils.getNgAfterContentChecked(ngMethod.value()));
        });
    }

    private void addAfterContentInit(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgAfterContentInit.class).forEach(ngMethod -> {
            compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentInit", "@angular/core"));
            compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentInit"));
            compConfig.getAfterContentInit().add(AnnotationUtils.getNgAfterContentInit(ngMethod.value()));
        });
    }


    private void addImportReferences(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgImportReference.class).forEach(importReference -> {
            if ((importReference.onSelf() && !checkForParent) || (importReference.onParent() && checkForParent))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference(importReference.value(), importReference.reference()));
            }
        });
    }

    private void addComponentReferences(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgComponentReference.class).forEach(importReference -> {
            if ((importReference.onSelf() && !checkForParent) || (importReference.onParent() && checkForParent))
            {
                List<NgImportReference> irs = new ImportsStatementsComponent()
                {
                }.putRelativeLinkInMap(compConfig.getRootComponent().getClass(), importReference);
                for (NgImportReference ir : irs)
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference(ir.value(), ir.reference()));
                }

                if (
                        INgComponent.class.isAssignableFrom(importReference.value())
                )
                {
                    compConfig.getInjects().add(
                            AnnotationUtils.getNgInject(
                                    AnnotationUtils.getTsVarName(importReference.value()),
                                    AnnotationUtils.getTsFilename(importReference.value())
                            )
                    );
                }
                if (
                        INgDirective.class.isAssignableFrom(importReference.value())
                )
                {
                    compConfig.getImportModules().add(AnnotationUtils.getNgImportModule(AnnotationUtils.getTsFilename(importReference.value())));
                }
                if (INgServiceProvider.class.isAssignableFrom(importReference.value()) ||
                        INgDataService.class.isAssignableFrom(importReference.value()) ||
                        INgProvider.class.isAssignableFrom(importReference.value())
                )
                {
                    compConfig.getInjects().add(
                            AnnotationUtils.getNgInject(
                                    AnnotationUtils.getTsVarName(importReference.value()),
                                    AnnotationUtils.getTsFilename(importReference.value())
                            )
                    );
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("inject", "@angular/core"));
                }
            }
        });
    }

    private void processClassToComponent(Class<?> componentClass, IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        addPipes(component, checkForParent);
        addLogicDirectives(component, checkForParent);
        addFieldInputDirectives(component, checkForParent);
        addFormsImports(component, checkForParent);

        addConstructorParameters(component, checkForParent);
        addConstructorBodies(component, checkForParent);
        addFields(component, checkForParent);
        addGlobalFields(component, checkForParent);
        addMethods(component, checkForParent);
        addInterfaces(component, checkForParent);

        if (!checkForParent)
        {
            addOnInit(component, checkForParent);
            addOnDestroy(component, checkForParent);
            addAfterContentChecked(component, checkForParent);
            addAfterContentInit(component, checkForParent);
            addAfterViewChecked(component, checkForParent);
            addAfterViewInit(component, checkForParent);
        }
        addImportModules(component, checkForParent);
        addImportProviders(component, checkForParent);
        addInjects(component, checkForParent);
        addModals(component, checkForParent);
        addSignals(component, checkForParent);
        addInputs(component, checkForParent);
        addOutputs(component, checkForParent);

        unwrapMethods(component, checkForParent);

        addImportReferences(component, checkForParent);
        addComponentReferences(component, checkForParent);

        var references = AnnotationUtils.getAnnotation(componentClass, NgComponentReference.class);
        for (var reference : references)
        {
            if (!((reference.onSelf() && !checkForParent || (reference.onParent() && checkForParent))))
            {
                continue;
            }

            //check which type of component reference it is, and process it accordingly
            //var ref = IGuiceContext.get(reference.value());
            var configClass = reference.value();
            if (configClass.isAnnotationPresent(NgComponent.class))
            {
                //component
                compConfig.getImportModules().add(AnnotationUtils.getNgImportModule(AnnotationUtils.getTsFilename(configClass)));
                compConfig.getInjects().add(AnnotationUtils.getNgInject(AnnotationUtils.getTsVarName(configClass), AnnotationUtils.getTsFilename(configClass)));
                //reference
                if (ImportsStatementsComponent.class.isAssignableFrom(configClass))
                {
                    List<NgImportReference> ngImportReferences = new ImportsStatementsComponent()
                    {
                    }.putRelativeLinkInMap(compConfig.getRootComponent().getClass(), reference);
                    for (NgImportReference ngImportReference : ngImportReferences)
                    {
                        compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference(ngImportReference.value(), ngImportReference.reference()));
                    }
                }
            }
            else if (configClass.isAnnotationPresent(NgDirective.class))
            {
                //directive
                compConfig.getImportModules().add(AnnotationUtils.getNgImportModule(AnnotationUtils.getTsFilename(configClass)));
            }
            else if (configClass.isAnnotationPresent(NgServiceProvider.class))
            {
                //service provider
                NgServiceProvider anno = configClass.getAnnotation(NgServiceProvider.class);
                if (!anno.singleton())
                {
                    compConfig.getImportProviders().add(AnnotationUtils.getNgImportProvider(AnnotationUtils.getTsFilename(configClass)));
                }
                compConfig.getInjects().add(AnnotationUtils.getNgInject(anno.referenceName(), AnnotationUtils.getTsFilename(configClass)));
            }
        }
    }


    private void configureAngularLifeCycleMethods(Class<?> componentClass, IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        for (NgOnInit ni : AnnotationUtils.getAnnotation(componentClass, NgOnInit.class))
        {
            if (!Strings.isNullOrEmpty(ni.value()))
            {
                compConfig.getOnInit().add(AnnotationUtils.getNgOnInit(ni.value()));
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnInit", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnInit"));
            }
        }

        for (NgOnDestroy ni : AnnotationUtils.getAnnotation(componentClass, NgOnDestroy.class))
        {
            if (!Strings.isNullOrEmpty(ni.value()))
            {
                compConfig.getOnDestroy().add(AnnotationUtils.getNgOnDestroy(ni.value()));
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnDestroy", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnDestroy"));
            }
        }
        for (NgAfterContentChecked ni : AnnotationUtils.getAnnotation(componentClass, NgAfterContentChecked.class))
        {
            if (!Strings.isNullOrEmpty(ni.value()))
            {
                compConfig.getAfterContentChecked().add(AnnotationUtils.getNgAfterContentChecked(ni.value()));
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentChecked", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentChecked"));
            }
        }

        for (NgAfterContentInit ni : AnnotationUtils.getAnnotation(componentClass, NgAfterContentInit.class))
        {
            if (!Strings.isNullOrEmpty(ni.value()))
            {
                compConfig.getAfterContentInit().add(AnnotationUtils.getNgAfterContentInit(ni.value()));
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentInit", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentInit"));
            }
        }

        for (NgAfterViewChecked ni : AnnotationUtils.getAnnotation(componentClass, NgAfterViewChecked.class))
        {
            if (!Strings.isNullOrEmpty(ni.value()))
            {
                compConfig.getAfterViewChecked().add(AnnotationUtils.getNgAfterViewChecked(ni.value()));
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewChecked", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewChecked"));
            }
        }

        for (NgAfterViewInit ni : AnnotationUtils.getAnnotation(componentClass, NgAfterViewInit.class))
        {
            if (!Strings.isNullOrEmpty(ni.value()))
            {
                compConfig.getAfterViewInit().add(AnnotationUtils.getNgAfterViewInit(ni.value()));
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewInit", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewInit"));
            }
        }
        if (component instanceof INgComponent<?> ngComponent)
        {
            for (String s : ngComponent.onInit())
            {
                if (!Strings.isNullOrEmpty(s))
                {
                    compConfig.getOnInit().add(AnnotationUtils.getNgOnInit(s.trim()));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnInit"));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnInit", "@angular/core"));
                }
            }
            for (String s : ngComponent.onDestroy())
            {
                if (!Strings.isNullOrEmpty(s))
                {
                    compConfig.getOnDestroy().add(AnnotationUtils.getNgOnDestroy(s.trim()));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnDestroy"));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnDestroy", "@angular/core"));
                }
            }
            for (String s : ngComponent.afterViewInit())
            {
                if (!Strings.isNullOrEmpty(s))
                {
                    compConfig.getAfterViewInit().add(AnnotationUtils.getNgAfterViewInit(s.trim()));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewInit"));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewInit", "@angular/core"));
                }
            }
            for (String s : ngComponent.afterViewChecked())
            {
                if (!Strings.isNullOrEmpty(s))
                {
                    compConfig.getAfterViewChecked().add(AnnotationUtils.getNgAfterViewChecked(s.trim()));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewChecked"));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewChecked", "@angular/core"));
                }
            }
            for (String s : ngComponent.afterContentInit())
            {
                if (!Strings.isNullOrEmpty(s))
                {
                    compConfig.getAfterContentInit().add(AnnotationUtils.getNgAfterContentInit(s.trim()));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentInit"));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentInit", "@angular/core"));
                }
            }
            for (String s : ngComponent.afterContentChecked())
            {
                if (!Strings.isNullOrEmpty(s))
                {
                    compConfig.getAfterContentChecked().add(AnnotationUtils.getNgAfterContentChecked(s.trim()));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentChecked"));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentChecked", "@angular/core"));
                }
            }
        }
    }

    private void addPipes(IComponentHierarchyBase<?, ?> component, boolean checkForParent)
    {
        for (String value : component.asAttributeBase().getAttributes().values())
        {
            if (!Strings.isNullOrEmpty(value) && value.contains("| number"))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("DecimalPipe", "@angular/common"));
                compConfig.getImportModules().add(AnnotationUtils.getNgImportModule("DecimalPipe"));
            }
        }
        for (String value : component.asAttributeBase().getAttributes().values())
        {
            if (!Strings.isNullOrEmpty(value) && value.contains("| date"))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("DatePipe", "@angular/common"));
                compConfig.getImportModules().add(AnnotationUtils.getNgImportModule("DatePipe"));
            }
        }
        for (String value : component.asAttributeBase().getAttributes().values())
        {
            if (!Strings.isNullOrEmpty(value) && value.contains("| currency"))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("CurrencyPipe", "@angular/common"));
                compConfig.getImportModules().add(AnnotationUtils.getNgImportModule("CurrencyPipe"));
            }
        }
        for (String value : component.asAttributeBase().getAttributes().keySet())
        {
            if (!Strings.isNullOrEmpty(value) && value.contains("routerLink"))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("RouterModule", "@angular/router"));
                compConfig.getImportModules().add(AnnotationUtils.getNgImportModule("RouterModule"));
            }
        }
    }

    private void addLogicDirectives(IComponentHierarchyBase<?, ?> component, boolean checkForParent)
    {
        if (!checkForParent)
        {
            if (componentString.contains("*ngIf") || componentString.contains("@if"))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("NgIf", "@angular/common"));
                compConfig.getImportModules().add(AnnotationUtils.getNgImportModule("NgIf"));
            }
            if (componentString.contains("*ngFor") || componentString.contains("@for"))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("NgFor", "@angular/common"));
                compConfig.getImportModules().add(AnnotationUtils.getNgImportModule("NgFor"));
            }
        }
    }

    private void addFieldInputDirectives(IComponentHierarchyBase<?, ?> comp, boolean checkForParent)
    {
        var ngInputs = AnnotationUtils.getAnnotation(comp.getClass(), NgInput.class);
        if (!ngInputs.isEmpty())
        {
            for (NgInput a : ngInputs)
            {
                if ((a.onSelf() && !checkForParent) || (a.onParent() && checkForParent))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("input", "@angular/core"));
                    compConfig.getInputs().add(AnnotationUtils.getNgInput(a.value(), a.mandatory(), a.type(), a.attributeReference(), a.renderAttributeReference(), a.additionalData()));

                    //rootComponent.get().addConfiguration(AnnotationUtils.getNgField("@Input('" + a.value() + "') " + a.value() + (a.mandatory() ? "!" : "?") + " : " + (a.type() == null ? "any" : a.type().getSimpleName())));
                    if (a.type() != null && !a.type().equals(any.class))
                    {
                        List<NgImportReference> inputReferences = new ImportsStatementsComponent()
                        {
                        }.putRelativeLinkInMap(compConfig.getRootComponent().getClass(), AnnotationUtils.getNgComponentReference(a.type()));

                        for (NgImportReference inputReference : inputReferences)
                        {
                            compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference(inputReference.value(), inputReference.reference()));
                        }
                    }
                }
            }
        }
    }
}
