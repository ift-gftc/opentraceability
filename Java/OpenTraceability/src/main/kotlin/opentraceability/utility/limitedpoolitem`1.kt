package opentraceability.utility

import java.util.concurrent.TimeUnit

class LimitedPoolItem<T>(
    val value: T,
    private val disposeAction: (LimitedPoolItem<T>) -> Unit,
    private val lifetime: Long = TimeUnit.HOURS.toMillis(1)
) {
    private val creationTime = System.currentTimeMillis()

    val isExpired: Boolean
        get() = System.currentTimeMillis() - creationTime > lifetime

    fun dispose() {
        disposeAction(this)
    }
}
