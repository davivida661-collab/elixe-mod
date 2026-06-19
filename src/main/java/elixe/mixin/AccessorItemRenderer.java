package elixe.mixin;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Expõe campos/métodos privados do ItemRenderer p/ o OldAnimations, que
 * reimplementa o render de item em 1ª pessoa. Os func_* viraram nomes legíveis
 * no stable_22 (ex.: func_178101_a -> rotateArroundXAndY).
 */
@Mixin(ItemRenderer.class)
public interface AccessorItemRenderer {

    @Accessor("itemToRender")
    ItemStack getItemToRender();

    @Accessor("equippedProgress")
    float getEquippedProgress();

    @Accessor("prevEquippedProgress")
    float getPrevEquippedProgress();

    @Invoker("transformFirstPersonItem")
    void invokeTransformFirstPersonItem(float equipProgress, float swingProgress);

    @Invoker("renderItemMap")
    void invokeRenderItemMap(AbstractClientPlayer player, float p2, float p3, float p4);

    @Invoker("rotateArroundXAndY")          // func_178101_a
    void invokeRotateArroundXAndY(float angleX, float angleY);

    @Invoker("setLightMapFromPlayer")       // func_178109_a
    void invokeSetLightMapFromPlayer(AbstractClientPlayer player);

    @Invoker("rotateWithPlayerRotations")   // func_178110_a
    void invokeRotateWithPlayerRotations(EntityPlayerSP player, float partialTicks);

    @Invoker("performDrinking")             // func_178104_a
    void invokePerformDrinking(AbstractClientPlayer player, float partialTicks);

    @Invoker("doBlockTransformations")      // func_178103_d
    void invokeDoBlockTransformations();

    @Invoker("doBowTransformations")        // func_178098_a
    void invokeDoBowTransformations(float partialTicks, AbstractClientPlayer player);

    @Invoker("doItemUsedTransformations")   // func_178105_d
    void invokeDoItemUsedTransformations(float swingProgress);

    @Invoker("renderPlayerArm")             // func_178095_a
    void invokeRenderPlayerArm(AbstractClientPlayer player, float equipProgress, float swingProgress);
}
