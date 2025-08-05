package me.miki.shindo.injection.mixin.mixins.lwjgl;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "org.lwjgl.opengl.LinuxKeycodes")
public interface MixinLinuxKeycodes {

    /**
     * Mapeia KeySyms do Linux para códigos de tecla do LWJGL.
     * Esta versão cobre teclas essenciais (WASD, números, setas, F1–F12, etc.)
     */
    @Invoker(value = "mapKeySymToLWJGLKeyCode", remap = false)
    static int mapKeySymToLWJGLKeyCode(long keysym) {

        // Letras minúsculas a-z
        if (keysym >= 'a' && keysym <= 'z') {
            return Keyboard.KEY_A + (int) (keysym - 'a'); // mapeia a->KEY_A, b->KEY_B...
        }

        // Letras maiúsculas A-Z
        if (keysym >= 'A' && keysym <= 'Z') {
            return Keyboard.KEY_A + (int) (keysym - 'A');
        }

        // Números 0-9
        if (keysym >= '0' && keysym <= '9') {
            return Keyboard.KEY_0 + (int) (keysym - '0');
        }

        // Teclas especiais do Linux (KeySyms do X11)
        switch ((int) keysym) {
            case 65293:
                return Keyboard.KEY_RETURN; // Enter
            case 65307:
                return Keyboard.KEY_ESCAPE; // ESC
            case 65288:
                return Keyboard.KEY_BACK;   // Backspace
            case 65535:
                return Keyboard.KEY_DELETE; // Delete
            case 65361:
                return Keyboard.KEY_LEFT;
            case 65362:
                return Keyboard.KEY_UP;
            case 65363:
                return Keyboard.KEY_RIGHT;
            case 65364:
                return Keyboard.KEY_DOWN;
            case 65365:
                return Keyboard.KEY_PRIOR;  // Page Up
            case 65366:
                return Keyboard.KEY_NEXT;   // Page Down
            case 65360:
                return Keyboard.KEY_HOME;
            case 65367:
                return Keyboard.KEY_END;
            case 65470:
                return Keyboard.KEY_F1;
            case 65471:
                return Keyboard.KEY_F2;
            case 65472:
                return Keyboard.KEY_F3;
            case 65473:
                return Keyboard.KEY_F4;
            case 65474:
                return Keyboard.KEY_F5;
            case 65475:
                return Keyboard.KEY_F6;
            case 65476:
                return Keyboard.KEY_F7;
            case 65477:
                return Keyboard.KEY_F8;
            case 65478:
                return Keyboard.KEY_F9;
            case 65479:
                return Keyboard.KEY_F10;
            case 65480:
                return Keyboard.KEY_F11;
            case 65481:
                return Keyboard.KEY_F12;

            // Keypad
            case 65450:
                return Keyboard.KEY_DECIMAL;
            case 65451:
                return Keyboard.KEY_ADD;
            case 65452:
                return Keyboard.KEY_DIVIDE;
            case 65453:
                return Keyboard.KEY_SUBTRACT;
            case 65454:
                return Keyboard.KEY_MULTIPLY;

            case 65455:
                return Keyboard.KEY_NUMPAD0;
            case 65456:
                return Keyboard.KEY_NUMPAD1;
            case 65457:
                return Keyboard.KEY_NUMPAD2;
            case 65458:
                return Keyboard.KEY_NUMPAD3;
            case 65459:
                return Keyboard.KEY_NUMPAD4;
            case 65460:
                return Keyboard.KEY_NUMPAD5;
            case 65461:
                return Keyboard.KEY_NUMPAD6;
            case 65462:
                return Keyboard.KEY_NUMPAD7;
            case 65463:
                return Keyboard.KEY_NUMPAD8;
            case 65464:
                return Keyboard.KEY_NUMPAD9;


            default:
                // Log para debug de teclas não mapeadas
                System.out.println("[LinuxKeyMapper] KeySym não mapeado: " + keysym);
                return Keyboard.KEY_NONE;
        }
    }
}