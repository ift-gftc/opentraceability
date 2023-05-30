package utility

import java.util.concurrent.ConcurrentStack
import kotlin.concurrent.getOrSet

class LimitedPool<T>(
    private val valueFactory: () -> T,
    private val valueDisposeAction: (T) -> Unit,
    valueLifetime: Long = 60 * 60 * 1000 // Default value lifetime of 1 hour
) : AutoCloseable {
    private val pool = ConcurrentStack<LimitedPoolItem<T>>()
    private var disposed = false

    val idleCount: Int
        get() = pool.size

    init {
        require(valueLifetime > 0) { "Value lifetime must be positive." }
    }

    fun get(): LimitedPoolItem<T> {
        var item: LimitedPoolItem<T>?
        while (!disposed && pool.tryPop().also { item = it } != null) {
            if (!item!!.expired) {
                return item!!
            }
            item!!.dispose()
        }
        return LimitedPoolItem(valueFactory(), { disposedItem ->
            if (disposedItem.expired) {
                valueDisposeAction(disposedItem.value)
            } else if (!disposed) {
                pool.push(disposedItem)
            }
        }, valueLifetime)
    }

    override fun close() {
        dispose()
    }

    fun dispose() {
        if (disposed) {
            return
        }
        disposed = true
        val items = pool.toTypedArray()
        items.forEach { valueDisposeAction(it.value) }
        pool.clear()
    }
}
