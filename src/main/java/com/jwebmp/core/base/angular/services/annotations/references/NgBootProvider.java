package com.jwebmp.core.base.angular.services.annotations.references;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(NgBootProviders.class)
@Inherited
public @interface NgBootProvider
{
	String value();
	
	boolean onParent() default false;
	boolean onSelf() default true;
}
