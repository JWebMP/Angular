package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;

@NgServiceProvider(variableName = "appData", referenceName = "data",value = AngularFormDataService.class, dataType = DataComponentData.class)
public class AngularFormDataProvider implements INgServiceProvider<AngularFormDataProvider>
{
}


