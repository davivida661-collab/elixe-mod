package elixe.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

public class Interpolation {
	public static double[] interpolateEntity(Minecraft mc, double tickDelta, Entity entity) {
		double interpolatedX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * tickDelta;
		double interpolatedY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * tickDelta;
		double interpolatedZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * tickDelta;
		return new double[] { interpolatedX, interpolatedY, interpolatedZ };
	}
	
	public static double[] compensateRenderManager(double[] pos, RenderManager renderManager) {
		double renderX = pos[0] - ((elixe.mixin.AccessorRenderManager) renderManager).getRenderPosX();
        double renderY = pos[1] - ((elixe.mixin.AccessorRenderManager) renderManager).getRenderPosY();
        double renderZ = pos[2] - ((elixe.mixin.AccessorRenderManager) renderManager).getRenderPosZ();
		return new double[] { renderX, renderY, renderZ };
	}
}
