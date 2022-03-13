package com.jwebmp.core.base.angular.services.annotations;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
public @interface NgDataType
{
	DataTypeClass value() default DataTypeClass.Class;
	
	boolean exports() default true;
	
	enum DataTypeClass
	{
		Class,
		Enum,
		AbstractClass,
		Interface,
		Const,
		;
		
		public String description()
		{
			return name().toLowerCase();
		}
	}
}
