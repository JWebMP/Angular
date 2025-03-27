package com.jwebmp.core.base.angular.components;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.components.NgInput;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.annotations.functions.*;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.annotations.structures.NgInterface;
import com.jwebmp.core.base.angular.client.annotations.structures.NgMethod;
import com.jwebmp.core.base.angular.client.services.EventBusListenerDirective;
import com.jwebmp.core.base.angular.client.services.EventBusService;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;

import java.util.List;

@NgMethod("""
        test()
        {
            console.log('test');
        }
        """)
@NgComponent("test-component")
@NgField("field1 : any;")
@NgField("field2 : any;")

@NgConstructorParameter("param1 : string")
@NgConstructorParameter("param2 : string")
@NgConstructorParameter("param3 : string")

@NgInterface("OnInit")
@NgOnInit("console.log('init')")

@NgAfterViewInit("console.log('after view init')")
@NgAfterViewChecked("console.log('after view checked')")
@NgAfterContentInit("console.log('after content init')")
@NgAfterContentChecked("console.log('after content checked')")

@NgOnDestroy("console.log('on destroy')")

@NgComponentReference(EventBusService.class)
@NgComponentReference(EventBusListenerDirective.class)

@NgInput("testInput")
public class ComponentRenderingTest extends DivSimple<ComponentRenderingTest> implements INgComponent<ComponentRenderingTest>
{
    public ComponentRenderingTest()
    {
    }

    @Override
    public List<String> methods()
    {
        return List.of("""
                test2()
                {
                    console.log('test2');
                }
                """);
    }

    @Override
    public List<String> componentMethods()
    {
        return List.of("""
                test3()
                {
                    console.log('test3');
                }
                """);
    }

    @Override
    public List<String> fields()
    {
        return List.of("field3 : any");
    }

    @Override
    public List<String> constructorParameters()
    {
        return List.of("param4 : string");
    }

    @Override
    public List<String> onInit()
    {
        return List.of("console.log('init2')");
    }

    @Override
    public List<String> afterViewInit()
    {
        return List.of("console.log('after view init2')");
    }

    @Override
    public List<String> afterViewChecked()
    {
        return List.of("console.log('after view checked2')");
    }


    @Override
    public List<String> afterContentInit()
    {
        return List.of("console.log('after content init2')");
    }

    @Override
    public List<String> afterContentChecked()
    {
        return List.of("console.log('after content checked2')");
    }

    @Override
    public List<String> onDestroy()
    {
        return List.of("console.log('on destroy2')");
    }
}
