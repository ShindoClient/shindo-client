package me.miki.shindo.gui.mainmenu.impl

import me.miki.extensions.ui.animation.setAnimation
import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.account.AccountManager
import me.miki.shindo.management.account.data.SavedAccount
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.curve.DecelerateAnimation
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.BrowserUtils
import me.miki.shindo.utils.IOUtils
import me.miki.shindo.utils.MathUtils.lerp
import me.miki.shindo.utils.mouse.MouseUtils.isInside
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class AccountScene(
    parent: GuiShindoMainMenu,
) : MainMenuScene(parent) {
    private val introAnim = DecelerateAnimation(600, 1.0)

    private enum class Step { CHOOSE, MS_CODE, MS_WAITING, OFFLINE_INPUT, SUCCESS, ERROR }

    private var step = Step.CHOOSE
    private var deviceCode: AccountManager.DeviceCodeInfo? = null

    private var offlineInput = ""
    private var offlineCursorVisible = true
    private var offlineCursorTick = 0L
    private var offlineError = ""

    private var authThread: Thread? = null
    private var resultMessage = ""
    private var resultAccount: SavedAccount? = null

    private var msHover = 0f
    private var offHover = 0f
    private var backHover = 0f
    private var confirmHover = 0f
    private var doneHover = 0f
    private var copyHover = 0f

    private var browserOpenFailed = false
    private var linkCopied = false
    private var linkCopiedTick = 0L

    private val stepAnim = SimpleAnimation(0f)
    private var stepAnimTarget = 1f

    override fun initScene() {
        introAnim.reset()
        introAnim.setDirection(Direction.FORWARDS)
        step = Step.CHOOSE
        offlineInput = ""
        offlineError = ""
        resultMessage = ""
        deviceCode = null
        authThread?.interrupt()
        authThread = null
        stepAnimTarget = 1f
        browserOpenFailed = false
        linkCopied = false
        linkCopiedTick = 0L
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        nvg.setupAndDraw { drawNanoVG(instance, nvg, mouseX, mouseY) }
    }

    private fun drawNanoVG(
        instance: Shindo,
        nvg: NanoVGManager,
        mouseX: Int,
        mouseY: Int,
    ) {
        val sr = ScaledResolution(mc)
        val sw = sr.scaledWidth.toFloat()
        val sh = sr.scaledHeight.toFloat()
        val anim = introAnim.getValueFloat()

        stepAnim.setAnimation(stepAnimTarget, 10)
        val sa = stepAnim.getValue() * anim

        // Auto-clear "Copied!" feedback after 2 s
        if (linkCopied && System.currentTimeMillis() - linkCopiedTick > 2000L) {
            linkCopied = false
        }

        val pw = 280f
        val ph =
            when (step) {
                Step.CHOOSE -> 130f
                Step.MS_CODE -> 150f
                Step.MS_WAITING -> 110f
                Step.OFFLINE_INPUT -> 120f
                Step.SUCCESS, Step.ERROR -> 110f
            }
        val px = sw / 2f - pw / 2f
        val py = sh / 2f - ph / 2f

        // Panel background
        nvg.drawRoundedRect(px, py, pw, ph, 8f, Color(12, 12, 18, (sa * 235).toInt()))
        nvg.drawOutlineRoundedRect(px, py, pw, ph, 8f, 1.2f, Color(255, 255, 255, (sa * 35).toInt()))

        when (step) {
            Step.CHOOSE -> drawChoose(nvg, px, py, pw, ph, mouseX, mouseY, sa)
            Step.MS_CODE -> drawMsCode(nvg, px, py, pw, ph, mouseX, mouseY, sa)
            Step.MS_WAITING -> drawMsWaiting(nvg, sw, sh, px, py, pw, ph, sa)
            Step.OFFLINE_INPUT -> drawOfflineInput(nvg, px, py, pw, ph, mouseX, mouseY, sa)
            Step.SUCCESS -> drawSuccess(nvg, sw, sh, px, py, pw, ph, mouseX, mouseY, sa)
            Step.ERROR -> drawError(nvg, sw, sh, px, py, pw, ph, mouseX, mouseY, sa)
        }

        // Back button (always visible except success/error which have their own done button)
        if (step != Step.SUCCESS && step != Step.ERROR) {
            drawBackButton(nvg, px, py, mouseX, mouseY, sa)
        }
    }

    private fun drawChoose(
        nvg: NanoVGManager,
        px: Float,
        py: Float,
        pw: Float,
        ph: Float,
        mouseX: Int,
        mouseY: Int,
        sa: Float,
    ) {
        nvg.drawCenteredText("Add Account", px + pw / 2f, py + 14f, Color(255, 255, 255, (sa * 220).toInt()), 11f, Fonts.SEMIBOLD)

        val cardW = (pw - 36f) / 2f
        val cardH = 56f
        val cardY = py + 36f

        // Microsoft card
        val msX = px + 12f
        msHover = lerp(msHover, if (isInside(mouseX, mouseY, msX, cardY, cardW, cardH)) 1f else 0f, 0.18f)
        nvg.drawRoundedRect(msX, cardY, cardW, cardH, 6f, Color(30, 40, 70, (sa * (160 + msHover * 80)).toInt()))
        nvg.drawOutlineRoundedRect(msX, cardY, cardW, cardH, 6f, 1f, Color(100, 140, 255, (sa * (80 + msHover * 120)).toInt()))
        nvg.drawCenteredText(
            Lucide.MONITOR,
            msX + cardW / 2f,
            cardY + 12f,
            Color(120, 170, 255, (sa * (180 + msHover * 75)).toInt()),
            18f,
            Fonts.LUCIDE,
        )
        nvg.drawCenteredText(
            "Microsoft",
            msX + cardW / 2f,
            cardY + cardH - 14f,
            Color(200, 215, 255, (sa * (200 + msHover * 55)).toInt()),
            9.5f,
            Fonts.SEMIBOLD,
        )

        // Offline card
        val offX = msX + cardW + 12f
        offHover = lerp(offHover, if (isInside(mouseX, mouseY, offX, cardY, cardW, cardH)) 1f else 0f, 0.18f)
        nvg.drawRoundedRect(offX, cardY, cardW, cardH, 6f, Color(30, 30, 40, (sa * (160 + offHover * 80)).toInt()))
        nvg.drawOutlineRoundedRect(offX, cardY, cardW, cardH, 6f, 1f, Color(160, 160, 180, (sa * (60 + offHover * 100)).toInt()))
        nvg.drawCenteredText(
            Lucide.USER,
            offX + cardW / 2f,
            cardY + 12f,
            Color(180, 180, 200, (sa * (180 + offHover * 75)).toInt()),
            18f,
            Fonts.LUCIDE,
        )
        nvg.drawCenteredText(
            "Offline",
            offX + cardW / 2f,
            cardY + cardH - 14f,
            Color(190, 190, 210, (sa * (200 + offHover * 55)).toInt()),
            9.5f,
            Fonts.SEMIBOLD,
        )
    }

    private fun drawMsCode(
        nvg: NanoVGManager,
        px: Float,
        py: Float,
        pw: Float,
        ph: Float,
        mouseX: Int,
        mouseY: Int,
        sa: Float,
    ) {
        val code = deviceCode ?: return

        nvg.drawCenteredText("Microsoft Login", px + pw / 2f, py + 14f, Color(255, 255, 255, (sa * 220).toInt()), 11f, Fonts.SEMIBOLD)

        // Status line — shows what happened when the panel opened
        val statusText = if (browserOpenFailed) "Browser failed. Copy the link below:" else "Your browser should have opened."
        val statusColor = if (browserOpenFailed) Color(255, 160, 80, (sa * 210).toInt()) else Color(140, 200, 140, (sa * 190).toInt())
        nvg.drawCenteredText(statusText, px + pw / 2f, py + 32f, statusColor, 8f, Fonts.REGULAR)

        // Code box
        val codeBoxW = 110f
        val codeBoxH = 24f
        val codeBoxX = px + pw / 2f - codeBoxW / 2f
        val codeBoxY = py + 44f
        nvg.drawRoundedRect(codeBoxX, codeBoxY, codeBoxW, codeBoxH, 5f, Color(20, 20, 30, (sa * 200).toInt()))
        nvg.drawOutlineRoundedRect(codeBoxX, codeBoxY, codeBoxW, codeBoxH, 5f, 1.2f, Color(100, 140, 255, (sa * 160).toInt()))
        nvg.drawCenteredText(code.userCode, px + pw / 2f, codeBoxY + 7f, Color(255, 255, 255, (sa * 240).toInt()), 11f, Fonts.SEMIBOLD)

        // Fallback copy button — always visible, more prominent when browser failed
        val copyBtnW = pw - 32f
        val copyBtnH = 20f
        val copyBtnX = px + 16f
        val copyBtnY = py + 78f
        copyHover = lerp(copyHover, if (isInside(mouseX, mouseY, copyBtnX, copyBtnY, copyBtnW, copyBtnH)) 1f else 0f, 0.18f)

        val copyBtnAlpha = if (browserOpenFailed) sa else sa * 0.75f
        nvg.drawRoundedRect(
            copyBtnX,
            copyBtnY,
            copyBtnW,
            copyBtnH,
            5f,
            Color(30, 30, 45, (copyBtnAlpha * (150 + copyHover * 80)).toInt()),
        )
        nvg.drawOutlineRoundedRect(
            copyBtnX,
            copyBtnY,
            copyBtnW,
            copyBtnH,
            5f,
            1f,
            Color(160, 160, 200, (copyBtnAlpha * (50 + copyHover * 100)).toInt()),
        )

        val copyLabel = if (linkCopied) "${Lucide.CHECK}  Copied!" else "${Lucide.COPY}  Copy link to clipboard"
        val copyLabelColor =
            if (linkCopied) {
                Color(120, 210, 130, (copyBtnAlpha * 230).toInt())
            } else {
                Color(190, 190, 215, (copyBtnAlpha * (180 + copyHover * 75)).toInt())
            }
        nvg.drawCenteredText(copyLabel, px + pw / 2f, copyBtnY + 6f, copyLabelColor, 9f, Fonts.SEMIBOLD)

        // Hint above the copy button when browser succeeded
        if (!browserOpenFailed) {
            nvg.drawCenteredText(
                "If the browser didn't open, copy the link:",
                px + pw / 2f,
                copyBtnY - 10f,
                Color(130, 130, 155, (sa * 140).toInt()),
                7.5f,
                Fonts.REGULAR,
            )
        }

        // Done / "I authenticated" button
        val btnW = 130f
        val btnH = 20f
        val btnX = px + pw / 2f - btnW / 2f
        val btnY = py + ph - 28f
        confirmHover = lerp(confirmHover, if (isInside(mouseX, mouseY, btnX, btnY, btnW, btnH)) 1f else 0f, 0.18f)
        nvg.drawRoundedRect(btnX, btnY, btnW, btnH, 5f, Color(40, 70, 160, (sa * (160 + confirmHover * 80)).toInt()))
        nvg.drawOutlineRoundedRect(btnX, btnY, btnW, btnH, 5f, 1f, Color(100, 140, 255, (sa * (80 + confirmHover * 100)).toInt()))
        nvg.drawCenteredText(
            "I've logged in",
            px + pw / 2f,
            btnY + 6f,
            Color(200, 215, 255, (sa * (200 + confirmHover * 55)).toInt()),
            9f,
            Fonts.SEMIBOLD,
        )
    }

    private fun drawMsWaiting(
        nvg: NanoVGManager,
        sw: Float,
        sh: Float,
        px: Float,
        py: Float,
        pw: Float,
        ph: Float,
        sa: Float,
    ) {
        nvg.drawCenteredText("Microsoft Login", px + pw / 2f, py + 14f, Color(255, 255, 255, (sa * 220).toInt()), 11f, Fonts.SEMIBOLD)
        nvg.drawCenteredText(
            "Waiting for authentication...",
            px + pw / 2f,
            py + ph / 2f - 10f,
            Color(180, 180, 200, (sa * 180).toInt()),
            9f,
            Fonts.REGULAR,
        )

        // Simple animated dots
        val dots =
            when ((System.currentTimeMillis() / 400) % 4) {
                0L -> "."
                1L -> ".."
                2L -> "..."
                else -> ""
            }
        nvg.drawCenteredText(dots, px + pw / 2f, py + ph / 2f + 8f, Color(120, 170, 255, (sa * 200).toInt()), 12f, Fonts.SEMIBOLD)
    }

    private fun drawOfflineInput(
        nvg: NanoVGManager,
        px: Float,
        py: Float,
        pw: Float,
        ph: Float,
        mouseX: Int,
        mouseY: Int,
        sa: Float,
    ) {
        nvg.drawCenteredText("Offline Account", px + pw / 2f, py + 14f, Color(255, 255, 255, (sa * 220).toInt()), 11f, Fonts.SEMIBOLD)
        nvg.drawCenteredText("Enter your username:", px + pw / 2f, py + 34f, Color(180, 180, 200, (sa * 180).toInt()), 8.5f, Fonts.REGULAR)

        // Input field
        val fieldW = pw - 32f
        val fieldH = 22f
        val fieldX = px + 16f
        val fieldY = py + 50f
        nvg.drawRoundedRect(fieldX, fieldY, fieldW, fieldH, 5f, Color(20, 20, 28, (sa * 200).toInt()))
        nvg.drawOutlineRoundedRect(fieldX, fieldY, fieldW, fieldH, 5f, 1f, Color(255, 255, 255, (sa * 50).toInt()))

        val cursor = if (blinkCursor()) "|" else ""
        nvg.drawText("$offlineInput$cursor", fieldX + 8f, fieldY + 6f, Color(255, 255, 255, (sa * 230).toInt()), 9.5f, Fonts.REGULAR)

        // Error
        if (offlineError.isNotEmpty()) {
            nvg.drawCenteredText(
                offlineError,
                px + pw / 2f,
                fieldY + fieldH + 8f,
                Color(255, 100, 100, (sa * 200).toInt()),
                8f,
                Fonts.REGULAR,
            )
        }

        // Confirm button
        val btnW = 100f
        val btnH = 20f
        val btnX = px + pw / 2f - btnW / 2f
        val btnY = py + ph - 28f
        confirmHover = lerp(confirmHover, if (isInside(mouseX, mouseY, btnX, btnY, btnW, btnH)) 1f else 0f, 0.18f)
        nvg.drawRoundedRect(btnX, btnY, btnW, btnH, 5f, Color(30, 30, 42, (sa * (160 + confirmHover * 80)).toInt()))
        nvg.drawOutlineRoundedRect(btnX, btnY, btnW, btnH, 5f, 1f, Color(160, 160, 180, (sa * (60 + confirmHover * 100)).toInt()))
        nvg.drawCenteredText(
            "Add",
            px + pw / 2f,
            btnY + 6f,
            Color(200, 200, 220, (sa * (200 + confirmHover * 55)).toInt()),
            9.5f,
            Fonts.SEMIBOLD,
        )
    }

    private fun drawSuccess(
        nvg: NanoVGManager,
        sw: Float,
        sh: Float,
        px: Float,
        py: Float,
        pw: Float,
        ph: Float,
        mouseX: Int,
        mouseY: Int,
        sa: Float,
    ) {
        nvg.drawCenteredText(Lucide.CHECK, px + pw / 2f, py + 18f, Color(100, 220, 130, (sa * 230).toInt()), 22f, Fonts.LUCIDE)
        nvg.drawCenteredText("Account added!", px + pw / 2f, py + 48f, Color(255, 255, 255, (sa * 220).toInt()), 11f, Fonts.SEMIBOLD)
        nvg.drawCenteredText(resultMessage, px + pw / 2f, py + 64f, Color(160, 200, 160, (sa * 180).toInt()), 8.5f, Fonts.REGULAR)
        drawDoneButton(nvg, px, py, pw, ph, mouseX, mouseY, sa)
    }

    private fun drawError(
        nvg: NanoVGManager,
        sw: Float,
        sh: Float,
        px: Float,
        py: Float,
        pw: Float,
        ph: Float,
        mouseX: Int,
        mouseY: Int,
        sa: Float,
    ) {
        nvg.drawCenteredText(Lucide.X, px + pw / 2f, py + 18f, Color(220, 80, 80, (sa * 230).toInt()), 22f, Fonts.LUCIDE)
        nvg.drawCenteredText("Authentication failed", px + pw / 2f, py + 48f, Color(255, 255, 255, (sa * 220).toInt()), 11f, Fonts.SEMIBOLD)
        nvg.drawCenteredText(resultMessage.take(48), px + pw / 2f, py + 64f, Color(220, 140, 140, (sa * 180).toInt()), 8f, Fonts.REGULAR)
        drawDoneButton(nvg, px, py, pw, ph, mouseX, mouseY, sa)
    }

    private fun drawDoneButton(
        nvg: NanoVGManager,
        px: Float,
        py: Float,
        pw: Float,
        ph: Float,
        mouseX: Int,
        mouseY: Int,
        sa: Float,
    ) {
        val btnW = 80f
        val btnH = 20f
        val btnX = px + pw / 2f - btnW / 2f
        val btnY = py + ph - 28f
        doneHover = lerp(doneHover, if (isInside(mouseX, mouseY, btnX, btnY, btnW, btnH)) 1f else 0f, 0.18f)
        nvg.drawRoundedRect(btnX, btnY, btnW, btnH, 5f, Color(30, 30, 42, (sa * (160 + doneHover * 80)).toInt()))
        nvg.drawOutlineRoundedRect(btnX, btnY, btnW, btnH, 5f, 1f, Color(255, 255, 255, (sa * (35 + doneHover * 65)).toInt()))
        nvg.drawCenteredText(
            "Done",
            px + pw / 2f,
            btnY + 6f,
            Color(210, 210, 230, (sa * (200 + doneHover * 55)).toInt()),
            9.5f,
            Fonts.SEMIBOLD,
        )
    }

    private fun drawBackButton(
        nvg: NanoVGManager,
        px: Float,
        py: Float,
        mouseX: Int,
        mouseY: Int,
        sa: Float,
    ) {
        val size = 16f
        backHover = lerp(backHover, if (isInside(mouseX, mouseY, px + 6f, py + 6f, size, size)) 1f else 0f, 0.18f)
        nvg.drawCenteredText(
            Lucide.ARROW_LEFT,
            px + 6f + size / 2f,
            py + 6f + size / 2f - 7f,
            Color(180, 180, 200, (sa * (140 + backHover * 115)).toInt()),
            12f,
            Fonts.LUCIDE,
        )
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton != 0) return

        val sr = ScaledResolution(mc)
        val sw = sr.scaledWidth.toFloat()
        val sh = sr.scaledHeight.toFloat()

        val pw = 280f
        val ph =
            when (step) {
                Step.CHOOSE -> 130f
                Step.MS_CODE -> 150f
                Step.MS_WAITING -> 110f
                Step.OFFLINE_INPUT -> 120f
                Step.SUCCESS, Step.ERROR -> 110f
            }
        val px = sw / 2f - pw / 2f
        val py = sh / 2f - ph / 2f

        when (step) {
            Step.CHOOSE -> {
                handleChooseClick(mouseX, mouseY, px, py, pw, ph)
            }

            Step.MS_CODE -> {
                handleMsCodeClick(mouseX, mouseY, px, py, pw, ph)
            }

            Step.OFFLINE_INPUT -> {
                handleOfflineClick(mouseX, mouseY, px, py, pw, ph)
            }

            Step.SUCCESS, Step.ERROR -> {
                handleDoneClick(mouseX, mouseY, px, py, pw, ph)
            }

            else -> {}
        }

        // Back button
        if (step != Step.SUCCESS && step != Step.ERROR) {
            if (isInside(mouseX, mouseY, px + 6f, py + 6f, 16f, 16f)) {
                if (step == Step.CHOOSE) {
                    goBack()
                } else {
                    authThread?.interrupt()
                    authThread = null
                    step = Step.CHOOSE
                }
            }
        }
    }

    private fun handleChooseClick(
        mouseX: Int,
        mouseY: Int,
        px: Float,
        py: Float,
        pw: Float,
        ph: Float,
    ) {
        val cardW = (pw - 36f) / 2f
        val cardH = 56f
        val cardY = py + 36f
        val msX = px + 12f
        val offX = msX + cardW + 12f

        if (isInside(mouseX, mouseY, msX, cardY, cardW, cardH)) {
            startMicrosoftAuth()
        } else if (isInside(mouseX, mouseY, offX, cardY, cardW, cardH)) {
            step = Step.OFFLINE_INPUT
            offlineInput = ""
            offlineError = ""
        }
    }

    private fun handleMsCodeClick(
        mouseX: Int,
        mouseY: Int,
        px: Float,
        py: Float,
        pw: Float,
        ph: Float,
    ) {
        val code = deviceCode ?: return

        // Copy button
        val copyBtnW = pw - 32f
        val copyBtnH = 20f
        val copyBtnX = px + 16f
        val copyBtnY = py + 78f
        if (isInside(mouseX, mouseY, copyBtnX, copyBtnY, copyBtnW, copyBtnH)) {
            IOUtils.copyStringToClipboard(code.directUrl)
            linkCopied = true
            linkCopiedTick = System.currentTimeMillis()
            return
        }

        // "I've logged in" button — advance to waiting state
        val btnW = 130f
        val btnH = 20f
        val btnX = px + pw / 2f - btnW / 2f
        val btnY = py + ph - 28f
        if (isInside(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            step = Step.MS_WAITING
        }
    }

    private fun handleOfflineClick(
        mouseX: Int,
        mouseY: Int,
        px: Float,
        py: Float,
        pw: Float,
        ph: Float,
    ) {
        val btnW = 100f
        val btnH = 20f
        val btnX = px + pw / 2f - btnW / 2f
        val btnY = py + ph - 28f
        if (isInside(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            submitOffline()
        }
    }

    private fun handleDoneClick(
        mouseX: Int,
        mouseY: Int,
        px: Float,
        py: Float,
        pw: Float,
        ph: Float,
    ) {
        val btnW = 80f
        val btnH = 20f
        val btnX = px + pw / 2f - btnW / 2f
        val btnY = py + ph - 28f
        if (isInside(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            goBack()
        }
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        if (step != Step.OFFLINE_INPUT) return

        when {
            keyCode == 14 -> { // Backspace
                if (offlineInput.isNotEmpty()) offlineInput = offlineInput.dropLast(1)
                offlineError = ""
            }

            keyCode == 28 -> {
                submitOffline()
            }

            // Enter
            typedChar.isLetterOrDigit() || typedChar == '_' || typedChar == '-' -> {
                if (offlineInput.length < 16) {
                    offlineInput += typedChar
                    offlineError = ""
                }
            }
        }
    }

    private fun startMicrosoftAuth() {
        val mgr = Shindo.getInstance().getAccountManager()
        step = Step.MS_CODE
        deviceCode = null
        browserOpenFailed = false
        linkCopied = false

        authThread =
            Thread {
                mgr.addMicrosoftAccount(
                    onCode = { code ->
                        deviceCode = code
                        step = Step.MS_CODE
                        browserOpenFailed = !BrowserUtils.tryOpenBrowser(code.directUrl)
                        IOUtils.copyStringToClipboard(code.directUrl)
                        linkCopied = true
                        linkCopiedTick = System.currentTimeMillis()
                    },
                    onSuccess = { acc ->
                        resultMessage = "Welcome, ${acc.username}!"
                        resultAccount = acc
                        mgr.switchAccount(acc.id)
                        step = Step.SUCCESS
                    },
                    onFailure = { e ->
                        resultMessage = e.message ?: "Unknown error"
                        step = Step.ERROR
                    },
                )
            }.also {
                it.isDaemon = true
                it.start()
            }
    }

    private fun submitOffline() {
        val name = offlineInput.trim()
        if (name.length < 3) {
            offlineError = "Username too short (min 3 chars)"
            return
        }
        val mgr = Shindo.getInstance().getAccountManager()
        val acc = mgr.addOfflineAccount(name)
        mgr.switchAccount(acc.id)
        resultMessage = "Playing as $name (offline)"
        step = Step.SUCCESS
    }

    private fun goBack() {
        this.setCurrentScene(this.getSceneByClass(MainScene::class.java))
    }

    private fun blinkCursor(): Boolean {
        val now = System.currentTimeMillis()
        return (now / 530) % 2 == 0L
    }
}
