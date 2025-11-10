package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventTick;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.impl.mechibes.SoundKey;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.RandomUtils;
import me.miki.shindo.utils.Sound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.HashMap;
import java.util.Map;

public class MechvibesMod extends Mod {

    private final Sound mouseLeftSound = new Sound();
    private final Sound mouseRightSound = new Sound();

    private final HashMap<Integer, SoundKey> keyMap = new HashMap<Integer, SoundKey>();

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.KEYBOARD, category = "Keyboard")
    private boolean keyboardEnabled = true;

    @Property(type = PropertyType.COMBO, translate = TranslateText.TYPE, category = "Keyboard")
    private KeyType keyType = KeyType.NK_CREAM;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.KEYBOARD_VOLUME, category = "Keyboard", min = 0, max = 1, step = 0.05, current = 0.5)
    private double keyboardVolume = 0.5;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.MOUSE, category = "Mouse")
    private boolean mouseEnabled = true;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.MOUSE_VOLUME, category = "Mouse", min = 0, max = 1, step = 0.05, current = 0.5)
    private double mouseVolume = 0.5;
    private float tempKeyboardVolume;
    private String tempKeyboardMode;
    private float tempMouseVolume;
    private boolean mouseLeftPress, mouseRightPress;
    private boolean loaded;

    public MechvibesMod() {
        super(TranslateText.MECHVIBES, TranslateText.MECHVIBES_DESCRIPTION, ModCategory.OTHER);
    }

    @Override
    public void setup() {
        loaded = false;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        loadKeyboardSounds(keyType.getResourceFolder());
        loadMouseSounds();
    }

    @EventTarget
    public void onTick(EventTick event) {

        if (loaded) {

            String mode = keyType.getResourceFolder();

            if (!mode.equals(tempKeyboardMode)) {
                tempKeyboardMode = mode;
                loadKeyboardSounds(mode);
            }

            float currentKeyboardVolume = (float) keyboardVolume;
            if (Float.compare(tempKeyboardVolume, currentKeyboardVolume) != 0) {
                tempKeyboardVolume = currentKeyboardVolume;
                for (SoundKey key : keyMap.values()) {
                    key.setVolume(tempKeyboardVolume);
                }
            }

            float currentMouseVolume = (float) mouseVolume;
            if (Float.compare(tempMouseVolume, currentMouseVolume) != 0) {
                tempMouseVolume = currentMouseVolume;
                mouseLeftSound.setVolume(tempMouseVolume);
                mouseRightSound.setVolume(tempMouseVolume);
            }

            for (Map.Entry<Integer, SoundKey> entry : keyMap.entrySet()) {
                SoundKey key = entry.getValue();
                if (key.isPressed() && !Keyboard.isKeyDown(entry.getKey())) {
                    key.setPressed(false);
                }
            }

            if (keyboardEnabled) {
                for (int keyCode = 0; keyCode < 256; keyCode++) {
                    if (!Keyboard.isKeyDown(keyCode)) {
                        continue;
                    }
                    SoundKey key = keyMap.get(keyCode);
                    if (key == null) {
                        continue;
                    }
                    if (!key.isPressed()) {
                        key.play();
                        key.setPressed(true);
                    }
                }
            }

            if (mouseEnabled) {

                if (Mouse.isButtonDown(0) && !mouseLeftPress) {
                    mouseLeftPress = true;
                    mouseLeftSound.play();
                }

                if (!Mouse.isButtonDown(0) && mouseLeftPress) {
                    mouseLeftPress = false;
                }

                if (Mouse.isButtonDown(1) && !mouseRightPress) {
                    mouseRightPress = true;
                    mouseRightSound.play();
                }

                if (!Mouse.isButtonDown(1) && mouseRightPress) {
                    mouseRightPress = false;
                }
            }
        }
    }

    private void loadKeyboardSounds(String type) {

        Multithreading.runAsync(() -> {
            for (int keyCode = 0; keyCode < 256; keyCode++) {

                if (keyCode == Keyboard.KEY_TAB) {
                    keyMap.put(Keyboard.KEY_TAB, new SoundKey(type, "tab"));
                    continue;
                }

                if (keyCode == 14) {
                    keyMap.put(14, new SoundKey(type, "backspace"));
                    continue;
                }

                if (keyCode == 58) {
                    keyMap.put(58, new SoundKey(type, "capslock"));
                    continue;
                }

                if (keyCode == 28) {
                    keyMap.put(28, new SoundKey(type, "enter"));
                    continue;
                }

                if (keyCode == Keyboard.KEY_SPACE) {
                    keyMap.put(Keyboard.KEY_SPACE, new SoundKey(type, "space"));
                    continue;
                }

                if (keyCode == Keyboard.KEY_LSHIFT) {
                    keyMap.put(Keyboard.KEY_LSHIFT, new SoundKey(type, "shift"));
                    continue;
                }

                if (keyCode == Keyboard.KEY_RSHIFT) {
                    keyMap.put(Keyboard.KEY_RSHIFT, new SoundKey(type, "shift"));
                    continue;
                }

                keyMap.put(keyCode, new SoundKey(type, String.valueOf(RandomUtils.getRandomInt(1, 5))));
            }
        });
    }

    private void loadMouseSounds() {
        Multithreading.runAsync(() -> {
            try {
                mouseLeftSound.loadClip(new ResourceLocation("shindo/mechvibes/mouse.wav"));
                mouseRightSound.loadClip(new ResourceLocation("shindo/mechvibes/mouse.wav"));
            } catch (Exception e) {
            }
            mouseLeftSound.setVolume((float) mouseVolume);
            mouseRightSound.setVolume((float) mouseVolume);
            loaded = true;
        });
    }

    private enum KeyType implements PropertyEnum {
        NK_CREAM("nk_cream", TranslateText.NK_CREAM),
        MX_BLUE("mx_blue", TranslateText.MX_BLUE),
        MX_SILVER("mx_silver", TranslateText.MX_SILVER),
        RAZER_GREEN("razer_green", TranslateText.RAZER_GREEN),
        HYPERX_AQUA("hyperx_aqua", TranslateText.HYPERX_AQUA),
        MX_BLACK("mx_black", TranslateText.MX_BLACK),
        TOPRE_PURPLE("topre_purple", TranslateText.TOPRE_PURPLE);

        private final String resourceFolder;
        private final TranslateText translate;

        KeyType(String resourceFolder, TranslateText translate) {
            this.resourceFolder = resourceFolder;
            this.translate = translate;
        }

        String getResourceFolder() {
            return resourceFolder;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}
