package me.miki.shindo.gui.mainmenu.widget.impl

import me.miki.extensions.ui.animation.setAnimation
import me.miki.shindo.Shindo
import me.miki.shindo.api.websocket.AccountType
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.widget.MainMenuWidget
import me.miki.shindo.gui.mainmenu.widget.MainMenuWidgetContext
import me.miki.shindo.gui.mainmenu.widget.WidgetAnchor
import me.miki.shindo.management.account.data.SavedAccount
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.MathUtils.lerp
import me.miki.shindo.utils.mouse.MouseUtils.isInside
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.DefaultPlayerSkin
import java.awt.Color

class AccountDropdownWidget(
    private val parent: GuiShindoMainMenu,
) : MainMenuWidget {
    override val anchor = WidgetAnchor.TOP_LEFT
    override val anchorPadding = 10f
    override val width: Float get() = _headerW
    override val height: Float get() = HEADER_H

    private val mc: Minecraft = Minecraft.getMinecraft()
    private var open = false
    private val openAnim = SimpleAnimation(0f)
    private val rowHovers = mutableMapOf<Int, Float>()
    private var cachedAccounts: List<SavedAccount> = emptyList()
    private var lastCacheTick = 0L
    private var _headerW = 0f
    private var _dropY = 0f
    private var _dropH = 0f

    private val HEADER_H = 24f
    private val DROP_W = 170f
    private val ROW_H = 28f
    private val ADD_ROW_H = 26f
    private val HEAD_SZ = 16f
    private val HEAD_PAD = 6f
    private val CORNER_R = 6f

    var onAddAccountClicked: (() -> Unit)? = null

    override fun onSceneInit() {
        open = false
        rowHovers.clear()
    }

    override fun draw(
        ctx: MainMenuWidgetContext,
        x: Float,
        y: Float,
    ) {
        refreshCacheIfNeeded(ctx.nvg)

        openAnim.setAnimation(if (open) 1f else 0f, 12)

        val active = cachedAccounts.firstOrNull { it.active }
        val name = active?.username ?: mc.session.username
        val nameW = ctx.nvg.getTextWidth(name, 10f, Fonts.SEMIBOLD)
        val headerW = nameW + HEAD_SZ + HEAD_PAD * 3f
        _headerW = headerW

        val anim = ctx.anim
        val mouseX = ctx.mouseX
        val mouseY = ctx.mouseY

        val hHov =
            lerp(
                rowHovers.getOrDefault(-1, 0f),
                if (isInside(mouseX, mouseY, x, y, headerW, HEADER_H)) 1f else 0f,
                0.18f,
            )
        rowHovers[-1] = hHov

        ctx.nvg.drawRoundedRect(
            x,
            y,
            headerW,
            HEADER_H,
            CORNER_R,
            Color(20, 20, 25, (anim * (180 + hHov * 60)).toInt()),
        )
        ctx.nvg.drawOutlineRoundedRect(
            x,
            y,
            headerW,
            HEADER_H,
            CORNER_R,
            1f,
            Color(255, 255, 255, (anim * (30 + hHov * 50)).toInt()),
        )

        drawHead(ctx.nvg, active, x + HEAD_PAD, y + (HEADER_H - HEAD_SZ) / 2f, HEAD_SZ, anim)

        ctx.nvg.drawText(
            name,
            x + HEAD_SZ + HEAD_PAD * 2f,
            y + HEADER_H / 2f - 5f,
            Color(255, 255, 255, (anim * 230).toInt()),
            10f,
            Fonts.SEMIBOLD,
        )

        // Chevron
        ctx.nvg.drawText(
            if (open) Lucide.CHEVRON_UP else Lucide.CHEVRON_DOWN,
            x + headerW - 14f,
            y + HEADER_H / 2f - 6f,
            Color(200, 200, 220, (anim * (100 + hHov * 80)).toInt()),
            10f,
            Fonts.LUCIDE,
        )

        val dropAlpha = openAnim.getValue() * anim
        if (dropAlpha <= 0.01f) return

        val others = cachedAccounts.filter { !it.active }
        val dropH = others.size * ROW_H + ADD_ROW_H + 12f
        val dropY = y + HEADER_H + 4f

        _dropY = dropY
        _dropH = dropH

        ctx.nvg.drawRoundedRect(
            x,
            dropY,
            DROP_W,
            dropH,
            CORNER_R,
            Color(15, 15, 20, (dropAlpha * 230).toInt()),
        )
        ctx.nvg.drawOutlineRoundedRect(
            x,
            dropY,
            DROP_W,
            dropH,
            CORNER_R,
            1f,
            Color(255, 255, 255, (dropAlpha * 40).toInt()),
        )

        var rowY = dropY + 4f

        others.forEachIndexed { idx, acc ->
            val hov =
                lerp(
                    rowHovers.getOrDefault(idx, 0f),
                    if (isInside(mouseX, mouseY, x + 4f, rowY, DROP_W - 8f, ROW_H)) 1f else 0f,
                    0.18f,
                )
            rowHovers[idx] = hov

            if (hov > 0.01f) {
                ctx.nvg.drawRoundedRect(
                    x + 4f,
                    rowY,
                    DROP_W - 8f,
                    ROW_H,
                    4f,
                    Color(255, 255, 255, (dropAlpha * hov * 25).toInt()),
                )
            }

            drawHead(ctx.nvg, acc, x + HEAD_PAD + 4f, rowY + (ROW_H - HEAD_SZ) / 2f, HEAD_SZ, dropAlpha)

            val displayName = if (acc.username.length > 16) acc.username.take(14) + ".." else acc.username
            ctx.nvg.drawText(
                displayName,
                x + HEAD_SZ + HEAD_PAD * 2f + 4f,
                rowY + ROW_H / 2f - 5f,
                Color(220, 220, 220, (dropAlpha * (180 + hov * 75)).toInt()),
                9.5f,
                Fonts.REGULAR,
            )

            val badge = if (acc.type == AccountType.MICROSOFT) "MS" else "OFF"
            val badgeColor =
                if (acc.type == AccountType.MICROSOFT) {
                    Color(100, 160, 255, (dropAlpha * 160).toInt())
                } else {
                    Color(160, 160, 160, (dropAlpha * 140).toInt())
                }
            ctx.nvg.drawText(badge, x + DROP_W - 26f, rowY + ROW_H / 2f - 4f, badgeColor, 7.5f, Fonts.SEMIBOLD)

            rowY += ROW_H
        }

        // Divider
        ctx.nvg.drawRoundedRect(
            x + 8f,
            rowY,
            DROP_W - 16f,
            1f,
            0.5f,
            Color(255, 255, 255, (dropAlpha * 30).toInt()),
        )
        rowY += 5f

        // Add account row
        val addHov =
            lerp(
                rowHovers.getOrDefault(99, 0f),
                if (isInside(mouseX, mouseY, x + 4f, rowY, DROP_W - 8f, ADD_ROW_H)) 1f else 0f,
                0.18f,
            )
        rowHovers[99] = addHov

        if (addHov > 0.01f) {
            ctx.nvg.drawRoundedRect(
                x + 4f,
                rowY,
                DROP_W - 8f,
                ADD_ROW_H,
                4f,
                Color(255, 255, 255, (dropAlpha * addHov * 22).toInt()),
            )
        }

        ctx.nvg.drawCenteredText(
            Lucide.PLUS,
            x + 18f,
            rowY + ADD_ROW_H / 2f - 7f,
            Color(180, 180, 200, (dropAlpha * (160 + addHov * 95)).toInt()),
            12f,
            Fonts.LUCIDE,
        )
        ctx.nvg.drawText(
            "Add account",
            x + 30f,
            rowY + ADD_ROW_H / 2f - 5f,
            Color(180, 180, 200, (dropAlpha * (160 + addHov * 95)).toInt()),
            9.5f,
            Fonts.REGULAR,
        )
    }

    override fun mouseClicked(
        ctx: MainMenuWidgetContext,
        x: Float,
        y: Float,
        mouseButton: Int,
    ): Boolean {
        if (mouseButton != 0) return false

        val mouseX = ctx.mouseX
        val mouseY = ctx.mouseY

        // Header — toggle open
        if (isInside(mouseX, mouseY, x, y, _headerW, HEADER_H)) {
            open = !open
            return true
        }

        if (!open) return false

        // Outside panel — close, don't consume so scene handles the underlying click
        if (!isInside(mouseX, mouseY, x, _dropY, DROP_W, _dropH)) {
            open = false
            return false
        }

        val others = cachedAccounts.filter { !it.active }
        var rowY = _dropY + 4f

        for (acc in others) {
            if (isInside(mouseX, mouseY, x + 4f, rowY, DROP_W - 8f, ROW_H)) {
                switchAccount(acc.id)
                open = false
                return true
            }
            rowY += ROW_H
        }

        rowY += 5f // divider gap

        if (isInside(mouseX, mouseY, x + 4f, rowY, DROP_W - 8f, ADD_ROW_H)) {
            open = false
            onAddAccountClicked?.invoke()
            return true
        }

        return true
    }

    private fun drawHead(
        nvg: NanoVGManager,
        acc: SavedAccount?,
        x: Float,
        y: Float,
        size: Float,
        alpha: Float,
    ) {
        val skin =
            try {
                val profile = mc.session.profile
                if (profile?.id != null) {
                    DefaultPlayerSkin.getDefaultSkin(profile.id)
                } else {
                    DefaultPlayerSkin.getDefaultSkinLegacy()
                }
            } catch (_: Exception) {
                DefaultPlayerSkin.getDefaultSkinLegacy()
            }
        nvg.drawPlayerHead(skin, x, y, size, size, 3f, alpha)
    }

    private fun switchAccount(id: String) {
        val mgr = Shindo.getInstance().getAccountManager()
        Thread {
            try {
                val acc = mgr.refreshIfNeeded(id)
                mgr.switchAccount(acc.id)
            } catch (_: Exception) {
                mgr.switchAccount(id)
            } finally {
                refreshCache()
            }
        }.also { it.isDaemon = true }.start()
    }

    private fun refreshCacheIfNeeded(nvg: NanoVGManager) {
        if (System.currentTimeMillis() - lastCacheTick > 2000L) refreshCache()
    }

    private fun refreshCache() {
        cachedAccounts = Shindo.getInstance().getAccountManager().getAccounts()
        lastCacheTick = System.currentTimeMillis()
    }
}
