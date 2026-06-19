package elixe.mixin;

import elixe.duck.IModelHeight;
import net.minecraft.client.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reintroduz o campo custom {@code height} que o MCP-919 tinha no ModelRenderer
 * (usado pelo Skeletal). Captura a altura do box quando {@code addBox} é chamado.
 */
@Mixin(ModelRenderer.class)
public abstract class MixinModelRenderer implements IModelHeight {

    @Unique
    private float elixe$height;

    // addBox usado pelo ModelBiped (com scaleFactor)
    @Inject(method = "addBox(FFFIIIF)V", at = @At("TAIL"), require = 0)
    private void elixe$captureHeightScaled(float x, float y, float z, int w, int h, int d, float scale, CallbackInfo ci) {
        this.elixe$height = h;
    }

    @Override
    public float elixe$getHeight() {
        return this.elixe$height;
    }
}
