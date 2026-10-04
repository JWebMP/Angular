package com.jwebmp.core.base.angular.implementations;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/** Enforces limits at the actual STOMP write boundary, including bridge broadcasts. */
public final class BoundedStompSocket {
    private static final Set<String> WRITES = Set.of("write", "writeFrame", "writeTextMessage", "writeBinaryMessage", "writeFinalTextFrame", "writeFinalBinaryFrame", "writePing", "writePong");
    public static ServerWebSocket wrap(ServerWebSocket socket) {
        return wrap(socket, null);
    }
    public static ServerWebSocket wrap(ServerWebSocket socket, io.vertx.core.http.HttpConnection upgradeConnection) {
        socket.setWriteQueueMaxSize(65536);
        var closing = new java.util.concurrent.atomic.AtomicBoolean();
        return (ServerWebSocket) Proxy.newProxyInstance(ServerWebSocket.class.getClassLoader(),
                new Class<?>[]{ServerWebSocket.class}, (proxy, method, args) -> {
            if (method.getName().equals("endHandler")) {
                @SuppressWarnings("unchecked")
                var end = (io.vertx.core.Handler<Void>) args[0];
                var ended = new java.util.concurrent.atomic.AtomicBoolean();
                io.vertx.core.Handler<Void> cleanup = ignored -> {
                    if (end != null && ended.compareAndSet(false, true)) end.handle(null);
                };
                socket.endHandler(cleanup);
                // Vert.x STOMP installs only end/exception handlers. A local graceful
                // shutdown emits close without end, so explicitly retire its connection.
                socket.closeHandler(ignored -> {
                    org.apache.logging.log4j.LogManager.getLogger(BoundedStompSocket.class).trace(
                            "STOMP WebSocket closed: code={}, reason={}", socket.closeStatusCode(), socket.closeReason());
                    cleanup.handle(null);
                });
                return proxy;
            }
            if (WRITES.contains(method.getName())) {
                int bytes = args == null || args.length == 0 ? 0 : args[0] instanceof Buffer buffer ? buffer.length()
                        : args[0] instanceof WebSocketFrame frame ? frame.binaryData().length()
                        : args[0] instanceof String text ? text.getBytes(StandardCharsets.UTF_8).length : 0;
                if (closing.get()) return Future.failedFuture("STOMP socket stopping");
                if (socket.isClosed() || socket.writeQueueFull() || bytes > 1024 * 1024) {
                    if (!socket.isClosed() && closing.compareAndSet(false, true)) {
                        org.apache.logging.log4j.LogManager.getLogger(BoundedStompSocket.class).warn("STOMP socket write capacity exceeded: bytes={}", bytes);
                        com.guicedee.vertx.WebSocketBackpressure.close(socket, upgradeConnection, (short) 1013, "Slow consumer or oversized frame");
                    }
                    return Future.failedFuture("STOMP socket write capacity exceeded");
                }
            }
            try { Object result = method.invoke(socket, args); return result == socket ? proxy : result; }
            catch (InvocationTargetException failure) { throw failure.getCause(); }
        });
    }
    private BoundedStompSocket() { }
}
