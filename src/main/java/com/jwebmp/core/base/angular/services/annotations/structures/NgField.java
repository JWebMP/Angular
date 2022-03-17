package com.jwebmp.core.base.angular.services.annotations.structures;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(NgFields.class)
@Inherited
public @interface NgField
{
	String value();
	
	boolean onParent() default false;
	
	boolean onSelf() default true;
}
