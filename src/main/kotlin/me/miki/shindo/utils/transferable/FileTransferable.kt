package me.miki.shindo.utils.transferable

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.File

class FileTransferable(private val file: File) : Transferable {

    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.javaFileListFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = DataFlavor.javaFileListFlavor == flavor

    override fun getTransferData(flavor: DataFlavor): Any {
        val files = ArrayList<File>()
        files.add(file)
        return files
    }
}
