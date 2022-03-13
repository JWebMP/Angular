package com.jwebmp.core.base.angular.services.interfaces;

import com.guicedee.guicedinjection.representations.IJsonRepresentation;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.databind.*;

import java.io.*;
import java.util.*;

@NgSourceDirectoryReference()
public interface INgDataType<J extends INgDataType<J>>
		extends ITSComponent<J>, IConfiguration, IJsonRepresentation<J>
{
}
