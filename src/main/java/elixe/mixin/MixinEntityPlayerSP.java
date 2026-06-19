package elixe.mixin;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import elixe.Elixe;
import elixe.events.OnLivingUpdateEvent;
import elixe.events.OnMoveEvent;
import elixe.events.OnPushOutBlocksEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.world.World;

/**
 * Hooks do EntityPlayerSP (Sprint/Velocity/Phase/MushExploit).
 *
 * O mixin estende AbstractClientPlayer (superclasse direta do alvo) só pra poder expressar
 * super.moveEntity(...) no override adicionado (vanilla não tem moveEntity em EntityPlayerSP).
 * O construtor é dummy — Mixin não o mescla; existe só pra o compilador aceitar o extends.
 */
@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {

    private MixinEntityPlayerSP(World w, GameProfile g) {
        super(w, g);
    }

    // OnMoveEvent — Phase/MushExploit. O MCP-919 ADICIONA este override wrapando super.moveEntity,
    // e OffsetPhase modifica x/z via setX/setZ. MCP-919 EntityPlayerSP L198.
    @Override
    public void moveEntity(double x, double y, double z) {
        OnMoveEvent event = new OnMoveEvent(x, y, z);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            return;
        }
        super.moveEntity(event.getX(), event.getY(), event.getZ());
    }

    // OnLivingUpdateEvent — Sprint/Velocity/Phase. Cacheado p/ o redirect do sprint abaixo. MCP-919 L805.
    @Unique
    private OnLivingUpdateEvent elixe$livingUpdateEvent;

    @Inject(method = "onLivingUpdate()V", at = @At("HEAD"))
    private void elixe$onLivingUpdate(CallbackInfo ci) {
        boolean holdingSprinting = Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown();
        OnLivingUpdateEvent event = new OnLivingUpdateEvent(holdingSprinting);
        this.elixe$livingUpdateEvent = event;
        Elixe.INSTANCE.EVENT_BUS.post(event);
    }

    // Sprint — o MCP-919 troca os 2 keyBindSprint.isKeyDown() do onLivingUpdate por
    // event.isHoldingSprinting() (Sprint seta =true). Ambos isKeyDown() do método são do keyBindSprint.
    @Redirect(method = "onLivingUpdate()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"))
    private boolean elixe$sprintIsKeyDown(KeyBinding keyBinding) {
        OnLivingUpdateEvent event = this.elixe$livingUpdateEvent;
        if (event != null) {
            return event.isHoldingSprinting();
        }
        return keyBinding.isKeyDown();
    }

    // OnPushOutBlocksEvent — Phase (noclip). Cancelar => retorna false. MCP-919 L502.
    @Inject(method = "pushOutOfBlocks(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void elixe$onPushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        OnPushOutBlocksEvent event = new OnPushOutBlocksEvent();
        Elixe.INSTANCE.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }
}
