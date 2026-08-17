package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.interfaces.INgConfig;

@NgImportReference(value = "Router", reference = "@angular/router",onSelf = false,onParent = true)
//@NgImportModule(value = "Router", onSelf = false,onParent = true)
@NgField(value = "readonly router = inject(Router);",onSelf = false,onParent = true)
@NgImportReference(value = "inject", reference = "@angular/core", onSelf = false,onParent = true)
public class RouterConfig implements INgConfig<RouterConfig>
{
}
