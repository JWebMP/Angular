package com.jwebmp.core.base.angular.components;

import com.jwebmp.core.base.html.DivSimple;

public class NgElse<J extends NgElse<J>> extends DivSimple<J>
{
    public NgElse()
    {
    }

    @Override
    protected StringBuilder renderHTML(int tabCount)
    {
        StringBuilder sb = new StringBuilder();
        setCurrentTabIndents(tabCount);
        sb.append(getCurrentTabIndentString())
          .append("@else {");
        //sb.append(super.renderBeforeChildren());
        sb.append(super.renderChildren());
        //sb.append(super.renderAfterChildren());
        sb.append("\n" + getCurrentTabIndentString())
          .append("}");
        return sb;
    }

}
