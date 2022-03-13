package com.jwebmp.core.base.angular.rnd;

import com.google.inject.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
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
