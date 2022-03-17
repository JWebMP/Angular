package com.jwebmp.core.base.angular.services.annotations.references;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(NgBootDeclarations.class)
@Inherited
public @interface NgBootDeclaration
{
	String value();
	
	boolean onParent() default false;
	boolean onSelf() default true;
}
