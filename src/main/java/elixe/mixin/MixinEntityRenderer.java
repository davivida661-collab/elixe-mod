package elixe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import elixe.Elixe;
import elixe.events.OnBlindnessEvent;
import elixe.events.OnGetMouseOverEvent;
import elixe.events.OnNauseaScaleEvent;
import elixe.events.OnOrientCameraEvent;
import elixe.events.OnPlayerAnglesEvent;
import elixe.events.OnRender2DEvent;
import elixe.events.OnRender3DEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

/**
 * Hooks limpos do EntityRenderer.
 *
 * NAO incluido (lote de verificacao in-game, cancelam bloco no meio do metodo / usam locals):
 * OnOrientCameraEvent (orientCamera L700), OnNauseaScaleEvent (setupCameraTransform L833),
 * OnPlayerAnglesEvent (updateCameraAndRender L1185 — AimAssist, value-modify + locals),
 * OnBlindnessEvent (setupFog L2245).
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    // OnGetMouseOverEvent — Reach. Cancela getMouseOver inteiro (return). MCP-919 L434.
    @Inject(method = "getMouseOver", at = @At("HEAD"), cancellable = true)
    private void elixe$onGetMouseOver(float partialTicks, CallbackInfo ci) {
        OnGetMouseOverEvent event = new OnGetMouseOverEvent(partialTicks, (EntityRenderer) (Object) this);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    // OnRender3DEvent — ESP/Skeletal. Logo antes da secao "hand" em renderWorldPass. MCP-919 L1712.
    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE_STRING",
            target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = "ldc=hand"))
    private void elixe$onRender3D(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        Elixe.INSTANCE.EVENT_BUS.post(new OnRender3DEvent(partialTicks));
    }

    // OnPlayerAnglesEvent — AimAssist. Redireciona o setAngles do ramo NAO-smoothCamera (else de
    // `if (smoothCamera)`) em updateCameraAndRender. ordinal 1 (0=smoothCamera branch). MCP-919 L1184.
    @Redirect(method = "updateCameraAndRender", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/client/entity/EntityPlayerSP;setAngles(FF)V"))
    private void elixe$onPlayerAngles(EntityPlayerSP player, float yaw, float pitchTimesI) {
        int i = Minecraft.getMinecraft().gameSettings.invertMouse ? -1 : 1;
        float f3 = pitchTimesI / (float) i; // recupera o delta de pitch original (f3)
        OnPlayerAnglesEvent event = new OnPlayerAnglesEvent(yaw, f3);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        if (!event.isCancelled()) {
            player.setAngles(event.getYaw(), event.getPitch() * (float) i);
        }
    }

    // OnBlindnessEvent — Aesthetics. Post puro dentro do branch blindness de setupFog
    // (âncora no getActivePotionEffect, único do método). MCP-919 L2245.
    @Inject(method = "setupFog", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/entity/EntityLivingBase;getActivePotionEffect(Lnet/minecraft/potion/Potion;)Lnet/minecraft/potion/PotionEffect;"))
    private void elixe$onSetupFogBlindness(int startCoords, float partialTicks, CallbackInfo ci) {
        Elixe.INSTANCE.EVENT_BUS.post(new OnBlindnessEvent());
    }

    // OnNauseaScaleEvent — Aesthetics. setupCameraTransform: cancela => zera f1 (FSTORE #1) e pula
    // o bloco `if (f1 > 0)` da distorção de portal (método continua). MCP-919 L832.
    @ModifyVariable(method = "setupCameraTransform", at = @At("STORE"), ordinal = 1)
    private float elixe$onNauseaScale(float f1) {
        OnNauseaScaleEvent event = new OnNauseaScaleEvent();
        Elixe.INSTANCE.EVENT_BUS.post(event);
        return event.isCancelled() ? 0.0F : f1;
    }

    // ===== OnOrientCameraEvent — Camera. orientCamera: posta no início do branch third-person
    // (Camera faz o próprio GL no post) e, se cancelado, neutraliza os translate/rotate vanilla do
    // branch (slice até getBlockAtEntityViewpoint; o tail Forge/translate(0,-f,0) sempre roda). MCP-919 L699.
    @Unique
    private boolean elixe$orientCamCancelled;

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void elixe$resetOrientCam(float partialTicks, CallbackInfo ci) {
        this.elixe$orientCamCancelled = false;
    }

    // Recalcula entity/d0/d1/d2 (em vez de LocalCapture, que falhava silencioso) — igual vanilla L611-615.
    @Inject(method = "orientCamera",
            at = @At(value = "FIELD", ordinal = 0,
                    target = "Lnet/minecraft/client/renderer/EntityRenderer;thirdPersonDistanceTemp:F"))
    private void elixe$postOrientCamera(float partialTicks, CallbackInfo ci) {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        float f = entity.getEyeHeight();
        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + f;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
        OnOrientCameraEvent event = new OnOrientCameraEvent(entity, d0, d1, d2, partialTicks);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        this.elixe$orientCamCancelled = event.isCancelled();
    }

    @Redirect(method = "orientCamera",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V"),
            slice = @Slice(
                    from = @At(value = "FIELD", ordinal = 0,
                            target = "Lnet/minecraft/client/renderer/EntityRenderer;thirdPersonDistanceTemp:F"),
                    to = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/Block;")))
    private void elixe$thirdPersonTranslate(float x, float y, float z) {
        if (!this.elixe$orientCamCancelled) {
            GlStateManager.translate(x, y, z);
        }
    }

    @Redirect(method = "orientCamera",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V"),
            slice = @Slice(
                    from = @At(value = "FIELD", ordinal = 0,
                            target = "Lnet/minecraft/client/renderer/EntityRenderer;thirdPersonDistanceTemp:F"),
                    to = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/Block;")))
    private void elixe$thirdPersonRotate(float angle, float x, float y, float z) {
        if (!this.elixe$orientCamCancelled) {
            GlStateManager.rotate(angle, x, y, z);
        }
    }

    // OnRender2DEvent — HUD / HealthLog. Injetado no CALL-SITE (logo após mc.ingameGUI.renderGameOverlay
    // em updateCameraAndRender) em vez de no GuiIngameForge — esse é classe do Forge e o refmap
    // qualificava renderGameOverlay como GuiIngame;func_175180_a, o que o Mixin rejeita em produção
    // (@Inject cross-classe). O INVOKE aqui tem owner GuiIngame (classe MCP) e resolve nos 2 ambientes.
    // O HUD usa GL11 raw; montamos estado 2D limpo antes e RESSINCRONIZAMOS o GlStateManager depois
    // (senão o cache desincroniza e o fundo translúcido de GUIs vira preto).
    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V"))
    private void elixe$onRender2D(float partialTicks, long nanoTime, CallbackInfo ci) {
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Elixe.INSTANCE.EVENT_BUS.post(new OnRender2DEvent(partialTicks));

        GlStateManager.disableBlend();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
