# Framework de UI do Minecraft para Shindo Client

Este framework permite redesenhar todas as UIs do Minecraft com o estilo visual e paleta de cores do Shindo Client, mantendo toda a funcionalidade original.

## 📋 Visão Geral

O framework intercepta a renderização de componentes do Minecraft (botões, campos de texto, sliders, etc.) e aplica o estilo visual do Shindo Client usando NanoVG, mantendo toda a funcionalidade original intacta.

## 🎨 Características

- **Estilo Visual Consistente**: Aplica a paleta de cores e tema do Shindo Client
- **Funcionalidade Preservada**: Mantém toda a funcionalidade original do Minecraft
- **Extensível**: Fácil adicionar novos componentes e renderizadores
- **Configurável**: Pode ser habilitado/desabilitado facilmente
- **Performance**: Usa NanoVG para renderização eficiente

## 🏗️ Estrutura

```
ui/minecraft/
├── MinecraftUIFramework.kt          # Framework principal
├── component/
│   ├── MinecraftButtonRenderer.kt    # Renderizador de botões
│   ├── MinecraftTextFieldRenderer.kt # Renderizador de campos de texto
│   ├── MinecraftSliderRenderer.kt    # Renderizador de sliders
│   └── MinecraftComponentRegistry.kt # Registro de renderizadores
└── README.md                         # Esta documentação
```

## 🚀 Uso Básico

### Habilitar/Desabilitar o Framework

```kotlin
// Habilitar
MinecraftUIFramework.setEnabled(true)

// Desabilitar
MinecraftUIFramework.setEnabled(false)
```

### Verificar se Está Habilitado

```kotlin
val enabled = MinecraftUIFramework.enabled
```

### Obter Cores do Framework

```kotlin
// Obter cor de fundo padrão
val bgColor = MinecraftUIFramework.getDefaultBackgroundColor()

// Obter cor de texto padrão
val textColor = MinecraftUIFramework.getDefaultFontColor()

// Obter cor de hover
val hoverColor = MinecraftUIFramework.getHoverBackgroundColor()

// Obter cores de destaque (accent)
val accent1 = MinecraftUIFramework.getAccentGradientColor1()
val accent2 = MinecraftUIFramework.getAccentGradientColor2()
```

## 🎨 Componentes Suportados

### ✅ Botões (GuiButton, GuiOptionButton)
- Renderização com bordas arredondadas
- Efeito hover com gradiente de destaque
- Estados: normal, hover, disabled
- Texto centralizado
- Suporte para botões de opção

### ✅ Campos de Texto (GuiTextField)
- Fundo com bordas arredondadas
- Borda destacada quando focado
- Cursor piscante
- Estados: normal, focused, disabled

### ✅ Sliders (GuiSlider)
- Trilha com preenchimento gradiente
- Knob arredondado
- Efeito hover
- Exibição de valor

### ✅ Labels (GuiLabel)
- Renderização de texto com estilo do Shindo Client
- Suporte para múltiplas linhas
- Cores baseadas no tema

### ✅ Listas (GuiListExtended)
- Fundo estilizado
- Scrollbar customizada
- Suporte para:
  - Listas de resource packs (GuiResourcePackAvailable, GuiResourcePackSelected)
  - Listas de servidores (ServerSelectionList)
  - Outras listas genéricas

### ✅ Containers (GuiContainer)
- Fundo com bordas arredondadas
- Transparência configurável
- Suporte para inventários, baús, etc.

### ✅ Slots de Inventário
- Destaque quando hovered
- Bordas arredondadas
- Cores de destaque (accent)

### ✅ Chat (GuiChat)
- Fundo estilizado
- Transparência configurável

### ✅ Painéis com Scroll
- Scrollbar customizada
- Fundo estilizado

## 🔧 Como Funciona

1. **Interceptação**: Mixins interceptam métodos de renderização do Minecraft
2. **Verificação**: Verifica se o framework está habilitado e se deve aplicar o estilo
3. **Renderização**: Usa NanoVG para renderizar com o estilo do Shindo Client
4. **Cancelamento**: Cancela a renderização padrão do Minecraft

## 📝 Exemplo de Mixin

```java
@Mixin(GuiButton.class)
public class MixinGuiButton {
    @Inject(
        method = "drawButton",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onDrawButton(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        GuiButton button = (GuiButton) (Object) this;
        GuiScreen currentScreen = mc.currentScreen;
        
        if (MinecraftUIFramework.shouldApplyStyle(currentScreen)) {
            MinecraftComponentRegistry.renderButton(button, mouseX, mouseY, mc.getRenderPartialTicks());
            ci.cancel();
        }
    }
}
```

## 🎯 Adicionando Novos Componentes

Para adicionar suporte a um novo componente:

1. **Criar Renderizador**:
```kotlin
object MinecraftNewComponentRenderer {
    fun renderComponent(component: NewComponent, mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Lógica de renderização
    }
}
```

2. **Registrar no Registry**:
```kotlin
object MinecraftComponentRegistry {
    fun renderNewComponent(component: NewComponent, mouseX: Int, mouseY: Int, partialTicks: Float) {
        MinecraftNewComponentRenderer.renderComponent(component, mouseX, mouseY, partialTicks)
    }
}
```

3. **Criar Mixin**:
```java
@Mixin(NewComponent.class)
public class MixinNewComponent {
    @Inject(method = "drawComponent", at = @At("HEAD"), cancellable = true)
    private void onDrawComponent(CallbackInfo ci) {
        // Interceptar e renderizar
    }
}
```

## ⚙️ Configuração

O framework verifica automaticamente:
- Se está habilitado (`MinecraftUIFramework.enabled`)
- Se a tela atual não é uma tela do Shindo Client (que já tem estilo próprio)
- Se deve aplicar o estilo baseado no contexto

## 🎨 Cores e Temas

O framework usa automaticamente:
- **Paleta de Cores**: Do `ColorManager` do Shindo Client
- **Tema Atual**: Do `ColorManager.theme`
- **Cor de Destaque**: Do `ColorManager.currentColor`

Todas as cores são obtidas dinamicamente, então mudanças no tema são refletidas automaticamente.

## 🔍 Detalhes Técnicos

- **Renderização**: Usa NanoVG através do `NanoVGManager`
- **Thread Safety**: Renderização acontece no thread principal do Minecraft
- **Performance**: Renderização otimizada com cache de cores
- **Compatibilidade**: Funciona com todas as telas do Minecraft

## 📚 Telas do Minecraft Suportadas

### ✅ Telas de Opções
- **GuiOptions** - Menu principal de opções
- **GuiVideoSettings** - Configurações de vídeo
- **GuiControls** - Controles e keybinds
- **GuiLanguage** - Seleção de idioma
- **GuiScreenResourcePacks** - Gerenciamento de resource packs

### ✅ Telas do OptiFine
- **GuiVideoSettingsOF** - Video Settings do OptiFine
- **GuiOtherSettingsOF** - Other Settings do OptiFine
- **GuiQualitySettingsOF** - Quality Settings do OptiFine
- **GuiPerformanceSettingsOF** - Performance Settings do OptiFine
- **GuiAnimationsSettingsOF** - Animations Settings do OptiFine
- **GuiDetailsSettingsOF** - Details Settings do OptiFine
- E outras telas do OptiFine (detecção automática)

### ✅ Outras Telas
- **GuiChat** - Chat
- **GuiContainer** - Containers (inventários, baús, etc.)
- **GuiListExtended** - Listas genéricas
- **GuiResourcePackAvailable** - Lista de resource packs disponíveis
- **GuiResourcePackSelected** - Lista de resource packs selecionados
- **ServerSelectionList** - Lista de servidores

## 🐛 Troubleshooting

### Componentes não estão sendo renderizados
- Verifique se `MinecraftUIFramework.enabled` está `true`
- Verifique se o Mixin está registrado em `mixins.shindo.json`
- Verifique se a tela não é uma `IShindoScreen`

### Cores não estão corretas
- Verifique se o `ColorManager` está inicializado
- Verifique se o tema está configurado corretamente

### Performance
- O framework usa NanoVG que é otimizado
- Se houver problemas, desabilite temporariamente com `setEnabled(false)`

## 📝 Notas

- O framework **não** modifica a funcionalidade dos componentes, apenas a aparência
- Telas do Shindo Client (`IShindoScreen`) não são afetadas (já têm estilo próprio)
- O framework pode ser habilitado/desabilitado em tempo de execução
- Todas as cores são obtidas dinamicamente do sistema de cores do Shindo Client

## 🚧 Roadmap

- [ ] Suporte para mais componentes (GuiLabel, GuiList, etc.)
- [ ] Animações suaves para transições
- [ ] Suporte para tooltips customizados
- [ ] Configuração por componente
- [ ] Cache de renderização para melhor performance
