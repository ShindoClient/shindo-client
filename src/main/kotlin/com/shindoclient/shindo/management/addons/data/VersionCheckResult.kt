package com.shindoclient.shindo.management.addons.data

sealed class VersionCheckResult {
    data object Compatible : VersionCheckResult()

    data class Incompatible(
        val reason: String,
    ) : VersionCheckResult()
}
