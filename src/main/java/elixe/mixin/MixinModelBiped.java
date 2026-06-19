package elixe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import elixe.Elixe;
import elixe.events.OnSetModelAngles;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;

/**
 * OnSetModelAngles — Skeletal (lê os rotateAngles do ModelBiped por entidade). O MCP-919 posta em
 * ModelBiped.render logo após setRotationAngles. ModelPlayer herda render() de ModelBiped (não
 * sobrescreve), então o player também dispara. MCP-919 ModelBiped L92.
 */
@Mixin(ModelBiped.class)
public class MixinModelBiped {

    @Inject(method = "render(Lnet/minecraft/entity/Entity;FFFFFF)V",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/client/model/ModelBiped;setRotationAngles(FFFFFFLnet/minecraft/entity/Entity;)V"))
    private void elixe$onSetModelAngles(Entity entityIn, float p2, float p3, float p4, float p5, float p6,
            float scale, CallbackInfo ci) {
        Elixe.INSTANCE.EVENT_BUS.post(new OnSetModelAngles(scale, (ModelBiped) (Object) this, entityIn));
    }
}
