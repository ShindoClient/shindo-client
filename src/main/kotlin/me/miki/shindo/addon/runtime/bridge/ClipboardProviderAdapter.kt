package me.miki.shindo.addon.runtime.bridge

import me.miki.shindo.addon.api.clipboard.IClipboardProvider
import me.miki.shindo.utils.IOUtils

class ClipboardProviderAdapter : IClipboardProvider {

    override fun setText(text: String) {
        IOUtils.copyStringToClipboard(text)
    }

    override fun getText(): String? =
        IOUtils.getStringFromClipboard()
}
