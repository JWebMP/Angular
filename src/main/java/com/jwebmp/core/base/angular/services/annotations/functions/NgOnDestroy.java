package com.jwebmp.core.base.angular.services.annotations.functions;

import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.base.angular.services.annotations.structures.*;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(NgOnDestroys.class)
@Inherited
@NgImportReference(name = "OnDestroy", reference = "@angular/core")
@NgInterface("OnDestroy")
public @interface NgOnDestroy
{
	String[] onDestroy() default {};
}
