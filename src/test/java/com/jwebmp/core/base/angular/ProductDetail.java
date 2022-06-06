package com.jwebmp.core.base.angular;

import com.google.inject.*;

import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.routing.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.html.*;

@NgComponent(value = "product-detail")
@NgRoutable(path = "products")
public class ProductDetail extends DivSimple<ProductDetail>
		implements INgComponent<ProductDetail>
{
	@Inject
	public void initialize()
	{
		add(new ProductDetailPart());
	}
	
}
