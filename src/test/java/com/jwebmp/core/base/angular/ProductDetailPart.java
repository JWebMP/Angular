package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.RouterOutlet;

import com.jwebmp.core.base.html.*;

import static com.jwebmp.core.base.angular.components.modules.RouterModule.applyRoute;


@NgComponent(value = "product-detail-part")
public class ProductDetailPart extends DivSimple<ProductDetailPart>
        implements INgComponent<ProductDetailPart>
{
    public ProductDetailPart()
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
