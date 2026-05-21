package me.miki.shindo.management.event

import java.lang.reflect.Method

@Suppress("UNUSED")
class EventManager {
    @Suppress("ktlint:standard:property-naming")
    private val REGISTRY_MAP: MutableMap<Class<*>, ArrayHelper<Data>> = HashMap()

    fun register(o: Any) {
        for (method in o.javaClass.declaredMethods) {
            if (!isMethodBad(method)) {
                register(method, o)
            }
        }
    }

    fun register(
        o: Any,
        clazz: Class<out Event>,
    ) {
        for (method in o.javaClass.declaredMethods) {
            if (!isMethodBad(method, clazz)) {
                register(method, o)
            }
        }
    }

    private fun register(
        method: Method,
        o: Any,
    ) {
        val clazz = method.parameterTypes[0]
        val methodData = Data(o, method, method.getAnnotation(EventTarget::class.java).value)

        if (!methodData.target.isAccessible) {
            methodData.target.isAccessible = true
        }

        if (REGISTRY_MAP.containsKey(clazz)) {
            val helper = REGISTRY_MAP[clazz]
            if (helper != null && !helper.contains(methodData)) {
                helper.add(methodData)
                sortListValue(clazz)
            }
        } else {
            val helper = ArrayHelper<Data>()
            helper.add(methodData)
            REGISTRY_MAP[clazz] = helper
        }
    }

    fun unregister(o: Any) {
        for (flexibalArray in REGISTRY_MAP.values) {
            for (methodData in flexibalArray) {
                if (methodData.source == o) {
                    flexibalArray.remove(methodData)
                }
            }
        }

        cleanMap(true)
    }

    fun unregister(
        o: Any,
        clazz: Class<out Event>,
    ) {
        if (REGISTRY_MAP.containsKey(clazz)) {
            val helper = REGISTRY_MAP[clazz]
            if (helper != null) {
                for (methodData in helper) {
                    if (methodData.source == o) {
                        helper.remove(methodData)
                    }
                }
            }

            cleanMap(true)
        }
    }

    fun cleanMap(b: Boolean) {
        val iterator = REGISTRY_MAP.entries.iterator()

        while (iterator.hasNext()) {
            if (!b || iterator.next().value.isEmpty()) {
                iterator.remove()
            }
        }
    }

    fun removeEnty(clazz: Class<out Event>) {
        val iterator = REGISTRY_MAP.entries.iterator()

        while (iterator.hasNext()) {
            if (iterator.next().key == clazz) {
                iterator.remove()
                break
            }
        }
    }

    private fun sortListValue(clazz: Class<*>) {
        val flexibleArray = ArrayHelper<Data>()

        for (b in Priority.VALUE_ARRAY) {
            val helper = REGISTRY_MAP[clazz]
            if (helper != null) {
                for (methodData in helper) {
                    if (methodData.priority == b) {
                        flexibleArray.add(methodData)
                    }
                }
            }
        }

        REGISTRY_MAP[clazz] = flexibleArray
    }

    private fun isMethodBad(method: Method): Boolean =
        method.parameterTypes.size != 1 || !method.isAnnotationPresent(EventTarget::class.java)

    private fun isMethodBad(
        method: Method,
        clazz: Class<out Event>,
    ): Boolean = isMethodBad(method) || method.parameterTypes[0] == clazz

    fun get(clazz: Class<out Event>): ArrayHelper<Data>? = REGISTRY_MAP[clazz]

    fun getAny(clazz: Class<*>): ArrayHelper<Data>? = REGISTRY_MAP[clazz]

    fun shutdown() {
        REGISTRY_MAP.clear()
    }
}
