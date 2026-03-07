package me.miki.shindo.addon.api.event

/**
 * Marca método como listener de evento. O EventManager do client invoca métodos anotados
 * quando o evento correspondente (tipo do parâmetro) é disparado.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventTarget(val value: Byte = 2)
