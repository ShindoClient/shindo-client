package me.miki.shindo.utils

import me.miki.shindo.Shindo
import me.miki.shindo.management.notification.NotificationType
import javax.swing.JOptionPane

object DialogUtils {

    @JvmStatic
    fun info(title: String, message: String) {
        showFeedback(title, message, NotificationType.INFO, JOptionPane.INFORMATION_MESSAGE)
    }

    @JvmStatic
    fun warn(title: String, message: String) {
        showFeedback(title, message, NotificationType.WARNING, JOptionPane.WARNING_MESSAGE)
    }

    @JvmStatic
    fun error(title: String, message: String) {
        showFeedback(title, message, NotificationType.ERROR, JOptionPane.ERROR_MESSAGE)
    }

    @JvmStatic
    fun error(e: Exception) {
        val message = e.message ?: e.toString()
        error("Error occurred", message)
    }

    @JvmStatic
    fun confirm(title: String, message: String): Boolean {
        val result = JOptionPane.showConfirmDialog(
            null,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        )
        return result == JOptionPane.YES_OPTION
    }

    @JvmStatic
    fun confirmDanger(
        title: String,
        message: String,
        confirmLabel: String = "Delete",
        cancelLabel: String = "Cancel"
    ): Boolean {
        val options = arrayOf(confirmLabel, cancelLabel)
        val result = JOptionPane.showOptionDialog(
            null,
            message,
            title,
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            cancelLabel
        )
        return result == 0
    }

    @JvmStatic
    fun confirmRetry(
        title: String,
        message: String,
        retryLabel: String = "Retry",
        cancelLabel: String = "Cancel"
    ): Boolean {
        val options = arrayOf(retryLabel, cancelLabel)
        val result = JOptionPane.showOptionDialog(
            null,
            message,
            title,
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            retryLabel
        )
        return result == 0
    }

    private fun showFeedback(title: String, message: String, type: NotificationType, swingType: Int) {
        if (tryPostNotification(title, message, type)) {
            return
        }
        JOptionPane.showMessageDialog(null, message, title, swingType)
    }

    private fun tryPostNotification(title: String, message: String, type: NotificationType): Boolean {
        return try {
            val instance = Shindo.getInstance()
            if (!instance.hasStarted()) {
                false
            } else {
                instance.getNotificationManager().post(title, message, type)
                true
            }
        } catch (_: Throwable) {
            false
        }
    }
}
