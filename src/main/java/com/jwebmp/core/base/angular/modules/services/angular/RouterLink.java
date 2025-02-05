package com.jwebmp.core.base.angular.modules.services.angular;

import com.guicedee.services.jsonrepresentation.IJsonRepresentation;
import com.jwebmp.core.base.html.Link;
import lombok.*;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RouterLink extends Link<RouterLink>
{
    private String location;
    private Map<String, Object> params;
    private Map<String, Object> state;

    public RouterLink(String location)
    {
        this(location, null, null);
    }

    @SneakyThrows
    @Override
    public void init()
    {
        if (!isInitialized())
        {
            addAttribute("[routerLink]", location.startsWith("!") ? location : "['" + location + "']");
            if (state != null && !state.isEmpty())
            {
                addAttribute("[state]", IJsonRepresentation.getObjectMapper().writeValueAsString(state)
                        .replace('\'', ' ')
                        .replace('\"', ' ')
                );
            }
            if (params != null && !params.isEmpty())
            {
                addAttribute("[queryParams]", IJsonRepresentation.getObjectMapper().writeValueAsString(params)
                        .replace('\'', ' ')
                        .replace('\"', ' ')
                );
                addAttribute("queryParamsHandling", "merge");
            }
        }
        super.init();
    }

}
