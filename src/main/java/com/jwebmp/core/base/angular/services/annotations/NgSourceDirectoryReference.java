package com.jwebmp.core.base.angular.services.annotations;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Inherited
public @interface NgSourceDirectoryReference
{
	SourceDirectories value() default SourceDirectories.App;
	
	String name() default "";
	
	boolean inPackage() default true;
	
	enum SourceDirectories
	{
		Main("./"),
		Source("src/"),
		Self(""),
		App("src/app/"),
		FixedApp("src/app/"),
		Environment("src/environment/"),
		Assets("src/assets/"),
		FixedSrc("src/app/"),
		FixedMain("src/app/");
		private String directoryName;
		
		SourceDirectories(String directoryName)
		{
			this.directoryName = directoryName;
		}
	}
}
