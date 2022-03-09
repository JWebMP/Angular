package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.modules.services.*;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@NgDataService()
@NgProviderReference(SocketClientService.class)
@NgDataTypeReference(DataComponentData.class)
@NgModuleReference(InjectableModule.class)
@NgModuleReference(OnInitModule.class)
public class AngularDataService implements INgDataService<AngularDataService>
{
	public AngularDataService()
	{
		System.out.println(".");
	}
	
	@Override
	public Set<String> interfaces()
	{
		return Set.of("OnInit");
	}
	
	@Override
	public DataComponentData getData(AjaxCall<?> call)
	{
		return new DataComponentData().setName("Name was set and sent!");
	}
	
	@Override
	public Set<String> fields()
	{
		return Set.of("private _subject = new Subject();",
				"public observable = this._subject.asObservable();");
	}
	
	@Override
	public Class<? extends INgDataType<?>> dataTypeReturned()
	{
		return DataComponentData.class;
	}
	
	@Override
	public String signalFetchName()
	{
		return "updateDataComponent";
	}
	
	@Override
	public List<String> constructorBody()
	{
		return List.of("this.fetchData();");
	}
	
	@Override
	public List<String> methods()
	{
		return List.of("ngOnInit(){" +
		               "this.fetchData();" +
		               "}",
				
				"fetchData(){" +
				" alert('fetching data');" +
				"   this.socketClientService.send('data',{className :  '" + getClass().getCanonicalName() + "'},' " + signalFetchName() + "');" +
				"}",
				
				"");
	}
}
