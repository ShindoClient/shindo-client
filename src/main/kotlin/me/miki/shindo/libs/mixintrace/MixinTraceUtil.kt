package me.miki.shindo.libs.mixintrace

import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import org.spongepowered.asm.mixin.transformer.ClassInfo
import java.util.function.Consumer

object MixinTraceUtil {
    /**
     * Appends mixin information about classes in the stacktrace to the crash report StringBuilder.
     * @param sb The crash report StringBuilder.
     * @param stackTrace the crash report's stackTrace.
     */
    @JvmStatic
    fun addMixinInfoToCrashReport(
        sb: StringBuilder,
        stackTrace: Array<StackTraceElement>?,
    ) {
        if (stackTrace != null && stackTrace.isNotEmpty()) {
            sb.append("-- Mixins affecting classes in stacktrace --\n")

            // Recursion exists, so stackTrace may have multiple times the same class
            val classNames: MutableSet<String> = HashSet()
            for (ste in stackTrace) {
                classNames.add(ste.className)
            }
            var found = false
            for (className in classNames) {
                val infoSet = getMixinInfoFromClass(className)
                if (infoSet == null) {
                    sb.append("Failed to get Mixin metadata\n\n")
                    return
                }
                if (infoSet.isEmpty()) continue
                found = true
                sb.append(className).append(":")
                infoSet.forEach(
                    Consumer { info: IMixinInfo ->
                        sb.append(
                            stringifyMixinInfo(
                                info,
                            ),
                        )
                    },
                )
                sb.append("\n")
            }
            if (!found) sb.append("None found\n")
            sb.append("\n")
        }
    }

    /**
     * Returns a String containing relevant information for the provided mixin information.
     * @param info The mixin information provided.
     * @return The string containing relevant bits of the mixin information.
     */
    private fun stringifyMixinInfo(info: IMixinInfo): String =
        """
	${info.className} (${info.config.name})"""

    /**
     * Fetching mixin information for a given class name. Returns null in case the fetching failed, you need to handle that case.
     * @param className The name of a class. Example: "com.example.SomeClass".
     * @return null in case fetching mixin info from class name failed, and the set of mixin info otherwise.
     */
    private fun getMixinInfoFromClass(className: String): Set<IMixinInfo>? {
        val classInfo = ClassInfo.forName(className) ?: return HashSet()
        val infoSet: Set<IMixinInfo>
        return try {
            // There is a getter for mixins, but it is protected, so we have to use reflection to get its content
            // There is also getAppliedMixins but for some reasons it's always empty even when the mixins are very much
            // applied, so reflection it is
            val mixinsField = ClassInfo::class.java.getDeclaredField("mixins")
            mixinsField.isAccessible = true
            infoSet = mixinsField[classInfo] as Set<IMixinInfo>
            infoSet
        } catch (e: Exception) {
            null
        }
    }
}