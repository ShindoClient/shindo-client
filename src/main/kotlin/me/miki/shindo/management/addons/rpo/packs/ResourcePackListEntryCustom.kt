package me.miki.shindo.management.addons.rpo.packs

import me.miki.shindo.gui.GuiBetterResourcePacks
import net.minecraft.client.resources.ResourcePackListEntryFound

abstract class ResourcePackListEntryCustom(ownerScreen: GuiBetterResourcePacks) :
    ResourcePackListEntryFound(ownerScreen, null) {

    abstract override fun func_148313_c()

    public abstract override fun func_148311_a(): String

    public abstract override fun func_148312_b(): String

    override fun func_148310_d(): Boolean {
        return super.func_148310_d()
    }

    override fun func_148307_h(): Boolean {
        return super.func_148307_h()
    }

    override fun func_148308_f(): Boolean {
        return super.func_148308_f()
    }

    override fun func_148309_e(): Boolean {
        return super.func_148309_e()
    }

    override fun func_148314_g(): Boolean {
        return super.func_148314_g()
    }
}

