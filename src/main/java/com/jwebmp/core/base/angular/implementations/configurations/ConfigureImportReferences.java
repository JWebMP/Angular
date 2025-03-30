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
import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.client.annotations.structures.*;
import com.jwebmp.core.base.angular.client.services.AbstractReferences;
import com.jwebmp.core.base.angular.client.services.ComponentConfiguration;
import com.jwebmp.core.base.angular.client.services.DataServiceConfiguration;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.html.DivSimple;
import com.jwebmp.core.base.html.Input;
import com.jwebmp.core.base.html.interfaces.GlobalChildren;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.databind.IConfiguration;
import com.jwebmp.core.databind.IOnComponentConfigured;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigureImportReferences extends AbstractReferences<ComponentConfiguration<?>> implements IOnComponentConfigured<ConfigureImportReferences>
{
    private final ComponentConfiguration<?> compConfig = new ComponentConfiguration<>();
    private String componentString;

    public ComponentConfiguration<?> getConfiguration()
    {
        return compConfig;
    }

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
        processClassToComponent(componentClass, component, checkForParent);

        var disconnectedChildren = new ArrayList<>(component.getChildren());

        for (GlobalChildren child : disconnectedChildren)
        {
            IComponentHierarchyBase<?, ?> childComponent = (IComponentHierarchyBase<?, ?>) child;
            if (!checkForParent)
            {
                if (INgComponent.class.isAssignableFrom(child.getClass())
                        && child.getClass().getDeclaredAnnotationsByType(NgComponent.class).length > 0)
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
                        compConfig.getImportModules().add(AnnotationUtils.getNgImportModule(ngImportReferences.getFirst().value()));
                        //replace the tag with the angular component reference
                        updateTag(component.getChildren(), childComponent);
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
        configureAngularLifeCycleMethods(componentClass, component, checkForParent);
        processComponentConfigurations(component, checkForParent);

        if (parent == null)
        {
            //finished
            component.asBase().getProperties().put("AngularConfiguration", compConfig);
        }
    }

    private void updateTag(List<GlobalChildren> childList, IComponentHierarchyBase<?, ?> component)
    {
        DivSimple<?> replacementTag = new DivSimple<>();
        var anno = component.getClass().getAnnotationsByType(NgComponent.class)[0];
        replacementTag.setTag(anno.value());
        replacementTag.asAttributeBase().getAttributes().putAll(component.asAttributeBase().getOverrideAttributes());

        var inputs = AnnotationUtils.getAnnotation(component.getClass(), NgInput.class);
        inputs.addAll(component.getConfigurations(NgInput.class, false));

        Set<NgInput> uniqueValues = new HashSet<>();
        for (NgInput a : inputs)
        {
            if (a.renderAttributeReference())
            {
                if (uniqueValues.add(a))
                {
                    replacementTag.asAttributeBase()
                            .getAttributes()
                            .put("[" + a.value() + "]",
                                    (Strings.isNullOrEmpty(a.attributeReference()) ? a.value() : a.attributeReference()));
                }
            }
        }

        var outputs = AnnotationUtils.getAnnotation(component.getClass(), NgOutput.class);
        outputs.addAll(component.getConfigurations(NgOutput.class, false));

        Set<NgOutput> uniqueOutValues = new HashSet<>();
        for (NgOutput a : outputs)
        {
            if (uniqueOutValues.add(a))
            {
                replacementTag.asAttributeBase()
                        .getAttributes()
                        .put("(" + a.value() + ")", a.parentMethodName());
            }
        }

        var childIndex = childList.indexOf(component);
        if (childIndex >= 0)
        {
            childList.set(childIndex, replacementTag);
        }
    }

    private void processComponentConfigurations(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        for (IConfiguration configuration : component.getConfigurations())
        {
            if (configuration instanceof NgComponentReference ngComponentReference && component instanceof ImportsStatementsComponent<?> imp && compConfig instanceof INgComponent)
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
            else if (configuration instanceof NgComponentReference ngComponentReference)
            {
                Class<?> configReferenceClass = ngComponentReference.value();
                if (INgDirective.class.isAssignableFrom(configReferenceClass) ||
                        INgModule.class.isAssignableFrom(configReferenceClass))
                {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    List<NgImportReference>
                            ngImportReferences = new ImportsStatementsComponent()
                    {
                    }
                            .putRelativeLinkInMap(((INgComponent<?>) compConfig.getRootComponent()).getClass(), ngComponentReference);
                    for (NgImportReference ngImportReference : ngImportReferences)
                    {
                        compConfig.getImportReferences().add((AnnotationUtils.getNgImportReference(ngImportReference.value(), ngImportReference.reference())));
                    }
                    compConfig.getImportModules().add(AnnotationUtils.getNgImportModule(ngImportReferences.getFirst().value()));
                }
                else if (INgProvider.class.isAssignableFrom(configReferenceClass))
                {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    List<NgImportReference>
                            ngImportReferences = new ImportsStatementsComponent()
                    {
                    }
                            .putRelativeLinkInMap(((INgComponent<?>) compConfig.getRootComponent()).getClass(), ngComponentReference);
                    for (NgImportReference ngImportReference : ngImportReferences)
                    {
                        compConfig.getImportReferences().add((AnnotationUtils.getNgImportReference(ngImportReference.value(), ngImportReference.reference())));
                    }
                    //add parent configs for this
                    processClassToComponent(configReferenceClass, component, true);
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
                if ((ngComponentReference.onSelf() && !checkForParent) || (ngComponentReference.onParent() && checkForParent))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnInit", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnInit"));
                    compConfig.getOnInit().add(AnnotationUtils.getNgOnInit(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgOnDestroy ngComponentReference)
            {
                if ((ngComponentReference.onSelf() && !checkForParent) || (ngComponentReference.onParent() && checkForParent))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnDestroy", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnDestroy"));
                    compConfig.getOnDestroy().add(AnnotationUtils.getNgOnDestroy(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgAfterViewInit ngComponentReference)
            {
                if ((ngComponentReference.onSelf() && !checkForParent) || (ngComponentReference.onParent() && checkForParent))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewInit", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewInit"));
                    compConfig.getAfterViewInit().add(AnnotationUtils.getNgAfterViewInit(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgAfterViewChecked ngComponentReference)
            {
                if ((ngComponentReference.onSelf() && !checkForParent) || (ngComponentReference.onParent() && checkForParent))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewChecked", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewChecked"));
                    compConfig.getAfterViewChecked().add(AnnotationUtils.getNgAfterViewChecked(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgAfterContentInit ngComponentReference)
            {
                if ((ngComponentReference.onSelf() && !checkForParent) || (ngComponentReference.onParent() && checkForParent))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentInit", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentInit"));
                    compConfig.getAfterContentInit().add(AnnotationUtils.getNgAfterContentInit(ngComponentReference.value()));
                }
            }
            else if (configuration instanceof NgAfterContentChecked ngComponentReference)
            {
                if ((ngComponentReference.onSelf() && !checkForParent) || (ngComponentReference.onParent() && checkForParent))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentChecked", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentChecked"));
                    compConfig.getAfterContentChecked().add(AnnotationUtils.getNgAfterContentChecked(ngComponentReference.value()));
                }
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
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("computed", "@angular/core"));
                    compConfig.getSignals().add(AnnotationUtils.getNgSignal(ngComponentReference.referenceName(), ngComponentReference.value(), ngComponentReference.type()));
                }
                if (ngComponentReference.onParent() && checkForParent)
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("signal", "@angular/core"));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("computed", "@angular/core"));
                    compConfig.getSignals().add(AnnotationUtils.getNgSignal(ngComponentReference.referenceName(), ngComponentReference.value(), ngComponentReference.type()));
                }
            }
        }
    }


    private void unwrapMethods(IComponentHierarchyBase<GlobalChildren, ?> comp, boolean checkForParent)
    {
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
        }

        if (comp instanceof IComponent<?> component)
        {
            super.unwrapMethods(component, checkForParent);
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
        }

        if (comp instanceof INgComponent<?> component)
        {

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

    protected void addImportModules(Class<?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component, NgImportModule.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getImportModules().add(AnnotationUtils.getNgImportModule(ngMethod.value()));
            }
        });
    }

    protected void addImportProviders(Class<?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component, NgImportProvider.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getImportProviders().add(AnnotationUtils.getNgImportProvider(ngMethod.value()));
            }
        });
    }

    protected void addModals(Class<?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component, NgModal.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getModals().add(AnnotationUtils.getNgModal(ngMethod.referenceName(), ngMethod.value()));
            }
        });
    }

    protected void addSignals(Class<?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component, NgSignal.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("signal", "@angular/core"));
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("computed", "@angular/core"));
                compConfig.getSignals().add(AnnotationUtils.getNgSignal(ngMethod.referenceName(), ngMethod.value(), ngMethod.type()));
            }
        });
    }

    protected void addInputs(Class<?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component, NgInput.class).forEach(ngMethod -> {
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

    protected void addOutputs(Class<?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component, NgOutput.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("output", "@angular/core"));
                compConfig.getOutputs().add(AnnotationUtils.getNgOutput(ngMethod.value(), ngMethod.parentMethodName()));
            }
        });
    }


    protected void addAfterViewInit(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgAfterViewInit.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewInit", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewInit"));
                compConfig.getAfterViewInit().add(AnnotationUtils.getNgAfterViewInit(ngMethod.value()));
            }
        });
    }

    protected void addAfterViewChecked(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgAfterViewChecked.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewChecked", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewChecked"));
                compConfig.getAfterViewChecked().add(AnnotationUtils.getNgAfterViewChecked(ngMethod.value()));
            }
        });
    }

    protected void addAfterContentChecked(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgAfterContentChecked.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentChecked", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentChecked"));
                compConfig.getAfterContentChecked().add(AnnotationUtils.getNgAfterContentChecked(ngMethod.value()));
            }
        });
    }

    protected void addAfterContentInit(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgAfterContentInit.class).forEach(ngMethod -> {
            if ((ngMethod.onSelf() && !checkForParent) || (ngMethod.onParent() && checkForParent))
            {
                compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentInit", "@angular/core"));
                compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentInit"));
                compConfig.getAfterContentInit().add(AnnotationUtils.getNgAfterContentInit(ngMethod.value()));
            }
        });
    }

    protected void addComponentReferences(IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        AnnotationUtils.getAnnotation(component.getClass(), NgComponentReference.class).forEach(importReference -> {
            if ((importReference.onSelf() && !checkForParent) || (importReference.onParent() && checkForParent))
            {
                List<NgImportReference> irs = retrieveRelativePathForReference(importReference);
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
                    processClassToComponent(importReference.value(), component, true);
                }
                if (INgServiceProvider.class.isAssignableFrom(importReference.value()) ||
                        INgDataService.class.isAssignableFrom(importReference.value()) ||
                        INgProvider.class.isAssignableFrom(importReference.value())
                )
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("inject", "@angular/core"));
                    processClassToComponent(importReference.value(), component, true);
                }
            }
        });
    }

    protected List<NgImportReference> retrieveRelativePathForReference(NgComponentReference importReference)
    {
        List<NgImportReference> irs = new ImportsStatementsComponent()
        {
        }.putRelativeLinkInMap(compConfig.getRootComponent().getClass(), importReference);
        return irs;
    }

    protected void processClassToComponent(Class<?> componentClass, IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        addPipes(component, checkForParent);
        addLogicDirectives(checkForParent);
        addFieldInputDirectives(componentClass, checkForParent);
        addFormsImports(component, checkForParent);

        addConstructorParameters(componentClass, checkForParent);
        addConstructorBodies(componentClass, checkForParent);
        addFields(componentClass, checkForParent);
        addGlobalFields(componentClass, checkForParent);
        addMethods(componentClass, checkForParent);
        addInterfaces(componentClass, checkForParent);

        addOnInit(componentClass, checkForParent);
        addOnDestroy(componentClass, checkForParent);
        addAfterContentChecked(component, checkForParent);
        addAfterContentInit(component, checkForParent);
        addAfterViewChecked(component, checkForParent);
        addAfterViewInit(component, checkForParent);

        addImportModules(componentClass, checkForParent);
        addImportProviders(componentClass, checkForParent);
        addInjects(componentClass, checkForParent);
        addModals(componentClass, checkForParent);
        addSignals(componentClass, checkForParent);
        addInputs(componentClass, checkForParent);
        addOutputs(componentClass, checkForParent);

        if (!checkForParent)
        {
            unwrapMethods(component, checkForParent);
        }

        addImportReferences(componentClass, checkForParent);
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
                    List<NgImportReference> ngImportReferences = retrieveRelativePathForReference(reference);
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


    protected void configureAngularLifeCycleMethods(Class<?> componentClass, IComponentHierarchyBase<GlobalChildren, ?> component, boolean checkForParent)
    {
        for (NgOnInit ni : AnnotationUtils.getAnnotation(componentClass, NgOnInit.class))
        {
            if (!Strings.isNullOrEmpty(ni.value()))
            {
                if ((ni.onSelf() && !checkForParent) || (ni.onParent() && checkForParent))
                {
                    compConfig.getOnInit().add(AnnotationUtils.getNgOnInit(ni.value()));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnInit", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnInit"));
                }
            }
        }

        for (NgOnDestroy ni : AnnotationUtils.getAnnotation(componentClass, NgOnDestroy.class))
        {
            if (!Strings.isNullOrEmpty(ni.value()))
            {
                if ((ni.onSelf() && !checkForParent) || (ni.onParent() && checkForParent))
                {
                    compConfig.getOnDestroy().add(AnnotationUtils.getNgOnDestroy(ni.value()));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("OnDestroy", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("OnDestroy"));
                }
            }
        }
        for (NgAfterContentChecked ni : AnnotationUtils.getAnnotation(componentClass, NgAfterContentChecked.class))
        {
            if (!Strings.isNullOrEmpty(ni.value()))
            {
                if ((ni.onSelf() && !checkForParent) || (ni.onParent() && checkForParent))
                {
                    compConfig.getAfterContentChecked().add(AnnotationUtils.getNgAfterContentChecked(ni.value()));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentChecked", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentChecked"));
                }
            }
        }

        for (NgAfterContentInit ni : AnnotationUtils.getAnnotation(componentClass, NgAfterContentInit.class))
        {
            if (!Strings.isNullOrEmpty(ni.value()))
            {
                if ((ni.onSelf() && !checkForParent) || (ni.onParent() && checkForParent))
                {
                    compConfig.getAfterContentInit().add(AnnotationUtils.getNgAfterContentInit(ni.value()));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterContentInit", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterContentInit"));
                }
            }
        }

        for (NgAfterViewChecked ni : AnnotationUtils.getAnnotation(componentClass, NgAfterViewChecked.class))
        {
            if (!Strings.isNullOrEmpty(ni.value()))
            {
                if ((ni.onSelf() && !checkForParent) || (ni.onParent() && checkForParent))
                {
                    compConfig.getAfterViewChecked().add(AnnotationUtils.getNgAfterViewChecked(ni.value()));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewChecked", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewChecked"));
                }
            }
        }

        for (NgAfterViewInit ni : AnnotationUtils.getAnnotation(componentClass, NgAfterViewInit.class))
        {
            if (!Strings.isNullOrEmpty(ni.value()))
            {
                if ((ni.onSelf() && !checkForParent) || (ni.onParent() && checkForParent))
                {
                    compConfig.getAfterViewInit().add(AnnotationUtils.getNgAfterViewInit(ni.value()));
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("AfterViewInit", "@angular/core"));
                    compConfig.getInterfaces().add(AnnotationUtils.getNgInterface("AfterViewInit"));
                }
            }
        }
        if (component instanceof INgComponent<?> ngComponent && !checkForParent)
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


    private void addLogicDirectives(boolean checkForParent)
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

    private void addFieldInputDirectives(Class<?> comp, boolean checkForParent)
    {
        var ngInputs = AnnotationUtils.getAnnotation(comp, NgInput.class);
        if (!ngInputs.isEmpty())
        {
            for (NgInput a : ngInputs)
            {
                if ((a.onSelf() && !checkForParent) || (a.onParent() && checkForParent))
                {
                    compConfig.getImportReferences().add(AnnotationUtils.getNgImportReference("input", "@angular/core"));
                    compConfig.getInputs().add(AnnotationUtils.getNgInput(a.value(), a.mandatory(), a.type(), a.attributeReference(), a.renderAttributeReference(), a.additionalData()));

                    //rootComponent.get().addConfiguration(AnnotationUtils.getNgField("@Input('" + a.value() + "') " + a.value() + (a.mandatory() ? "!" : "?") + " : " + (a.type() == null ? "any" : a.type().getSimpleName())));
                    if (a.type() != null && !a.type().isAnnotationPresent(NgIgnoreImportReference.class))
                    {
                        List<NgImportReference> inputReferences = retrieveRelativePathForReference(AnnotationUtils.getNgComponentReference(a.type()));

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
