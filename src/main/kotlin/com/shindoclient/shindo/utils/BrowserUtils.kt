package com.shindoclient.shindo.utils

import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

object BrowserUtils {
    @JvmStatic
    fun tryOpenBrowser(uri: String): Boolean {
        val target = URI(uri)

        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()

            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(target)
                return true
            }
        }

        if (OSUtils.windows) {
            OSUtils.runWindowsBrowser(uri)
            return true
        }

        if (OSUtils.mac) {
            OSUtils.runMacBrowser(uri)
            return true
        }

        if (OSUtils.linux) {
            return OSUtils.runLinuxBrowser(uri)
        }

        return false
    }
}
