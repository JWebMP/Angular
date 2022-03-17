package com.jwebmp.core.base.angular.services.annotations.references;

import com.jwebmp.core.base.angular.services.interfaces.ITSComponent;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(NgComponentReferences.class)
@Inherited
public @interface NgComponentReference
{
	Class<? extends ITSComponent<?>> value();
	
	boolean onParent() default false;
	boolean onSelf() default true;
}
