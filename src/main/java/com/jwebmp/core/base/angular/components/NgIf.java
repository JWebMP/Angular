package com.jwebmp.core.base.angular.components;

import com.jwebmp.core.base.html.DivSimple;

public class NgIf extends DivSimple<NgIf>
{
    private String condition;

    public NgIf()
    {
    }

    public NgIf(String condition)
    {
        this.condition = condition;
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
        return sb;
    }

}
