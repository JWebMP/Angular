import com.guicedee.client.services.lifecycle.IGuicePostStartup;
import com.guicedee.client.services.config.IGuiceScanModuleInclusions;
import com.jwebmp.core.base.angular.client.services.TypescriptIndexPageConfigurator;
import com.jwebmp.core.base.angular.modules.listeners.OnClickListener;
import com.jwebmp.core.base.angular.implementations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.guicedee.client.services.lifecycle.IGuiceConfigurator;
import com.guicedee.client.services.lifecycle.IGuiceModule;
import com.guicedee.client.services.lifecycle.IGuicePreStartup;
import com.guicedee.client.services.websocket.IWebSocketAuthDataProvider;
import com.guicedee.client.services.websocket.IWebSocketMessageReceiver;
import com.guicedee.vertx.web.spi.VertxRouterConfigurator;

import com.jwebmp.core.base.angular.implementations.configurations.ConfigureImportReferences;
import com.jwebmp.core.base.angular.services.RenderedAssets;
import com.jwebmp.core.events.services.IOnClickService;
import com.jwebmp.core.services.IPageConfigurator;


module com.jwebmp.core.angular {
    uses com.jwebmp.core.base.angular.services.AngularScanPackages;

    exports com.jwebmp.core.base.angular;
    exports com.jwebmp.core.base.angular.implementations;

    exports com.jwebmp.core.base.angular.services.compiler;
    exports com.jwebmp.core.base.angular.services;

    requires transitive com.jwebmp.core.base.angular.client;
    requires transitive com.jwebmp.vertx;

    requires static lombok;

    requires org.apache.commons.io;

    requires transitive org.apache.commons.text;
    requires transitive org.apache.commons.lang3;
    requires transitive io.vertx.eventbusbridge;
    requires transitive io.vertx.stomp;

    provides IGuiceScanModuleInclusions with AngularScanModuleInclusion;
    provides IGuiceConfigurator with SearchPathConfigurator;

    provides IGuicePreStartup with AngularPreStartup;
    provides IGuiceModule with AngularTSSiteBinder;
    provides VertxRouterConfigurator with AngularTSSiteBinder;

    provides IGuicePostStartup with AngularTSPostStartup;

    provides com.jwebmp.core.databind.IOnComponentConfigured with ConfigureImportReferences;

    provides IOnClickService with OnClickListener;

    provides IWebSocketMessageReceiver with WebSocketAjaxCallReceiver, WebSocketDataRequestCallReceiver, WebSocketDataSendCallReceiver,
            WSAddToGroupMessageReceiver, WSRemoveFromWebsocketGroupMessageReceiver;
    provides com.guicedee.vertx.web.spi.VertxHttpServerOptionsConfigurator with AngularTSSiteBinder;

    uses IWebSocketAuthDataProvider;
    uses RenderedAssets;
    uses NpmrcConfigurator;
    uses com.jwebmp.core.base.angular.modules.services.angular.WebSocketGroupAdd;

    uses TypescriptIndexPageConfigurator;
    uses IPageConfigurator;

    exports com.jwebmp.core.base.angular.modules.directives;
    exports com.jwebmp.core.base.angular.components.modules;
    exports com.jwebmp.core.base.angular.implementations.configurations;

    opens com.jwebmp.core.base.angular.typescript.JWebMP to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
    opens com.jwebmp.core.base.angular.components.modules to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
    opens com.jwebmp.core.base.angular.implementations.configurations to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
    opens com.jwebmp.core.base.angular.services to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
    opens com.jwebmp.core.base.angular.components to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
    exports com.jwebmp.core.base.angular.components;

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
