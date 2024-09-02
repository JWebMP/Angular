package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.client.annotations.functions.NgOnDestroy;
import com.jwebmp.core.base.angular.client.annotations.functions.NgOnInit;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.html.DivSimple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OnComponentAddedTest
{

    @Test
    void onComponentAdded()
    {
        Comp comp = new Comp();
        OnComponentAdded onComponentAdded = new OnComponentAdded();
        onComponentAdded.onComponentAdded(null, comp);
        System.out.println("------ CONFIGS ----");
        System.out.println(comp.getConfigurations());
        Assertions.assertEquals(2, comp.getConfigurations(NgField.class)
                                       .size());
    }

    @NgField("field")
    @NgField("field")
    @NgField(value = "field", onParent = true)
    @NgOnInit("On Init Method")
    @NgOnDestroy("On Destroy Method")
    public static class Comp extends DivSimple<Comp>
    {

    }
}