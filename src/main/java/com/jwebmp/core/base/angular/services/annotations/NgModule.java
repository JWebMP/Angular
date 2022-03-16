package com.jwebmp.core.base.angular.services.annotations;

import com.jwebmp.core.base.angular.services.annotations.angularconfig.*;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
@NgModuleImportReference(name = "NgModule", reference = "@angular/core")
public @interface NgModule
{
	/**
	 * The name of the .ts file to render
	 *
	 * @return
	 */
	String name() default "";
	
	boolean renderInAngularBootModule() default true;
}
