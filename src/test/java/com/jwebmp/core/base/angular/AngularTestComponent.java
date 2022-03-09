package com.jwebmp.core.base.angular;

import com.google.inject.*;
import com.jwebmp.core.base.angular.modules.services.*;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.modules.services.storage.*;
import com.jwebmp.core.base.angular.services.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.html.*;

import java.util.*;
import java.util.List;

@NgComponent(value = "test-component")
@NgProviderReference(SocketClientService.class)
@NgModuleReference(OnInitModule.class)
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
		
		add(new AngularDataComponent());
	}

}
