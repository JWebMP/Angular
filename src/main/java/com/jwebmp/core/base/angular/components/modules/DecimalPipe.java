package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgConfig;

@NgImportReference(value = "DecimalPipe", reference = "@angular/common",onSelf = false,onParent = true)
@NgImportModule(value = "DecimalPipe", onSelf = false,onParent = true)
public class DecimalPipe implements INgConfig<DecimalPipe>
{
}
