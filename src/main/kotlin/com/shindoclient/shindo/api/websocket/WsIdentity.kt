package com.shindoclient.shindo.api.websocket

data class WsIdentity(
    val uuid: String,
    val name: String,
    val roles: Array<String>?,
    val accountType: AccountType,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WsIdentity) return false
        if (uuid != other.uuid) return false
        if (name != other.name) return false
        if (accountType != other.accountType) return false
        if (roles != null) {
            if (other.roles == null) return false
            if (!roles.contentEquals(other.roles)) return false
        } else if (other.roles != null) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (roles?.contentHashCode() ?: 0)
        result = 31 * result + accountType.hashCode()
        return result
    }
}
