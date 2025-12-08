package me.miki.shindo.management.network;

import me.miki.shindo.logger.ShindoLogger;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight helpers for latency probing and speed tests. This avoids blocking the UI thread by running work in
 * background threads and limits payload sizes to keep tests quick.
 */
public final class NetworkDiagnostics {

    private NetworkDiagnostics() {
    }

    public static void runLatencyTest(List<String> hosts, int timeoutMs, LatencyCallback callback) {
        new Thread(() -> {
            List<LatencyResult> results = new ArrayList<>();
            for (String host : hosts) {
                if (host == null || host.trim().isEmpty()) {
                    continue;
                }
                results.add(pingHost(host.trim(), timeoutMs));
            }
            if (callback != null) {
                callback.onLatency(results);
            }
        }, "shindo-latency").start();
    }

    public static void runSpeedTest(String url, int bytesLimit, SpeedCallback callback) {
        new Thread(() -> {
            SpeedResult result = download(url, bytesLimit);
            if (callback != null) {
                callback.onSpeed(result);
            }
        }, "shindo-speedtest").start();
    }

    private static LatencyResult pingHost(String host, int timeoutMs) {
        long start = System.currentTimeMillis();
        try {
            InetAddress address = InetAddress.getByName(host);
            boolean reachable = address.isReachable(timeoutMs);
            long took = System.currentTimeMillis() - start;
            return new LatencyResult(host, reachable ? (int) took : -1, null);
        } catch (Exception ex) {
            return new LatencyResult(host, -1, ex.getMessage());
        }
    }

    private static SpeedResult download(String targetUrl, int limitBytes) {
        long start = System.currentTimeMillis();
        int readBytes = 0;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(targetUrl).openConnection();
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(4000);
            connection.setRequestProperty("User-Agent", "Shindo-SpeedTest");
            connection.connect();
            int status = connection.getResponseCode();
            if (status >= 400) {
                return new SpeedResult(0D, 0, "HTTP " + status);
            }
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream())) {
                byte[] buffer = new byte[8192];
                while (readBytes < limitBytes) {
                    int read = in.read(buffer, 0, Math.min(buffer.length, limitBytes - readBytes));
                    if (read == -1) {
                        break;
                    }
                    readBytes += read;
                }
            }
            long durationMs = Math.max(1L, System.currentTimeMillis() - start);
            double mbps = (readBytes * 8D) / (durationMs / 1000D) / (1024D * 1024D);
            return new SpeedResult(mbps, readBytes, null);
        } catch (Exception ex) {
            ShindoLogger.warn("Speed test failed", ex);
            return new SpeedResult(0D, readBytes, ex.getMessage());
        }
    }

    public interface LatencyCallback {
        void onLatency(List<LatencyResult> results);
    }

    public interface SpeedCallback {
        void onSpeed(SpeedResult result);
    }

    public static final class LatencyResult {
        public final String host;
        public final int pingMs;
        public final String error;

        public LatencyResult(String host, int pingMs, String error) {
            this.host = host;
            this.pingMs = pingMs;
            this.error = error;
        }
    }

    public static final class SpeedResult {
        public final double downloadMbps;
        public final int bytesRead;
        public final String error;

        public SpeedResult(double downloadMbps, int bytesRead, String error) {
            this.downloadMbps = downloadMbps;
            this.bytesRead = bytesRead;
            this.error = error;
        }
    }
}
