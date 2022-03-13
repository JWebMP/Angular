package com.jwebmp.core.base.angular.services.annotations;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
@Repeatable(NgModuleReferences.class)
public @interface NgModuleReference
{
	/**
	 * The boot module to call from the angular app
	 *
	 * @return
	 */
	Class<? extends INgModule<?>> value();
}
