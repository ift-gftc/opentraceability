package utility
import java.net.http.HttpClient
class HttpClientPool {
    companion object{
    }

    fun GetClient(): Utility.LimitedPoolItem<HttpClient> {
        // Method body goes here
        return Utility.LimitedPoolItem<HttpClient>()
    }
}
