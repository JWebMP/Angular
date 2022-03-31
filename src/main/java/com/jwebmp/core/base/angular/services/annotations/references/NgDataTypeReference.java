package com.jwebmp.core.base.angular.services.annotations.references;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
@Repeatable(NgDataTypeReferences.class)
public @interface NgDataTypeReference
{
	/**
	 * The boot module to call from the angular app
	 *
	 * @return
	 */
	Class<? extends INgDataType<?>> value();
	
	boolean primary() default true;
	
	String signalName() default "";
}
