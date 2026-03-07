package me.miki.shindo.addon.api.comp

/**
 * Interface base para componentes de UI.
 * O client fornece implementações. Addons podem criar e usar comps via ICompFactory.
 */
interface IComp {

    var x: Float
    var y: Float
    var width: Float
    var height: Float
    var visible: Boolean

    fun draw(mouseX: Int, mouseY: Int, partialTicks: Float)

    fun update(partialTicks: Float)

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int)

    fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int)

    fun addChild(child: IComp)
}
