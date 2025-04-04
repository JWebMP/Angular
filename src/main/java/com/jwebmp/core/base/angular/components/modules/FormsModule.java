package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgIgnoreRender;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgModule;

@NgImportReference(value = "FormsModule", reference = "@angular/forms", onParent = true)
@NgImportModule(value = "FormsModule", onParent = true)
@NgIgnoreRender
public class FormsModule implements INgModule<FormsModule>
{
}
