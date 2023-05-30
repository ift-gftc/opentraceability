
enum class LogLevel(val value: Int) {
    Info(0),
    Warning(1),
    Error(2),
    Debug(3),
}


typealias OnLogDelegate = (log: OTLog) -> Unit


object OTLogger {
    private val listeners: MutableList<OnLogDelegate> = mutableListOf()

    fun addListener(listener: OnLogDelegate) {
        listeners.add(listener)
    }

    fun removeListener(listener: OnLogDelegate) {
        listeners.remove(listener)
    }

    fun error(ex: Exception) {
        val log = OTLog().apply {
            Level = LogLevel.Error
            Exception = ex
        }
        notifyListeners(log)
    }

    private fun notifyListeners(log: OTLog) {
        listeners.forEach { listener ->
            listener(log)
        }
    }
}
