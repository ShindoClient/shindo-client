package com.shindoclient.shindo.management.settings.config

interface ConfigOwner {
    fun getConfigId(): String

    fun getDisplayName(): String = getConfigId()
}
