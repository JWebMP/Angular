package com.jwebmp.core.base.angular.services.annotations;

import com.jwebmp.core.base.angular.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.services.interfaces.INgRoutable;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
public @interface NgRoutable {
    String path();

    /**
     * Only one parent allowed, set as an array to not enforce being set
     * @return
     */
    Class<? extends INgComponent<?>>[] parent() default {};
}
