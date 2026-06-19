package elixe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import elixe.Elixe;
import elixe.events.OnAttackEntityEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Posta {@link OnAttackEntityEvent} ao atacar (WTap, Criticals, Ninja, MushExploit).
 * MCP-919: PlayerControllerMP.attackEntity L503.
 */
@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Inject(method = "attackEntity(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V",
            at = @At("HEAD"))
    private void elixe$onAttackEntity(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
        Elixe.INSTANCE.EVENT_BUS.post(new OnAttackEntityEvent(targetEntity));
    }
}
