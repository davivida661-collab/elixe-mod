package elixe.mixin;

import net.minecraft.network.play.client.C03PacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Permite escrever o campo {@code onGround} do pacote de movimento (NoFall). */
@Mixin(C03PacketPlayer.class)
public interface AccessorC03PacketPlayer {

    @Accessor("onGround")
    void setOnGround(boolean onGround);
}
