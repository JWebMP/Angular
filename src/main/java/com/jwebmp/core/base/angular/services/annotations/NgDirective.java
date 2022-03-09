package com.jwebmp.core.base.angular.services.annotations;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Inherited
public @interface NgDirective
{
	String selector();
	
	String[] inputs() default {};
	
	String[] outputs() default {};
	
	String exportAs() default "";
	
	String[] queries() default {};
	
	String host() default "";
	
	boolean includeADeclaration() default true;
}
