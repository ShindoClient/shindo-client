package me.miki.shindo.management.addons.rpo.packs;

import me.miki.shindo.gui.GuiBetterResourcePacks;
import net.minecraft.client.resources.ResourcePackListEntryFound;

public abstract class ResourcePackListEntryCustom extends ResourcePackListEntryFound {
    public ResourcePackListEntryCustom(GuiBetterResourcePacks ownerScreen) {
        super(ownerScreen, null);
    }

    @Override
    public abstract void func_148313_c();

    @Override
    public abstract String func_148311_a();

    @Override
    public abstract String func_148312_b();

    @Override
    public boolean func_148310_d() {
        return super.func_148310_d();
    }

    @Override
    public boolean func_148307_h() {
        return super.func_148307_h();
    }

    @Override
    public boolean func_148308_f() {
        return super.func_148308_f();
    }

    @Override
    public boolean func_148309_e() {
        return super.func_148309_e();
    }

    @Override
    public boolean func_148314_g() {
        return super.func_148314_g();
    }
}