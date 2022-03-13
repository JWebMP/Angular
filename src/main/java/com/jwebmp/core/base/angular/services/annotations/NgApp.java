package com.jwebmp.core.base.angular.services.annotations;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.lang.annotation.*;

import static com.jwebmp.core.base.angular.services.annotations.NgSourceDirectoryReference.SourceDirectories.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
public @interface NgApp
{
	/**
	 * The name of the .ts file for this app
	 *
	 * @return
	 */
	String value() default "";
	
	/**
	 * The name of the .ts file for this app
	 *
	 * @return
	 */
	String[] assets() default {};
	
	/**
	 * The name of the .ts file for this app
	 *
	 * @return
	 */
	String[] stylesheets() default {};
	
	/**
	 * The name of the .ts file for this app
	 *
	 * @return
	 */
	String[] scripts() default {};
	
	/**
	 * Include the packages to render
	 *
	 * @return
	 */
	String[] includePackages() default {};
	
	/**
	 * The boot module to call from the angular app
	 *
	 * @return
	 */
	Class<? extends INgComponent<?>> bootComponent();
}
