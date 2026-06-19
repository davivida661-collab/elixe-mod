package elixe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.settings.KeyBinding;

/**
 * Expõe {@code pressTime} do KeyBinding. O MCP-919 tinha um {@code isPressedSilent()} custom
 * (= {@code pressTime != 0}, lê sem consumir). Usado nos hooks de keybind (AutoSoup/MLG).
 */
@Mixin(KeyBinding.class)
public interface AccessorKeyBinding {
    @Accessor("pressTime")
    int getPressTime();
}
