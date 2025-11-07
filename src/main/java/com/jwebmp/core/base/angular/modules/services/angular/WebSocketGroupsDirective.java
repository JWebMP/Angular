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

@NgImportReference(value = "inject", reference = "@angular/core")

public class WebSocketGroupsDirective implements INgDirective<WebSocketGroupsDirective>
{
    public WebSocketGroupsDirective()
    {
    }

    public static <T extends IComponentHierarchyBase<?, ?>> T addGroup(T component, String groupName)
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
            }
            else
            {
                groups = new String[]{groupName};
            }
            component.asAttributeBase()
                     .addAttribute("[appEventBusListener]", "[" + wrapGroupsWithHtmlEscapedQuotes(List.of(groups)) + "]");
            component.addConfiguration(AnnotationUtils.getNgComponentReference(EventBusListenerDirective.class));
        }
        return component;
    }

    public static String wrapGroupsWithHtmlEscapedQuotes(List<String> groups)
    {
        return groups.stream()
                     .map(group -> "'" + StringEscapeUtils.escapeHtml4(group) + "'") // Wrap and escape
                     .collect(Collectors.joining(",")); // Join with a comma
    }


}
