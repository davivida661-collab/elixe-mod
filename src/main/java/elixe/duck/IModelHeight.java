package elixe.duck;

/**
 * Duck interface p/ ler a altura do box capturada no ModelRenderer (Skeletal).
 *
 * IMPORTANTE: fica FORA do pacote elixe.mixin de proposito. O Mixin trata todo o pacote
 * de mixins como "nao referenciavel diretamente"; como o ModelRenderer passa a implementar
 * esta interface, qualquer `new ModelRenderer(...)` (ex.: ModelSpider) precisa carrega-la
 * direto — entao ela NAO pode estar no pacote dos mixins.
 */
public interface IModelHeight {
    float elixe$getHeight();
}
