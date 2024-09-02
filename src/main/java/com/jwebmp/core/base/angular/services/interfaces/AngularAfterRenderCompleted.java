package com.jwebmp.core.base.angular.services.interfaces;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.services.AnnotationHelper;
import com.jwebmp.core.base.html.interfaces.GlobalChildren;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.databind.IAfterRenderComplete;
import com.jwebmp.core.databind.IConfiguration;
import lombok.extern.java.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static com.jwebmp.core.base.angular.services.interfaces.IOnNgComponentAdded.findOwningNgComponent;

@Log
public class AngularAfterRenderCompleted implements IAfterRenderComplete<AngularAfterRenderCompleted>
{

    @Override
    public void process(IComponentHierarchyBase<?, ?> component)
    {
        AnnotationHelper ah = IGuiceContext.get(AnnotationHelper.class);
        //run through children addition
        component.asBase()
                 .getProperties()
                 .put("tsReRender", "true");
        var children = component.getChildren();
        for (GlobalChildren child : children)
        {
            if (child.getClass()
                     .isAnnotationPresent(NgComponent.class))
            {
                continue;
            }
            var cc = (IComponentHierarchyBase<GlobalChildren, ?>) child;
            if (!cc.asBase()
                   .getProperties()
                   .containsKey("tsReRender"))
            {
                if (cc.getConfigurations(NgComponent.class)
                      .isEmpty())
                {
                    process(cc);
                }
            }
        }

        IComponentHierarchyBase<?, ?> ngComponent = findOwningNgComponent(component);
        if (ngComponent == null)
        {
            //not ready yet, wait for adding to a component
            log.log(Level.SEVERE, "Reading component no on an angular tree - " + component.getClass()
                                                                                          .getName());
            return;
            //ngComponent = component;
        }

        moveParentConfigurations(component, ngComponent);

/*
        if (ngComponent.equals(component) && component.getClass()
                                                      .isAnnotationPresent(NgComponent.class))
        {
            NgComponent annotation = component.getClass()
                                              .getAnnotation(NgComponent.class);

            DivSimple<?> displayDiv = new DivSimple<>().setTag(annotation.value())
                                                       .setRenderIDAttribute(false);
            displayDiv.setID(component.asBase()
                                      .getID());

            //for all inputs on my @ngcomponent render the input tag attributes for this web component
            var inputs = ngComponent.getConfigurations(NgInput.class, false);
            Set<NgInput> uniqueValues = new HashSet<>();
            for (NgInput a : inputs)
            {
                if (a.renderAttributeReference())
                {
                    if (uniqueValues.add(a))
                    {
                        displayDiv.getAttributes()
                                  .put("[" + a.value() + "]", "" + (Strings.isNullOrEmpty(a.attributeReference()) ? a.value() : a.attributeReference()) + "");
                    }
                }
            }

            //Add any custom tag attributes specified to this ng component
            List<NgComponentTagAttribute> tagAttributes = ah.getAnnotationFromClass(component.getClass(), NgComponentTagAttribute.class);
            Set<NgComponentTagAttribute> uniqueTagValues = new HashSet<>();
            for (NgComponentTagAttribute a : tagAttributes)
            {
                if (uniqueTagValues.add(a))
                {
                    displayDiv.getAttributes()
                              .put(a.key(), a.value());
                }
            }


            if (component.readChildrenPropertyFirstResult("renderAttributes", false))
            {
                Set<String> removables = new HashSet<>();

                displayDiv.getAttributes()
                          .putAll(component.cast()
                                           .asAttributeBase()
                                           .getAttributes());
                displayDiv.getAttributes()
                          .keySet()
                          .stream()
                          .filter(a -> a.startsWith("#"))
                          .forEach(removables::add);
                removables.forEach(a -> displayDiv.getAttributes()
                                                  .remove(a));
                displayDiv.getClasses()
                          .addAll(component.getClasses());
            }

            for (Object loop : component.cast()
                                        .asAttributeBase()
                                        .getOverrideAttributes()
                                        .entrySet())
            {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) loop;
                String key = entry.getKey();
                String value = entry.getValue();
                displayDiv.getAttributes()
                          .put(key, value);
            }

            IComponentHierarchyBase parent = component.getParent();
            if (parent != null && !parent.getChildren()
                                         .contains(displayDiv))
            {
                parent.add(displayDiv);
            }

            //hide the original component, but keep it on the children chain? is that right?
            component.setRenderChildren(false);
            component.cast()
                     .asTagBase()
                     .setRenderTag(false);
            displayDiv.getConfigurations()
                      .addAll(component.getConfigurations());

            if (parent != null)
            {
                parent.remove(component);
            }
*//*

            component.cast()
                     .asTagBase()
                     .clearHtmlCache();

            parent.cast()
                  .asTagBase()
                  .clearHtmlCache();
*//*

            //parent.addConfiguration((IConfiguration) component);
        }




        */
    }

    private static void moveParentConfigurations(IComponentHierarchyBase<?, ?> component, IComponentHierarchyBase<?, ?> ngComponent)
    {
        var myConfigs = component.getConfigurations();
        Set<IConfiguration> moveToParent = new HashSet<>();
        for (IConfiguration myConfig : myConfigs)
        {
            try
            {
                var onParentMethod = myConfig.getClass()
                                             .getMethod("onParent");
                onParentMethod.setAccessible(true);
                boolean onParent = (boolean) onParentMethod.invoke(myConfig);
                if (ngComponent.getParent() != null && onParent)
                {
                    var parentNgComponent = findOwningNgComponent(ngComponent.getParent());
                    if (parentNgComponent != null)
                    {
                        var setOnParentMethod = myConfig.getClass()
                                                        .getMethod("setOnParent", Boolean.class);
                        setOnParentMethod.setAccessible(true);
                        setOnParentMethod.invoke(myConfig, false);
                        var setOnSelfMethod = myConfig.getClass()
                                                      .getMethod("setOnSelf", Boolean.class);
                        setOnSelfMethod.setAccessible(true);
                        setOnSelfMethod.invoke(myConfig, true);
                        moveToParent.add(myConfig);
                        ngComponent.getConfigurations()
                                   .add(myConfig);
                    }
                }
            }
            catch (NoSuchMethodException e)
            {
                //this is not a parent compatible annotation
            }
            catch (InvocationTargetException | IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        component.getConfigurations()
                 .removeAll(moveToParent);

        ngComponent.getConfigurations()
                   .addAll(component.getConfigurations());
    }
}
