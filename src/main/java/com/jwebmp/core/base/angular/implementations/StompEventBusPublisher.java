package com.jwebmp.core.base.angular.implementations;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import lombok.extern.log4j.Log4j2;

/**
 * Helper class to publish messages to the STOMP event bus
 */
@Log4j2
public class StompEventBusPublisher {

    /**
     * Publishes a message to the STOMP event bus
     * 
     * @param vertx The Vertx instance
     * @param address The address to publish to
     * @param message The message to publish
     */
    public static void publish(Vertx vertx, String address, Object message) {
        String stompDestination = "/toStomp/" + address;
        vertx.eventBus().publish(stompDestination, message);
        log.debug("Published message to STOMP destination: {}", stompDestination);
    }
    
    /**
     * Publishes a message to the STOMP event bus with delivery options
     * 
     * @param vertx The Vertx instance
     * @param address The address to publish to
     * @param message The message to publish
     * @param options The delivery options
     */
    public static void publish(Vertx vertx, String address, Object message, DeliveryOptions options) {
        String stompDestination = "/toStomp/" + address;
        vertx.eventBus().publish(stompDestination, message, options);
        log.debug("Published message to STOMP destination: {} with options", stompDestination);
    }
    
    /**
     * Sends a message to the STOMP event bus
     * 
     * @param vertx The Vertx instance
     * @param address The address to send to
     * @param message The message to send
     */
    public static void send(Vertx vertx, String address, Object message) {
        String stompDestination = "/toStomp/" + address;
        vertx.eventBus().send(stompDestination, message);
        log.debug("Sent message to STOMP destination: {}", stompDestination);
    }
    
    /**
     * Sends a message to the STOMP event bus with delivery options
     * 
     * @param vertx The Vertx instance
     * @param address The address to send to
     * @param message The message to send
     * @param options The delivery options
     */
    public static void send(Vertx vertx, String address, Object message, DeliveryOptions options) {
        String stompDestination = "/toStomp/" + address;
        vertx.eventBus().send(stompDestination, message, options);
        log.debug("Sent message to STOMP destination: {} with options", stompDestination);
    }
}