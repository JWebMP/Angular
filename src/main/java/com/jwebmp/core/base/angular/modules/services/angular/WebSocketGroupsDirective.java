package com.jwebmp.core.base.angular.modules.services.angular;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgDirective;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.services.EventBusListenerDirective;
import com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDirective;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import org.apache.commons.text.StringEscapeUtils;

import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

@NgDirective(value = "[wsgroup]", standalone = true)
/*
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
*/


//@NgComponentReference(SocketClientService.class)

@NgImportReference(value = "inject", reference = "@angular/core")

public class WebSocketGroupsDirective implements INgDirective<WebSocketGroupsDirective>
{
    public WebSocketGroupsDirective()
    {
    }

    public static void addGroup(IComponentHierarchyBase<?, ?> component, String groupName)
    {
        Set<WebSocketGroupAdd> s = IGuiceContext.loaderToSet(ServiceLoader.load(WebSocketGroupAdd.class));
        boolean performed = false;
        for (WebSocketGroupAdd webSocketGroupAdd : s)
        {
            performed = webSocketGroupAdd.addGroup(component, groupName);
            if (performed)
            {
                break;
            }
        }
        if (!performed)
        {
            String[] groups;
            if (groupName.contains(","))
            {
                groups = groupName.split(",");
            } else
            {
                groups = new String[]{groupName};
            }
            component.asAttributeBase()
                    .addAttribute("[appEventBusListener]", "[" + wrapGroupsWithHtmlEscapedQuotes(List.of(groups)) + "]");
            component.addConfiguration(AnnotationUtils.getNgComponentReference(EventBusListenerDirective.class));
        }
    }

    public static String wrapGroupsWithHtmlEscapedQuotes(List<String> groups)
    {
        return groups.stream()
                .map(group -> "'" + StringEscapeUtils.escapeHtml4(group) + "'") // Wrap and escape
                .collect(Collectors.joining(",")); // Join with a comma
    }


}
