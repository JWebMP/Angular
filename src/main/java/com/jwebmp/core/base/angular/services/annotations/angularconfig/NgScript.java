package com.jwebmp.core.base.angular.services.annotations.angularconfig;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
@Repeatable(NgScripts.class)
public @interface NgScript
{
	String value();
	
	String name() default "";
	String[] replaces() default {};
	int sortOrder() default 100;
}
