package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgConfig;

@NgImportReference(value = "CurrencyPipe", reference = "@angular/common",onSelf = false,onParent = true)
@NgImportModule(value = "CurrencyPipe", onSelf = false,onParent = true)
public class CurrencyPipe implements INgConfig<CurrencyPipe>
{
}
