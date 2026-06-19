package elixe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import elixe.Elixe;
import elixe.events.OnRenderEntityEvent;
import elixe.events.OnRenderLayersEvent;
import elixe.events.OnRenderNameEvent;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;

/**
 * Hooks cancelaveis limpos do RendererLivingEntity (todos os args sao params do metodo alvo).
 *
 * NAO incluido (lote de verificacao in-game): OnRenderEntityEvent — Chams. Sao 4 posts do MESMO
 * evento com setState(0..3) ao longo de doRender, com didAlreadyRender() pulando o render vanilla
 * e captura de locais (f6,f5,...). Migracao complexa, fica separada.
 */
@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity {

    // OnRenderLayersEvent — OldAnimations. Cancela renderLayers inteiro. MCP-919 L536.
    @Inject(method = "renderLayers(Lnet/minecraft/entity/EntityLivingBase;FFFFFFF)V",
            at = @At("HEAD"), cancellable = true)
    private void elixe$onRenderLayers(EntityLivingBase entity, float p2, float p3, float partialTicks,
            float p5, float p6, float p7, float p8, CallbackInfo ci) {
        OnRenderLayersEvent event = new OnRenderLayersEvent(
                (RendererLivingEntity) (Object) this, entity, partialTicks, p2, p3, p5, p6, p7, p8);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    // OnRenderNameEvent — ESP (nametags custom). Cancela renderName inteiro. MCP-919 L604.
    @Inject(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V",
            at = @At("HEAD"), cancellable = true)
    private void elixe$onRenderName(EntityLivingBase entity, double x, double y, double z, CallbackInfo ci) {
        OnRenderNameEvent event = new OnRenderNameEvent(entity);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    // ===== OnRenderEntityEvent (Chams) — UM evento reusado em 4 estágios de doRender (setState 0..3).
    // Guardado num @Unique para atravessar as fases. NÃO reentrante (ok no 1.8.9 vanilla). MCP-919 L117-285.
    @Unique
    private OnRenderEntityEvent elixe$renderEntityEvent;

    // state 0 — HEAD.
    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("HEAD"))
    private void elixe$onRenderEntityState0(EntityLivingBase entity, double x, double y, double z,
            float entityYaw, float partialTicks, CallbackInfo ci) {
        OnRenderEntityEvent event = new OnRenderEntityEvent((RendererLivingEntity) (Object) this, entity);
        event.setState(0);
        this.elixe$renderEntityEvent = event;
        Elixe.INSTANCE.EVENT_BUS.post(event);
    }

    // state 1 — após setRotationAngles (L160), antes do render do modelo. Captura locais f6/f5/f8/f2/f7.
    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/client/model/ModelBase;setRotationAngles(FFFFFFLnet/minecraft/entity/Entity;)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void elixe$onRenderEntityState1(EntityLivingBase entity, double x, double y, double z,
            float entityYaw, float partialTicks, CallbackInfo ci,
            boolean shouldSit, float f, float f1, float f2, float f7, float f8, float f4, float f5, float f6) {
        OnRenderEntityEvent event = new OnRenderEntityEvent(
                (RendererLivingEntity) (Object) this, entity, f6, f5, f8, f2, f7, 0.0625F, partialTicks);
        event.setState(1);
        this.elixe$renderEntityEvent = event;
        Elixe.INSTANCE.EVENT_BUS.post(event);
    }

    // gate — pula o renderModel vanilla (else-branch, ordinal 1) se o módulo já renderizou (didAlreadyRender).
    @Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "INVOKE", ordinal = 1,
                    target = "Lnet/minecraft/client/renderer/entity/RendererLivingEntity;renderModel(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)V"))
    private void elixe$gateRenderModel(RendererLivingEntity self, EntityLivingBase entity,
            float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
        OnRenderEntityEvent event = this.elixe$renderEntityEvent;
        if (event != null && event.didAlreadyRender()) {
            return;
        }
        // renderModel é protected → chama via o @Invoker existente (AccessorRendererLivingEntity).
        ((AccessorRendererLivingEntity) (Object) self).invokeRenderModel(
                entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
    }

    // state 2 — após depthMask(true) (L182), antes de renderLayers.
    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/client/renderer/GlStateManager;depthMask(Z)V"))
    private void elixe$onRenderEntityState2(EntityLivingBase entity, double x, double y, double z,
            float entityYaw, float partialTicks, CallbackInfo ci) {
        OnRenderEntityEvent event = this.elixe$renderEntityEvent;
        if (event != null) {
            event.setState(2);
            Elixe.INSTANCE.EVENT_BUS.post(event);
        }
    }

    // state 3 — RETURN (cobre o early-return do Forge Pre + fim normal, igual ao MCP-919).
    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("RETURN"))
    private void elixe$onRenderEntityState3(EntityLivingBase entity, double x, double y, double z,
            float entityYaw, float partialTicks, CallbackInfo ci) {
        OnRenderEntityEvent event = this.elixe$renderEntityEvent;
        if (event != null) {
            event.setState(3);
            Elixe.INSTANCE.EVENT_BUS.post(event);
        }
    }
}
