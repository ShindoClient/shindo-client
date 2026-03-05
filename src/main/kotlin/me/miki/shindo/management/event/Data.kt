package me.miki.shindo.management.event

import java.lang.reflect.Method

class Data(
    @JvmField val source: Any,
    @JvmField val target: Method,
    @JvmField val priority: Byte
)
