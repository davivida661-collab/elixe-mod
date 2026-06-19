package elixe.mixin;

import net.minecraft.client.renderer.entity.RenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Expõe os campos privados renderPosX/Y/Z do RenderManager (ESP). */
@Mixin(RenderManager.class)
public interface AccessorRenderManager {

    @Accessor("renderPosX")
    double getRenderPosX();

    @Accessor("renderPosY")
    double getRenderPosY();

    @Accessor("renderPosZ")
    double getRenderPosZ();
}
