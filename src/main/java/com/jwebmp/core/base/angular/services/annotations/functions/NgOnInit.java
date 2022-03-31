package com.jwebmp.core.base.angular.services.annotations.functions;

import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.base.angular.services.annotations.structures.*;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(NgOnInits.class)
@Inherited
@NgImportReference(name = "OnInit", reference = "@angular/core")
@NgInterface("OnInit")
public @interface NgOnInit
{
	String[] onInit() default {};
}
