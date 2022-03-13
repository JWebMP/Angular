package com.jwebmp.core.base.angular.nested;

import com.jwebmp.core.base.angular.rnd.ProductDetail;
import com.jwebmp.core.base.angular.services.annotations.NgComponent;
import com.jwebmp.core.base.angular.services.annotations.NgRoutable;
import com.jwebmp.core.base.angular.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;

@NgComponent(value = "product-detail-part-b")
@NgRoutable(path = "products-child-b",parent = {ProductDetail.class})
public class ProductDetailPartChildB   extends DivSimple<ProductDetailPartChildB>
        implements INgComponent<ProductDetailPartChildB> {
    public ProductDetailPartChildB() {
        add("This is child b");
    }
}
