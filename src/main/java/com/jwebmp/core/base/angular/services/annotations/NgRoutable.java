package com.jwebmp.core.base.angular.services.annotations;

import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
@NgImportReference(name = "RouterModule, ParamMap,Router", reference = "@angular/router")
public @interface NgRoutable
{
	String path();
	
	String redirectTo() default "";
	
	String pathMatch() default "";
	
	/**
	 * Only one parent allowed, set as an array to not enforce being set
	 *
	 * @return
	 */
	Class<? extends INgComponent<?>>[] parent() default {};
}
