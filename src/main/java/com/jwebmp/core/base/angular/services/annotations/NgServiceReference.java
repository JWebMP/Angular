package com.jwebmp.core.base.angular.services.annotations;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
@Repeatable(NgServiceReferences.class)
public @interface NgServiceReference {
    /**
     * The boot module to call from the angular app
     *
     * @return
     */
    Class<? extends INgDataService<?>> value();

    /**
     * If this reference must be rendered in the provides clause
     *
     * @return
     */
    boolean provide() default false;
}
