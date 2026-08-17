package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgConfig;

@NgImportReference(value = "DatePipe", reference = "@angular/common",onSelf = false,onParent = true)
@NgImportModule(value = "DatePipe", onSelf = false,onParent = true)
public class DatePipe implements INgConfig<DatePipe>
{
}
