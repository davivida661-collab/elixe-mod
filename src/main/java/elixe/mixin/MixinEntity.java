package elixe.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import elixe.Elixe;
import elixe.events.OnBrightnessEntityEvent;
import elixe.events.OnGetCollisionBorderEvent;
import elixe.events.OnGoingToFallEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;

/**
 * Hooks de value-modify do Entity + OnGoingToFallEvent (SafeWalk) no moveEntity.
 */
@Mixin(Entity.class)
public class MixinEntity {

    // OnBrightnessEntityEvent — Chams (fullbright). Modifica o retorno de getBrightnessForRender. MCP-919 L1273.
    @Inject(method = "getBrightnessForRender", at = @At("RETURN"), cancellable = true)
    private void elixe$onBrightnessForRender(float partialTicks, CallbackInfoReturnable<Integer> cir) {
        OnBrightnessEntityEvent event = new OnBrightnessEntityEvent((Entity) (Object) this, cir.getReturnValue());
        Elixe.INSTANCE.EVENT_BUS.post(event);
        cir.setReturnValue(event.getLight());
    }

    // OnGetCollisionBorderEvent — Hitbox. Substitui o retorno de getCollisionBorderSize (default 0.1F). MCP-919 L2069.
    @Inject(method = "getCollisionBorderSize", at = @At("HEAD"), cancellable = true)
    private void elixe$onCollisionBorderSize(CallbackInfoReturnable<Float> cir) {
        OnGetCollisionBorderEvent event = new OnGetCollisionBorderEvent(0.1F);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        cir.setReturnValue(event.getBorderSize());
    }

    // ===== OnGoingToFallEvent (SafeWalk) — Entity.moveEntity. Mesmo evento postado 2x (state 0 antes
    // do bloco sneak-edge, state 1 depois), guardado num @Unique. SafeWalk seta shouldBlock no state 0;
    // 2 @Redirect fingem flag = onGround && isSneaking && instanceof EntityPlayer (rodam o bloco mesmo
    // sem sneak/no ar). Um 3º @Redirect replica setWillFall(true) dentro dos loops. MCP-919 L636/L712.
    @Unique
    private OnGoingToFallEvent elixe$goingToFallEvent;

    // state 0 — antes dos reads de onGround/isSneaking (âncora: primeiro posX, L557). Só p/ o player local.
    @Inject(method = "moveEntity", at = @At(value = "FIELD", ordinal = 0,
            target = "Lnet/minecraft/entity/Entity;posX:D"))
    private void elixe$onGoingToFallPre(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerSP) {
            OnGoingToFallEvent event = new OnGoingToFallEvent(0);
            this.elixe$goingToFallEvent = event;
            Elixe.INSTANCE.EVENT_BUS.post(event);
        } else {
            this.elixe$goingToFallEvent = null;
        }
    }

    // gate 1 — onGround dentro do flag (L575, ordinal 0). true quando SafeWalk pediu pra bloquear.
    @Redirect(method = "moveEntity", at = @At(value = "FIELD", ordinal = 0,
            target = "Lnet/minecraft/entity/Entity;onGround:Z"))
    private boolean elixe$safeWalkOnGround(Entity self) {
        OnGoingToFallEvent event = this.elixe$goingToFallEvent;
        if (event != null && event.shouldBlockFall()) {
            return true;
        }
        return self.onGround;
    }

    // gate 2 — isSneaking() dentro do flag (L575, único isSneaking em moveEntity).
    @Redirect(method = "moveEntity", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    private boolean elixe$safeWalkSneaking(Entity self) {
        OnGoingToFallEvent event = this.elixe$goingToFallEvent;
        if (event != null && event.shouldBlockFall()) {
            return true;
        }
        return self.isSneaking();
    }

    // willFall — replica setWillFall(true) que o MCP-919 roda nos loops sneak-edge (3 List.isEmpty()).
    @Redirect(method = "moveEntity", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    private boolean elixe$safeWalkEdgeEmpty(List<?> list) {
        boolean empty = list.isEmpty();
        OnGoingToFallEvent event = this.elixe$goingToFallEvent;
        if (empty && event != null) {
            event.setWillFall(true);
        }
        return empty;
    }

    // state 1 — após o bloco sneak-edge (âncora: 4º getCollidingBoundingBoxes = ordinal 3, L645).
    @Inject(method = "moveEntity", at = @At(value = "INVOKE", ordinal = 3,
            target = "Lnet/minecraft/world/World;getCollidingBoundingBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;"))
    private void elixe$onGoingToFallPost(double x, double y, double z, CallbackInfo ci) {
        OnGoingToFallEvent event = this.elixe$goingToFallEvent;
        if (event != null) {
            event.setState(1);
            Elixe.INSTANCE.EVENT_BUS.post(event);
        }
    }
}
