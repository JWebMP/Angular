package com.jwebmp.core.base.angular.services.annotations.structures;

import com.jwebmp.core.base.angular.services.annotations.references.NgImportReferences;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(NgConstructorParameters.class)
@Inherited
public @interface NgConstructorParameter
{
	String value();
	
	boolean onParent() default false;
	boolean onSelf() default true;
}
