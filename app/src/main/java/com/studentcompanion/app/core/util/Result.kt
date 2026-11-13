package com.studentcompanion.app.core.util

import kotlinx.coroutines.CancellationException

/**
 * Generic wrapper for domain-layer outcomes so ViewModels never depend on
 * data-layer exception types (Retrofit, OkHttp, etc.) directly.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>
    data object Loading : Result<Nothing>
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (String) -> Unit): Result<T> {
    if (this is Result.Error) action(message)
    return this
}

/** Runs [block], wrapping a thrown exception's message as [Result.Error] with [fallbackMessage] if it has none. */
suspend inline fun <T> safeCall(fallbackMessage: String, block: suspend () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: CancellationException) {
    throw e // structured concurrency: a cancelled coroutine must keep propagating, never become a Result.Error
} catch (e: Exception) {
    Result.Error(e.message ?: fallbackMessage, e)
}
