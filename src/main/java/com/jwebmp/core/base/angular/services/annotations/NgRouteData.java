package com.jwebmp.core.base.angular.services.annotations;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Inherited
public @interface NgRouteData
{
	String dataVariableName();
	
	Class<? extends INgDataService<?>> dataVariableType();
}
