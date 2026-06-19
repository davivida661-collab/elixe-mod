package elixe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import elixe.Elixe;
import elixe.events.OnOverlayDrawEvent;
import elixe.events.OnScoreboardPlayerNameEvent;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;

/**
 * Hooks que ficam nos MÉTODOS do GuiIngame (declarados nele) — o GuiIngameForge os CHAMA herdados,
 * então funcionam, e o refmap mapeia same-class (sem o problema cross-classe do renderGameOverlay).
 * - OnScoreboardPlayerName (NameProtect): @ModifyArg no nome dentro de renderScoreboard.
 * - OnOverlayDraw (Aesthetics): HEAD cancelável de renderPumpkinOverlay (type 0) e renderScoreboard
 *   (type 1) — se o módulo der skip(), cancela o método (não renderiza). Equivale ao gating original.
 *
 * (OnRender2DEvent está no MixinEntityRenderer; OnDrawTitleEvent no MixinGuiIngameForge.renderTitle.)
 */
@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @ModifyArg(method = "renderScoreboard(Lnet/minecraft/scoreboard/ScoreObjective;Lnet/minecraft/client/gui/ScaledResolution;)V",
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I"),
            index = 0)
    private String elixe$onScoreboardPlayerName(String line) {
        OnScoreboardPlayerNameEvent event = new OnScoreboardPlayerNameEvent(line);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        return event.getLine();
    }

    @Inject(method = "renderPumpkinOverlay(Lnet/minecraft/client/gui/ScaledResolution;)V",
            at = @At("HEAD"), cancellable = true)
    private void elixe$onPumpkinOverlay(ScaledResolution res, CallbackInfo ci) {
        OnOverlayDrawEvent event = new OnOverlayDrawEvent(0);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        if (event.shouldSkip()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderScoreboard(Lnet/minecraft/scoreboard/ScoreObjective;Lnet/minecraft/client/gui/ScaledResolution;)V",
            at = @At("HEAD"), cancellable = true)
    private void elixe$onScoreboardOverlay(ScoreObjective objective, ScaledResolution res, CallbackInfo ci) {
        OnOverlayDrawEvent event = new OnOverlayDrawEvent(1);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        if (event.shouldSkip()) {
            ci.cancel();
        }
    }
}
