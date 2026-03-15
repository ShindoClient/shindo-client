package me.miki.shindo.utils.concurrent

sealed class TaskResult<T> {
    data class Success<T>(val value: T) : TaskResult<T>()
    data class Failure(val exception: Throwable) : TaskResult<Nothing>()

    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw exception
    }

    fun getOrDefault(default: T): T = when (this) {
        is Success -> value
        is Failure -> default
    }

    fun onSuccess(action: (T) -> Unit): TaskResult<T> {
        if (this is Success) {
            action(value)
        }
        return this
    }

    fun onFailure(action: (Throwable) -> Unit): TaskResult<T> {
        if (this is Failure) {
            action(exception)
        }
        return this
    }
}
