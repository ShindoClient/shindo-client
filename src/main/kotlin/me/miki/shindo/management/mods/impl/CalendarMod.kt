package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import java.util.*

class CalendarMod : HUDMod(TranslateText.CALENDAR, TranslateText.CALENDAR_DESCRIPTION, Shinconic.MOD_CALENDAR) {
    private var calendarHeight = 0

    @EventTarget
    fun onRender2D(event: EventNVG?) {
        val calendar = Calendar.getInstance()

        val currentColor = Shindo.getInstance().getColorManager().getCurrentColor()

        val dayOfWeek = arrayOf<String?>("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
        var offsetX = 0f
        var offsetY = 0f
        var index = 1
        var weekIndex = 0

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val firstDayCalendar = calendar.clone() as Calendar

        firstDayCalendar.set(year, month, 1)

        this.drawBackground(100f, calendarHeight.toFloat())
        this.drawText(getMonthByNumber(month) + " " + year, 6f, 6f, 11f, getHudFont(2))

        for (s in dayOfWeek) {
            this.drawText(s!!, 6 + offsetX, 22f, 6.5f, getHudFont(2))

            offsetX += 13.4.toFloat()
        }

        offsetX = 0f
        index = firstDayCalendar.get(Calendar.DAY_OF_WEEK)
        offsetX = (index - 1) * 13.4f

        for (i in 1..maxDay) {
            if (i == day) {
                this.drawRoundedRect(4.5f + offsetX, 30.5f + offsetY, 10f, 10f, 10f / 2)
            }

            this.drawCenteredText(
                i.toString(),
                10 + offsetX,
                33 + offsetY,
                6f,
                getHudFont(1),
                if (i == day) currentColor.getInterpolateColor() else this.getFontColor(),
            )

            offsetX += 13.4.toFloat()

            if (index % 7 == 0 && i != maxDay) {
                offsetY += 13.4f
                offsetX = 0f
                weekIndex++
            }

            index++
        }

        calendarHeight = if (weekIndex < 5) 97 else 110

        this.setWidth(100)
        this.setHeight(if (weekIndex < 5) 97 else 110)
    }

    private fun getMonthByNumber(month: Int): String {
        when (month) {
            0 -> return "January"
            1 -> return "February"
            2 -> return "March"
            3 -> return "April"
            4 -> return "May"
            5 -> return "June"
            6 -> return "July"
            7 -> return "August"
            8 -> return "September"
            9 -> return "October"
            10 -> return "November"
            11 -> return "December"
        }

        return "null"
    }
}
