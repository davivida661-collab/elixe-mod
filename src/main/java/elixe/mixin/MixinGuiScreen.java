package elixe.mixin;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import elixe.Elixe;
import elixe.events.OnMouseInputGUIEvent;
import net.minecraft.client.gui.GuiScreen;

/**
 * OnMouseInputGUIEvent — AutoClicker (GUI). O módulo MODIFICA o evento (setEventButton/State),
 * então roteamos os dois reads do LWJGL (Mouse.getEventButton/getEventButtonState em
 * handleMouseInput) pelo evento. getEventButton roda antes, então o evento já está em cache
 * quando o redirect de getEventButtonState lê. MCP-919 GuiScreen L603.
 */
@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Unique
    private OnMouseInputGUIEvent elixe$mouseInputEvent;

    @Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false))
    private int elixe$onGuiGetEventButton() {
        OnMouseInputGUIEvent event = new OnMouseInputGUIEvent(Mouse.getEventButton(), Mouse.getEventButtonState());
        Elixe.INSTANCE.EVENT_BUS.post(event);
        this.elixe$mouseInputEvent = event;
        return event.getEventButton();
    }

    @Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/input/Mouse;getEventButtonState()Z", remap = false))
    private boolean elixe$onGuiGetEventButtonState() {
        return this.elixe$mouseInputEvent.getEventButtonState();
    }
}
