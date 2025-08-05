package me.miki.shindo.utils.mouse;

public class MouseUtils {

    public static boolean isInside(int mouseX, int mouseY, double x, double y, double w, double h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
