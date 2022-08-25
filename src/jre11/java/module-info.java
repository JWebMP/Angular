import com.guicedee.guicedinjection.interfaces.*;
import com.guicedee.guicedservlets.websockets.services.*;
import com.jwebmp.core.base.angular.client.services.spi.*;
import com.jwebmp.core.base.angular.implementations.*;
import com.jwebmp.core.base.angular.modules.listeners.*;
import com.jwebmp.core.base.angular.services.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.databind.*;
import com.jwebmp.core.events.click.*;

module com.jwebmp.core.angular {
	uses com.jwebmp.core.base.angular.services.AngularScanPackages;
	
	exports com.jwebmp.core.base.angular;
	exports com.jwebmp.core.base.angular.implementations;
	exports com.jwebmp.core.base.angular.forms;
	exports com.jwebmp.core.base.angular.forms.enumerations;
	exports com.jwebmp.core.base.angular.services.compiler;
	exports com.jwebmp.core.base.angular.services;

	requires transitive com.jwebmp.core;
	requires transitive com.jwebmp.core.base.angular.client;
	
	requires com.fasterxml.jackson.databind;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;

	requires com.guicedee.guicedservlets.websockets;
	
	requires transitive undertow.core;
	requires transitive undertow.servlet;
	
	requires undertow.websockets.jsr;
	requires jakarta.websocket.api;
	requires org.apache.commons.text;
	
	requires com.guicedee.guicedservlets.undertow;
	
	provides OnGetAllConstructorParameters with OnFetchAllConstructorParameters;
	provides OnGetAllConstructorBodies with OnFetchAllConstructorBodies;
	provides OnGetAllFields with OnFetchAllFields;
	provides OnGetAllImports with OnFetchAllImports;
	provides OnGetAllMethods with OnFetchAllMethods;
	
	
	provides com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions with AngularScanModuleInclusion;
	provides com.guicedee.guicedservlets.websockets.services.IWebSocketPreConfiguration with JWebMPGuicedUndertowWebSocketConfiguration;
	provides IOnComponentHtmlRender with OnComponentRenderApplyAngular;
	provides IGuiceConfigurator with SearchPathConfigurator;
	provides IOnComponentAdded with OnComponentAdded;
	provides com.guicedee.guicedservlets.services.IGuiceSiteBinder with AngularTSSiteBinder;
	provides com.guicedee.guicedinjection.interfaces.IGuicePostStartup with AngularTSPostStartup;
	//provides com.guicedee.guicedservlets.undertow.services.UndertowPathHandler with AngularTSPathHandler;
	provides com.jwebmp.core.databind.IOnDataBind with AngularTSOnBind;
	provides IGuicePreStartup with AngularTSPreStartup;
	provides IOnClickService with OnClickListener;
	
	provides IWebSocketMessageReceiver with WebSocketAjaxCallReceiver,WebSocketDataRequestCallReceiver,WebSocketDataSendCallReceiver,
			WSAddToGroupMessageReceiver, WSRemoveFromWebsocketGroupMessageReceiver;
	
	uses IWebSocketAuthDataProvider;
	uses RenderedAssets;
	
	//opens com.jwebmp.core.base.angular.modules to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
	opens com.jwebmp.core.base.angular.typescript.JWebMP to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
	opens com.jwebmp.core.base.angular.services to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
	
	exports com.jwebmp.core.base.angular.services.interfaces;
	opens com.jwebmp.core.base.angular.services.interfaces to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	opens com.jwebmp.core.base.angular.implementations to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;

	opens com.jwebmp.core.base.angular.modules.listeners to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	opens com.jwebmp.core.base.angular.modules.directives to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	exports com.jwebmp.core.base.angular.modules.services.angular;
	opens com.jwebmp.core.base.angular.modules.services.angular to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	exports com.jwebmp.core.base.angular.modules.services.rxtxjs.stompjs;
	exports com.jwebmp.core.base.angular.modules.services.base;
	opens com.jwebmp.core.base.angular.modules.services.base to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;

	opens com.jwebmp.core.base.angular.services.compiler to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
}
