package me.miki.shindo.management.mods.impl

import me.miki.shindo.injection.mixin.interfaces.client.IMixinMinecraft
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventPreRenderTick
import me.miki.shindo.management.event.impl.EventRenderPlayer
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
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
            Data_Player.dataList[i].update((mc as IMixinMinecraft).getTimer().renderPartialTicks)
        }
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        if (mc.theWorld == null) {
            return
        }

        for (i in Data_Player.dataList.indices) {
            val data = Data_Player.dataList[i]
            val entity = mc.theWorld.getEntityByID(data.entityID)

            if (entity != null) {
                if (!data.entityType.equals(entity.name, ignoreCase = true)) {
                    Data_Player.dataList.remove(data)
                    Data_Player.add(Data_Player(entity.entityId))
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
        if (event.entity !is EntityPlayer) {
            return
        }

        val animated = AnimatedEntity.getByEntity(event.entity) ?: return
        if (animated.animate) {
            val player = event.entity as AbstractClientPlayer

            if (!currentlyRenderedEntities.contains(event.entity.uniqueID)) {
                currentlyRenderedEntities.add(event.entity.uniqueID)
                event.setCancelled(true)

                val renderer = AnimatedEntity.getPlayerRenderer(player)
                val model = renderer.mainModel as ModelBendsPlayer

                model.bipedHead.isHidden = false
                model.bipedHeadwear.isHidden = false

                val entityYaw =
                    event.entity.prevRotationYaw + (event.entity.rotationYaw - event.entity.prevRotationYaw) * event.partialTicks
                AnimatedEntity.getPlayerRenderer(player)
                    .doRender(player, event.x, event.y, event.z, entityYaw, event.partialTicks)
                currentlyRenderedEntities.remove(event.entity.uniqueID)
            }
        }
    }

    override fun onEnable() {
        super.onEnable()

        val skin3D = Skin3DMod.instance
        if (skin3D != null && skin3D.isToggled()) {
            skin3D.setToggled(false)
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




