package elixe.mixin;

import net.minecraft.network.play.server.S12PacketEntityVelocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Permite escrever os campos int motionX/Y/Z do pacote de velocidade (Velocity). */
@Mixin(S12PacketEntityVelocity.class)
public interface AccessorS12PacketEntityVelocity {

    @Accessor("motionX")
    void setMotionX(int motionX);

    @Accessor("motionY")
    void setMotionY(int motionY);

    @Accessor("motionZ")
    void setMotionZ(int motionZ);
}
