package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.injection.mixin.interfaces.client.IMixinMinecraft
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventPreRenderTick
import me.miki.shindo.management.event.impl.EventRenderPlayer
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.Mod.isToggled
import me.miki.shindo.management.mods.Mod.setToggled
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.management.settings.impl.ColorSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry
import me.miki.shindo.mobends.AnimatedEntity
import me.miki.shindo.mobends.client.model.entity.ModelBendsPlayer
import me.miki.shindo.mobends.data.Data_Player
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.util.vector.Vector3f
import java.awt.Color
import java.util.*

class MoBendsMod :
    Mod(TranslateText.MO_BENDS, TranslateText.MO_BENDS_DESCRIPTION, ModCategory.PLAYER, LegacyIcon.MOD_MO_BENDS) {
    private val loaded = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CUSTOM_COLOR)
    @JvmField
    var customColorSetting = false

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR)
    @JvmField
    var colorSetting: Color? = null
    var currentlyRenderedEntities: MutableList<UUID?> = ArrayList<UUID?>()

    var isRenderingGuiScreen: Boolean = false

    init {
        instance = this
    }

    @EventTarget
    fun onPreRenderTick(event: EventPreRenderTick?) {
        if (mc.theWorld == null) {
            return
        }

        for (i in Data_Player.dataList.indices) {
            Data_Player.dataList.get(i).update((mc as IMixinMinecraft).getTimer().renderPartialTicks)
        }
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        if (mc.theWorld == null) {
            return
        }

        for (i in Data_Player.dataList.indices) {
            val data = Data_Player.dataList.get(i)
            val entity = mc.theWorld.getEntityByID(data.entityID)

            if (entity != null) {
                if (!data.entityType.equals(entity.getName(), ignoreCase = true)) {
                    Data_Player.dataList.remove(data)
                    Data_Player.add(Data_Player(entity.getEntityId()))
                } else {
                    data.motion_prev.set(data.motion)

                    data.motion.x = entity.posX.toFloat() - data.position.x
                    data.motion.y = entity.posY.toFloat() - data.position.y
                    data.motion.z = entity.posZ.toFloat() - data.position.z

                    data.position = Vector3f(entity.posX.toFloat(), entity.posY.toFloat(), entity.posZ.toFloat())
                }
            } else {
                Data_Player.dataList.remove(data)
            }
        }
    }

    @EventTarget
    fun onRenderPlayer(event: EventRenderPlayer) {
        if (event.getEntity() !is EntityPlayer) {
            return
        }

        if (AnimatedEntity.getByEntity(event.getEntity()) == null) {
            return
        }

        if (Objects.requireNonNull<AnimatedEntity?>(AnimatedEntity.getByEntity(event.getEntity())).animate) {
            val player = event.getEntity() as AbstractClientPlayer

            if (!currentlyRenderedEntities.contains(event.getEntity().getUniqueID())) {
                currentlyRenderedEntities.add(event.getEntity().getUniqueID())
                event.setCancelled(true)

                val renderer = AnimatedEntity.getPlayerRenderer(player)
                val model = renderer.getMainModel() as ModelBendsPlayer

                model.bipedHead.isHidden = false
                model.bipedHeadwear.isHidden = false

                val entityYaw =
                    event.getEntity().prevRotationYaw + (event.getEntity().rotationYaw - event.getEntity().prevRotationYaw) * event.getPartialTicks()
                AnimatedEntity.getPlayerRenderer(player)
                    .doRender(player, event.getX(), event.getY(), event.getZ(), entityYaw, event.getPartialTicks())
                currentlyRenderedEntities.remove(event.getEntity().getUniqueID())
            }
        }
    }

    public override fun onEnable() {
        super.onEnable()

        if (Skin3DMod.Companion.getInstance().isToggled()) {
            Skin3DMod.Companion.getInstance().setToggled(false)
        }


        if (!loaded) {
            AnimatedEntity.register()
        }
    }

    companion object {
        @JvmField
        var instance: MoBendsMod? = null
    }

    fun getCustomColorSetting(): BooleanSetting? =
        SettingRegistry.getBooleanSetting(this, "customColorSetting")

    fun getColorSetting(): ColorSetting? = SettingRegistry.getColorSetting(this, "colorSetting")
}




