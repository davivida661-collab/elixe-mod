package elixe.mixin;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import elixe.Elixe;
import elixe.events.OnChangeWorldEvent;
import elixe.events.OnKeyEvent;
import elixe.events.OnKeybindActionEvent;
import elixe.events.OnMouseEvent;
import elixe.events.OnRightClickDelayTimerEvent;
import elixe.events.OnTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

/**
 * Reintroduz os posts de evento que o MCP-919 fazia direto no {@code Minecraft.java}.
 * Cada hook abaixo replica fielmente a posicao do post original (ver docs/mixin-migration.md sec. 4).
 *
 * NAO incluido aqui (lote de verificacao in-game): OnKeybindActionEvent (attack/use, L2139/L2144)
 * — ancora mid-expressao + isPressedSilent() custom; e OnSetSessionEvent (setSession e metodo
 * custom do altmanager removido, sem caller).
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {

    // OnTickEvent — runTick(), topo. Dirige a maioria dos modulos.
    @Inject(method = "runTick", at = @At("HEAD"))
    private void elixe$onTick(CallbackInfo ci) {
        Elixe.INSTANCE.EVENT_BUS.post(new OnTickEvent());
    }

    // OnRightClickDelayTimerEvent — substitui o literal `this.rightClickDelayTimer = 4`
    // dentro de rightClickMouse(), deixando o FastPlace alterar o valor. (MCP-919 L1396)
    @ModifyConstant(method = "rightClickMouse", constant = @Constant(intValue = 4))
    private int elixe$onRightClickDelay(int original) {
        OnRightClickDelayTimerEvent event = new OnRightClickDelayTimerEvent(original);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        return event.getRightClickDelayTimer();
    }

    // OnMouseEvent — runTick(), logo apos o setKeyBindState do mouse (ordinal 0).
    // Recomputa do LWJGL pra nao depender de locals. (MCP-919 L1611)
    @Inject(method = "runTick", at = @At(value = "INVOKE", ordinal = 0, shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/settings/KeyBinding;setKeyBindState(IZ)V"))
    private void elixe$onMouse(CallbackInfo ci) {
        int i = Mouse.getEventButton();
        if (i != -101) {
            Elixe.INSTANCE.EVENT_BUS.post(new OnMouseEvent(Mouse.getEventButtonState(), i - 100));
        }
    }

    // OnKeyEvent — runTick(), logo apos o setKeyBindState do teclado (ordinal 1).
    // Dirige o toggle de modulos pelo ModuleManager. (MCP-919 L1663)
    @Inject(method = "runTick", at = @At(value = "INVOKE", ordinal = 1, shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/settings/KeyBinding;setKeyBindState(IZ)V"))
    private void elixe$onKey(CallbackInfo ci) {
        int k = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
        Elixe.INSTANCE.EVENT_BUS.post(new OnKeyEvent(Keyboard.getEventKeyState(), k));
    }

    // OnChangeWorldEvent — fim de loadWorld(WorldClient, String). (MCP-919 L2086)
    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V",
            at = @At("TAIL"))
    private void elixe$onChangeWorld(CallbackInfo ci) {
        Elixe.INSTANCE.EVENT_BUS.post(new OnChangeWorldEvent());
    }

    // OnKeybindActionEvent (attack) — AutoSoup/MLG. runTick, else-branch (não usando item),
    // antes do while(keyBindAttack.isPressed()). Âncora: GETFIELD keyBindAttack ordinal 1
    // (0=if-branch isUsingItem L2122, 1=else-branch L2139, 2=sendClickBlock L2160).
    // isPressedSilent() custom = pressTime != 0 (via AccessorKeyBinding, lê sem consumir).
    @Inject(method = "runTick", at = @At(value = "FIELD", ordinal = 1,
            target = "Lnet/minecraft/client/settings/GameSettings;keyBindAttack:Lnet/minecraft/client/settings/KeyBinding;"))
    private void elixe$onKeybindAttack(CallbackInfo ci) {
        KeyBinding kb = Minecraft.getMinecraft().gameSettings.keyBindAttack;
        OnKeybindActionEvent event = new OnKeybindActionEvent(
                ((AccessorKeyBinding) (Object) kb).getPressTime() != 0, kb.getKeyCode());
        Elixe.INSTANCE.EVENT_BUS.post(event);
    }

    // OnKeybindActionEvent (use) — antes do while(keyBindUseItem.isPressed()). Âncora: GETFIELD
    // keyBindUseItem ordinal 2 (0=isUsingItem isKeyDown L2117, 1=if-branch L2127, 2=else-branch L2144,
    // 3=rightClickDelay L2155).
    @Inject(method = "runTick", at = @At(value = "FIELD", ordinal = 2,
            target = "Lnet/minecraft/client/settings/GameSettings;keyBindUseItem:Lnet/minecraft/client/settings/KeyBinding;"))
    private void elixe$onKeybindUse(CallbackInfo ci) {
        KeyBinding kb = Minecraft.getMinecraft().gameSettings.keyBindUseItem;
        OnKeybindActionEvent event = new OnKeybindActionEvent(
                ((AccessorKeyBinding) (Object) kb).getPressTime() != 0, kb.getKeyCode());
        Elixe.INSTANCE.EVENT_BUS.post(event);
    }
}
