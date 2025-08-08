package com.jwebmp.core.base.angular.components;

import com.jwebmp.core.base.html.DivSimple;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;

public class NgIfElse extends DivSimple<NgIfElse>
{
    private String condition;
    private IComponentHierarchyBase<?, ?> elseComponent;

    public NgIfElse()
    {
    }

    public NgIfElse(String condition, IComponentHierarchyBase<?, ?> elseComponent)
    {
        this.condition = condition;
        this.elseComponent = elseComponent;
    }

    @Override
    protected StringBuilder renderHTML(int tabCount)
    {
        StringBuilder sb = new StringBuilder();
        setCurrentTabIndents(tabCount);

        sb.append(getCurrentTabIndentString())
          .append("@if (" + condition + ") {");
        //sb.append(super.renderBeforeChildren());
        sb.append(super.renderChildren());
        //sb.append(super.renderAfterChildren());
        sb.append("\n" + getCurrentTabIndentString())
          .append("}");

        sb.append(getCurrentTabIndentString())
          .append("@else {");
        //sb.append(super.renderBeforeChildren());
        sb.append(elseComponent.toString(0));
        //sb.append(super.renderAfterChildren());
        sb.append("\n" + getCurrentTabIndentString())
          .append("}");


        return sb;
    }

}
