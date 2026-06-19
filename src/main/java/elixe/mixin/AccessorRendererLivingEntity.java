package elixe.mixin;

import java.nio.FloatBuffer;
import java.util.List;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Expõe membros protegidos/privados do RendererLivingEntity (Chams, OldAnimations). */
@Mixin(RendererLivingEntity.class)
public interface AccessorRendererLivingEntity {

    @Accessor("brightnessBuffer")
    FloatBuffer getBrightnessBuffer();

    @Accessor("layerRenderers")
    List<LayerRenderer<EntityLivingBase>> getLayerRenderers();

    // field_177096_e -> textureBrightness (estático) no stable_22
    @Accessor(value = "textureBrightness", remap = true)
    DynamicTexture getTextureBrightness();

    @Invoker("unsetBrightness")
    void invokeUnsetBrightness();

    @Invoker("getColorMultiplier")
    int invokeGetColorMultiplier(EntityLivingBase entity, float brightness, float partialTicks);

    @Invoker("setDoRenderBrightness")
    boolean invokeSetDoRenderBrightness(EntityLivingBase entity, float partialTicks);

    @Invoker("renderModel")
    void invokeRenderModel(EntityLivingBase entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float headYaw, float headPitch, float scale);
}
