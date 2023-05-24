package utility

import java.net.http.HttpClient

class HttpClientPool {
    companion object {

        internal lateinit var _clientPool: LimitedPoolItem<HttpClient>

        fun GetClient(): LimitedPoolItem<HttpClient> {
            TODO("Not yet implemented")
        }

    }
}
