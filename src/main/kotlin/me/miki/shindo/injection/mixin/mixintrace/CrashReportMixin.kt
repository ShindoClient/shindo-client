package me.miki.shindo.injection.mixin.mixintrace

import me.miki.shindo.libs.mixintrace.MixinTraceUtil
import net.minecraft.crash.CrashReport
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.lang.StringBuilder


@Mixin(CrashReport::class)
abstract class CrashReportMixin {

    @Shadow
    private lateinit var stacktrace: Array<StackTraceElement>

    @Inject(
        method = ["getSectionsInStringBuilder"],
        at = [At("TAIL")]
    )
    private fun addMixinTraceDetails(sb: StringBuilder, ci: CallbackInfo) {
        MixinTraceUtil.addMixinInfoToCrashReport(sb, stacktrace)
    }
}
