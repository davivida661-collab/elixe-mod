package elixe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import elixe.Elixe;
import elixe.events.OnDrawChatLineEvent;
import net.minecraft.client.gui.GuiNewChat;

/**
 * OnDrawChatLineEvent — NameProtect (oculta nome em linhas de chat).
 * Intercepta a string desenhada no unico drawStringWithShadow de drawChat. MCP-919 L88/L89.
 */
@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {

    @ModifyArg(method = "drawChat", index = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private String elixe$onDrawChatLine(String text) {
        OnDrawChatLineEvent event = new OnDrawChatLineEvent(text);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        return event.getChatLine();
    }
}
