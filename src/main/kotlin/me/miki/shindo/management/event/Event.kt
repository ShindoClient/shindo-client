package me.miki.shindo.management.event

import me.miki.shindo.Shindo
import java.lang.reflect.InvocationTargetException

@Suppress("UNCHECKED_CAST")
abstract class Event {

    private var cancelled: Boolean = false

    companion object {
        @JvmStatic
        private fun call(event: Event) {
            val instance = Shindo.getInstance()
            val eventManager = instance.getEventManager()
            val invoked = HashSet<Data>()
            var c: Class<*>? = event.javaClass

            while (c != null) {
                val dataList = eventManager.get(c as Class<out Event>)
                if (dataList != null) {
                    for (data in dataList) {
                        if (invoked.add(data)) {
                            try {
                                data.target.invoke(data.source, event)
                            } catch (e: IllegalAccessException) {
                                e.printStackTrace()
                            } catch (e: InvocationTargetException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                for (iface in c.interfaces) {
                    val byInterface = eventManager.getAny(iface)
                    if (byInterface != null) {
                        for (data in byInterface) {
                            if (invoked.add(data)) {
                                try {
                                    data.target.invoke(data.source, event)
                                } catch (e: IllegalAccessException) {
                                    e.printStackTrace()
                                } catch (e: InvocationTargetException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }

                c = c.superclass
            }
        }
    }

    fun call(): Event {
        cancelled = false
        call(this)
        return this
    }

    fun isCancelled(): Boolean = cancelled

    fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }
}
