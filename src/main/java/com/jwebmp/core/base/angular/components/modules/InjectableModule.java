package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgConfig;

@NgImportReference(value = "Injectable", reference = "@angular/core")
@NgImportReference(value = "inject", reference = "@angular/core", onParent = true, onSelf = false)
public class InjectableModule implements INgConfig<InjectableModule>
{
}