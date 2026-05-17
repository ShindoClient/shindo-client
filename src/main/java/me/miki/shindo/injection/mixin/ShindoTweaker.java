package me.miki.shindo.injection.mixin;

//import com.llamalad7.mixinextras.MixinExtrasBootstrap;

import me.miki.shindo.injection.transformer.LwjglTransformer;
import me.miki.shindo.logger.ShindoLogger;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ShindoTweaker implements ITweaker {

    public static boolean hasOptifine = false;
    private final List<String> launchArguments = new ArrayList<>();

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        try {
            Class.forName("optifine.Patcher");
            hasOptifine = true;
        } catch (ClassNotFoundException e) {
        }

        this.launchArguments.addAll(args);

        if (profile != null) {
            launchArguments.add("--version");
            launchArguments.add(profile);
        }

        if (assetsDir != null) {
            launchArguments.add("--assetsDir");
            launchArguments.add(assetsDir.getAbsolutePath());
        }

        if (gameDir != null) {
            launchArguments.add("--gameDir");
            launchArguments.add(gameDir.getAbsolutePath());
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {

        classLoader.registerTransformer(LwjglTransformer.class.getName());
        MixinBootstrap.init();

        MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();
        Mixins.addConfiguration("mixins.shindo.json");

        if (env.getObfuscationContext() == null) {
            env.setObfuscationContext("notch");
        }

        env.setSide(MixinEnvironment.Side.CLIENT);

        this.unlockLwjgl();
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return launchArguments.toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    private void unlockLwjgl() {
        try {
            Field classLoaderExceptionsField = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");
            classLoaderExceptionsField.setAccessible(true);

            Set<String> classLoaderExceptions = (Set<String>) classLoaderExceptionsField.get(Launch.classLoader);
            if (classLoaderExceptions == null || classLoaderExceptions.isEmpty()) {
                return;
            }

            Set<String> removed = new LinkedHashSet<>();
            for (String excludedPackage : new ArrayList<>(classLoaderExceptions)) {
                if (excludedPackage == null) {
                    continue;
                }

                if ("org.lwjgl.".equals(excludedPackage)
                        || excludedPackage.startsWith("me.miki.shindo.injection.mixin")
                        || excludedPackage.startsWith("me.miki.shindo.injection.interfaces")
                        || excludedPackage.startsWith("me.miki.shindo.injection.mixin.accessors")) {
                    if (classLoaderExceptions.remove(excludedPackage)) {
                        removed.add(excludedPackage);
                    }
                }
            }

            if (!removed.isEmpty() && Boolean.getBoolean("shindo.debug")) {
                ShindoLogger.info("[ShindoTweaker] Removed classloader exclusions: " + removed);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
    }

}
