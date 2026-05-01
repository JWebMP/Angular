package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgDataType;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDataType;

/**
 * A test data type (injectable class) that exposes fields and imports with onParent=true.
 * Similar to the real App class in the GuicedEE website.
 */
@NgDataType(NgDataType.DataTypeClass.Class)
@NgImportReference(value = "Injectable", reference = "@angular/core")
@NgImportReference(value = "inject", reference = "@angular/core", onParent = true, onSelf = false)
@NgField(value = "appConfig: TestAppConfig = inject(TestAppConfig);", onParent = true, onSelf = false)
@NgField(value = "private configValue: string = 'default';", onParent = false, onSelf = true)
public class TestAppConfig implements INgDataType<TestAppConfig>
{
    @Override
    public String renderBeforeClass()
    {
        return "@Injectable({ providedIn: 'root' })\n";
    }
}

