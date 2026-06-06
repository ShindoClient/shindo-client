package com.shindoclient.shindo.management.event

/**
 * Marca método como listener de evento para o EventManager interno do client.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventTarget(
    val value: Byte = 2,
)
