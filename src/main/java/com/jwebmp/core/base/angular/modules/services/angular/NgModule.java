package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@TsDependency(value = "@angular/common", version = "^13.2.0")
@TsDependency(value = "@angular/compiler", version = "^13.2.0")
@TsDependency(value = "@angular/core", version = "^13.2.0")
@TsDependency(value = "@angular/forms", version = "^13.2.0")
@TsDependency(value = "rxjs", version = "^7.5.0")
@TsDependency(value = "tslib", version = "^2.3.0")
@TsDependency(value = "zone.js", version = "^0.11.4")

@TsDevDependency(value = "@angular-devkit/build-angular", version = "^13.2.4")
@TsDevDependency(value = "@angular/cli", version = "^13.2.4")
@TsDevDependency(value = "@angular/compiler-cli", version = "^13.2.0")
@TsDevDependency(value = "@types/node", version = "^12.11.1")
@TsDevDependency(value = "typescript", version = "^4.5.2")
public class NgModule implements INgModule<NgModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("NgModule", "@angular/core");
	}
	
}
