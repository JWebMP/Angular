package com.jwebmp.core.base.angular;

import com.google.inject.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.html.*;

@NgComponent(value = "product-list")
public class ProductList extends DivSimple<ProductList> implements INgComponent<ProductList>
{
	@Inject
	public void initialize()
	{
		add("This is the product list");
	}
	
	@Override
	protected StringBuilder renderHTML(int tabCount)
	{
		return super.renderHTML(tabCount);
	}
}
