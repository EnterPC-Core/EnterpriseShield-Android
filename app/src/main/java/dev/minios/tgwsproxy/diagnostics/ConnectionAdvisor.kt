package dev.minios.tgwsproxy.diagnostics

/**
 * Заглушка ConnectionAdvisor — без UI и BuildConfig.
 */
object ConnectionAdvisor {
    fun advice(throwable: Throwable?): String =
        throwable?.message?.take(200) ?: "нет деталей"
}
