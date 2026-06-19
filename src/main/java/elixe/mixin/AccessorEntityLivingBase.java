package elixe.mixin;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Dá acesso de escrita ao campo privado {@code jumpTicks} de
 * {@link EntityLivingBase} — substitui o edit que o client MCP fazia (campo
 * {@code public}). Usado pelo módulo NoJumpDelay (Padrão D do roteiro).
 */
@Mixin(EntityLivingBase.class)
public interface AccessorEntityLivingBase {

    @Accessor("jumpTicks")
    void elixe$setJumpTicks(int value);
}
