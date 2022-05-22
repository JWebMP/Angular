/*
 * Copyright (C) 2017 GedMarc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jwebmp.core.base.angular;

import com.guicedee.logger.*;
import com.jwebmp.core.*;
import com.jwebmp.core.base.angular.services.*;
import com.jwebmp.core.base.angular.services.compiler.*;
import com.jwebmp.core.plugins.*;
import com.jwebmp.core.services.*;
import jakarta.validation.constraints.*;

import java.util.*;
import java.util.logging.*;

/**
 * @author GedMarc
 * @since 21 Feb 2017
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
@PluginInformation(pluginName = "AngularTS",
                   pluginDescription = "AngularTS renders TypeScript files and compiles them for Angular rendering",
                   pluginUniqueName = "angular",
                   pluginVersion = "1.8.2",
                   pluginDependancyUniqueIDs = "jquery",
                   pluginCategories = "jquery, angular, data-binding, ng, google",
                   pluginGitUrl = "https://github.com/GedMarc/JWebMP-AngularJS",
                   pluginSourceUrl = "https://angularjs.org",
                   pluginWikiUrl = "https://github.com/GedMarc/JWebMP-AngularJS/wiki",
                   pluginOriginalHomepage = "https://angularjs.org",
                   pluginDownloadUrl = "https://angularjs.org/",
                   pluginIconImageUrl = "https://angularjs.org/img/AngularJS-large.png",
                   pluginIconUrl = "https://angularjs.org/img/AngularJS-large.png",
                   pluginLastUpdatedDate = "2020/12/14",
                   pluginStatus = PluginStatus.Released,
                   pluginGroupId = "com.jwebmp.plugins.angular",
                   pluginArtifactId = "jwebmp-plugins-angularts",
                   pluginModuleName = "com.jwebmp.core.angularts",
                   pluginSubtitle = "AngularTS lets you extend HTML vocabulary for your application. The resulting environment is extraordinarily expressive, readable, and quick to develop."
)
public class TSPageConfigurator
		implements IPageConfigurator<TSPageConfigurator>
{
	private static final Logger log = LogFactory.getLog("Angular Page Configurator");
	/**
	 * If this configurator is enabled
	 */
	private static boolean enabled = true;
	/**
	 * If the angular functionality is requires or not
	 */
	private static boolean required;
	
	private Map<Page<?>, JWebMPTypeScriptCompiler> pageCompilers = new HashMap<>();
	private Set<String> assetLocations = new LinkedHashSet<>();
	
	/**
	 * Configures the angular page
	 */
	public TSPageConfigurator()
	{
		//No config required
	}
	
	/**
	 * Method isEnabled returns the enabled of this AngularAnimatedChangePageConfigurator object.
	 * <p>
	 * If this configurator is enabled
	 *
	 * @return the enabled (type boolean) of this AngularAnimatedChangePageConfigurator object.
	 */
	public static boolean isEnabled()
	{
		return TSPageConfigurator.enabled;
	}
	
	/**
	 * Method setEnabled sets the enabled of this AngularAnimatedChangePageConfigurator object.
	 * <p>
	 * If this configurator is enabled
	 *
	 * @param mustEnable the enabled of this AngularAnimatedChangePageConfigurator object.
	 */
	public static void setEnabled(boolean mustEnable)
	{
		TSPageConfigurator.enabled = mustEnable;
	}
	
	/**
	 * If the configurator is required
	 *
	 * @return If it is required to render
	 */
	public static boolean isRequired()
	{
		return TSPageConfigurator.required;
	}
	
	/**
	 * Sets angular as a required component
	 *
	 * @param required If it is required to render
	 */
	public static void setRequired(boolean required)
	{
		TSPageConfigurator.required = required;
	}
	
	@NotNull
	@Override
	public Page<?> configure(Page<?> page)
	{
		return page;
	}
	
	@Override
	public boolean enabled()
	{
		return TSPageConfigurator.enabled;
	}
	
	@Override
	public Integer sortOrder()
	{
		return Integer.MAX_VALUE - 100;
	}
	
}
