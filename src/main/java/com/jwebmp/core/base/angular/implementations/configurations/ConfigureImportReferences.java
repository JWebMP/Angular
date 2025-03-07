package com.jwebmp.core.base.angular.implementations.configurations;

import com.google.common.base.Strings;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.components.NgInput;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorBody;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.annotations.functions.*;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.annotations.structures.NgMethod;
import com.jwebmp.core.base.angular.client.services.any;
import com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.ImportsStatementsComponent;
import com.jwebmp.core.base.angular.modules.directives.OnClickListenerDirective;
import com.jwebmp.core.base.html.Input;
import com.jwebmp.core.base.html.interfaces.GlobalChildren;
import com.jwebmp.core.base.html.interfaces.events.GlobalEvents;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.databind.IOnComponentConfigured;
import com.jwebmp.core.events.click.ClickAdapter;
import com.jwebmp.core.htmlbuilder.javascript.events.interfaces.IEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ConfigureImportReferences implements IOnComponentConfigured<ConfigureImportReferences>
{

    private IComponentHierarchyBase<GlobalChildren, ?> rootComponent;

    @Override
    public void onComponentConfigured(IComponentHierarchyBase<GlobalChildren, ?> parent, IComponentHierarchyBase<GlobalChildren, ?> component)
    {
        onComponentConfigured(parent, component, false);
    }

    public void onComponentConfigured(IComponentHierarchyBase<GlobalChildren, ?> parent, IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        if (component.getClass().getCanonicalName().contains("TasksCreateModal"))
        {
            System.out.println("here");
        }
        IComponentHierarchyBase<GlobalChildren, ?> adjust = null;
        if (parent == null)
        {
            adjust = component;
            if (rootComponent == null)
                rootComponent = component;
            adjust = rootComponent;
        } else
        {
            adjust = rootComponent;
        }

        Class<?> componentClass = component.getClass();
        processClassToComponent(componentClass, adjust, checkForParent);

        if (!checkForParent)
        {
            for (GlobalChildren child : component.getChildren())
            {
                if (child instanceof INgComponent && child.getClass().isAnnotationPresent(NgComponent.class))
                {
                    //todo this must check for exactly the same process and apply to the same root component,
                    // where isParent() is true on the annotation, for the add<xxx> component items
                    //set check for parent to true here on next chain
                    onComponentConfigured(component, (IComponentHierarchyBase<GlobalChildren, ?>) child, true);
                    break;
                }
                onComponentConfigured(component, (IComponentHierarchyBase<GlobalChildren, ?>) child, false);
            }
        }

        if (component.getClass().getCanonicalName().contains("TasksCreateModal"))
        {
            System.out.println("here");
        }
        if (component.getClass().getSimpleName().equalsIgnoreCase("BSModal"))
        {
            System.out.println("here 2");
        }
        var references = AnnotationUtils.getAnnotation(componentClass, NgComponentReference.class);
        for (var reference : references)
        {
            if (parent == null && reference.onSelf())
            {
                processClassToComponent(reference.value(), adjust, checkForParent);
            } else if (parent != null && reference.onParent())
            {
                processClassToComponent(reference.value(), adjust, checkForParent);
            }

            if (reference instanceof ImportsStatementsComponent<?> imp)
            {
                List<NgImportReference> ngImportReferences = imp.putRelativeLinkInMap(getClass(), reference);
                for (NgImportReference ngImportReference : ngImportReferences)
                {
                    if (parent != null)
                    {
                        if (reference.onParent())
                        {
                            adjust.addConfiguration(AnnotationUtils.getNgImportReference(ngImportReference.value(), ngImportReference.value()));
                        }
                    } else
                    {
                        if (ngImportReference.onSelf())
                            adjust.addConfiguration(AnnotationUtils.getNgImportReference(ngImportReference.value(), ngImportReference.value()));
                    }
                }
            }
        }


        addPipes(component, checkForParent);
        addLogicDirectives(component, checkForParent);
        addFieldInputDirectives(component, checkForParent);
        addFormsImports(component, checkForParent);

        addImportReferences(component, checkForParent);
        addConstructorParameters(component, checkForParent);
        addConstructorBodies(component, checkForParent);
        addFields(component, checkForParent);
        addMethods(component, checkForParent);
        unwrapMethods(component, checkForParent);
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
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                        rootComponent.addConfiguration(ng);
                }
            }

            for (String componentMethod : component.methods())
            {
                if (!Strings.isNullOrEmpty(componentMethod))
                {
                    var ng = AnnotationUtils.getNgComponentMethod(componentMethod);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                        rootComponent.addConfiguration(ng);
                }
            }

            for (String componentField : component.globalFields())
            {
                if (!Strings.isNullOrEmpty(componentField))
                {
                    var ng = AnnotationUtils.getNgGlobalField(componentField);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                        rootComponent.addConfiguration(ng);
                }
            }

            for (String componentField : component.fields())
            {
                if (!Strings.isNullOrEmpty(componentField))
                {
                    var ng = AnnotationUtils.getNgField(componentField);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                        rootComponent.addConfiguration(ng);
                }
            }

            for (String componentField : component.moduleImports())
            {
                if (!Strings.isNullOrEmpty(componentField))
                {
                    var ng = AnnotationUtils.getNgImportModule(componentField);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                        rootComponent.addConfiguration(ng);
                }
            }

            for (String contentChecked : component.onInit())
            {
                if (!Strings.isNullOrEmpty(contentChecked))
                {
                    var ng = AnnotationUtils.getNgOnInit(contentChecked);
                    rootComponent.addConfiguration(ng);
                }
            }
            for (String contentChecked : component.onDestroy())
            {
                if (!Strings.isNullOrEmpty(contentChecked))
                {
                    var ng = AnnotationUtils.getNgOnDestroy(contentChecked);
                    rootComponent.addConfiguration(ng);
                }
            }

            for (String contentChecked : component.afterContentChecked())
            {
                if (!Strings.isNullOrEmpty(contentChecked))
                {
                    var ng = AnnotationUtils.getNgAfterContentChecked(contentChecked);
                    rootComponent.addConfiguration(ng);
                }
            }
            for (String contentChecked : component.afterContentInit())
            {
                if (!Strings.isNullOrEmpty(contentChecked))
                {
                    var ng = AnnotationUtils.getNgAfterContentInit(contentChecked);
                    rootComponent.addConfiguration(ng);
                }
            }

            for (String contentChecked : component.afterViewInit())
            {
                if (!Strings.isNullOrEmpty(contentChecked))
                {
                    var ng = AnnotationUtils.getNgAfterViewInit(contentChecked);
                    rootComponent.addConfiguration(ng);
                }
            }
            for (String contentChecked : component.afterViewChecked())
            {
                if (!Strings.isNullOrEmpty(contentChecked))
                {
                    var ng = AnnotationUtils.getNgAfterViewChecked(contentChecked);
                    rootComponent.addConfiguration(ng);
                }
            }
            for (String componentConstructorParameter : component.constructorParameters())
            {
                if (!Strings.isNullOrEmpty(componentConstructorParameter))
                {
                    var ng = AnnotationUtils.getNgConstructorParameter(componentConstructorParameter);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                        rootComponent.addConfiguration(ng);
                }
            }
            for (String componentConstructorParameter : component.constructorBody())
            {
                if (!Strings.isNullOrEmpty(componentConstructorParameter))
                {
                    var ng = AnnotationUtils.getNgConstructorBody(componentConstructorParameter);
                    if ((ng.isOnSelf() && !checkForParent) || (ng.isOnParent() && checkForParent))
                        rootComponent.addConfiguration(ng);
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
                        rootComponent.addConfiguration(ng);
                }
            }
            for (String input : component.inputs())
            {
                if (!Strings.isNullOrEmpty(input))
                {
                    var ng = AnnotationUtils.getNgInput(input);
                    rootComponent.addConfiguration(ng);
                }
            }
        }

        getChildrenHierarchy(comp).forEach(child -> {
            for (var iEvent : comp.asEventBase().getEvents())
            {
                if (iEvent instanceof ClickAdapter<?> ev)
                {
                    rootComponent.addConfiguration(AnnotationUtils.getNgComponentReference(OnClickListenerDirective.class));
                }
            }
        });
    }


    private void addFormsImports(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        if (component.asAttributeBase()
                .getAttributes()
                .containsValue("ngModel") || component.asAttributeBase()
                .getAttributes()
                .containsValue("ngForm"))
        {
            component.addConfiguration(AnnotationUtils.getNgImportReference("FormsModule", "@angular/forms"));
            component.addConfiguration(AnnotationUtils.getNgImportModule("FormsModule"));
        }
        if (component instanceof Input input)
        {
            if (!input.getProperties()
                    .containsKey("noName") && input.getAttributes()
                    .containsKey("[(ngModel)]"))
            {
                input.addAttribute("#" + (Strings.isNullOrEmpty(input.getName()) ? input.getID() : input.getName()), "ngModel");
                component.addConfiguration(AnnotationUtils.getNgImportReference("FormsModule", "@angular/forms"));
                component.addConfiguration(AnnotationUtils.getNgImportModule("FormsModule"));
            }
        }
        if (component.asAttributeBase()
                .getAttributes()
                .containsKey("ngClass"))
        {
            component.addConfiguration(AnnotationUtils.getNgImportReference("NgClass", "@angular/common"));
            component.addConfiguration(AnnotationUtils.getNgImportModule("NgClass"));
        }
    }


    private void addConstructorParameters(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        if (AnnotationUtils.hasAnnotation(component.getClass(), NgConstructorParameter.class))
        {
            AnnotationUtils.getAnnotation(component.getClass(), NgConstructorParameter.class)
                    .forEach(constructorParameter -> {
                        if ((constructorParameter.onSelf() && !checkForParent) || constructorParameter.onParent() && checkForParent)
                            rootComponent.addConfiguration(AnnotationUtils.getNgConstructorParameter(constructorParameter.value()));
                    });
        }
    }

    private void addConstructorBodies(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        if (AnnotationUtils.hasAnnotation(component.getClass(), NgConstructorBody.class))
        {
            AnnotationUtils.getAnnotation(component.getClass(), NgConstructorBody.class)
                    .forEach(constructorParameter -> {
                        if ((constructorParameter.onSelf() && !checkForParent) || constructorParameter.onParent() && checkForParent)
                            rootComponent.addConfiguration(AnnotationUtils.getNgConstructorBody(constructorParameter.value()));
                    });
        }
    }

    private void addFields(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        if (AnnotationUtils.hasAnnotation(component.getClass(), NgField.class))
        {
            AnnotationUtils.getAnnotation(component.getClass(), NgField.class)
                    .forEach(constructorParameter -> {
                        if ((constructorParameter.onSelf() && !checkForParent) || constructorParameter.onParent() && checkForParent)
                            rootComponent.addConfiguration(AnnotationUtils.getNgField(constructorParameter.value()));
                    });
        }
    }

    private void addMethods(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        if (AnnotationUtils.hasAnnotation(component.getClass(), NgMethod.class))
        {
            AnnotationUtils.getAnnotation(component.getClass(), NgMethod.class)
                    .forEach(constructorParameter -> {
                        if ((constructorParameter.onSelf() && !checkForParent) || constructorParameter.onParent() && checkForParent)
                            rootComponent.addConfiguration(AnnotationUtils.getNgMethod(constructorParameter.value()));
                    });
        }
    }

    private void addImportReferences(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        if (AnnotationUtils.hasAnnotation(component.getClass(), NgImportReference.class))
        {
            AnnotationUtils.getAnnotation(component.getClass(), NgImportReference.class)
                    .forEach(constructorParameter -> {
                        if ((constructorParameter.onSelf() && !checkForParent) || constructorParameter.onParent() && checkForParent)
                            rootComponent.addConfiguration(AnnotationUtils.getNgImportReference(constructorParameter.value(), constructorParameter.reference()));
                    });
        }
        var references = component.getConfigurations(NgComponentReference.class);
        for (var reference : references)
        {
            if (reference instanceof ImportsStatementsComponent<?> imp)
            {
                List<NgImportReference> ngImportReferences = imp.putRelativeLinkInMap(getClass(), reference);
                for (NgImportReference ngImportReference : ngImportReferences)
                {
                    if ((ngImportReference.onSelf() && !checkForParent) || ngImportReference.onParent() && checkForParent)
                        component.addConfiguration(AnnotationUtils.getNgImportReference(ngImportReference.value(), ngImportReference.value()));
                }
            }
        }
    }

    public Set<IComponentHierarchyBase<?, ?>> getChildrenHierarchy(IComponentHierarchyBase<?, ?> hierarchyBase)
    {
        Set<IComponentHierarchyBase<?, ?>> components = new LinkedHashSet<>();
        for (IComponentHierarchyBase<?, ?> iComponentHierarchyBase : hierarchyBase.getChildrenHierarchy(false))
        {
            if (iComponentHierarchyBase instanceof INgComponent<?> &&
                    iComponentHierarchyBase.getClass().isAnnotationPresent(NgComponent.class))
            {
                break;
            }
            components.add(iComponentHierarchyBase);
        }
        return components;
    }


    private void processClassToComponent(Class<?> componentClass, IComponentHierarchyBase<GlobalChildren, ?> adjust, boolean checkForParent)
    {
        if (AnnotationUtils.hasAnnotation(componentClass, NgOnInit.class))
        {
            for (NgOnInit ni : AnnotationUtils.getAnnotation(componentClass, NgOnInit.class))
            {
                rootComponent.addConfiguration(AnnotationUtils.getNgInterface("OnInit"));
                rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("OnInit", "@angular/core"));
            }
        }
        if (AnnotationUtils.hasAnnotation(componentClass, NgOnDestroy.class))
        {
            for (NgOnDestroy ni : AnnotationUtils.getAnnotation(componentClass, NgOnDestroy.class))
            {
                rootComponent.addConfiguration(AnnotationUtils.getNgInterface("OnDestroy"));
                rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("OnDestroy", "@angular/core"));
            }
        }
        if (AnnotationUtils.hasAnnotation(componentClass, NgAfterContentChecked.class))
        {
            for (NgAfterContentChecked ni : AnnotationUtils.getAnnotation(componentClass, NgAfterContentChecked.class))
            {
                rootComponent.addConfiguration(AnnotationUtils.getNgInterface("AfterContentChecked"));
                rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("AfterContentChecked", "@angular/core"));
            }
        }
        if (AnnotationUtils.hasAnnotation(componentClass, NgAfterContentInit.class))
        {
            for (NgAfterContentInit ni : AnnotationUtils.getAnnotation(componentClass, NgAfterContentInit.class))
            {
                rootComponent.addConfiguration(AnnotationUtils.getNgInterface("AfterContentInit"));
                rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("AfterContentInit", "@angular/core"));
            }
        }
        if (AnnotationUtils.hasAnnotation(componentClass, NgAfterViewChecked.class))
        {
            for (NgAfterViewChecked ni : AnnotationUtils.getAnnotation(componentClass, NgAfterViewChecked.class))
            {
                rootComponent.addConfiguration(AnnotationUtils.getNgInterface("AfterViewChecked"));
                rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("AfterViewChecked", "@angular/core"));
            }
        }
        if (AnnotationUtils.hasAnnotation(componentClass, NgAfterViewInit.class))
        {
            for (NgAfterViewInit ni : AnnotationUtils.getAnnotation(componentClass, NgAfterViewInit.class))
            {
                rootComponent.addConfiguration(AnnotationUtils.getNgInterface("AfterViewInit"));
                rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("AfterViewInit", "@angular/core"));
            }
        }
    }

    private void addPipes(IComponentHierarchyBase<?, ?> component, boolean checkForParent)
    {
        for (String value : component.asAttributeBase().getAttributes().values())
        {
            if (!Strings.isNullOrEmpty(value) && value.contains("| number"))
            {
                rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("DecimalPipe", "@angular/common"));
                rootComponent.addConfiguration(AnnotationUtils.getNgImportModule("DecimalPipe"));
            }
        }
        if (component.asBase().getText(0).toString().contains("| number"))
        {
            rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("DecimalPipe", "@angular/common"));
            rootComponent.addConfiguration(AnnotationUtils.getNgImportModule("DecimalPipe"));
        }
        for (String value : component.asAttributeBase().getAttributes().values())
        {
            if (!Strings.isNullOrEmpty(value) && value.contains("| date"))
            {
                rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("DatePipe", "@angular/common"));
                rootComponent.addConfiguration(AnnotationUtils.getNgImportModule("DatePipe"));
            }
        }
        if (component.asBase().getText(0).toString().contains("| date"))
        {
            rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("DatePipe", "@angular/common"));
            rootComponent.addConfiguration(AnnotationUtils.getNgImportModule("DatePipe"));
        }
        for (String value : component.asAttributeBase().getAttributes().values())
        {
            if (!Strings.isNullOrEmpty(value) && value.contains("| currency"))
            {
                rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("CurrencyPipe", "@angular/common"));
                rootComponent.addConfiguration(AnnotationUtils.getNgImportModule("CurrencyPipe"));
            }
        }
        for (String value : component.asAttributeBase().getAttributes().values())
        {
            if (!Strings.isNullOrEmpty(value) && value.contains("routerLink"))
            {
                component.addConfiguration(AnnotationUtils.getNgImportReference("RouterLink", "@angular/router"));
                component.addConfiguration(AnnotationUtils.getNgImportModule("RouterLink"));
            }
        }
        if (component.asBase().getText(0).toString().contains("| currency"))
        {
            rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("CurrencyPipe", "@angular/common"));
            rootComponent.addConfiguration(AnnotationUtils.getNgImportModule("CurrencyPipe"));
        }
    }

    private void addLogicDirectives(IComponentHierarchyBase<?, ?> component, boolean checkForParent)
    {
        if (component.asAttributeBase().getAttributes().containsKey("*ngIf"))
        {
            rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("NgIf", "@angular/common"));
            rootComponent.addConfiguration(AnnotationUtils.getNgImportModule("NgIf"));
        }
        if (component.asAttributeBase().getAttributes().containsKey("*ngFor"))
        {
            rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("NgForOf", "@angular/common"));
            rootComponent.addConfiguration(AnnotationUtils.getNgImportModule("NgForOf"));
        }
    }

    private void addFieldInputDirectives(IComponentHierarchyBase<?, ?> comp, boolean checkForParent)
    {
        var ngInputs = comp.getConfigurations(NgInput.class, false);
        if (ngInputs != null && !ngInputs.isEmpty())
        {
            for (NgInput a : ngInputs)
            {
                rootComponent.addConfiguration(AnnotationUtils.getNgField("@Input('" + a.value() + "') " + a.value() + (a.mandatory() ? "!" : "?") + " : " + (a.type() == null ? "any" : a.type()
                        .getSimpleName())));
                if (a.type() != null && !a.type()
                        .equals(any.class))
                {
                    rootComponent.addConfiguration(AnnotationUtils.getNgComponentReference((Class<? extends IComponent<?>>) a.type()));
                }
                rootComponent.addConfiguration(AnnotationUtils.getNgImportReference("Input", "@angular/core"));
            }
        }
    }

}
