package com.jwebmp.core.base.angular.typescript;
import com.jwebmp.core.base.angular.implementations.BoundedStompSocket;
import io.vertx.core.Future;
import io.vertx.core.http.ServerWebSocket;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import static org.junit.jupiter.api.Assertions.*;
class BoundedStompSocketTest {
    static final class SocketFixture {
        int writes, queueSize, closeCode; boolean full;
        io.vertx.core.Handler<Void> end, close;
        final io.vertx.core.MultiMap headers = io.vertx.core.MultiMap.caseInsensitiveMultiMap();
        final Future<Void> completion = Future.succeededFuture();
        final ServerWebSocket socket = (ServerWebSocket) Proxy.newProxyInstance(ServerWebSocket.class.getClassLoader(),
                new Class<?>[]{ServerWebSocket.class}, (proxy, method, args) -> switch(method.getName()) {
            case "setWriteQueueMaxSize" -> { queueSize = (int)args[0]; yield proxy; }
            case "writeQueueFull" -> full;
            case "isClosed" -> false;
            case "headers" -> headers;
            case "writeTextMessage" -> { writes++; yield completion; }
            case "shutdown" -> { assertEquals(java.time.Duration.ofSeconds(2), args[0]); closeCode = (short)args[1]; yield completion; }
            case "endHandler" -> { end = (io.vertx.core.Handler<Void>)args[0]; yield proxy; }
            case "closeHandler" -> { close = (io.vertx.core.Handler<Void>)args[0]; yield proxy; }
            case "closeStatusCode" -> (short)1000;
            case "closeReason" -> "closed";
            default -> throw new AssertionError("Unexpected socket call: " + method);
        });
    }
    @Test void normalWritesRetainTheUnderlyingCompletion() {
        var fixture = new SocketFixture();
        assertSame(fixture.completion, BoundedStompSocket.wrap(fixture.socket).writeTextMessage("ok"));
        assertEquals(65536, fixture.queueSize); assertEquals(1, fixture.writes); assertEquals(0, fixture.closeCode);
    }
    @Test void aSlowSocketIsClosedBeforeTheNextWriteIsQueued() {
        var fixture = new SocketFixture(); fixture.full = true;
        assertTrue(BoundedStompSocket.wrap(fixture.socket).writeTextMessage("snapshot").failed());
        assertEquals(1013, fixture.closeCode); assertEquals(0, fixture.writes);
    }
    @Test void oversizedUtf8OutputCannotBypassTheByteLimit() {
        var fixture = new SocketFixture();
        assertTrue(BoundedStompSocket.wrap(fixture.socket).writeTextMessage("\u20ac".repeat(400000)).failed());
        assertEquals(0, fixture.writes); assertEquals(1013, fixture.closeCode);
    }
    @Test void localTransportCloseRetiresTheStompConnectionEvenWithoutAnEndFrame() {
        var fixture = new SocketFixture();
        var retired = new java.util.concurrent.atomic.AtomicInteger();
        BoundedStompSocket.wrap(fixture.socket).endHandler(ignored -> retired.incrementAndGet());
        fixture.close.handle(null);
        fixture.end.handle(null);
        fixture.close.handle(null);
        assertEquals(1, retired.get());
    }
}
