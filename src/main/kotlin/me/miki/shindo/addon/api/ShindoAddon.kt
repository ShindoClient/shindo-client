package me.miki.shindo.addon.api

/**
 * Interface principal que todo addon externo do Shindo Client deve implementar.
 * Addons são carregados dinamicamente a partir de JARs na pasta shindo/addons/.
 *
 * Use [ShindoAddonContext] em [onLoad] para acessar eventos, render (NanoVG, Fonts),
 * animações e componentes do client. Os addons devem usar o sistema de eventos do client
 * diretamente (EventManager com @EventTarget), não um sistema próprio.
 */
interface ShindoAddon {

    /**
     * Retorna os metadados do addon (id, versão, nome, descrição, ícone, tipo).
     */
    fun getMetadata(): AddonMetadata

    /**
     * Chamado quando o addon é carregado pela primeira vez.
     * Recebe [context] com acesso ao EventManager, NanoVGManager, Fonts, ColorManager, etc.
     */
    fun onLoad(context: ShindoAddonContext) {}

    /**
     * Chamado quando o addon é habilitado pelo usuário.
     */
    fun onEnable()

    /**
     * Chamado quando o addon é desabilitado pelo usuário.
     */
    fun onDisable()
}
