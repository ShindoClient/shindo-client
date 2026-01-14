package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventReceivePacket
import me.miki.shindo.management.event.impl.EventSendChat
import me.miki.shindo.management.event.impl.EventSendPacket
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.mods.impl.hypixel.HypixelGameMode
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.ColorUtils.removeColorCode
import me.miki.shindo.utils.Multithreading.schedule
import me.miki.shindo.utils.ServerUtils.isHypixel
import me.miki.shindo.utils.TimerUtils
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.network.play.server.S45PacketTitle
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class HypixelMod :
    Mod(TranslateText.HYPIXEL, TranslateText.HYPIXEL_DESCRIPTION, ModCategory.OTHER, LegacyIcon.MOD_HYPIXEL, "hytill") {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.AUTO_GG)
    private val autoggSetting = false

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.AUTO_GG_DELAY,
        min = 0.0,
        max = 5.0,
        current = 3.0,
        step = 1.0
    )
    private val autoggDelaySetting = 3

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.AUTO_GL)
    private val autoglSetting = false

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.AUTO_GL_DELAY,
        min = 0.0,
        max = 5.0,
        current = 1.0,
        step = 1.0
    )
    private val autoglDelaySetting = 1

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.AUTO_PLAY)
    private val autoPlaySetting = false

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.AUTO_PLAY_DELAY,
        min = 0.0,
        max = 5.0,
        current = 3.0,
        step = 1.0
    )
    private val autoPlayDelaySetting = 3

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.AUTO_TIP)
    private val autoTipSetting = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ANTI_L)
    private val antiLSetting = false

    private val tipTimer = TimerUtils()

    private var currentMode: HypixelGameMode? = null

    init {
        instance = this
    }

    public override fun setup() {
        currentMode = HypixelGameMode.SKYWARS_SOLO_NORMAL
    }

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        if (!isHypixel()) {
            tipTimer.reset()
            return
        }

        val scoreboard = mc.theWorld.getScoreboard()

        if (scoreboard != null && scoreboard.getObjectiveInDisplaySlot(1) != null) {
            val title = removeColorCode(scoreboard.getObjectiveInDisplaySlot(1).getDisplayName())

            if (title.contains("TNT RUN")) {
                currentMode = HypixelGameMode.TNT_RUN
            }

            if (title.contains("BOW SPLEEF")) {
                currentMode = HypixelGameMode.BOW_SPLEEF
            }

            if (title.contains("PVP RUN")) {
                currentMode = HypixelGameMode.PVP_RUN
            }

            if (title.contains("TNT TAG")) {
                currentMode = HypixelGameMode.TNT_TAG
            }

            if (title.contains("TNT WIZARDS")) {
                currentMode = HypixelGameMode.TNT_WIZARDS
            }
        }

        if (autoTipSetting) {
            if (tipTimer.delay(1200000)) {
                mc.thePlayer.sendChatMessage("/tip all")
                tipTimer.reset()
            }
        } else {
            tipTimer.reset()
        }
    }

    @EventTarget
    fun onSentChat(event: EventSendChat) {
        if (!isHypixel()) {
            return
        }

        val message = event.getMessage()

        if (message.startsWith("/play")) {
            val mode: HypixelGameMode? = HypixelGameMode.Companion.getModeByCommand(message)

            if (mode != null) {
                currentMode = mode
            }
        }
    }

    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        if (!isHypixel()) {
            return
        }

        if (event.getPacket() is S2FPacketSetSlot) {
            val slotPacket = event.getPacket() as S2FPacketSetSlot
            val stack = slotPacket.func_149174_e()

            if (stack != null && stack.getItem() == Items.paper &&
                currentMode != null &&
                (HypixelGameMode.isBedwars(currentMode!!) || HypixelGameMode.isTntGames(currentMode!!))
            ) {
                sendNextGame()
                return
            }
        }

        if (event.getPacket() is S02PacketChat) {
            val chatPacket = event.getPacket() as S02PacketChat
            val chatMessage = chatPacket.getChatComponent().getUnformattedText()

            if (antiLSetting) {
                val regex = Pattern.compile(".*\\b[Ll]+\\b.*")
                val matcher = regex.matcher(chatMessage)

                event.setCancelled(matcher.find())
            }

            if (autoglSetting && chatMessage.contains("The game starts in 5")) {
                schedule(Runnable {
                    mc.thePlayer.sendChatMessage("/achat gl")
                }, autoglDelaySetting.toLong(), TimeUnit.SECONDS)
            }
        }

        if (event.getPacket() is S45PacketTitle) {
            val titlePacket = event.getPacket() as S45PacketTitle

            if (titlePacket.getMessage() != null) {
                val title = titlePacket.getMessage().getFormattedText()

                if (autoggSetting && title.startsWith("\u00a76\u00a7l") && title.endsWith("\u00a7r")) {
                    schedule(Runnable {
                        mc.thePlayer.sendChatMessage("/achat gg")
                    }, autoggDelaySetting.toLong(), TimeUnit.SECONDS)
                }

                if (title.startsWith("\u00a76\u00a7l") && title.endsWith("\u00a7r") || title.startsWith("\u00a7c\u00a7lY") && title.endsWith(
                        "\u00a7r"
                    )
                ) {
                    sendNextGame()
                }
            }
        }
    }

    @EventTarget
    fun onSendPacket(event: EventSendPacket) {
        if (!isHypixel()) {
            return
        }

        if (event.getPacket() is C0EPacketClickWindow) {
            val packet = event.getPacket() as C0EPacketClickWindow
            val itemname: String

            if (packet.getClickedItem() == null) {
                return
            }

            itemname = packet.getClickedItem().getDisplayName()

            if (packet.getClickedItem().getDisplayName().startsWith("\u00a7a")) {
                val itemID = Item.getIdFromItem(packet.getClickedItem().getItem())

                if (itemID == 381 || itemID == 368) {
                    if (itemname.contains("SkyWars")) {
                        if (itemname.contains("Doubles")) {
                            if (itemname.contains("Normal")) {
                                currentMode = HypixelGameMode.SKYWARS_DOUBLES_NORMAL
                            } else if (itemname.contains("Insane")) {
                                currentMode = HypixelGameMode.SKYWARS_DOUBLES_INSANE
                            }
                        } else if (itemname.contains("Solo")) {
                            if (itemname.contains("Normal")) {
                                currentMode = HypixelGameMode.SKYWARS_SOLO_NORMAL
                            } else if (itemname.contains("Insane")) {
                                currentMode = HypixelGameMode.SKYWARS_SOLO_INSANE
                            }
                        }
                    }
                } else if (itemID == 355) {
                    if (itemname.contains("Bed Wars")) {
                        if (itemname.contains("4v4")) {
                            currentMode = HypixelGameMode.BEDWARS_4V4
                        } else if (itemname.contains("3v3")) {
                            currentMode = HypixelGameMode.BEDWARS_3V3
                        } else if (itemname.contains("Doubles")) {
                            currentMode = HypixelGameMode.BEDWARS_DOUBLES
                        } else if (itemname.contains("Solo")) {
                            currentMode = HypixelGameMode.BEDWARS_SOLO
                        }
                    }
                } else if (itemID == 397) {
                    if (itemname.contains("UHC Duel")) {
                        if (itemname.contains("1v1")) {
                            currentMode = HypixelGameMode.UHC_DUEL_1V1
                        } else if (itemname.contains("2v2")) {
                            currentMode = HypixelGameMode.UHC_DUEL_2V2
                        } else if (itemname.contains("4v4")) {
                            currentMode = HypixelGameMode.UHC_DUEL_4V4
                        } else if (itemname.contains("Player FFA")) {
                            currentMode = HypixelGameMode.UHC_DUEL_MEETUP
                        }
                    }
                }
            }
        }
    }

    private fun sendNextGame() {
        if (autoPlaySetting) {
            schedule(Runnable {
                val command = currentMode?.command
                if (command != null) {
                    mc.thePlayer.sendChatMessage(command)
                }
            }, autoPlayDelaySetting.toLong(), TimeUnit.SECONDS)
        }
    }

    companion object {
        @JvmField
        var instance: HypixelMod? = null
    }
}



