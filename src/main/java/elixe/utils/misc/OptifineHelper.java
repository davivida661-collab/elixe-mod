package elixe.utils.misc;

import java.lang.reflect.Method;

/**
 * Ponte <b>opcional</b> para o OptiFine via reflection.
 *
 * <p>O Elixe (como mod) <b>não embute</b> o OptiFine. Se o usuário instalar o
 * mod do OptiFine, estas chamadas passam a ter efeito; caso contrário degradam
 * graciosamente para {@code false} / no-op. Assim o mod compila e roda sem o
 * OptiFine presente.</p>
 */
public final class OptifineHelper {

    private static final boolean PRESENT;

    private static Method mIsShaders;          // net.minecraft.src.Config#isShaders()
    private static Method mIsSkipRenderHand;   // net.optifine.shaders.Shaders#isSkipRenderHand()
    private static Method mSetEntityColor;     // net.optifine.shaders.Shaders#setEntityColor(F,F,F,F)
    private static Method mEmissiveIsActive;   // net.optifine.EmissiveTextures#isActive()
    private static Method mEmissiveBeginRender;// net.optifine.EmissiveTextures#beginRender()

    static {
        boolean present = false;
        try {
            Class<?> config = Class.forName("net.minecraft.src.Config");
            mIsShaders = config.getMethod("isShaders");

            Class<?> shaders = Class.forName("net.optifine.shaders.Shaders");
            mIsSkipRenderHand = shaders.getMethod("isSkipRenderHand");
            mSetEntityColor = shaders.getMethod("setEntityColor",
                    float.class, float.class, float.class, float.class);

            Class<?> emissive = Class.forName("net.optifine.EmissiveTextures");
            mEmissiveIsActive = emissive.getMethod("isActive");
            mEmissiveBeginRender = emissive.getMethod("beginRender");

            present = true;
        } catch (Throwable t) {
            present = false; // OptiFine ausente — tudo vira no-op
        }
        PRESENT = present;
    }

    private OptifineHelper() {}

    /** True somente se o OptiFine estiver carregado. */
    public static boolean isPresent() {
        return PRESENT;
    }

    public static boolean isShaders() {
        if (!PRESENT) return false;
        try {
            return (Boolean) mIsShaders.invoke(null);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isSkipRenderHand() {
        if (!PRESENT) return false;
        try {
            return (Boolean) mIsSkipRenderHand.invoke(null);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isEmissiveActive() {
        if (!PRESENT) return false;
        try {
            return (Boolean) mEmissiveIsActive.invoke(null);
        } catch (Throwable t) {
            return false;
        }
    }

    public static void emissiveBeginRender() {
        if (!PRESENT) return;
        try {
            mEmissiveBeginRender.invoke(null);
        } catch (Throwable t) {
            // ignore
        }
    }

    public static void setEntityColor(float r, float g, float b, float a) {
        if (!PRESENT) return;
        try {
            mSetEntityColor.invoke(null, r, g, b, a);
        } catch (Throwable t) {
            // ignore
        }
    }
}
