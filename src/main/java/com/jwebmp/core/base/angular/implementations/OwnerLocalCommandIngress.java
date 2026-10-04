package com.jwebmp.core.base.angular.implementations;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.ext.stomp.*;
import java.util.*;

/** Commands retain the accepting node's call context; notifications keep the broadcast bridge. */
public final class OwnerLocalCommandIngress {
    public static final String ADDRESS = "/toBus/incoming";
    private final Vertx vertx;
    private final Map<StompServerConnection, Map<String, String>> subscriptions = new IdentityHashMap<>();
    private final Map<StompServerConnection, Integer> pending = new IdentityHashMap<>();
    public OwnerLocalCommandIngress(Vertx vertx) { this.vertx = vertx; }

    public synchronized void subscribed(ServerFrame frame) {
        var destination = frame.connection().handler().getDestination(frame.frame().getDestination());
        if (destination != null && destination.getSubscriptions(frame.connection()).contains(frame.frame().getId()))
            subscriptions.computeIfAbsent(frame.connection(), ignored -> new HashMap<>())
                    .put(frame.frame().getId(), frame.frame().getDestination());
    }
    public synchronized void unsubscribed(ServerFrame frame) {
        var entries = subscriptions.get(frame.connection());
        if (entries != null) entries.remove(frame.frame().getId());
    }
    public synchronized void closed(StompServerConnection connection) {
        subscriptions.remove(connection); pending.remove(connection);
    }
    public boolean send(ServerFrame frame) {
        if (!ADDRESS.equals(frame.frame().getDestination())) return false;
        var connection = frame.connection();
        String reply = frame.frame().getHeader("reply-address");
        String replyId = null;
        synchronized (this) {
            if (reply != null) replyId = subscriptions.getOrDefault(connection, Map.of()).entrySet().stream()
                    .filter(entry -> reply.equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
            if (frame.frame().getHeader("transaction") != null || frame.frame().getBody() == null
                    || frame.frame().getBody().length() > 1024 * 1024 || reply != null && replyId == null
                    || pending.getOrDefault(connection, 0) >= 32) {
                connection.close(); return true;
            }
            pending.merge(connection, 1, Integer::sum);
        }
        String subscriptionId = replyId;
        var options = new DeliveryOptions().setLocalOnly(true).setSendTimeout(10000);
        frame.frame().getHeaders().forEach(options::addHeader);
        // Do not accept a browser's claim about the transport session.
        options.addHeader("stomp-session", connection.session());
        vertx.eventBus().request(ADDRESS, frame.frame().getBody(), options).onComplete(result -> {
            synchronized (this) {
                if (!pending.containsKey(connection)) return;
                pending.computeIfPresent(connection, (key, count) -> count <= 1 ? null : count - 1);
            }
            if (result.failed()) { connection.close(); return; }
            Object guidValue = new io.vertx.core.json.JsonObject(frame.frame().getBody()).getJsonObject("data", new io.vertx.core.json.JsonObject()).getValue("guid");
            String guid = guidValue == null ? null : guidValue.toString();
            for (String scope : List.of("SessionStorage", "LocalStorage")) {
                String payload = result.result().headers().get("jwebmp-" + scope.toLowerCase(Locale.ROOT));
                if (payload == null || guid == null) continue;
                String destination = "/toStomp/" + guid + "." + scope;
                List<String> ids;
                synchronized (this) { ids = subscriptions.getOrDefault(connection, Map.of()).entrySet().stream()
                        .filter(entry -> destination.equals(entry.getValue())).map(Map.Entry::getKey).toList(); }
                for (String id : ids) connection.write(new Frame().setCommand(Command.MESSAGE).setHeaders(Map.of(
                        "destination", destination, "subscription", id, "message-id", UUID.randomUUID().toString(),
                        "content-type", "application/json")).setBody(Buffer.buffer(payload)));
            }
            if (reply != null) {
                Object body = result.result().body();
                Buffer encoded = body instanceof Buffer buffer ? buffer : Buffer.buffer(
                        body instanceof String text ? text : io.vertx.core.json.Json.encode(body));
                connection.write(new Frame().setCommand(Command.MESSAGE).setHeaders(Map.of(
                        "destination", reply, "subscription", subscriptionId, "message-id", UUID.randomUUID().toString(),
                        "content-type", "application/json")).setBody(encoded));
            }
            Frames.handleReceipt(frame.frame(), connection);
        });
        return true;
    }
}
