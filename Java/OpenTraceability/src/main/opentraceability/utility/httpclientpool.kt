package utility

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

object HttpClientPool {
    private val clientPool = LimitedPool<OkHttpClient>({
        OkHttpClient.Builder().build()
    }, { client ->
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }, TimeUnit.MINUTES.toMillis(5))

    fun getClient(): LimitedPoolItem<OkHttpClient> {
        return clientPool.get()
    }
}
