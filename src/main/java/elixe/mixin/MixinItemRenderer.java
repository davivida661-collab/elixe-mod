package elixe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import elixe.Elixe;
import elixe.events.OnFireFirstPersonEvent;
import elixe.events.OnRenderItemFirstPersonEvent;
import net.minecraft.client.renderer.ItemRenderer;

/**
 * Hooks de primeira pessoa do ItemRenderer (OldAnimations / Aesthetics).
 */
@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    // OnRenderItemFirstPersonEvent — OldAnimations. Cancela o render da mao. MCP-919 L338.
    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"), cancellable = true)
    private void elixe$onRenderItemFP(float partialTicks, CallbackInfo ci) {
        OnRenderItemFirstPersonEvent event = new OnRenderItemFirstPersonEvent((ItemRenderer) (Object) this, partialTicks);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    // OnFireFirstPersonEvent — Aesthetics. Substitui o offset Y -0.3F do fogo em 1a pessoa.
    // Posta por uso (2x no loop) mas retorna sempre o valor modificado. MCP-919 L538/L555.
    @ModifyConstant(method = "renderFireInFirstPerson", constant = @Constant(floatValue = -0.3F))
    private float elixe$onFireFP(float original) {
        OnFireFirstPersonEvent event = new OnFireFirstPersonEvent(original);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        return event.getHeight();
    }
}
