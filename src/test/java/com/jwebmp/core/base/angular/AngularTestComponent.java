package com.jwebmp.core.base.angular;

import com.google.inject.*;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.functions.*;
import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.client.services.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.services.*;
import com.jwebmp.core.base.html.*;

@NgComponent(value = "test-component")
@NgComponentReference(SocketClientService.class)
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
		
		add("forms");
		add(new TestForm());
	}

}
