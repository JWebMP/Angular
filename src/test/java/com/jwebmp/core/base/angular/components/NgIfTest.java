package com.jwebmp.core.base.angular.components;

import com.jwebmp.core.base.html.DivSimple;
import org.junit.jupiter.api.Test;

class NgIfTest
{

    @Test
    void renderHTML()
    {
        DivSimple<?> div = new DivSimple<>();
        NgIf i = new NgIf("isTest");
        i.add(new DivSimple<>("Display when true"));
        div.add(i);
        div.add(new NgElse().add("Display when false"));
        System.out.println(div.toString(0));
    }


    @Test
    void renderHTMLNgFor()
    {
        DivSimple<?> div = new DivSimple<>();
        NgFor i = new NgFor("item", "items", "item.id");
        i.setIndex(true);
        i.setEven(true);
        i.add(new DivSimple<>("Display for each true"));
        div.add(i);
        System.out.println(div.toString(0));
    }
}