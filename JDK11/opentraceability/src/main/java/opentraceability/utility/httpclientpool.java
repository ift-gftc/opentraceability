package opentraceability.utility;

import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class HttpClientPool {
    private static LimitedPool<OkHttpClient> clientPool = new LimitedPool<OkHttpClient>(() -> {
        return new OkHttpClient.Builder().build();
    }, client -> {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }, TimeUnit.MINUTES.toMillis(5));

    public static LimitedPoolItem<OkHttpClient> getClient() {
        return clientPool.get();
    }
}