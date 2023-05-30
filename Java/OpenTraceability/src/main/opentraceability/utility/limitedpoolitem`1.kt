package utility

import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

class LimitedPoolItem<T>(
    private val value: T,
    private val disposeAction: (LimitedPoolItem<T>) -> Unit,
    private val lifetime: Long
) : AutoCloseable {
    private var expired = false
    private val stopwatch = Stopwatch()

    init {
        stopwatch.start()
    }

    internal val isExpired: Boolean
        get() {
            if (expired) {
                return true
            }
            expired = stopwatch.elapsed(TimeUnit.NANOSECONDS) > lifetime
            return expired
        }

    val getValue: T
        get() = value

    override fun close() {
        dispose()
    }

    fun dispose() {
        dispose(true)
        //GC.suppressFinalize(this)
    }

    fun dispose(disposing: Boolean) {
        if (disposing) {
            disposeAction(this)
        }
    }
}

private class Stopwatch {
    private var startTime: Long = 0

    fun start() {
        startTime = System.nanoTime()
    }

    fun elapsed(unit: TimeUnit): Long {
        val endTime = System.nanoTime()
        val elapsedTime = endTime - startTime
        return unit.convert(elapsedTime, TimeUnit.NANOSECONDS)
    }
}
