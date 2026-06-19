package elixe.core;

import java.util.Map;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

/**
 * Bootstrap do Mixin para o ambiente de DESENVOLVIMENTO (runClient / GradleStart).
 *
 * No jar de PRODUCAO o {@code MixinTweaker} (manifest TweakClass) acha o
 * {@code mixins.elixe.json} pelo atributo {@code MixinConfigs} do manifest. No dev nao
 * existe jar com manifest, entao precisamos inicializar o Mixin e registrar o config
 * programaticamente. Este coremod faz exatamente isso.
 *
 * Ativado SO no dev, via VM arg {@code -Dfml.coreMods.load=elixe.core.ElixeCoreMod}
 * (configurado na task runClient do build.gradle). Em producao ele NAO e declarado no
 * manifest, entao fica inerte e quem carrega os mixins e o tweaker — sem dupla init.
 */
@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.Name("Elixe Core")
@IFMLLoadingPlugin.TransformerExclusions({ "org.spongepowered.asm.", "elixe.core." })
@IFMLLoadingPlugin.SortingIndex(1001)
public class ElixeCoreMod implements IFMLLoadingPlugin {

    public ElixeCoreMod() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.elixe.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
