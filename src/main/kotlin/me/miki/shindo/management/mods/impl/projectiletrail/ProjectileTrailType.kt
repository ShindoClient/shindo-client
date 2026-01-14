package me.miki.shindo.management.mods.impl.projectiletrail

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.config.PropertyEnum
import net.minecraft.util.EnumParticleTypes

enum class ProjectileTrailType(
    particle: EnumParticleTypes,
    nameTranslate: TranslateText,
    velocity: Float,
    translate: Float,
    count: Int
) : PropertyEnum {
    BLACK_SMOKE(EnumParticleTypes.SMOKE_NORMAL, TranslateText.BLACK_SMOKE, 0.07f, 0.0f, 2),
    FIRE(EnumParticleTypes.FLAME, TranslateText.FIRE, 0.1f, 0.0f, 1),
    GREEN_STAR(EnumParticleTypes.VILLAGER_HAPPY, TranslateText.GREEN_STAR, 0.0f, 0.1f, 1),
    HEARTS(EnumParticleTypes.HEART, TranslateText.HEARTS, 0.0f, 0.2f, 1),
    MAGIC(EnumParticleTypes.SPELL_WITCH, TranslateText.MAGIC, 1.0f, 0.0f, 2),
    MUSIC_NOTES(EnumParticleTypes.NOTE, TranslateText.MUSIC_NOTES, 1.0f, 1.0f, 2),
    SLIME(EnumParticleTypes.SLIME, TranslateText.SLIME, 0.5f, 0.3f, 1),
    SPARK(EnumParticleTypes.FIREWORKS_SPARK, TranslateText.SPARK, 0.05f, 0.0f, 1),
    SWIRL(EnumParticleTypes.SPELL_MOB, TranslateText.SWIRL, 1.0f, 0.0f, 1),
    WHITE_SMOKE(EnumParticleTypes.SNOW_SHOVEL, TranslateText.WHITE_SMOKE, 0.07f, 0.0f, 2);

    val nameTranslate: TranslateText
    var particle: EnumParticleTypes?
    var velocity: Float
    var translate: Float
    var count: Int

    init {
        this.particle = particle
        this.nameTranslate = nameTranslate
        this.velocity = velocity
        this.translate = translate
        this.count = count
    }

    override fun getTranslate(): TranslateText {
        return nameTranslate
    }

    override fun getDisplayName(): String = super.getDisplayName()

    override fun getNameKey(): String = super.getNameKey()

    companion object {
        fun getTypeByKey(key: String?): ProjectileTrailType {
            for (t in ProjectileTrailType.entries) {
                if (t.nameTranslate.getKey() == key) {
                    return t
                }
            }

            return ProjectileTrailType.HEARTS
        }
    }
}
