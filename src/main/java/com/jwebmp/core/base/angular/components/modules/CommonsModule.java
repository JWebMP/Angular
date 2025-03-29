package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgIgnoreRender;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgModule;

@NgImportReference(value = "CommonsModule", reference = "@angular/core")
@NgImportModule("CommonsModule")
@NgIgnoreRender
public class CommonsModule implements INgModule<CommonsModule>
{
}
