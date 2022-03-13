package com.jwebmp.core.base.angular.nested;

import com.jwebmp.core.base.angular.rnd.ProductDetail;
import com.jwebmp.core.base.angular.services.annotations.NgComponent;
import com.jwebmp.core.base.angular.services.annotations.NgRoutable;
import com.jwebmp.core.base.angular.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;

@NgComponent(value = "product-detail-part-a")
@NgRoutable(path = "products-child-a",parent = {ProductDetail.class})
public class ProductDetailPartChildA  extends DivSimple<ProductDetailPartChildA>
        implements INgComponent<ProductDetailPartChildA>  {
    public ProductDetailPartChildA() {
        add("this is child a");
    }
}
