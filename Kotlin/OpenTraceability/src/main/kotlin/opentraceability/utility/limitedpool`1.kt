package opentraceability.utility

import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.TimeUnit

class LimitedPool<T>(private val valueFactory: () -> T, private val valueDisposeAction: (T) -> Unit, private val valueLifetime: Long = TimeUnit.HOURS.toMillis(1)) {
    private val pool = ConcurrentLinkedDeque<LimitedPoolItem<T>>()
    @Volatile private var disposed = false

    val idleCount: Int
        get() = pool.size

    fun get(): LimitedPoolItem<T> {
        var item: LimitedPoolItem<T>? = null
        while (!disposed) {
            item = pool.pollLast()
            if (item == null || item.isExpired) {
                item?.dispose()
            } else {
                return item
            }
        }
        return LimitedPoolItem(valueFactory(), { disposedItem ->
            if (disposedItem.isExpired) {
                valueDisposeAction(disposedItem.value)
            } else {
                if (!disposed) {
                    pool.addLast(disposedItem)
                }
            }
        }, valueLifetime)
    }

    fun dispose() {
        disposed = true
        val items = pool.toTypedArray()
        for (item in items) {
            valueDisposeAction(item.value)
        }
    }
}
