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
@NgImportReference(name = "BehaviorSubject, Observable, Subject, Subscription", reference = "rxjs")
@NgImportReference(name = "bufferTime", reference = "rxjs")
@NgComponentReference(SocketClientService.class)
@NgOnInit
@NgOnDestroy(onDestroy = "this.subscription?.unsubscribe();")
@NgOnDestroy(onDestroy = "this.socketClientService.deregisterListener(this.listenerName);")
@NgOnDestroy(onDestroy = "this._data.unsubscribe();")
@NgImportReference(name = "OnDestroy", reference = "@angular/core")
@NgDataTypeReference(DynamicData.class)
public interface INgDataService<J extends INgDataService<J>> extends INgComponent<J>
{
	DynamicData getData(AjaxCall<?> call);
	
	default void receiveData(AjaxCall<?> call, AjaxResponse<?> response)
	{
	}
	
	@Override
	default List<String> componentDecorators()
	{
		return List.of("@Injectable({\n" +
		               "  providedIn: '" + providedIn() + "'\n" +
		               "})");
	}
	
	@Override
	default List<String> componentInterfaces()
	{
		return List.of("OnDestroy");
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
				           "" + (buffer() ? ".pipe(bufferTime(1500))" : "") +
				           ".subscribe((message : " + ITSComponent.getTsFilename(dReference.value()) + ") => {\n" +
				           "" +
				           "" +
				           "this.dataStore.datas = message; \n" +
				           "this._data.next(Object.assign({}, this.dataStore).datas);" +
				           "" +
				           "" +
				           "" +
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
		List<NgDataTypeReference> dReferences = getAnnotations(getClass(), NgDataTypeReference.class);
		String dtRef = "";
		for (NgDataTypeReference dReference : dReferences)
		{
			if (dReference.primary())
			{
				dtRef = ITSComponent.getTsFilename(dReference.value());
			}
		}
		
		methods.add("fetchData(){\n" +
		            "   this.socketClientService.send('data',{className :  '" +
		            getClass().getCanonicalName() + "'},this.listenerName);\n" +
		            "}\n" +
		            "" +
		            "get data() : Observable<" + dtRef + "> {\n" +
		            "        return this._data.asObservable();\n" +
		            "    }" +
		            "" +
		            "");
		
		methods.add("public sendData(datas : any) {\n" +
		            "        this.socketClientService.send('dataSend', {data :{...datas},\n" +
		            "            className: '" + getClass().getCanonicalName() + "'}, this.listenerName);\n" +
		            "    }");
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
				fields.add(" private _data = new BehaviorSubject<" + getTsFilename(dReference.value()) + ">({});");
				fields.add(" private dataStore: { datas: " + getTsFilename(dReference.value()) + " } = { datas: {} }; ");
				//	fields.add(" public data : " + getTsFilename(dReference.value()) + " = {};\n");
			}
		}
		
		NgDataService dService = getAnnotations(getClass(), NgDataService.class).get(0);
		fields.add(" private listenerName = '" + dService.value() + "';");
		fields.add(" private subscription? : Subscription;\n");
		return fields;
	}
	
	default boolean buffer()
	{
		return false;
	}
	
	default String providedIn()
	{
		return "any";
	}
}
