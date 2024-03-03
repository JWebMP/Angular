package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.components.*;
import com.jwebmp.core.base.angular.client.annotations.functions.*;
import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.client.services.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;

@NgDirective(value = "[wsgroup]")
@NgInput("wsgroup")
@NgOnInit(
        "this.socketClientService.send('AddToWebSocketGroup',{groupName : this.wsgroup},'onClick',{},this.elementRef);"
)
@NgOnDestroy("this.socketClientService.send('RemoveFromWebSocketGroup',{groupName : this.wsgroup},'onClick',{},this.elementRef);")
@NgComponentReference(SocketClientService.class)
public class WebSocketGroupsDirective implements INgDirective<WebSocketGroupsDirective>
{
    public WebSocketGroupsDirective()
    {
    }
}
