package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.routing.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.html.*;

@NgComponent(value = "product-detail-part-b")
@NgRoutable(path = "products-child-b",parent = {ProductDetail.class})
public class ProductDetailPartChildB   extends DivSimple<ProductDetailPartChildB>
        implements INgComponent<ProductDetailPartChildB>
{
    public ProductDetailPartChildB() {
        add("This is child b");
    }
}
