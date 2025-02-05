package com.jwebmp.core.base.angular.services.interfaces;

import com.google.common.base.Strings;
import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.components.NgComponentTagAttribute;
import com.jwebmp.core.base.angular.client.annotations.components.NgInput;
import com.jwebmp.core.base.angular.client.annotations.components.NgOutput;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorBody;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.annotations.functions.*;
import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.annotations.structures.NgGlobalField;
import com.jwebmp.core.base.angular.client.annotations.structures.NgInterface;
import com.jwebmp.core.base.angular.client.annotations.structures.NgMethod;
import com.jwebmp.core.base.angular.client.services.AnnotationHelper;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.modules.directives.OnClickListenerDirective;
import com.jwebmp.core.base.html.Input;
import com.jwebmp.core.base.html.interfaces.GlobalChildren;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.events.click.ClickAdapter;
import com.jwebmp.core.htmlbuilder.javascript.events.interfaces.IEvent;
import lombok.extern.java.Log;

import java.util.List;

@Log
public class OnComponentAdded implements IOnNgComponentAdded<OnComponentAdded>
{

    @Override
    public void onComponentAdded(IComponentHierarchyBase<GlobalChildren, ?> parent, IComponentHierarchyBase<GlobalChildren, ?> component)
    {
        // add all configurations to the component, the parent, and if necessary the parents first ng component find
        AnnotationHelper ah = IGuiceContext.get(AnnotationHelper.class);
        //run through children addition
        component.asBase()
                .getProperties()
                .put("tsConfigured", "true");
        var children = component.getChildren();
        for (GlobalChildren child : children)
        {
           /* if (child.getClass()
                     .isAnnotationPresent(NgComponent.class))
            {
                continue;
            }*/
            var cc = (IComponentHierarchyBase<GlobalChildren, ?>) child;
            if (!cc.asBase()
                    .getProperties()
                    .containsKey("tsConfigured"))
            {
                onComponentAdded(component, cc);
            }
        }

        configureByAttribute(component);
        unwrapAnnotations(component);
        unwrapMethods(component);
        determineImportReferences(component);

        //mark this component as an angular web component
        if (!component.getConfigurations(NgComponent.class)
                .isEmpty())
        {
            component.asBase()
                    .getProperties()
                    .put("angular-web-component", true);
        }

        //at this point all the configurations for all the classes must be set

        //load component annotated references into the ng component
       /* for (Class<? extends Annotation> annotation : AnnotationsMap.annotations)
        {
            var annos = ah.getAnnotationFromClass(component.getClass(), annotation);
            for (var anno : annos)
            {
                boolean onParent = false;
                try
                {
                    var meth = anno.getClass()
                                   .getMethod("onParent");
                    var result = meth.invoke(anno);
                    onParent = (boolean) result;
                }
                catch (NoSuchMethodException | IllegalAccessException e)
                {

                }
                catch (InvocationTargetException e)
                {
                    throw new RuntimeException(e);
                }
                if (onParent)
                {
                    IComponentHierarchyBase<?, ?> parent1 = ngComponent.getParent();
                    if (parent1 != null)
                    {
                        IComponentHierarchyBase<?, ?> ngComponent1 = findOwningNgComponent(parent1);
                        if (ngComponent1 != null)
                        {
                            ngComponent1.addConfiguration(anno);
                        }
                    }
                }
                else
                {
                    ngComponent.addConfiguration(anno);
                }
            }
        }*/


        //add all my custom configuratiaons


        //if i am a ng component, and render typescript is enabled, find the parents ng owning component and add a component reference
     /*   if (ngComponent.equals(component) && component instanceof IComponent<?>)
        {
            if (parent != null)
            {
                IComponentHierarchyBase<?, ?> parentNgComponent = findOwningNgComponent(parent);
                if (parentNgComponent != null)
                {
                    @SuppressWarnings("unchecked")
                    var componentReference = AnnotationUtils.getNgComponentReference((Class<? extends IComponent<?>>) component.getClass());
                    parentNgComponent.addConfiguration(componentReference);
                    parentNgComponent.addConfiguration(AnnotationUtils.getNgImportModule(component.getClass()
                                                                                                  .getSimpleName()));
                }
            }
        }*/

        //if i am the ng component, and render typescript is enabled, replace the tag with the angular reference

    }

    private void determineImportReferences(IComponentHierarchyBase<GlobalChildren, ?> component)
    {
        if (!component.getConfigurations(NgOnInit.class)
                .isEmpty())
        {
            component.addConfiguration(AnnotationUtils.getNgInterface("OnInit"));
            component.addConfiguration(AnnotationUtils.getNgImportReference("OnInit", "@angular/core"));
        }
        if (!component.getConfigurations(NgOnDestroy.class)
                .isEmpty())
        {
            component.addConfiguration(AnnotationUtils.getNgInterface("OnDestroy"));
            component.addConfiguration(AnnotationUtils.getNgImportReference("OnDestroy", "@angular/core"));
        }
        if (!component.getConfigurations(NgAfterContentChecked.class)
                .isEmpty())
        {
            component.addConfiguration(AnnotationUtils.getNgInterface("AfterContentChecked"));
            component.addConfiguration(AnnotationUtils.getNgImportReference("AfterContentChecked", "@angular/core"));
        }
        if (!component.getConfigurations(NgAfterContentInit.class)
                .isEmpty())
        {
            component.addConfiguration(AnnotationUtils.getNgInterface("AfterContentInit"));
            component.addConfiguration(AnnotationUtils.getNgImportReference("AfterContentInit", "@angular/core"));
        }
        if (!component.getConfigurations(NgAfterViewChecked.class)
                .isEmpty())
        {
            component.addConfiguration(AnnotationUtils.getNgInterface("AfterViewChecked"));
            component.addConfiguration(AnnotationUtils.getNgImportReference("AfterViewChecked", "@angular/core"));
        }
        if (!component.getConfigurations(NgAfterViewInit.class)
                .isEmpty())
        {
            component.addConfiguration(AnnotationUtils.getNgInterface("AfterViewInit"));
            component.addConfiguration(AnnotationUtils.getNgImportReference("AfterViewInit", "@angular/core"));
        }

        var references = component.getConfigurations(NgComponentReference.class);
        for (var reference : references)
        {
            if (reference instanceof ImportsStatementsComponent<?> imp)
            {
                List<NgImportReference> ngImportReferences = imp.putRelativeLinkInMap(getClass(), reference);
                for (NgImportReference ngImportReference : ngImportReferences)
                {
                    component.addConfiguration(AnnotationUtils.getNgImportReference(ngImportReference.value(), ngImportReference.value()));
                }
            }
        }

    }

    private void unwrapMethods(IComponentHierarchyBase<GlobalChildren, ?> comp)
    {
        if (comp instanceof IComponent<?> component)
        {
            for (String componentMethod : component.interfaces())
            {
                if (!Strings.isNullOrEmpty(componentMethod))
                {
                    var ng = AnnotationUtils.getNgInterface(componentMethod);
                    comp.addConfiguration(ng);
                }
            }

            for (String componentMethod : component.methods())
            {
                if (!Strings.isNullOrEmpty(componentMethod))
                {
                    var ng = AnnotationUtils.getNgComponentMethod(componentMethod);
                    comp.addConfiguration(ng);
                }
            }

            for (String componentField : component.globalFields())
            {
                if (!Strings.isNullOrEmpty(componentField))
                {
                    var ng = AnnotationUtils.getNgGlobalField(componentField);
                    comp.addConfiguration(ng);
                }
            }

            for (String componentField : component.fields())
            {
                if (!Strings.isNullOrEmpty(componentField))
                {
                    var ng = AnnotationUtils.getNgField(componentField);
                    comp.addConfiguration(ng);
                }
            }

            for (String componentField : component.moduleImports())
            {
                if (!Strings.isNullOrEmpty(componentField))
                {
                    var ng = AnnotationUtils.getNgImportModule(componentField);
                    comp.addConfiguration(ng);
                }
            }

            for (String contentChecked : component.onInit())
            {
                if (!Strings.isNullOrEmpty(contentChecked))
                {
                    var ng = AnnotationUtils.getNgOnInit(contentChecked);
                    comp.addConfiguration(ng);
                }
            }
            for (String contentChecked : component.onDestroy())
            {
                if (!Strings.isNullOrEmpty(contentChecked))
                {
                    var ng = AnnotationUtils.getNgOnDestroy(contentChecked);
                    comp.addConfiguration(ng);
                }
            }

            for (String contentChecked : component.afterContentChecked())
            {
                if (!Strings.isNullOrEmpty(contentChecked))
                {
                    var ng = AnnotationUtils.getNgAfterContentChecked(contentChecked);
                    comp.addConfiguration(ng);
                }
            }
            for (String contentChecked : component.afterContentInit())
            {
                if (!Strings.isNullOrEmpty(contentChecked))
                {
                    var ng = AnnotationUtils.getNgAfterContentInit(contentChecked);
                    comp.addConfiguration(ng);
                }
            }

            for (String contentChecked : component.afterViewInit())
            {
                if (!Strings.isNullOrEmpty(contentChecked))
                {
                    var ng = AnnotationUtils.getNgAfterViewInit(contentChecked);
                    comp.addConfiguration(ng);
                }
            }
            for (String contentChecked : component.afterViewChecked())
            {
                if (!Strings.isNullOrEmpty(contentChecked))
                {
                    var ng = AnnotationUtils.getNgAfterViewChecked(contentChecked);
                    comp.addConfiguration(ng);
                }
            }
            for (String componentConstructorParameter : component.constructorParameters())
            {
                if (!Strings.isNullOrEmpty(componentConstructorParameter))
                {
                    var ng = AnnotationUtils.getNgConstructorParameter(componentConstructorParameter);
                    comp.addConfiguration(ng);
                }
            }
            for (String componentConstructorParameter : component.constructorBody())
            {
                if (!Strings.isNullOrEmpty(componentConstructorParameter))
                {
                    var ng = AnnotationUtils.getNgConstructorBody(componentConstructorParameter);
                    comp.addConfiguration(ng);
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
                    comp.addConfiguration(ng);
                }
            }
            for (String input : component.inputs())
            {
                if (!Strings.isNullOrEmpty(input))
                {
                    var ng = AnnotationUtils.getNgInput(input);
                    comp.addConfiguration(ng);
                }
            }
        }

        for (IEvent<?, ?> iEvent : comp.getEventsAll())
        {
            if (iEvent instanceof ClickAdapter<?> ev)
            {
                comp.addConfiguration(AnnotationUtils.getNgComponentReference(OnClickListenerDirective.class));
            }
        }
    }

    /**
     * Takes the annotations and creates anno-objects out of them for equality and hash
     */
    private void unwrapAnnotations(IComponentHierarchyBase<GlobalChildren, ?> component)
    {
        var ah = IGuiceContext.get(AnnotationHelper.class);
        ah.getAnnotationFromClass(component.getClass(), NgField.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgField(a.value())
                        .setOnSelf(a.onSelf())
                        .setOnParent(a.onParent())));

        ah.getAnnotationFromClass(component.getClass(), NgGlobalField.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgGlobalField(a.value())
                        .setOnSelf(a.onSelf())
                        .setOnParent(a.onParent())));

        ah.getAnnotationFromClass(component.getClass(), NgMethod.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgMethod(a.value())
                        .setOnSelf(a.onSelf())
                        .setOnParent(a.onParent())));

        ah.getAnnotationFromClass(component.getClass(), NgInterface.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgInterface(a.value())
                        .setOnSelf(a.onSelf())
                        .setOnParent(a.onParent())));

        ah.getAnnotationFromClass(component.getClass(), NgInput.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgInput(a.value())
                        .setRenderAttributeReference(a.renderAttributeReference())
                        .setMandatory(a.mandatory())
                        .setAdditionalData(a.additionalData())
                        .setAttributeReference(a.attributeReference())
                        .setType((Class<? extends INgDataType<?>>) a.type())
                ));

        ah.getAnnotationFromClass(component.getClass(), NgComponentTagAttribute.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgComponentTagAttribute(a.key(), a.value())));

        ah.getAnnotationFromClass(component.getClass(), NgOutput.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgOutput(a.value(), a.parentMethodName())));

        ah.getAnnotationFromClass(component.getClass(), NgConstructorParameter.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgConstructorParameter(a.value())
                        .setOnSelf(a.onSelf())
                        .setOnParent(a.onParent())));

        ah.getAnnotationFromClass(component.getClass(), NgConstructorBody.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgConstructorBody(a.value())
                        .setOnSelf(a.onSelf())
                        .setOnParent(a.onParent())));

        ah.getAnnotationFromClass(component.getClass(), NgComponentReference.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgComponentReference((Class<? extends IComponent<?>>) a.value())));

        ah.getAnnotationFromClass(component.getClass(), NgDataTypeReference.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgDataTypeReference(a.value())));

        ah.getAnnotationFromClass(component.getClass(), NgImportModule.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgImportModule(a.value())
                        .setOnSelf(a.onSelf())
                        .setOnParent(a.onParent())));

        ah.getAnnotationFromClass(component.getClass(), NgImportProvider.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgImportProvider(a.value())
                        .setOnSelf(a.onSelf())
                        .setOnParent(a.onParent())));

        ah.getAnnotationFromClass(component.getClass(), NgImportReference.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgImportReference(a.value(), a.reference())
                        .setOnSelf(a.onSelf())
                        .setOnParent(a.onParent())));

        ah.getAnnotationFromClass(component.getClass(), NgAfterContentChecked.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgAfterContentChecked(a.value())));

        ah.getAnnotationFromClass(component.getClass(), NgAfterContentInit.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgAfterContentInit(a.value())));

        ah.getAnnotationFromClass(component.getClass(), NgAfterViewChecked.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgAfterViewChecked(a.value())));

        ah.getAnnotationFromClass(component.getClass(), NgAfterViewInit.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgAfterViewInit(a.value())));

        ah.getAnnotationFromClass(component.getClass(), NgOnDestroy.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgOnDestroy(a.value())));

        ah.getAnnotationFromClass(component.getClass(), NgOnInit.class)
                .forEach(a -> component.addConfiguration(AnnotationUtils.getNgOnInit(a.value())));

        ah.getAnnotationFromClass(component.getClass(), NgComponent.class)
                .forEach(a -> {
                    component.addConfiguration(AnnotationUtils.getNgComponent(a.value())
                            .setProvidedIn(a.providedIn())
                            .setStandalone(a.standalone()));
                    if (!Strings.isNullOrEmpty(a.providedIn()))
                    {
                        component.addConfiguration(AnnotationUtils.getNgImportReference("Injectable", "@angular/core"));
                    }
                });
    }

    private void configureByAttribute(IComponentHierarchyBase<GlobalChildren, ?> component)
    {
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
                .containsKey("[ngClass]"))
        {
            component.addConfiguration(AnnotationUtils.getNgImportReference("NgClass", "@angular/common"));
            component.addConfiguration(AnnotationUtils.getNgImportModule("NgClass"));
        }

        if (component.asAttributeBase()
                .getAttributes()
                .containsKey("[routerLink]") || component.asAttributeBase()
                .getAttributes()
                .containsKey("routerLink"))
        {
            component.addConfiguration(AnnotationUtils.getNgImportReference("RouterLink", "@angular/router"));
            component.addConfiguration(AnnotationUtils.getNgImportModule("RouterLink"));
        }

        if (component.asAttributeBase()
                .getAttributes()
                .containsKey("*ngIf"))
        {
            component.addConfiguration(AnnotationUtils.getNgImportReference("NgIf", "@angular/common"));
            component.addConfiguration(AnnotationUtils.getNgImportModule("NgIf"));
        }
        if (component.asAttributeBase()
                .getAttributes()
                .containsKey("*ngFor"))
        {
            component.addConfiguration(AnnotationUtils.getNgImportReference("NgForOf", "@angular/common"));
            component.addConfiguration(AnnotationUtils.getNgImportModule("NgForOf"));
        }
        for (String value : component.asAttributeBase()
                .getAttributes()
                .values())
        {
            if (!Strings.isNullOrEmpty(value) && value.contains("| json"))
            {
                component.addConfiguration(AnnotationUtils.getNgImportReference("JsonPipe", "@angular/common"));
                component.addConfiguration(AnnotationUtils.getNgImportModule("JsonPipe"));
            }
        }

        for (String value : component.asAttributeBase()
                .getAttributes()
                .values())
        {
            if (!Strings.isNullOrEmpty(value) && value.contains("| number"))
            {
                component.addConfiguration(AnnotationUtils.getNgImportReference("DecimalPipe", "@angular/common"));
                component.addConfiguration(AnnotationUtils.getNgImportModule("DecimalPipe"));
            }
        }
        if (component.asBase()
                .getText(0)
                .toString()
                .contains("| number"))
        {
            component.addConfiguration(AnnotationUtils.getNgImportReference("DecimalPipe", "@angular/common"));
            component.addConfiguration(AnnotationUtils.getNgImportModule("DecimalPipe"));
        }
        for (String value : component.asAttributeBase()
                .getAttributes()
                .values())
        {
            if (!Strings.isNullOrEmpty(value) && value.contains("| date"))
            {
                component.addConfiguration(AnnotationUtils.getNgImportReference("DatePipe", "@angular/common"));
                component.addConfiguration(AnnotationUtils.getNgImportModule("DatePipe"));
            }
        }
        if (component.asBase()
                .getText(0)
                .toString()
                .contains("| date"))
        {
            component.addConfiguration(AnnotationUtils.getNgImportReference("DatePipe", "@angular/common"));
            component.addConfiguration(AnnotationUtils.getNgImportModule("DatePipe"));
        }

        if (component.asAttributeBase()
                .getAttributes()
                .containsValue("ngModel") || component.asAttributeBase()
                .getAttributes()
                .containsValue("ngForm"))
        {
            component.addConfiguration(AnnotationUtils.getNgImportReference("FormsModule", "@angular/forms"));
            component.addConfiguration(AnnotationUtils.getNgImportModule("FormsModule"));
        }
    }

    @Override
    public Integer sortOrder()
    {
        return Integer.MAX_VALUE;
    }
}

