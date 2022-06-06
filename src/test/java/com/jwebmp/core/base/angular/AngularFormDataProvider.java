package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.client.*;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.modules.services.angular.forms.*;

@NgServiceProvider(variableName = "appData", referenceName = "data",value = AngularFormDataService.class, dataType = DataComponentData.class)
public class AngularFormDataProvider extends FormDataProvider<AngularFormDataProvider>
{
}


