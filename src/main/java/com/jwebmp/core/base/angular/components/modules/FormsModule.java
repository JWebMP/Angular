package com.jwebmp.core.base.angular.components.modules;

import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgModule;

@NgImportReference(value = "FormsModule", reference = "@angular/forms")
@NgImportModule("FormsModule")
public class FormsModule implements INgModule<FormsModule>
{
}
