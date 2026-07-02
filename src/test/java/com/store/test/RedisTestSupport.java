package com.store.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class RedisTestSupport {

    private RedisTestSupport() {
    }

    public static boolean isRedisAvailable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 6379), 500);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
