package me.miki.shindo.management.cosmetic.cape

import me.miki.shindo.Shindo
import me.miki.shindo.api.roles.Role
import me.miki.shindo.api.roles.RoleManager
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.cosmetic.CosmeticRoleTextMapper
import me.miki.shindo.management.cosmetic.cape.impl.Cape
import me.miki.shindo.management.cosmetic.cape.impl.CustomCape
import me.miki.shindo.management.cosmetic.cape.impl.NormalCape
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.utils.ImageUtils
import me.miki.shindo.utils.file.FileUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class CapeManager {

    private val capes = ArrayList<Cape>()
    private var currentCape: Cape? = null

    init {
        val instance = Shindo.getInstance()
        val fileManager = instance.fileManager
        val customCapeDir = fileManager.customCapeDir
        val cacheDir = fileManager.capeCacheDir

        capes.add(NormalCape("None", null, null, CapeCategory.ALL, Role.MEMBER))

        add("Minecon 2011", "minecon/2011-sample.png", "minecon/2011.png", CapeCategory.MINECON, Role.MEMBER)
        add("Minecon 2012", "minecon/2012-sample.png", "minecon/2012.png", CapeCategory.MINECON, Role.MEMBER)
        add("Minecon 2013", "minecon/2013-sample.png", "minecon/2013.png", CapeCategory.MINECON, Role.MEMBER)
        add("Minecon 2015", "minecon/2015-sample.png", "minecon/2015.png", CapeCategory.MINECON, Role.MEMBER)
        add("Minecon 2016", "minecon/2016-sample.png", "minecon/2016.png", CapeCategory.MINECON, Role.MEMBER)

        add("Canada", "flag/canada-sample.png", "flag/canada.png", CapeCategory.FLAG, Role.GOLD)
        add("commonwealth", "flag/commonwealth-sample.png", "flag/commonwealth.png", CapeCategory.FLAG, Role.GOLD)
        add("England", "flag/england-sample.png", "flag/england.png", CapeCategory.FLAG, Role.GOLD)
        add("Europe", "flag/europe-sample.png", "flag/europe.png", CapeCategory.FLAG, Role.GOLD)
        add("France", "flag/france-sample.png", "flag/france.png", CapeCategory.FLAG, Role.GOLD)
        add("Germany", "flag/germany-sample.png", "flag/germany.png", CapeCategory.FLAG, Role.GOLD)
        add("India", "flag/india-sample.png", "flag/india.png", CapeCategory.FLAG, Role.GOLD)
        add("Indonesia", "flag/indonesia-sample.png", "flag/indonesia.png", CapeCategory.FLAG, Role.GOLD)
        add("Italy", "flag/italy-sample.png", "flag/italy.png", CapeCategory.FLAG, Role.GOLD)
        add("Japan", "flag/japan-sample.png", "flag/japan.png", CapeCategory.FLAG, Role.GOLD)
        add("Korea", "flag/korean-sample.png", "flag/korean.png", CapeCategory.FLAG, Role.GOLD)
        add("LGBT", "flag/lgbt-sample.png", "flag/lgbt.png", CapeCategory.FLAG, Role.GOLD)
        add("NATO", "flag/nato-sample.png", "flag/nato.png", CapeCategory.FLAG, Role.GOLD)
        add("Scotland", "flag/scotland-sample.png", "flag/scotland.png", CapeCategory.FLAG, Role.GOLD)
        add("Trans", "flag/trans-sample.png", "flag/trans.png", CapeCategory.FLAG, Role.GOLD)
        add("Ukraine", "flag/ukraine-sample.png", "flag/ukraine.png", CapeCategory.FLAG, Role.GOLD)
        add("UN", "flag/un-sample.png", "flag/un.png", CapeCategory.FLAG, Role.GOLD)
        add("United Kingdom", "flag/united-kingdom-sample.png", "flag/united-kingdom.png", CapeCategory.FLAG, Role.GOLD)
        add("United States", "flag/united-states-sample.png", "flag/united-states.png", CapeCategory.FLAG, Role.GOLD)

        add("Aurora", "cartoon/aurora-sample.png", "cartoon/aurora.png", CapeCategory.CARTOON, Role.DIAMOND)
        add("Beach Girl", "cartoon/beachgirl-sample.png", "cartoon/beachgirl.png", CapeCategory.CARTOON, Role.DIAMOND)
        add("Beach Hut", "cartoon/beachhut-sample.png", "cartoon/beachhut.png", CapeCategory.CARTOON, Role.DIAMOND)
        add("Bridgeend", "cartoon/bridgeend-sample.png", "cartoon/bridgeend.png", CapeCategory.CARTOON, Role.DIAMOND)
        add("Cat", "cartoon/cat-sample.png", "cartoon/cat.png", CapeCategory.CARTOON, Role.DIAMOND)
        add("Cyber Cat", "cartoon/cybercat-sample.png", "cartoon/cybercat.png", CapeCategory.CARTOON, Role.DIAMOND)
        add("Decayed", "cartoon/decayed-sample.png", "cartoon/decayed.png", CapeCategory.CARTOON, Role.DIAMOND)
        add("Kitty", "cartoon/kitty-sample.png", "cartoon/kitty.png", CapeCategory.CARTOON, Role.DIAMOND)
        add("Lost World", "cartoon/lostworld-sample.png", "cartoon/lostworld.png", CapeCategory.CARTOON, Role.DIAMOND)
        add("Mountain", "cartoon/mountain-sample.png", "cartoon/mountain.png", CapeCategory.CARTOON, Role.DIAMOND)
        add(
            "Stargazing Girl",
            "cartoon/stargazinggirl-sample.png",
            "cartoon/stargazinggirl.png",
            CapeCategory.CARTOON,
            Role.DIAMOND
        )
        add("Stellagate", "cartoon/stellagate-sample.png", "cartoon/stellagate.png", CapeCategory.CARTOON, Role.DIAMOND)

        currentCape = getCapeByName(InternalSettingsMod.instance.capeConfigName!!)

        val mc = Minecraft.getMinecraft()
        customCapeDir.listFiles()?.forEach { f ->
            if (!FileUtils.isImageFile(f)) return@forEach
            var file = File(cacheDir, f.name + ".png")
            if (!file.exists()) {
                try {
                    val image = ImageIO.read(f) ?: return@forEach
                    val width = image.width
                    val height = image.height
                    val outputImage = ImageUtils.scissor(
                        image,
                        (width * 0.03125).toInt(),
                        (height * 0.0625).toInt(),
                        (width * 0.125).toInt(),
                        (height * 0.46875).toInt()
                    )
                    ImageIO.write(ImageUtils.resize(outputImage, 1000, 1700), "png", file)
                } catch (e: Exception) {
                    ShindoLogger.error("Failed to load image", e)
                    return@forEach
                }
            }
            if (file.exists()) {
                try {
                    val cape = DynamicTexture(ImageIO.read(f))
                    val ext = FileUtils.getExtension(f)
                    val nameWithoutExt = f.name.replace(".$ext", "")
                    addCustomCape(
                        nameWithoutExt,
                        file,
                        mc.textureManager.getDynamicTextureLocation(f.name.hashCode().toString(), cape),
                        CapeCategory.CUSTOM,
                        Role.DIAMOND
                    )
                } catch (e: Exception) {
                    ShindoLogger.error("Failed to load image", e)
                }
            }
        }

        for (c in capes) {
            when (c) {
                is NormalCape -> c.getSample()?.let { instance.nanoVGManager?.loadImage(it) }
                is CustomCape -> c.getSample().let { instance.nanoVGManager?.loadImage(it) }
            }
            c.getCape()?.let { mc.textureManager.bindTexture(it) }
        }
    }

    fun getCapes(): ArrayList<Cape> = capes
    fun getCurrentCape(): Cape? = currentCape
    fun setCurrentCape(cape: Cape?) {
        currentCape = cape
        cape?.let { InternalSettingsMod.instance.capeConfigName = (it.getName()) }
    }

    private fun add(name: String, samplePath: String, capePath: String, category: CapeCategory, requiredRole: Role) {
        val cosmeticPath = "shindo/cosmetics/cape/"
        capes.add(
            NormalCape(
                name,
                ResourceLocation(cosmeticPath + samplePath),
                ResourceLocation(cosmeticPath + capePath),
                category,
                requiredRole
            )
        )
    }

    private fun addCustomCape(
        name: String,
        sample: File,
        cape: ResourceLocation,
        category: CapeCategory,
        requiredRole: Role
    ) {
        capes.add(CustomCape(name, sample, cape, category, requiredRole))
    }

    fun getCapeByName(name: String): Cape =
        capes.firstOrNull { it.getName() == name } ?: getCapeByName("None")

    fun canUseCape(uuid: UUID, cape: Cape): Boolean =
        RoleManager.hasAtLeast(uuid, cape.getRequiredRole())

    fun getTranslateError(role: Role): TranslateText = CosmeticRoleTextMapper.getTranslateError(role)

    fun getTranslateText(role: Role): TranslateText = CosmeticRoleTextMapper.getTranslateText(role)
}
