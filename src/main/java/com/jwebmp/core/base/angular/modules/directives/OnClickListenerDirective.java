package com.jwebmp.core.base.angular.modules.directives;

import com.jwebmp.core.base.angular.modules.services.*;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@NgDirective(selector = "[className]")
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
	public Set<String> constructorParameters()
	{
		return Set.of("private elementRef: ElementRef");
	}
	
	@Override
	public Set<String> fields()
	{
		return Set.of("@Input() className: string ='';");
	}
	
	@Override
	public Set<String> interfaces()
	{
		return Set.of("OnInit");
	}
	
	@Override
	public List<String> methods()
	{
		return List.of(" ngOnInit() {\n" +
		               //"                          console.log('clickevent : ' + this.clickevent);\n" +
		               "                        }",
				"@HostListener('click', ['$event'])\n" +
				"  onClick(event: PointerEvent) {\n" +
				"  let elementId: string = (event.target as Element).id;\n" +
				//   " let attributes = document.getElementById(elementId)?.attributes;" +
				// " let clickevent = attributes?.getNamedItem('clickevent')?.value;" +
				"    console.log('clickered - ! - ' + elementId + ' - ' + this.className + ' - ' + event);\n" +
				"" +
				"this.socketClientService.send('ajax',{eventClass : this.className},'onClick',event,this.elementRef);\n" +
				"" +
				"" +
				"  }");
	}
	
	
}
