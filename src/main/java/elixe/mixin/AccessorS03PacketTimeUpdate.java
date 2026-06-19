package elixe.mixin;

import net.minecraft.network.play.server.S03PacketTimeUpdate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Permite escrever o campo {@code worldTime} do pacote de tempo (Aesthetics). */
@Mixin(S03PacketTimeUpdate.class)
public interface AccessorS03PacketTimeUpdate {

    @Accessor("worldTime")
    void setWorldTime(long time);
}
