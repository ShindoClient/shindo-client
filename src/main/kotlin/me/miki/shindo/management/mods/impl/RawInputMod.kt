package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import net.java.games.input.ControllerEnvironment
import net.java.games.input.Mouse

class RawInputMod : Mod(TranslateText.RAW_INPUT, TranslateText.RAW_INPUT_DESCRIPTION, ModCategory.OTHER, Shinconic.MOD_RAW_INPUT) {
    private val mouseList = ArrayList<Mouse>()
    private var thread: MouseThread? = null

    private var initialised = false
    var isAvailable: Boolean = false
        private set

    @Volatile
    var dx: Float = 0f
        private set

    @Volatile
    var dy: Float = 0f
        private set

    @Volatile
    private var running = false

    init {
        instance = this
    }

    override fun onEnable() {
        super.onEnable()

        if (!initialised) {
            initialised = true
            this.isAvailable = true

            try {
                val env = ControllerEnvironment.getDefaultEnvironment()

                if (env.isSupported) {
                    for (controller in env.controllers) {
                        if (controller is Mouse) {
                            mouseList.add(controller)
                        }
                    }
                } else {
                    this.isAvailable = false
                }
            } catch (e: Exception) {
                this.isAvailable = false
            }
        }

        running = true
        thread = MouseThread()
        thread!!.setDaemon(true)
        thread!!.start()
    }

    override fun onDisable() {
        super.onDisable()
        running = false
    }

    fun getThread(): MouseThread = thread!!

    inner class MouseThread : Thread() {
        override fun run() {
            while (running) {
                this@RawInputMod.isAvailable = !mouseList.isEmpty()

                for (mouse in mouseList) {
                    if (!mouse.poll()) {
                        continue
                    }

                    val dx = mouse.x.pollData
                    val dy = mouse.y.pollData

                    if (org.lwjgl.input.Mouse
                            .isGrabbed()
                    ) {
                        this@RawInputMod.dx += dx
                        this@RawInputMod.dy += dy
                    }
                }
            }
        }

        fun reset() {
            this@RawInputMod.dx = 0f
            this@RawInputMod.dy = 0f
        }
    }

    companion object {
        @JvmField
        var instance: RawInputMod? = null
    }
}
