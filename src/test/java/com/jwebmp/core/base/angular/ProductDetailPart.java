package com.jwebmp.core.base.angular;

import com.google.inject.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.html.*;

@NgComponent(value = "product-detail-part")
public class ProductDetailPart extends DivSimple<ProductDetailPart> implements INgComponent<ProductDetailPart>
{
	@Inject
	public void initialize()
	{
		add("Product Detail Section");
	}
}
