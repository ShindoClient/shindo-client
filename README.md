<div align="center">
  <img src="https://raw.githubusercontent.com/ShindoClient/Shindo-Client/master/assets/logo.png" alt="Shindo Client Logo" width="200"/>

# Shindo Client
**More features, fixes, and quality of life improvements for Minecraft.**

🎮 *A modern and evolving Minecraft client built with love and care.*  

[![Discord](https://img.shields.io/badge/Join%20our%20Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://shindoclient.com/discord)
[![License](https://img.shields.io/github/license/ShindoClient/shindo-client?style=for-the-badge)](https://github.com/ShindoClient/shindo-client/blob/master/LICENSE)

</div>

---

## 📜 License
Shindo Client is licensed under **[GPL v3](https://github.com/ShindoClient/shindo-client/blob/master/LICENSE)**.  
Feel free to fork, modify, and contribute – just remember to share under the same license.

---

## 🐛 Issues & Support
📥 **Found a bug?**  
- Open an [issue on GitHub](https://github.com/ShindoClient/shindo-client/issues).  
- Or join our [Discord](https://shindoclient.com/discord) and talk to us directly.

---

## 🛠️ Building (IntelliJ IDEA)

Clone the repo and run the following commands:

```bash
gradlew setupDecompWorkspace
gradlew genIntellijRuns
gradlew build
```

> **Build notes**
> - The project uses the bundled `gradlew` (Gradle 9.2.1) with the `legacy-looming` plugin (1.14-SNAPSHOT) over Minecraft 1.8.9, so it behaves as a tweaker/LaunchWrapper client without Fabric runtime libs.
> - Kotlin sources target JVM 1.8; keep any new compilation or lint tasks consistent with that target and the `ShindoTweaker` bootstrap.

## TODOs e fluxo de documentação temporária

Erros de compilação ou ajustes de migração do Kotlin devem ser registrados em `docs/TODOs/TODO-KOTLIN-MODS-BUGFIX.md`. Esse arquivo funciona como o backlog autoritativo dos problemas ainda pendentes, incluindo o contexto de onde surgiram (p.ex. os últimos ajustes em `HypixelMod`/`HUDMod` que existiam como Java antes da conversão) e as etapas necessárias para validá-los. Mantenha o histórico completo no TODO mesmo depois de resolver um item, e preserve o diretório `docs/TODOs/` sem removê-lo.

Como a DSL de mods foi descontinuada, todo novo código deve seguir a estrutura Kotlin/Java atual (veja `src/main/kotlin`/`src/main/java`) e usar os arquivos históricos da branch anterior (`git show HEAD^:src/main/java/...`) para validar comportamentos legados quando necessário.

Nota sobre eventos: @EventTarget injeta o metodo no call() do evento, e o primeiro parametro pode ser o evento. Nem todo evento precisa do argumento para funcionar, entao o parametro vent pode ficar sem uso e isso e esperado.

## Animation Engine v2
Novo engine de animacoes em docs/animation-engine.md. A partir de agora, use o engine novo para qualquer animacao. O sistema legado (Animation, SimpleAnimation, ScreenAnimation) sera removido em breve; as partes pendentes estao em docs/TODOs/TODO-ANIMATION-MIGRATION.md.
