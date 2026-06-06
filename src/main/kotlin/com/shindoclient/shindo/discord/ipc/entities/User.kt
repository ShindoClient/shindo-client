package com.shindoclient.shindo.discord.ipc.entities

class User(
    val name: String,
    val discriminator: String,
    val id: Long,
    val avatar: String?,
) {
    val idString: String
        get() = id.toString()

    val avatarUrl: String?
        get() =
            avatar?.let {
                "https://cdn.discordapp.com/avatars/$idString/$it${if (it.startsWith("a_")) ".gif" else ".png"}"
            }

    val defaultAvatarId: String
        get() = DefaultAvatar.values()[discriminator.toInt() % DefaultAvatar.values().size].toString()

    val defaultAvatarUrl: String
        get() = "https://discordapp.com/assets/$defaultAvatarId.png"

    val effectiveAvatarUrl: String
        get() = avatarUrl ?: defaultAvatarUrl

    val asMention: String
        get() = "<@$id>"

    fun isBot(): Boolean = false

    override fun equals(other: Any?): Boolean {
        if (other !is User) return false
        return this === other || id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "U:$name($id)"

    enum class DefaultAvatar(
        private val text: String,
    ) {
        BLURPLE("6debd47ed13483642cf09e832ed0bc1b"),
        GREY("322c936a8c8be1b803cd94861bdfa868"),
        GREEN("dd4dbc0016779df1378e7812eabaa04d"),
        ORANGE("0e291f67c9274a1abdddeb3fd919cbaa"),
        RED("1cbd08c76f8af6dddce02c5138971129"),
        ;

        override fun toString(): String = text
    }
}
