package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.interfaces.INgConfig;

@NgImportReference(value = "RouterModule", reference = "@angular/router",onSelf = false,onParent = true)
@NgImportModule(value = "RouterModule", onSelf = false,onParent = true)
public class RouterModuleConfig implements INgConfig<RouterModuleConfig>
{
}
