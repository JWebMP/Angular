package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.forms.*;


@NgDataTypeReference(DataComponentData.class)
@NgComponent("test-form")
public class TestForm extends AngularForm<TestForm>
{
	public TestForm()
	{
		super("testForm", new AngularFormDataProvider());
	}
}
