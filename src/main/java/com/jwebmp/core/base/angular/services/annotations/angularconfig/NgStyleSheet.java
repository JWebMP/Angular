package com.jwebmp.core.base.angular.services.annotations.angularconfig;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
@Repeatable(NgStyleSheets.class)
public @interface NgStyleSheet
{
	String value();
	
	String[] replaces() default {};
	
	String name() default "";
	
	int sortOrder() default 100;
}
