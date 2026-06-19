package elixe.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import elixe.Elixe;
import elixe.events.OnPlayerMoveStateEvent;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;

/**
 * OnPlayerMoveStateEvent — SafeWalk / InventoryMove.
 *
 * O MCP-919 reescreve updatePlayerMoveState inteiro em volta do evento (lê os keybinds, posta,
 * e aplica os booleans que o módulo pode sobrescrever via setForward/...). Replicamos o corpo
 * todo num @Inject(HEAD) e cancelamos o vanilla.
 *
 * moveStrafe/moveForward/jump/sneak são campos PUBLICOS da superclasse MovementInput. Em vez de
 * @Shadow (o AP não acharia o mapeamento por estarem na super → quebraria em produção), acessamos
 * via cast direto — o reobfJar remapeia refs diretas a campos vanilla. gameSettings é privado e
 * está na própria classe-alvo, então @Shadow mapeia certo.
 */
@Mixin(MovementInputFromOptions.class)
public abstract class MixinMovementInputFromOptions {

    @Shadow @Final private GameSettings gameSettings;

    @Inject(method = "updatePlayerMoveState()V", at = @At("HEAD"), cancellable = true)
    private void elixe$updatePlayerMoveState(CallbackInfo ci) {
        MovementInput mi = (MovementInput) (Object) this;
        mi.moveStrafe = 0.0F;
        mi.moveForward = 0.0F;

        OnPlayerMoveStateEvent event = new OnPlayerMoveStateEvent(
                this.gameSettings.keyBindForward.isKeyDown(), this.gameSettings.keyBindBack.isKeyDown(),
                this.gameSettings.keyBindLeft.isKeyDown(), this.gameSettings.keyBindRight.isKeyDown(),
                this.gameSettings.keyBindJump.isKeyDown(), this.gameSettings.keyBindSneak.isKeyDown());
        Elixe.INSTANCE.EVENT_BUS.post(event);

        if (event.isForward()) {
            ++mi.moveForward;
        }
        if (event.isBack()) {
            --mi.moveForward;
        }
        if (event.isLeft()) {
            ++mi.moveStrafe;
        }
        if (event.isRight()) {
            --mi.moveStrafe;
        }

        mi.jump = event.isJump();
        mi.sneak = event.isSneak();

        if (mi.sneak) {
            mi.moveStrafe = (float) (mi.moveStrafe * 0.3D);
            mi.moveForward = (float) (mi.moveForward * 0.3D);
        }

        ci.cancel();
    }
}
