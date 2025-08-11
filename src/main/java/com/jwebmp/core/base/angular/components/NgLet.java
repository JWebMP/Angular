package com.jwebmp.core.base.angular.components;

import com.jwebmp.core.base.html.Paragraph;

public class NgLet extends Paragraph<NgLet>
{
    public NgLet()
    {
        setTextOnly(true);
    }

    public NgLet(String text)
    {
        super(text);
        setTextOnly(true);
    }

    public NgLet(String variable, String value)
    {
        super("@let " + variable + " = " + value + ";");
        setTextOnly(true);
    }
}
