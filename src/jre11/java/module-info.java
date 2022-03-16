import com.guicedee.guicedinjection.interfaces.*;
import com.guicedee.guicedservlets.websockets.services.*;
import com.jwebmp.core.base.angular.implementations.*;
import com.jwebmp.core.base.angular.modules.listeners.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.databind.*;
import com.jwebmp.core.events.click.*;

module com.jwebmp.core.angular {
	uses com.jwebmp.core.base.angular.services.AngularScanPackages;
	
	exports com.jwebmp.core.base.angular.modules.services.angular.forms;
	exports com.jwebmp.core.base.angular;
	exports com.jwebmp.core.base.angular.forms;
	exports com.jwebmp.core.base.angular.forms.enumerations;
	exports com.jwebmp.core.base.angular.services;
	exports com.jwebmp.core.base.angular.services.annotations;
	
	requires transitive com.jwebmp.core;
	
	requires jakarta.validation;
	requires java.logging;
	requires com.guicedee.logmaster;
	requires com.google.guice;
	requires com.guicedee.guicedinjection;
	requires com.google.guice.extensions.servlet;
	requires com.guicedee.guicedservlets;
	
	requires com.fasterxml.jackson.databind;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires jakarta.servlet;
	requires com.guicedee.guicedservlets.websockets;
	
	requires transitive undertow.core;
	requires transitive undertow.servlet;
	requires undertow.websockets.jsr;
	requires jakarta.websocket.api;
	requires org.apache.commons.text;
	
	requires com.guicedee.guicedservlets.undertow;
	
	requires com.mangofactory.typescript4j;
	
	requires static com.jwebmp.plugins.security.localstorage ;
	requires static com.jwebmp.plugins.security.sessionstorage ;
	
	provides com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions with AngularScanModuleInclusion;
	provides com.guicedee.guicedservlets.websockets.services.IWebSocketPreConfiguration with JWebMPGuicedUndertowWebSocketConfiguration;
	provides IOnComponentHtmlRender with OnComponentRenderApplyAngular;
	provides IGuiceConfigurator with SearchPathConfigurator;
	provides IOnComponentAdded with OnComponentAdded;
	provides com.guicedee.guicedservlets.services.IGuiceSiteBinder with AngularTSSiteBinder;
	provides com.guicedee.guicedinjection.interfaces.IGuicePostStartup with AngularTSPostStartup;
	provides com.guicedee.guicedservlets.undertow.services.UndertowPathHandler with AngularTSPathHandler;
	provides com.jwebmp.core.databind.IOnDataBind with AngularTSOnBind;
	
	
	provides IOnClickService with OnClickListener;
	
	provides IWebSocketMessageReceiver with WebSocketAjaxCallReceiver,WebSocketDataRequestCallReceiver;
	
	uses IWebSocketAuthDataProvider;
	
	
	//opens com.jwebmp.core.base.angular.modules to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
	opens com.jwebmp.core.base.angular.typescript.JWebMP to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
	opens com.jwebmp.core.base.angular.services to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
	
	opens com.jwebmp.core.base.angular.services.annotations to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	exports com.jwebmp.core.base.angular.services.interfaces;
	opens com.jwebmp.core.base.angular.services.interfaces to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	opens com.jwebmp.core.base.angular.implementations to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	exports com.jwebmp.core.base.angular.modules.services;
	opens com.jwebmp.core.base.angular.modules.services to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	opens com.jwebmp.core.base.angular.modules.listeners to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	opens com.jwebmp.core.base.angular.modules.directives to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	exports com.jwebmp.core.base.angular.modules.services.rxtxjs;
	opens com.jwebmp.core.base.angular.modules.services.rxtxjs to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	exports com.jwebmp.core.base.angular.modules.services.angular;
	opens com.jwebmp.core.base.angular.modules.services.angular to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	exports com.jwebmp.core.base.angular.modules.services.observable;
	opens com.jwebmp.core.base.angular.modules.services.observable to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	exports com.jwebmp.core.base.angular.modules.services.rxtxjs.stompjs;
	exports com.jwebmp.core.base.angular.modules.services.websocket;
	opens com.jwebmp.core.base.angular.modules.services.websocket to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	exports com.jwebmp.core.base.angular.modules.services.base;
	opens com.jwebmp.core.base.angular.modules.services.base to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	opens com.jwebmp.core.base.angular.modules.services.storage to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
	exports com.jwebmp.core.base.angular.services.annotations.angularconfig;
	opens com.jwebmp.core.base.angular.services.annotations.angularconfig to com.fasterxml.jackson.databind, com.google.guice, com.jwebmp.core;
//	opens com.jwebmp.core.base.angular.typescript to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
	
}
