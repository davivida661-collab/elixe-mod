package elixe.utils.misc;

import net.minecraft.client.gui.FontRenderer;

/**
 * Reintroduz as sobrecargas custom de {@code drawString}/{@code drawStringWithShadow}
 * que o MCP-919 tinha no FontRenderer (cor por floats c / r,g,b + alpha). Em vez de
 * editar o FontRenderer vanilla, roteamos as chamadas do elixe por aqui. Inclui as
 * sobrecargas com cor {@code int} pra cobrir também as chamadas que já eram vanilla.
 */
public final class FontUtil {

    private FontUtil() {}

    private static int color(float r, float g, float b, float a) {
        return ((int) (a * 255f) & 0xFF) << 24
             | ((int) (r * 255f) & 0xFF) << 16
             | ((int) (g * 255f) & 0xFF) << 8
             |  (int) (b * 255f) & 0xFF;
    }

    // ---- drawStringWithShadow ----
    public static int drawStringWithShadow(FontRenderer fr, String text, float x, float y, int c) {
        return fr.drawStringWithShadow(text, x, y, c);
    }

    public static int drawStringWithShadow(FontRenderer fr, String text, float x, float y, float c, float a) {
        return fr.drawStringWithShadow(text, x, y, color(c, c, c, a));
    }

    public static int drawStringWithShadow(FontRenderer fr, String text, float x, float y, float r, float g, float b, float a) {
        return fr.drawStringWithShadow(text, x, y, color(r, g, b, a));
    }

    // ---- drawString (sem sombra; vanilla usa int x/y) ----
    public static int drawString(FontRenderer fr, String text, float x, float y, int c) {
        return fr.drawString(text, (int) x, (int) y, c);
    }

    public static int drawString(FontRenderer fr, String text, float x, float y, float c, float a) {
        return fr.drawString(text, (int) x, (int) y, color(c, c, c, a));
    }

    public static int drawString(FontRenderer fr, String text, float x, float y, float r, float g, float b, float a) {
        return fr.drawString(text, (int) x, (int) y, color(r, g, b, a));
    }
}
