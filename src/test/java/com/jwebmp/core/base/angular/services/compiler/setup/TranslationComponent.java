package com.jwebmp.core.base.angular.services.compiler.setup;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.services.TranslationService;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;

/** Top-level fixture so relative imports use the same layout as consumer components. */
@NgComponent("translation-test")
@NgComponentReference(TranslationService.class)
public class TranslationComponent extends DivSimple<TranslationComponent> implements INgComponent<TranslationComponent>
{
    public TranslationComponent() { setText("{{ 'orders.save' | transloco }}"); }
}
