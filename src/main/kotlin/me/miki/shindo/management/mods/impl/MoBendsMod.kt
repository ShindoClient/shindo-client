package me.miki.shindo.management.mods.impl

import me.miki.shindo.injection.interfaces.IMixinMinecraft
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventPreRenderTick
import me.miki.shindo.management.event.impl.EventRenderPlayer
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
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

class MoBendsMod : Mod(TranslateText.MO_BENDS, TranslateText.MO_BENDS_DESCRIPTION, ModCategory.PLAYER, Shinconic.MOD_MO_BENDS) {
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
            Data_Player.dataList[i].update(
                ((mc as IMixinMinecraft).timer as net.minecraft.util.Timer).renderPartialTicks,
            )
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
        if (event.getEntity() !is EntityPlayer) {
            return
        }

        val animated = AnimatedEntity.getByEntity(event.getEntity()) ?: return
        if (animated.animate) {
            val player = event.getEntity() as AbstractClientPlayer

            if (!currentlyRenderedEntities.contains(event.getEntity().uniqueID)) {
                currentlyRenderedEntities.add(event.getEntity().uniqueID)
                event.setCancelled(true)

                val renderer = AnimatedEntity.getPlayerRenderer(player)
                val model = renderer.mainModel as ModelBendsPlayer

                model.bipedHead.isHidden = false
                model.bipedHeadwear.isHidden = false

                val entityYaw =
                    event.getEntity().prevRotationYaw +
                        (event.getEntity().rotationYaw - event.getEntity().prevRotationYaw) * event.getPartialTicks()
                AnimatedEntity
                    .getPlayerRenderer(player)
                    .doRender(player, event.getX(), event.getY(), event.getZ(), entityYaw, event.getPartialTicks())
                currentlyRenderedEntities.remove(event.getEntity().uniqueID)
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

    fun getCustomColorSetting(): BooleanSetting? = SettingRegistry.getBooleanSetting(this, "customColorSetting")

    fun getColorSetting(): ColorSetting? = SettingRegistry.getColorSetting(this, "colorSetting")
}
