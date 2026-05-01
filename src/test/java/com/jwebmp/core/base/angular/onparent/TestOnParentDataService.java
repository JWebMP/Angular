package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.ajax.AjaxCall;
import com.jwebmp.core.base.ajax.AjaxResponse;
import com.jwebmp.core.base.angular.client.DynamicData;
import com.jwebmp.core.base.angular.client.annotations.angular.NgDataService;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.annotations.structures.NgMethod;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDataService;

/**
 * A test data service that exposes onParent=true annotations.
 */
@NgDataService("testOnParentDataService")
@NgImportReference(value = "HttpClient", reference = "@angular/common/http", onParent = true, onSelf = false)
@NgField(value = "private dataServiceRef: TestOnParentDataService;", onParent = true, onSelf = false)
@NgMethod(value = """
        fetchFromService(): void {
            this.dataServiceRef.data.subscribe();
        }""", onParent = true, onSelf = false)
public class TestOnParentDataService implements INgDataService<TestOnParentDataService>
{
    @Override
    public DynamicData getData(AjaxCall<?> call, AjaxResponse<?> response)
    {
        return new DynamicData();
    }
}

