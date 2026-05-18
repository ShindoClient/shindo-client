package me.miki.shindo.api.websocket.message

enum class MessageType(
    val wireType: String,
) {
    AUTH("auth"),
    AUTH_OK("auth.ok"),
    AUTH_ERROR("auth.error"),

    ROLES_UPDATE("roles.update"),

    USER_JOIN("user.join"),
    USER_LEAVE("user.leave"),
    USER_ROLES("user.roles"),

    PING("ping"),
    PONG("pong"),
    SERVER_KEEPALIVE("server.keepalive"),
    SERVER_VERIFY("server.verify"),

    WARP_STATUS("warp.status"),

    CHAT_FRIEND_LIST("chat.friend.list"),
    CHAT_FRIEND_REQUESTS("chat.friend.requests"),
    CHAT_FRIEND_REQUEST("chat.friend.request"),
    CHAT_FRIEND_REQUEST_OK("chat.friend.request.ok"),
    CHAT_FRIEND_REQUEST_ERROR("chat.friend.request.error"),
    CHAT_FRIEND_ACCEPT("chat.friend.accept"),
    CHAT_FRIEND_ACCEPT_OK("chat.friend.accept.ok"),
    CHAT_FRIEND_ACCEPT_ERROR("chat.friend.accept.error"),
    CHAT_FRIEND_REMOVE("chat.friend.remove"),
    CHAT_FRIEND_REMOVE_OK("chat.friend.remove.ok"),
    CHAT_FRIEND_REMOVE_ERROR("chat.friend.remove.error"),
    CHAT_MESSAGE_SEND("chat.message.send"),
    CHAT_MESSAGE_SEND_OK("chat.message.send.ok"),
    CHAT_MESSAGE("chat.message"),
    CHAT_MESSAGE_ERROR("chat.message.error"),
    CHAT_TOKEN("chat.token"),
    CHAT_TOKEN_OK("chat.token.ok"),

    BROADCAST_TOKEN("broadcast.token"),
    BROADCAST_TOKEN_OK("broadcast.token.ok"),
    BROADCAST("broadcast"),

    PROFILE_SHARE("profile.share"),
    PROFILE_SHARE_OK("profile.share.ok"),
    PROFILE_SHARE_ERROR("profile.share.error"),
    PROFILE_FETCH("profile.fetch"),
    PROFILE_FETCH_OK("profile.fetch.ok"),
    PROFILE_FETCH_ERROR("profile.fetch.error"),
    PROFILE_UNSHARE("profile.unshare"),
    PROFILE_UNSHARE_OK("profile.unshare.ok"),
    PROFILE_UNSHARE_ERROR("profile.unshare.error"),

    UNKNOWN("unknown"),
    ;

    companion object {
        @JvmStatic
        fun fromWire(rawType: String?): MessageType {
            if (rawType.isNullOrEmpty()) return UNKNOWN
            val normalized = rawType.trim()
            for (value in values()) {
                if (value.wireType.equals(normalized, ignoreCase = true)) return value
            }
            return UNKNOWN
        }
    }
}
