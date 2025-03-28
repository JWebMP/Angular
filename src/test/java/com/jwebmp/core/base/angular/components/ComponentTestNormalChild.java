package com.jwebmp.core.base.angular.components;

import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.html.DivSimple;


@NgField("childField1 : any;")
@NgField("childField2 : any;")

@NgConstructorParameter("childParam1 : string")
@NgConstructorParameter("childParam2 : string")

public class ComponentTestNormalChild extends DivSimple<ComponentTestNormalChild>
{
    public ComponentTestNormalChild()
    {
        super("Normal Child");
    }

}
