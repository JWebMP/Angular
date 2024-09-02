package com.jwebmp.core.base.angular.components;

import com.google.common.base.Strings;
import com.jwebmp.core.base.html.DivSimple;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NgFor extends DivSimple<NgFor>
{
    private String variableName;
    private String list;
    private boolean index;
    private boolean counted;
    private boolean odd;
    private boolean even;
    private boolean first;
    private boolean last;
    private String trackBy;

    public NgFor()
    {
    }

    public NgFor(String variableName, String list, String trackBy)
    {
        this.variableName = variableName;
        this.list = list;
        this.trackBy = trackBy;
    }

    @Override
    protected StringBuilder renderHTML(int tabCount)
    {
        StringBuilder sb = new StringBuilder();
        setCurrentTabIndents(tabCount);
        sb.append(getCurrentTabIndentString())
          .append("@for (" + variableName + " of " + list);
        if (!Strings.isNullOrEmpty(trackBy))
        {
            sb.append("; track " + trackBy);
        }
        if (index)
        {
            sb.append("; let index = $index");
        }
        if (counted)
        {
            sb.append("; let counted = $count");
        }
        if (odd)
        {
            sb.append("; let odd = $odd");
        }
        if (even)
        {
            sb.append("; let even = $even");
        }
        if (first)
        {
            sb.append("; let first = $first");
        }
        if (first)
        {
            sb.append("; let last = $last");
        }
        sb.append(") ");
        sb.append("{");

        //sb.append(super.renderBeforeChildren());
        sb.append(super.renderChildren());
        //sb.append(super.renderAfterChildren());
        sb.append("\n" + getCurrentTabIndentString())
          .append("}");
        return sb;
    }

}
