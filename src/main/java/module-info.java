import com.guicedee.guicedinjection.interfaces.IGuiceConfigurator;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedinjection.interfaces.IGuicePreStartup;
import com.guicedee.guicedservlets.websockets.services.IWebSocketAuthDataProvider;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import com.guicedee.vertx.spi.VertxRouterConfigurator;
import com.jwebmp.core.base.angular.client.services.spi.*;
import com.jwebmp.core.base.angular.implementations.*;
import com.jwebmp.core.base.angular.modules.listeners.OnClickListener;
import com.jwebmp.core.base.angular.services.RenderedAssets;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.databind.IAfterRenderComplete;
import com.jwebmp.core.databind.IOnComponentAdded;
import com.jwebmp.core.databind.IOnComponentHtmlRender;
import com.jwebmp.core.events.services.IOnClickService;

module com.jwebmp.core.angular {
    uses com.jwebmp.core.base.angular.services.AngularScanPackages;

    exports com.jwebmp.core.base.angular;
    exports com.jwebmp.core.base.angular.implementations;

    exports com.jwebmp.core.base.angular.services.compiler;
    exports com.jwebmp.core.base.angular.services;

    requires transitive com.guicedee.client;
    requires transitive com.jwebmp.client;
    requires transitive com.jwebmp.core.base.angular.client;


    requires static lombok;

    requires org.apache.commons.io;

    requires com.jwebmp.vertx;

    requires com.guicedee.jsonrepresentation;
    requires com.guicedee.vertx;
    requires com.jwebmp.core;
    requires org.apache.commons.text;
    requires org.apache.commons.lang3;
    requires io.vertx.eventbusbridge;

    provides OnGetAllConstructorParameters with OnFetchAllConstructorParameters;
    provides OnGetAllConstructorBodies with OnFetchAllConstructorBodies;
    provides OnGetAllFields with OnFetchAllFields;
    provides OnGetAllImports with OnFetchAllImports;
    provides OnGetAllMethods with OnFetchAllMethods;
    provides OnGetAllModuleImports with OnFetchAllModuleImports;


    provides com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions with AngularScanModuleInclusion;
    provides IOnComponentHtmlRender with OnComponentRenderApplyAngular;
    provides IGuiceConfigurator with SearchPathConfigurator;
    provides IOnComponentAdded with OnComponentAdded;
    provides IAfterRenderComplete with AngularAfterRenderCompleted, AngularTagReplacementAfterRender, AngularComponentReferencesAfterRender;

    provides IGuicePreStartup with AngularPreStartup;
    provides IGuiceModule with AngularTSSiteBinder;
    provides VertxRouterConfigurator with AngularTSSiteBinder;

    provides com.guicedee.guicedinjection.interfaces.IGuicePostStartup with AngularTSPostStartup;

    provides IOnClickService with OnClickListener;

    provides IWebSocketMessageReceiver with WebSocketAjaxCallReceiver, WebSocketDataRequestCallReceiver, WebSocketDataSendCallReceiver,
            WSAddToGroupMessageReceiver, WSRemoveFromWebsocketGroupMessageReceiver;

    uses IWebSocketAuthDataProvider;
    uses RenderedAssets;
    uses NpmrcConfigurator;
    uses com.jwebmp.core.base.angular.modules.services.angular.WebSocketGroupAdd;

    exports com.jwebmp.core.base.angular.modules.directives;

    opens com.jwebmp.core.base.angular.typescript.JWebMP to com.google.guice, com.fasterxml.jackson.databind, com.jwebmp.core;
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
