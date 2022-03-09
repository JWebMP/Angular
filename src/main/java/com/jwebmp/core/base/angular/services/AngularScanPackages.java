package com.jwebmp.core.base.angular.services;

import com.guicedee.guicedinjection.interfaces.*;

import java.util.*;

public interface AngularScanPackages<J extends AngularScanPackages<J>> extends IDefaultService<J>
{
	Set<String> packages();
}
