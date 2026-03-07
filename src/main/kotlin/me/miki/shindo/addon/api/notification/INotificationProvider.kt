package me.miki.shindo.addon.api.notification

/**
 * Envio de notificações/toasts para o usuário.
 */
interface INotificationProvider {

    /**
     * Exibe uma notificação.
     * @param title Título
     * @param message Mensagem
     * @param type Tipo visual (INFO, WARNING, ERROR, SUCCESS, MUSIC)
     */
    fun post(title: String, message: String, type: AddonNotificationType = AddonNotificationType.INFO)
}

enum class AddonNotificationType {
    INFO, WARNING, ERROR, SUCCESS, MUSIC
}
