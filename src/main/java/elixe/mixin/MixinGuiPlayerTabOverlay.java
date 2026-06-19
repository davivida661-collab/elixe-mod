package elixe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import elixe.Elixe;
import elixe.events.OnTabPlayerNameEvent;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;

/**
 * OnTabPlayerNameEvent — NameProtect / Cosmetics (altera nome exibido na tablist).
 * O return de getPlayerName e exatamente a expressao que o MCP-919 passava ao evento. MCP-919 L54.
 */
@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void elixe$onTabName(NetworkPlayerInfo info, CallbackInfoReturnable<String> cir) {
        OnTabPlayerNameEvent event = new OnTabPlayerNameEvent(cir.getReturnValue(), info.getGameProfile().getId());
        Elixe.INSTANCE.EVENT_BUS.post(event);
        cir.setReturnValue(event.getName());
    }
}
