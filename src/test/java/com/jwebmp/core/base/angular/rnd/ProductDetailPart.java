package com.jwebmp.core.base.angular.rnd;

import com.google.inject.*;
import com.jwebmp.core.base.angular.nested.ProductDetailPartChildA;
import com.jwebmp.core.base.angular.nested.ProductDetailPartChildB;
import com.jwebmp.core.base.angular.services.RouterOutlet;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.html.*;

import static com.jwebmp.core.base.angular.modules.services.angular.RoutingModule.applyRoute;

@NgComponent(value = "product-detail-part")
public class ProductDetailPart extends DivSimple<ProductDetailPart>
		implements INgComponent<ProductDetailPart>
{
	@Inject
	public void initialize()
	{
		add("Product Detail Section");
		
		Button child_a = new Button<>().setText("Child A");
		Button child_b = new Button<>().setText("Child B");
		
		applyRoute(child_a, ProductDetailPartChildA.class);
		applyRoute(child_b, ProductDetailPartChildB.class);
		
		add(child_a);
		add(child_b);
		
		add("nested route below : ");
		add(new RouterOutlet());
	}
}
