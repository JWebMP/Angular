package com.jwebmp.core.base.angular.services;

import com.guicedee.client.services.IDefaultService;

import java.util.*;

public interface AngularScanPackages<J extends AngularScanPackages<J>> extends IDefaultService<J>
{
	Set<String> packages();
}
