package me.miki.shindo.utils.concurrent

/**
 * Resultado de uma tarefa assíncrona.
 * Pode conter um valor de sucesso ou uma exceção.
 */
sealed class TaskResult<T> {
    /**
     * Sucesso com valor.
     */
    data class Success<T>(val value: T) : TaskResult<T>()
    
    /**
     * Falha com exceção.
     */
    data class Failure(val exception: Throwable) : TaskResult<Nothing>()
    
    /**
     * Verifica se o resultado é sucesso.
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Verifica se o resultado é falha.
     */
    fun isFailure(): Boolean = this is Failure
    
    /**
     * Obtém o valor se for sucesso, ou null se for falha.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }
    
    /**
     * Obtém o valor se for sucesso, ou lança a exceção se for falha.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw exception
    }
    
    /**
     * Obtém o valor se for sucesso, ou retorna o valor padrão se for falha.
     */
    fun getOrDefault(default: T): T = when (this) {
        is Success -> value
        is Failure -> default
    }
    
    /**
     * Executa uma ação se for sucesso.
     */
    fun onSuccess(action: (T) -> Unit): TaskResult<T> {
        if (this is Success) {
            action(value)
        }
        return this
    }
    
    /**
     * Executa uma ação se for falha.
     */
    fun onFailure(action: (Throwable) -> Unit): TaskResult<T> {
        if (this is Failure) {
            action(exception)
        }
        return this
    }
}
