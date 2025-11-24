# Code-Snippets.me – Padrões de Implementação do Shindo Client

## 1. Estilo Geral
```java
// Use final onde fizer sentido e mantenha construtores enxutos.
public class ExampleManager {
    private final Scroll scroll = new Scroll();
    private final SimpleAnimation toggleAnimation = new SimpleAnimation();

    public ExampleManager() {
        toggleAnimation.setValue(0F);
    }
}
```
* Sem `var`; Java 8.  
* Imports sempre específicos (`java.util.List`, nunca `*`).  
* `@Override` obrigatório.  
* Tratamento de erro: logar via `ShindoLogger` e tentar fallback seguro.

## 2. Chips de Categoria (Filtro)
```java
private final List<FilterChip> categoryChips = new ArrayList<>();

private void drawChips(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float startX, float width, float originY, float scrollOffset, int mouseX, int mouseY) {

    categoryChips.clear();
    float currentX = startX;
    float currentY = originY;
    float maxX = startX + width;

    for (MyEnum option : MyEnum.values()) {
        String label = option.getDisplay();
        float chipWidth = CategoryChipRenderer.computeWidth(nvg, label, option.getIcon());

        if (currentX + chipWidth > maxX) {
            currentX = startX;
            currentY += CategoryChipRenderer.CHIP_HEIGHT + 10F;
        }

        boolean active = option == currentOption;
        boolean hovered = MouseUtils.isInside(mouseX, mouseY, currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);
        CategoryChipRenderer.drawChip(nvg, palette, accent, currentX, currentY, chipWidth, label, option.getIcon(), active, hovered);

        FilterChip chip = new FilterChip(() -> {
            if (currentOption != option) {
                currentOption = option;
                scroll.resetAll();
            }
        });
        chip.setBounds(currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);
        categoryChips.add(chip);

        currentX += chipWidth + 10F;
    }
}
```
* Clique sempre chama `chip.click()` (nunca lógica duplicada).  
* Ao trocar o filtro, limpe caches dependentes (`moduleCardCache.clear()` etc).

## 3. Scroll + Viewport
```java
float contentTop = viewportY + headerHeight;
float contentHeight = Math.max(0F, viewportHeight - headerHeight);

if (MouseUtils.isInside(mouseX, mouseY, viewportX, contentTop, viewportWidth, contentHeight)) {
    scroll.onScroll();
}
scroll.onAnimation();
float scrollOffset = scroll.getValue();

nvg.save();
nvg.scissor(viewportX, contentTop, viewportWidth, contentHeight);
nvg.translate(0, scrollOffset);
// draw...
nvg.restore();
```
* Sempre que o conteúdo mudar de tamanho → `scroll.setMaxScroll(newHeight - viewportHeight)`.

## 4. Cards/Toggles
```java
addon.getAnimation().setAnimation(addon.isToggled() ? 1F : 0F, 16);
nvg.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, 12F, base);
nvg.drawGradientRoundedRect(cardX, cardY, cardWidth, cardHeight, 12F,
        ColorUtils.applyAlpha(accent.getColor1(), (int) (addon.getAnimation().getValue() * 90)),
        ColorUtils.applyAlpha(accent.getColor2(), (int) (addon.getAnimation().getValue() * 90)));

// Toggle pill
float toggleX = cardX + cardWidth - TOGGLE_WIDTH - 18F;
float knob = TOGGLE_HEIGHT - 8F;
float knobX = toggleX + 4F + addon.getAnimation().getValue() * (TOGGLE_WIDTH - knob - 8F);
nvg.drawRoundedRect(knobX, toggleY + 4F, knob, knob, knob / 2F, Color.WHITE);
```
* Estados `locked` → máscara translúcida + `LegacyIcon.LOCK`.

## 5. Persistência (JSON)
```java
private void saveConfig() {
    Path path = configFile.toPath();
    try {
        Files.createDirectories(path.getParent());
        JsonObject json = new JsonObject();
        json.addProperty("enabled", enabled);
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            gson.toJson(json, writer);
        }
    } catch (IOException exception) {
        ShindoLogger.error("Failed to save config", exception);
    }
}
```
* Ler usando `try (Reader reader = Files.newBufferedReader(...))`.  
* Sempre tratar `JsonParseException` e fallback para valores padrão.

## 6. Warp Proxy / Diagnósticos
```java
WarpProxyManager warpProxyManager = Shindo.getInstance().getWarpProxyManager();
if (warpProxyManager != null) {
    warpProxyManager.setEnabled(warpSetting.isToggled());
    WarpProxyManager.WarpDiagnostics diagnostics = warpProxyManager.getDiagnostics();
    snapshot.warpStatus = diagnostics.getStatus();
}
```
* Nunca consultar rede direto no GUI; use `WarpProxyManager`.

## 7. Painel de Configurações
```java
settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().getSettingsLayoutMode());
if (settingsOpen) {
    if (settingsPanel.mouseClicked(mouseX, mouseY, mouseButton, contentX, contentY, contentWidth, viewportHeight, settingsScroll)) {
        return;
    }
    settingsScroll.onScroll();
    settingsScroll.onAnimation();
}
```
* Sempre resetar `settingsPanel.clear()` ao fechar.

## 8. Convenções Rápidas

| Situação             | Padrão                                                      |
|----------------------|-------------------------------------------------------------|
| Logs                 | `ShindoLogger.info/debug/error`                             |
| Comparações          | `Objects.equals` para Strings/Objects                       |
| Recursos             | Sempre liberar `nvg.save()`/`nvg.restore()` em pares        |
| Novos componentes UI | Reaproveite helpers existentes antes de criar classes novas |

Tenha este arquivo aberto quando implementar novas features; mantém coerência visual e reduz refactors futuros.
