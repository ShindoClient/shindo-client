package me.miki.shindo.injection.mixin.mixintrace;

import me.miki.shindo.libs.mixintrace.MixinTraceUtil;
import net.minecraft.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
abstract class CrashReportMixin {
    @Shadow
    private StackTraceElement[] stacktrace;

    @Inject(
            method = "getSectionsInStringBuilder",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/crash/CrashReport;theReportCategory:Lnet/minecraft/crash/CrashReportCategory;",
                    shift = At.Shift.BEFORE)
    )
    private void mixintrace$addMixinTraceDetails(StringBuilder sb, CallbackInfo ci) {
        MixinTraceUtil.addMixinInfoToCrashReport(sb, stacktrace);
    }
}