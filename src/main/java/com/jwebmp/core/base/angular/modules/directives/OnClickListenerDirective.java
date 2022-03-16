package com.jwebmp.core.base.angular.modules.directives;

import com.jwebmp.core.base.angular.modules.services.*;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@NgDirective(selector = "[clickClassName]")
@NgModuleReference(NgDirectiveModule.class)
@NgModuleReference(HostListenerModule.class)
@NgModuleReference(NgInputModule.class)
@NgModuleReference(NgRouterModule.class)
@NgModuleReference(OnInitModule.class)
@NgModuleReference(ElementRefModule.class)
@NgProviderReference(SocketClientService.class)
public class OnClickListenerDirective implements INgDirective<OnClickListenerDirective>
{
	public OnClickListenerDirective()
	{
	}
	
	@Override
	public List<String> constructorParameters()
	{
		return List.of("private elementRef: ElementRef");
	}
	
	@Override
	public List<String> fields()
	{
		return List.of("@Input() clickClassName: string ='';");
	}
	
	@Override
	public List<String> interfaces()
	{
		return List.of("OnInit");
	}
	
	@Override
	public List<String> methods()
	{
		return List.of("ngOnInit() {}\n",
				"@HostListener('click', ['$event'])\n" +
				"  onClick(event: PointerEvent) {\n" +
				"  let elementId: string = (event.target as Element).id;\n" +
				"  this.socketClientService.send('ajax',{eventClass : this.clickClassName},'onClick',event,this.elementRef);\n" +
				"}\n");
	}
	
}
