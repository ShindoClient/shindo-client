package me.miki.shindo.management.mods.impl.skin3d.opengl

import me.miki.shindo.utils.GlUtils.getTexImage
import me.miki.shindo.utils.GlUtils.pixelStore
import java.nio.ByteBuffer

class NativeImage(
    format: Format,
    i: Int,
    j: Int,
    bl: Boolean,
) : AutoCloseable {
    private val format: Format

    val width: Int

    val height: Int
    private val size: Int
    private val buffer: ByteBuffer

    constructor(i: Int, j: Int, bl: Boolean) : this(Format.RGBA, i, j, bl)

    init {
        require(!(i <= 0 || j <= 0)) { "Invalid texture size: " + i + "x" + j }

        this.format = format
        this.width = i
        this.height = j
        this.size = i * j * format.components()
        buffer = ByteBuffer.allocateDirect(this.size)
    }

    override fun close() {
    }

    fun format(): Format = this.format

    fun getPixelRGBA(
        i: Int,
        j: Int,
    ): Int {
        val l = (i + j * this.width) * 4
        return buffer.getInt(l)
    }

    fun setPixelRGBA(
        i: Int,
        j: Int,
        k: Int,
    ) {
        val l = (i + j * this.width) * 4
        buffer.putInt(l, k)
    }

    fun getLuminanceOrAlpha(
        i: Int,
        j: Int,
    ): Byte {
        val k = (i + j * this.width) * this.format.components() + this.format.luminanceOrAlphaOffset() / 8
        return buffer.get(k)
    }

    fun downloadTexture(
        i: Int,
        bl: Boolean,
    ) {
        this.format.setPackPixelStoreState()
        getTexImage(3553, i, this.format.glFormat(), 5121, this.buffer)

        if (bl && this.format.hasAlpha()) {
            for (j in 0 until this.height) {
                for (k in 0 until this.width) {
                    setPixelRGBA(k, j, getPixelRGBA(k, j) or (255 shl this.format.alphaOffset()))
                }
            }
        }
    }

    enum class InternalGlFormat(
        private val glFormat: Int,
    ) {
        RGBA(6408),
        RGB(6407),
        RG(33319),
        RED(6403),
        ;

        fun glFormat(): Int = this.glFormat
    }

    enum class Format(
        val components: Int,
        private val glFormat: Int,
        private val hasRed: Boolean,
        private val hasGreen: Boolean,
        private val hasBlue: Boolean,
        private val hasLuminance: Boolean,
        private val hasAlpha: Boolean,
        private val redOffset: Int,
        private val greenOffset: Int,
        private val blueOffset: Int,
        private val luminanceOffset: Int,
        private val alphaOffset: Int,
        private val supportedByStb: Boolean,
    ) {
        RGBA(4, 6408, true, true, true, false, true, 0, 8, 16, 255, 24, true),
        RGB(
            3,
            6407,
            true,
            true,
            true,
            false,
            false,
            0,
            8,
            16,
            255,
            255,
            true,
        ),
        LUMINANCE_ALPHA(
            2,
            33319,
            false,
            false,
            false,
            true,
            true,
            255,
            255,
            255,
            0,
            8,
            true,
        ),
        LUMINANCE(1, 6403, false, false, false, true, false, 0, 0, 0, 0, 255, true),
        ;

        fun components(): Int = this.components

        fun setPackPixelStoreState() {
            pixelStore(3333, components())
        }

        fun setUnpackPixelStoreState() {
            pixelStore(3317, components())
        }

        fun glFormat(): Int = this.glFormat

        fun hasRed(): Boolean = this.hasRed

        fun hasGreen(): Boolean = this.hasGreen

        fun hasBlue(): Boolean = this.hasBlue

        fun hasLuminance(): Boolean = this.hasLuminance

        fun hasAlpha(): Boolean = this.hasAlpha

        fun redOffset(): Int = this.redOffset

        fun greenOffset(): Int = this.greenOffset

        fun blueOffset(): Int = this.blueOffset

        fun luminanceOffset(): Int = this.luminanceOffset

        fun alphaOffset(): Int = this.alphaOffset

        fun hasLuminanceOrRed(): Boolean = (this.hasLuminance || this.hasRed)

        fun hasLuminanceOrGreen(): Boolean = (this.hasLuminance || this.hasGreen)

        fun hasLuminanceOrBlue(): Boolean = (this.hasLuminance || this.hasBlue)

        fun hasLuminanceOrAlpha(): Boolean = (this.hasLuminance || this.hasAlpha)

        fun luminanceOrRedOffset(): Int = if (this.hasLuminance) this.luminanceOffset else this.redOffset

        fun luminanceOrGreenOffset(): Int = if (this.hasLuminance) this.luminanceOffset else this.greenOffset

        fun luminanceOrBlueOffset(): Int = if (this.hasLuminance) this.luminanceOffset else this.blueOffset

        fun luminanceOrAlphaOffset(): Int = if (this.hasLuminance) this.luminanceOffset else this.alphaOffset

        fun supportedByStb(): Boolean = this.supportedByStb

        companion object {
            fun getStbFormat(i: Int): Format {
                when (i) {
                    1 -> return LUMINANCE
                    2 -> return LUMINANCE_ALPHA
                    3 -> return RGB
                }
                return RGBA
            }
        }
    }

    companion object {
        fun getA(i: Int): Int = i shr 24 and 0xFF

        fun getR(i: Int): Int = i shr 0 and 0xFF

        fun getG(i: Int): Int = i shr 8 and 0xFF

        fun getB(i: Int): Int = i shr 16 and 0xFF

        fun combine(
            i: Int,
            j: Int,
            k: Int,
            l: Int,
        ): Int = (i and 0xFF) shl 24 or ((j and 0xFF) shl 16) or ((k and 0xFF) shl 8) or ((l and 0xFF) shl 0)
    }
}
