package com.jwebmp.core.base.angular.services.annotations.structures;

import com.jwebmp.core.base.angular.services.annotations.references.NgImportReference;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
public @interface NgConstructorParameters
{
	/**
	 * The string name of the dev dependency for the given ng app
	 *
	 * @return
	 */
	NgConstructorParameter[] value();
}