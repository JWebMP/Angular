package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;

@NgComponent(value = "test-on-push", changeDetection = NgComponent.ChangeDetection.ON_PUSH)
public class OnPushConsumerComponent extends DivSimple<OnPushConsumerComponent>
        implements INgComponent<OnPushConsumerComponent> {}
