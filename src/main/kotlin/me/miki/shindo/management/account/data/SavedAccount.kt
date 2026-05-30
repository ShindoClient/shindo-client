package me.miki.shindo.management.account.data

import me.miki.shindo.api.websocket.AccountType

data class SavedAccount(
    val id: String,
    val type: AccountType,
    val username: String,
    val uuid: String,
    val accessToken: String,
    val tokenExpiresAt: Long,
    val sessionJson: String,
    val active: Boolean,
)
