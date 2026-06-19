package elixe.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Expõe o campo privado {@code pointedEntity} do EntityRenderer (Reach). */
@Mixin(EntityRenderer.class)
public interface AccessorEntityRenderer {

    @Accessor("pointedEntity")
    Entity getPointedEntity();

    @Accessor("pointedEntity")
    void setPointedEntity(Entity entity);
}
