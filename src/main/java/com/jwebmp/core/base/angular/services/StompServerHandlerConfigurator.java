package com.jwebmp.core.base.angular.services;

import io.vertx.core.Vertx;
import io.vertx.ext.stomp.StompServerHandler;
import io.vertx.ext.stomp.StompServerConnection;
import io.vertx.ext.stomp.ServerFrame;
import io.vertx.ext.stomp.DefaultSubscribeHandler;
import io.vertx.ext.stomp.DefaultSendHandler;
import io.vertx.ext.stomp.DefaultUnsubscribeHandler;

/**
 * Application policy hook for the existing Angular STOMP bridge, applied once after bridge setup.
 * Return true only when a frame has been handled (including denial). Unclaimed frames use the defaults.
 * Exceptions abort bridge configuration rather than silently dropping security policy.
 */
public interface StompServerHandlerConfigurator {
    default boolean subscribe(ServerFrame frame) { return false; }
    default boolean send(ServerFrame frame) { return false; }
    default boolean unsubscribe(ServerFrame frame) { return false; }

    /** Composed by the bridge owner without exposing mutable connection hooks to policies. */
    default void closed(StompServerConnection connection) { }

    static void configureAll(Vertx vertx, StompServerHandler handler,
                             java.util.List<StompServerHandlerConfigurator> policies) {
        var subscribe = new DefaultSubscribeHandler();
        var send = new DefaultSendHandler();
        var unsubscribe = new DefaultUnsubscribeHandler();
        handler.subscribeHandler(frame -> {
            for (var policy : policies) if (policy.subscribe(frame)) return;
            subscribe.handle(frame);
        });
        handler.sendHandler(frame -> {
            for (var policy : policies) if (policy.send(frame)) return;
            send.handle(frame);
        });
        handler.unsubscribeHandler(frame -> {
            for (var policy : policies) if (policy.unsubscribe(frame)) return;
            unsubscribe.handle(frame);
        });
        handler.closeHandler(connection -> {
            for (var policy : policies) policy.closed(connection);
        });
    }
}
