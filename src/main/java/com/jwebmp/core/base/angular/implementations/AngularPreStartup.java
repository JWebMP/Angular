package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.interfaces.IGuicePreStartup;

public class AngularPreStartup implements IGuicePreStartup<AngularPreStartup>
{
    @Override
    public void onStartup()
    {
        System.setProperty("BIND_JW_PAGES", "false");
    }
}
