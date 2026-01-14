package me.miki.shindo.management.profile

import me.miki.shindo.utils.animation.simple.SimpleAnimation
import java.io.File

data class Profile(
    val id: Int,
    var serverIp: String?,
    val jsonFile: File?,
    val icon: ProfileIcon?,
    var customIcon: File?,
    var type: ProfileType = ProfileType.ALL,
    var shareCode: String? = null
) {
    val starAnimation: SimpleAnimation = SimpleAnimation()
    val name: String = jsonFile?.nameWithoutExtension ?: ""
}
