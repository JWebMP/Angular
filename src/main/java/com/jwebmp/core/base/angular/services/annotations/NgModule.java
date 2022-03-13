package com.jwebmp.core.base.angular.services.annotations;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
public @interface NgModule
{
	/**
	 * The name of the .ts file to render
	 *
	 * @return
	 */
	String name() default "";
	
	boolean renderInAngularBootModule() default true;
}
