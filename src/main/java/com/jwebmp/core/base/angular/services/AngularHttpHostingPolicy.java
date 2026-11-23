package com.jwebmp.core.base.angular.services;

import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;

/**
 * Startup policy for the HTTP pages, assets and SPA fallback of a discovered Angular app.
 * Register a provider with ServiceLoader and return true for apps it does not own.
 * A false result from any provider disables HTTP hosting for that app. TypeScript generation
 * and the STOMP bridge are configured independently.
 */
@FunctionalInterface
public interface AngularHttpHostingPolicy {
    boolean permitsHttpHosting(Class<? extends INgApp<?>> application);
}
