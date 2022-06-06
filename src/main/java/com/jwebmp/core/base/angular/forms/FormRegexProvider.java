package com.jwebmp.core.base.angular.forms;

import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.utilities.regex.*;

import java.util.*;

@NgDataType
public class FormRegexProvider implements INgDataType<FormRegexProvider>
{
	@Override
	public List<String> componentFields()
	{
		List<String> out = new ArrayList<>();
		RegularExpressionsDTO registeredExpressions = new RegularExpressionsDTO();
		registeredExpressions.getRegularExpressions().forEach((name,pattern)->{
			out.add("public static " + name + " : string = `" + pattern.toString().replace("`","\\`") + "`");
		});
		return out;
	}
}
