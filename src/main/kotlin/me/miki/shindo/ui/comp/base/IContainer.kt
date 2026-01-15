package me.miki.shindo.ui.comp.base

/**
 * Interface para componentes que podem conter outros componentes (children).
 */
interface IContainer {
    /**
     * Adiciona um componente filho.
     * @param component Componente a ser adicionado
     */
    fun addChild(component: IComponent?)

    /**
     * Remove um componente filho.
     * @param component Componente a ser removido
     */
    fun removeChild(component: IComponent)

    /**
     * Remove todos os componentes filhos.
     */
    fun clearChildren()

    /**
     * Retorna uma lista imutável de todos os componentes filhos.
     */
    fun getChildren(): List<IComponent>

    /**
     * Verifica se o componente tem filhos.
     */
    fun hasChildren(): Boolean
}
