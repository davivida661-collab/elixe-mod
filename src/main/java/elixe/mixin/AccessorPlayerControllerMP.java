package elixe.mixin;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Expõe o campo privado {@code isHittingBlock} (AutoSoup, AimAssist, MLG). */
@Mixin(PlayerControllerMP.class)
public interface AccessorPlayerControllerMP {

    @Accessor("isHittingBlock")
    boolean isHittingBlock();
}
