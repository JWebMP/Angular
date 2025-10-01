package com.jwebmp.core.base.angular.components;

import com.jwebmp.core.base.ajax.AjaxCall;
import com.jwebmp.core.base.ajax.AjaxResponse;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.components.NgInput;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;
import com.jwebmp.core.base.html.Paragraph;
import com.jwebmp.core.events.click.ClickAdapter;
import io.smallrye.mutiny.Uni;

@NgComponent("cmp-test-me")
@NgField("notOnParent : any;")
@NgField(value = "onMyParent : any;", onParent = true, onSelf = false)

@NgInput(value = "parentInputTest", attributeReference = "childField1")
@NgInput(value = "parentInputTest2", attributeReference = "childField2", mandatory = true)

public class ComponentTestTypeChild extends DivSimple<ComponentTestTypeChild>
        implements INgComponent<ComponentTestTypeChild>
{
    public ComponentTestTypeChild()
    {
        add("Test Child");
        add(new Paragraph<>("Test Event Click").addEvent(new TestClickAdapter()));
    }

    public static class TestClickAdapter extends ClickAdapter<TestClickAdapter>
    {
        @Override
        public Uni<Void> onClick(AjaxCall<?> call, AjaxResponse<?> response)
        {
            return Uni.createFrom()
                      .voidItem();
        }
    }

}
