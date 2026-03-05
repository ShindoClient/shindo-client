package me.miki.shindo.api.compat

import me.miki.client_api.clipboard.IClipboardProvider
import me.miki.shindo.utils.IOUtils

class ClipboardProviderAdapter : IClipboardProvider {

    override fun setText(text: String) {
        IOUtils.copyStringToClipboard(text)
    }

    override fun getText(): String? =
        IOUtils.getStringFromClipboard()
}
