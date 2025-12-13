package me.miki.shindo.utils

import javax.swing.JOptionPane

object DialogUtils {

    @JvmStatic
    fun info(title: String, message: String) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE)
    }

    @JvmStatic
    fun warn(title: String, message: String) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE)
    }

    @JvmStatic
    fun error(title: String, message: String) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)
    }

    @JvmStatic
    fun error(e: Exception) {
        error("Error occurred", e.toString())
    }
}
