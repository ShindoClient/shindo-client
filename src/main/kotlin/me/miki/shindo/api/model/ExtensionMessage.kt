package me.miki.extensions.serialization.kotlinx

import kotlinx.serialization.Serializable

/**
 * TODO: Replace this placeholder DTO set with real transport models.
 */
@Serializable
data class ExtensionMessage(
    val type: String = "",
    val payload: String = ""
)
