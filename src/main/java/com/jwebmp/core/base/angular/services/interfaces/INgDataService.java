package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.modules.services.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.functions.*;
import com.jwebmp.core.base.angular.services.annotations.references.*;

import java.util.*;

import static com.jwebmp.core.base.angular.services.compiler.AnnotationsMap.*;
import static com.jwebmp.core.base.angular.services.interfaces.ITSComponent.*;

@NgImportReference(name = "Injectable", reference = "@angular/core")
@NgImportReference(name = "Observable,Observer,Subscription", reference = "rxjs")
@NgImportReference(name = "Subject", reference = "rxjs")
@NgComponentReference(SocketClientService.class)
@NgOnInit
@NgOnDestroy(onDestroy = "this.subscription?.unsubscribe();")
public interface INgDataService<J extends INgDataService<J>> extends INgComponent<J>
{
	INgDataType<?> getData(AjaxCall<?> call);
	
	@Override
	default List<String> decorators()
	{
		return List.of("@Injectable({\n" +
		               "  providedIn: 'any'\n" +
		               "})");
	}
	
	@Override
	default List<String> componentConstructorBody()
	{
		List<String> bodies = new ArrayList<>();
		List<NgDataTypeReference> dReferences = getAnnotations(getClass(), NgDataTypeReference.class);
		for (NgDataTypeReference dReference : dReferences)
		{
			if (dReference.primary())
			{
				bodies.add("this.subscription = this.socketClientService.registerListener(this.listenerName)" +
				           ".subscribe((message : " + ITSComponent.getTsFilename(dReference.value()) + ") => {\n" +
				           "this.data = message; \n" +
				           "});\n");
			}
		}
		bodies.add("this.fetchData();\n");
		
		return bodies;
	}
	
	@Override
	default List<String> componentMethods()
	{
		List<String> methods = new ArrayList<>();
		methods.add("fetchData(){\n" +
		            "   this.socketClientService.send('data',{className :  '" +
		            getClass().getCanonicalName() + "'},this.listenerName);\n" +
		            "}\n");
		return methods;
	}
	
	@Override
	default List<String> componentFields()
	{
		List<String> fields = new ArrayList<>();
		
		List<NgDataTypeReference> dReferences =
				getAnnotations(getClass(), NgDataTypeReference.class);
		for (NgDataTypeReference dReference : dReferences)
		{
			if (dReference.primary())
			{
				fields.add(" public data : " + getTsFilename(dReference.value()) + " = {};\n");
			}
		}
		
		NgDataService dService = getAnnotations(getClass(), NgDataService.class).get(0);
		fields.add(" private listenerName = '" + dService.value() + "';");
		fields.add(" private subscription? : Subscription;\n");
		return fields;
	}
	
	
}
