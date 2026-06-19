<div align="center">

# elixe

**Utility client gratuito e open-source para Minecraft 1.8.9 — agora como mod de Forge + Mixin.**

[elixe.lol](https://elixe.lol) · free · open-source

</div>

---

## O que é

**Elixe** é um utility client de Minecraft **1.8.9** focado em PvP e qualidade de vida.
Diferente dos clients antigos (um `.jar` modificado do jogo inteiro), o Elixe é um **mod de
Forge** que usa **[Mixin](https://github.com/SpongePowered/Mixin)** pra injetar só o necessário —
então instala como qualquer outro mod, ao lado do Forge.

- 🆓 **Gratuito** — sem paywall, sem chave.
- 🔓 **Open-source** — audita, faz fork, contribui.
- 🧩 **Mod de Forge** — instala junto do Forge 1.8.9, não substitui o jogo.
- 🎨 **ClickGUI moderno** — tema dark + ciano, sliders, categorias, bind por tecla.

> **Prefere a versão standalone?** O Elixe também existe como **client/modificação standalone**
> (não-mod, base MCP 1.8.9) em **[SudanoJ/elixe](https://github.com/SudanoJ/elixe)** — código
> pós-decompilação pra mergear num workspace MCP. Este repositório (`elixe-mod`) é a versão
> **mod de Forge**, mais fácil de instalar. Use a que preferir.

## Instalação (jogadores)

1. Instale o **Minecraft Forge 1.8.9**.
2. Baixe o `elixe-X.X.jar` (em [Releases](../../releases)) e coloque em `.minecraft/mods`.
3. Abra no perfil do Forge 1.8.9 e use a ClickGUI.

> OptiFine é opcional — instale o mod do OptiFine 1.8.9 na pasta `mods` se quiser; o Elixe se
> integra via reflection quando ele está presente, e funciona sem ele.

## Módulos

**Combat:** Kill Aura · Aim Assist · Reach · Hitbox · AutoClicker · WTap · Velocity · Criticals · AutoSoup · Misplace
**Movement:** Sprint · Fly · NoFall · SafeWalk · InventoryMove · NoJumpDelay
**Render:** ClickGUI · HUD · ESP · Chams · Skeletal · Camera · Name Protect · Aesthetics · HealthLog · Cosmetics
**Player:** Phase · Derp · **World:** FastPlace · MLG
**Misc:** Old Animations · AntiBot · Ninja · ItemLock · Commands · Mush Exploit · AntiMonk

## Build (desenvolvedores)

Requer **JDK 8**.

```bash
cp gradle.properties.example gradle.properties   # ajuste o java.home se seu PATH java for JRE
./gradlew setupDecompWorkspace                    # primeira vez (~3 min, gera o forgeSrc)
./gradlew build                                   # gera build/libs/elixe-X.X.jar
```

Pra rodar em dev: `./gradlew runClient`.

**Stack:** Forge 1.8.9 (ForgeGradle 2.1) · SpongePowered Mixin 0.7.11 · event bus alpine.

## Licença

Veja [LICENSE](LICENSE). Forge e bibliotecas relacionadas mantêm suas próprias licenças
(arquivos `*-License.txt`).
