package com.jwebmp.core.base.angular.services.annotations;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Inherited
public @interface NgDataTypeReferences
{
	/**
	 * The string name of the dev dependency for the given ng app
	 *
	 * @return
	 */
	NgDataTypeReference[] value();
}
