package com.jwebmp.core.base.angular.modules.services.rxtxjs.stompjs;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@JsonAutoDetect(fieldVisibility = ANY,
                getterVisibility = NONE,
                setterVisibility = NONE)
@JsonInclude(NON_NULL)
public class ConnectHeaders
{
	@JsonValue
	private final Map<String, String> headers = new HashMap<>();
	
	public Map<String, String> getHeaders()
	{
		return headers;
	}
}
