package com.jwebmp.core.base.angular.implementations;

import com.guicedee.client.services.lifecycle.IGuicePreStartup;
import io.vertx.core.Future;

import java.util.List;

public class AngularPreStartup implements IGuicePreStartup<AngularPreStartup>
{
    @Override
    public List<Future<Boolean>> onStartup()
    {
        System.setProperty("BIND_JW_PAGES", "false");
        return List.of(Future.succeededFuture(true));
    }
}
