package com.jwebmp.core.base.angular;

import com.google.inject.Inject;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;

import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.modules.services.angular.RoutingModule;
import com.jwebmp.core.base.angular.services.RouterOutlet;
import com.jwebmp.core.base.html.Button;
import com.jwebmp.core.base.html.DivSimple;

@NgComponent(value = "test-component")
public class AngularTestComponent extends DivSimple<AngularTestComponent>
        implements INgComponent<AngularTestComponent>
{
    @Inject
    public void initialize()
    {
        add(new ProductList());

        add(new DivSimple<>().addClass("container")
                .add(new RouterOutlet()));

        Button button = new Button();
        button.setText("click me");
        RoutingModule.applyRoute(button, "products", "");
        add(button);

        Button serverClickable = new Button();
        serverClickable.setText("Server click?");
        serverClickable.addEvent(new ServerClickEvent());
        add(serverClickable);
        //add(new ProductList());

        Button rebuildButton = new Button();
        rebuildButton.setText("Rebuild app");
        rebuildButton.addEvent(new RebuildAppClickEvent());
        add(rebuildButton);
        //add(new ProductList());

        add(new AngularDataComponent());

    }

}
