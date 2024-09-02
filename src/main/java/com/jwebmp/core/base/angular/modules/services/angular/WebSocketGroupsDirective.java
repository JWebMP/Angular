package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.client.annotations.angular.NgDirective;
import com.jwebmp.core.base.angular.client.annotations.functions.NgOnDestroy;
import com.jwebmp.core.base.angular.client.annotations.functions.NgOnInit;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.SocketClientService;
import com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDirective;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;

@NgDirective(value = "[wsgroup]", standalone = true)
@NgField("@Input('wsgroup') wsgroup! : string;")
@NgOnInit(
        "this.socketClientService.send('AddToWebSocketGroup',{groupName : this.wsgroup},'onClick',{},this.elementRef);"
)
@NgOnDestroy(
        "this.socketClientService.send('RemoveFromWebSocketGroup',{groupName : this.wsgroup},'onClick',{},this.elementRef);")
@NgComponentReference(SocketClientService.class)
public class WebSocketGroupsDirective implements INgDirective<WebSocketGroupsDirective>
{
    public WebSocketGroupsDirective()
    {
    }

    public static void addGroup(IComponentHierarchyBase<?, ?> component, String groupName)
    {
        component.asAttributeBase()
                 .addAttribute("wsgroup", groupName);
        component.addConfiguration(AnnotationUtils.getNgComponentReference(WebSocketGroupsDirective.class));
    }

}
