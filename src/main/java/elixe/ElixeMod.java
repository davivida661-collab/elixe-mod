package elixe;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 * Ponto de entrada do Elixe como mod de Forge 1.8.9.
 *
 * <p>Substitui o antigo {@code Start.java} do client MCP: quem inicializa o
 * Elixe agora é o ciclo de vida do Forge. Os mixins ({@code elixe.mixin.*})
 * aplicam no load das classes vanilla (antes do jogo) via MixinTweaker; aqui
 * só criamos a instância do {@link Elixe} em runtime, quando já é seguro.</p>
 */
@Mod(modid = ElixeMod.MODID, name = ElixeMod.NAME, version = ElixeMod.VERSION, clientSideOnly = true)
public class ElixeMod {

    public static final String MODID = "elixe";
    public static final String NAME = "Elixe";
    public static final String VERSION = "8.0";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Mesma inicialização que o client MCP fazia no boot.
        new Elixe(Minecraft.getMinecraft());
    }
}
