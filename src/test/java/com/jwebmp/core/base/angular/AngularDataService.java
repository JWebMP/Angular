package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.data.DataComponentData;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

@NgDataTypeReference(DataComponentData.class)
public class AngularDataService implements INgDataService<AngularDataService> {

    @Override
    public DataComponentData getData(AjaxCall<?> call) {
        return new DataComponentData().setName("Name was set and sent!");
    }

    @Override
    public Class<? extends INgDataType<?>> dataTypeReturned() {
        return DataComponentData.class;
    }

    @Override
    public String signalFetchName() {
        return "updateDataComponent";
    }

}
