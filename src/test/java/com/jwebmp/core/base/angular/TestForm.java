package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.angular.data.*;
import com.jwebmp.core.base.angular.forms.*;
import com.jwebmp.core.base.angular.services.annotations.*;

@NgDataTypeReference(DataComponentData.class)
@NgServiceReference(AngularFormDataService.class)
@NgComponent("test-form")
public class TestForm extends AngularForm<TestForm>
{
	public TestForm()
	{
		super(new AngularFormDataService());
	}
}
