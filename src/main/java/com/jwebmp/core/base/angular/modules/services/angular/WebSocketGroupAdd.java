package com.jwebmp.core.base.angular.modules.services.angular;

import com.guicedee.guicedinjection.interfaces.IDefaultService;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;

public interface WebSocketGroupAdd<J extends WebSocketGroupAdd<J>> extends IDefaultService<J> {
    boolean addGroup(IComponentHierarchyBase<?, ?> component, String groupName);
}
