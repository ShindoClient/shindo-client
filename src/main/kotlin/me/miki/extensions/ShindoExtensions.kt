@file:JvmName("ShindoExtensions")

package me.miki.extensions

import me.miki.extensions.manager.ExtensionManager
import me.miki.shindo.Shindo
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.event.EventManager
import me.miki.shindo.management.file.FileManager
import me.miki.shindo.management.language.LanguageManager
import me.miki.shindo.management.mods.ModManager
import me.miki.shindo.management.music.MusicManager
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.network.NetworkManager
import me.miki.shindo.management.notification.NotificationManager
import me.miki.shindo.management.profile.ProfileManager
import me.miki.shindo.management.remote.download.DownloadManager


fun Shindo.getModManager(): ModManager {
    return Shindo.getInstance().modManager
}

fun Shindo.getProfileManager(): ProfileManager {
    return Shindo.getInstance().profileManager
}

fun Shindo.getColorManager(): ColorManager {
    return Shindo.getInstance().colorManager
}

fun Shindo.getDownloadManager(): DownloadManager {
    return Shindo.getInstance().downloadManager
}

fun Shindo.getEventManager(): EventManager {
    return Shindo.getInstance().eventManager
}

fun Shindo.getMusicManager(): MusicManager {
    return Shindo.getInstance().musicManager
}

fun Shindo.getNotificationManager(): NotificationManager {
    return Shindo.getInstance().notificationManager
}

fun Shindo.getNetworkManager(): NetworkManager {
    return Shindo.getInstance().networkManager
}

fun Shindo.getFileManager(): FileManager {
    return Shindo.getInstance().fileManager
}

fun Shindo.getNanoVGManager(): NanoVGManager? {
    return Shindo.getInstance().nanoVGManager
}

fun Shindo.getLanguageManager(): LanguageManager {
    return Shindo.getInstance().languageManager
}

fun Shindo.getExtensionManager(): ExtensionManager {
    return Shindo.getInstance().extensionManager
}
