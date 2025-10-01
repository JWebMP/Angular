package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.ajax.AjaxCall;
import com.jwebmp.core.base.ajax.AjaxResponse;
import com.jwebmp.core.base.ajax.AjaxResponseReaction;
import com.jwebmp.core.base.ajax.ReactionType;
import com.jwebmp.core.base.angular.client.DynamicData;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.events.click.ClickAdapter;
import io.smallrye.mutiny.Uni;

public class ServerClickEvent extends ClickAdapter<ServerClickEvent>
{
    public ServerClickEvent()
    {
    }

    public ServerClickEvent(IComponentHierarchyBase<?, ?> component)
    {
        super(component);
    }

    @Override
    public Uni<Void> onClick(AjaxCall<?> call, AjaxResponse<?> response)
    {
        response.addReaction(new AjaxResponseReaction<>("/products", ReactionType.RedirectUrl));
        response.addDataResponse("updateDataComponent", new DynamicData().addData(new DataComponentData().setName("Data from another event!")));// new DataComponentData().setName("This was updated from an event!"));
        return Uni.createFrom()
                  .voidItem();
    }

}
