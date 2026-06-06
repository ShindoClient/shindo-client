package com.shindoclient.shindo.management.profile

import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation
import java.io.File

data class Profile(
    val id: Int,
    var serverIp: String?,
    val jsonFile: File?,
    val icon: ProfileIcon?,
    var customIcon: File?,
    var type: ProfileType = ProfileType.ALL,
    var shareCode: String? = null,
) {
    val name: String = jsonFile?.nameWithoutExtension ?: ""
}
