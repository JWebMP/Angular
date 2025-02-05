package com.jwebmp.core.base.angular.modules.services.angular;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgDirective;
import com.jwebmp.core.base.angular.client.annotations.functions.NgOnDestroy;
import com.jwebmp.core.base.angular.client.annotations.functions.NgOnInit;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.SocketClientService;
import com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDirective;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;

import java.util.ServiceLoader;
import java.util.Set;

@NgDirective(value = "[wsgroup]", standalone = true)
@NgField("@Input('wsgroup') wsgroup! : string;")
@NgOnInit(
        """
                        if(this.socketClientService.groups.findIndex(x => x === this.wsgroup) === -1)
                        {
                            this.socketClientService.groups.push(this.wsgroup);
                        }
                        this.socketClientService.send('AddToWebSocketGroup',{groupName : this.wsgroup},'onClick',{},this.elementRef);
                """
)
@NgOnDestroy(
        "this.socketClientService.send('RemoveFromWebSocketGroup',{groupName : this.wsgroup},'onClick',{},this.elementRef);\n" +
                "this.socketClientService.groups.splice(this.socketClientService.groups.findIndex(x => x === this.wsgroup), 1);")


@NgComponentReference(SocketClientService.class)
public class WebSocketGroupsDirective implements INgDirective<WebSocketGroupsDirective> {
    public WebSocketGroupsDirective() {
    }

    public static void addGroup(IComponentHierarchyBase<?, ?> component, String groupName) {
        Set<WebSocketGroupAdd> s = IGuiceContext.loaderToSet(ServiceLoader.load(WebSocketGroupAdd.class));
        boolean performed = false;
        for (WebSocketGroupAdd webSocketGroupAdd : s) {
            performed = webSocketGroupAdd.addGroup(component, groupName);
            if (performed) {
                break;
            }
        }
        if (!performed) {
            component.asAttributeBase()
                    .addAttribute("wsgroup", groupName);
            component.addConfiguration(AnnotationUtils.getNgComponentReference(WebSocketGroupsDirective.class));
        }
    }

}
