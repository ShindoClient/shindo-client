package me.miki.shindo.ui.comp.base

interface IContainer {
    fun addChild(component: IComponent?)
    fun removeChild(component: IComponent)
    fun clearChildren()
    fun getChildren(): List<IComponent>
    fun hasChildren(): Boolean
}
