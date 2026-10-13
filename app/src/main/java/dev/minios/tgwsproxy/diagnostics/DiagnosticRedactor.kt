package dev.minios.tgwsproxy.diagnostics

/**
 * Заглушка DiagnosticRedactor — минимальная (логи без чувствительных данных).
 * Полная реализация в оригинале (tg-ws-proxy, MIT © crim50n).
 */
object DiagnosticRedactor {
    fun redact(value: String, maxLength: Int = 500): String = value.take(maxLength)
    fun redactPersisted(value: String): String = value
    fun field(key: String, value: Any?): String = "$key=$value"
}
