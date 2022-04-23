package com.jwebmp.core.base.angular.services.annotations.references;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(NgBootImportReferences.class)
@Inherited
public @interface NgBootImportReference
{
	String reference();
	String name();
	
	boolean overrides() default false;
	
	boolean onParent() default false;
	boolean onSelf() default true;
}
