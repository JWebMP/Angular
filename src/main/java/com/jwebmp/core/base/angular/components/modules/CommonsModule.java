package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgConfig;

@NgImportReference(value = "CommonModule", reference = "@angular/core")
@NgImportModule("CommonModule")
public class CommonsModule implements INgConfig<CommonsModule>
{
}
