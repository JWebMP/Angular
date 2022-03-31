package com.jwebmp.core.base.angular.modules.directives;

import com.jwebmp.core.base.angular.modules.services.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@NgDirective(selector = "[clickClassName]")
@NgImportReference(name = "ElementRef", reference = "@angular/core")
@NgImportReference(name = "Input", reference = "@angular/core")
@NgImportReference(name = "HostListener", reference = "@angular/core")
@NgImportReference(name = "RouterModule, ParamMap,Router", reference = "@angular/router")
@NgComponentReference(SocketClientService.class)
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
	//	return List.of("OnInit");
		return List.of();
	}
	
	@Override
	public List<String> methods()
	{
		return List.of(
				//"ngOnInit() {}\n",
				"@HostListener('click', ['$event'])\n" +
				"  onClick(event: PointerEvent) {\n" +
				"  let elementId: string = (event.target as Element).id;\n" +
				"  this.socketClientService.send('ajax',{eventClass : this.clickClassName},'onClick',event,this.elementRef);\n" +
				"}\n");
	}
	
}
