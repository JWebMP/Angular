package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.interfaces.INgConfig;

@NgImportReference(value = "EnvironmentInjector", reference = "@angular/core",onSelf = false,onParent = true)
@NgImportReference(value = "runInInjectionContext", reference = "@angular/core",onSelf = false,onParent = true)
@NgField(value = "readonly injector = inject(EnvironmentInjector);",onSelf = false,onParent = true)
@NgImportReference(value = "inject", reference = "@angular/core", onSelf = false,onParent = true)
public class EnvironmentInjectorConfig implements INgConfig<EnvironmentInjectorConfig>
{
}
