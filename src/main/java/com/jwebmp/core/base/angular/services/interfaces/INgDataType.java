package com.jwebmp.core.base.angular.services.interfaces;

import com.guicedee.guicedinjection.representations.*;
import com.jwebmp.core.databind.*;
import org.apache.commons.lang3.*;

import java.lang.reflect.*;
import java.math.*;
import java.util.*;

public interface INgDataType<J extends INgDataType<J>>
		extends ITSComponent<J>, IConfiguration, IJsonRepresentation<J>
{
	@Override
	default List<String> fields()
	{
		List<String> fields = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for (Field declaredField : getClass()
				.getDeclaredFields())
		{
			renderFieldTS(sb, declaredField.getName(), declaredField.getType(), declaredField, false);
		}
		fields.add(sb.toString());
		return fields;
	}
	
	default void renderFieldTS(StringBuilder out, String fieldName, Class fieldType, Field field, boolean array)
	{
		if (Number.class.isAssignableFrom(fieldType))
		{
			out.append(" public " + fieldName + "? : number" + (array ? "[]" : "") + " = " + (array ? "[]" : "0") + ";\n");
		}
		else if (BigDecimal.class.isAssignableFrom(fieldType))
		{
			out.append(" public " + fieldName + "? : number" + (array ? "[]" : "") + " = " + (array ? "[]" : "0") + ";\n");
		}
		else if (BigInteger.class.isAssignableFrom(fieldType))
		{
			out.append(" public " + fieldName + "? : number" + (array ? "[]" : "") + " = " + (array ? "[]" : "0") + ";\n");
		}
		else if (String.class.isAssignableFrom(fieldType))
		{
			out.append(" public " + fieldName + "? : string" + (array ? "[]" : "") + " = " + (array ? "[]" : "''") + ";\n");
		}
		else if (Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType))
		{
			out.append(" public " + fieldName + "? : boolean" + (array ? "[]" : "") + " =" + (array ? "[]" : "false") + ";\n");
		}
		else if (INgDataType.class.isAssignableFrom(fieldType))
		{
			//todo make this import the data type from the class
			//out.append(" public " + fieldName + "? : " + getTsFilename(fieldType) + "" + (array ? "[]" : "") + " = " + (array ? "[]" : "{}") + ";\n");
			out.append(" public " + fieldName + "? : any " + (array ? "[]" : "") + " = " + (array ? "[]" : "{}") + ";\n");
		}
		else if (Collection.class.isAssignableFrom(fieldType))
		{
			//get generic type
			String genericType = StringUtils.substringBetween(field.getGenericType()
			                                                       .getTypeName(), "<", ">");
			try
			{
				renderFieldTS(out, fieldName, Class.forName(genericType), field, true);
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			//out.append(" public " + fieldName + "? : " + getTsFilename(fieldType) + " = [];\n");
		}
		else if (Object.class.isAssignableFrom(fieldType))
		{
			out.append(" public " + fieldName + "? : any" + (array ? "[]" : "") + ";\n");
		}
	}
}
