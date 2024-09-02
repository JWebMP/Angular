package com.jwebmp.core.base.angular.components;

import com.jwebmp.core.base.html.DivSimple;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NgForEmpty extends DivSimple<NgForEmpty>
{
    public NgForEmpty()
    {
    }

    @Override
    protected StringBuilder renderHTML(int tabCount)
    {
        StringBuilder sb = new StringBuilder();
        setCurrentTabIndents(tabCount);
        sb.append(getCurrentTabIndentString())
          .append("@empty {");
        //sb.append(super.renderBeforeChildren());
        sb.append(super.renderChildren());
        //sb.append(super.renderAfterChildren());
        sb.append("\n" + getCurrentTabIndentString())
          .append("}");
        return sb;
    }

}
