package utility

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

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
