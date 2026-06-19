package elixe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import elixe.Elixe;
import elixe.events.OnDrawTitleEvent;
import net.minecraftforge.client.GuiIngameForge;

/**
 * OnDrawTitleEvent — NameProtect (esconde o nick em titles). O GuiIngameForge tem o seu PRÓPRIO
 * renderTitle (não existe no GuiIngame), que desenha displayedTitle (drawString ordinal 0) e
 * displayedSubTitle (ordinal 1).
 *
 * renderTitle é método do Forge (não-MCP) → fica literal no refmap, sem o problema cross-classe do
 * renderGameOverlay (que é herdado/MCP). Não usamos @Shadow dos campos: o NameProtect processa título
 * e subtítulo de forma independente, então passamos "" no outro campo (replace em "" é no-op).
 */
@Mixin(GuiIngameForge.class)
public class MixinGuiIngameForge {

    @ModifyArg(method = "renderTitle", index = 0,
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;FFIZ)I"))
    private String elixe$onDrawTitle(String title) {
        OnDrawTitleEvent event = new OnDrawTitleEvent(title, "");
        Elixe.INSTANCE.EVENT_BUS.post(event);
        return event.getTitle();
    }

    @ModifyArg(method = "renderTitle", index = 0,
            at = @At(value = "INVOKE", ordinal = 1,
                    target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;FFIZ)I"))
    private String elixe$onDrawSubtitle(String subtitle) {
        OnDrawTitleEvent event = new OnDrawTitleEvent("", subtitle);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        return event.getSubtitle();
    }
}
