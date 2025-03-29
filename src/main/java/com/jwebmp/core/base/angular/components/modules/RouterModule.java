package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgIgnoreRender;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgModule;

@NgImportReference(value = "RouterModule", reference = "@angular/router")
@NgImportModule("RouterModule")
@NgIgnoreRender
public class RouterModule implements INgModule<RouterModule>
{
}
