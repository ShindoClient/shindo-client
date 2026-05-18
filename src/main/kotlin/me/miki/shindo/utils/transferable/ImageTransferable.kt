package me.miki.shindo.utils.transferable

import java.awt.Image
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

class ImageTransferable(
    private val image: Image,
) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = DataFlavor.imageFlavor == flavor

    @Throws(UnsupportedFlavorException::class)
    override fun getTransferData(flavor: DataFlavor): Any {
        if (DataFlavor.imageFlavor != flavor) {
            throw UnsupportedFlavorException(flavor)
        }
        return image
    }
}
