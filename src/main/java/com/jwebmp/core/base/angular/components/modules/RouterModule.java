package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgConfig;

@NgImportReference(value = "RouterModule", reference = "@angular/router")
@NgImportModule("RouterModule")
public class RouterModule implements INgConfig<RouterModule>
{
}
