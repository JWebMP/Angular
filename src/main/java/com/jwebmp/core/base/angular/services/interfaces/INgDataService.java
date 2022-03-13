package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.modules.services.SocketClientService;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.modules.services.observable.ObservableModule;
import com.jwebmp.core.base.angular.modules.services.rxtxjs.RxSubjectModule;
import com.jwebmp.core.base.angular.services.annotations.*;

import java.util.*;

@NgDataService()
@NgModuleReference(InjectableModule.class)
@NgModuleReference(OnInitModule.class)
@NgModuleReference(OnDestroyModule.class)
@NgModuleReference(ObservableModule.class)
@NgModuleReference(RxSubjectModule.class)
@NgProviderReference(SocketClientService.class)
public interface INgDataService<J extends INgDataService<J>> extends INgComponent<J>
{
	INgDataType<?> getData(AjaxCall<?> call);
	
	Class<? extends INgDataType<?>> dataTypeReturned();
	
	String signalFetchName();
	
	@Override
	default List<String> decorators()
	{
		return List.of("@Injectable({\n" +
		              "  providedIn: 'root'\n" +
		              "})");
	}
}
