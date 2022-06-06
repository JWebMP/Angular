package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.routing.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.html.*;

@NgComponent(value = "product-detail-part-a")
@NgRoutable(path = "products-child-a",parent = {ProductDetail.class})
public class ProductDetailPartChildA  extends DivSimple<ProductDetailPartChildA>
        implements INgComponent<ProductDetailPartChildA>
{
    public ProductDetailPartChildA() {
        add("this is child a");
    }
}
