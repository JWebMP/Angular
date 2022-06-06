package com.jwebmp.core.base.angular.modules.directives;

import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.components.*;
import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.client.services.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import java.util.*;

@NgDirective(selector = "[clickClassName]")
@NgInput("clickClassName")
@NgImportReference(value = "HostListener", reference = "@angular/core")
@NgComponentReference(SocketClientService.class)

public class OnClickListenerDirective implements INgDirective<OnClickListenerDirective>
{
	public OnClickListenerDirective()
	{
	}
	
	@Override
	public List<String> methods()
	{
		List<String> out = INgDirective.super.methods();
		out.add(
				//"ngOnInit() {}\n",
				"@HostListener('click', ['$event'])\n" +
				"  onClick(event: PointerEvent) {\n" +
				"  let elementId: string = (event.target as Element).id;\n" +
				"  this.socketClientService.send('ajax',{eventClass : this.clickClassName},'onClick',event,this.elementRef);\n" +
				"}\n");
		
		return out;
	}
	
}
