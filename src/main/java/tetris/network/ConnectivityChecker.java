package tetris.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/* opens a real TCP connection to well-known host checking for internet access - actual network requirement not just sim. */

public final class ConnectivityChecker {
    private static  final String PROBE_HOST = "8.8.8.8";
    private static final int PROBE_PORT = 53;
    private static final int TIMEOUT_MS = 1500;

    private ConnectivityChecker() {
    }

    public static void checkAsync(Consumer<Boolean> onResult) {
        CompletableFuture
                .supplyAsync(ConnectivityChecker::isOnlineBlocking, Executors.newSingleThreadExecutor(runnable -> {
                    Thread thread = new Thread(runnable, "connectivity-check");
                    thread.setDaemon(true);
                    return thread;
                }))
                .thenAccept(onResult);
    }

    public static boolean isOnlineBlocking() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(PROBE_HOST, PROBE_PORT), TIMEOUT_MS);
            return true;
        } catch (IOException unreachable) {
            return false;
        }
    }
}