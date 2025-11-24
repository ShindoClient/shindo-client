package me.miki.shindo.gui.modmenu.category.impl;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.gui.modmenu.category.impl.shared.CategoryChipRenderer;
import me.miki.shindo.gui.modmenu.category.impl.shared.FilterChip;
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel;
import me.miki.shindo.gui.modmenu.category.impl.network.NetworkSection;
import me.miki.shindo.gui.modmenu.category.impl.network.module.NetworkModule;
import me.miki.shindo.gui.modmenu.category.impl.network.module.NetworkModuleContext;
import me.miki.shindo.gui.modmenu.category.impl.network.module.TweakerOverviewModule;
import me.miki.shindo.gui.modmenu.category.impl.network.module.WarpProxyModule;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.impl.CategorySetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import me.miki.shindo.management.network.ConnectionTweakerManager;
import me.miki.shindo.management.network.ConnectionTweakerManager.ProfileSnapshot;
import me.miki.shindo.ui.comp.impl.CompToggleButton;
import me.miki.shindo.utils.mouse.MouseUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NetworkCategory extends Category {

    private static final float CONTENT_PADDING = 20F;

    private final SettingsPanel settingsPanel = new SettingsPanel();
    private final List<FilterChip> navigationChips = new ArrayList<>();
    private final NetworkModuleContext moduleContext = new NetworkModuleContext(this, settingsPanel);
    private final List<NetworkModule> modules =
            Arrays.asList(new TweakerOverviewModule(), new WarpProxyModule());

    private ConnectionTweakerManager manager;
    private NetworkSection activeSection = NetworkSection.TWEAKER;

    public NetworkCategory(GuiModMenu parent) {
        super(parent, TranslateText.NETWORK, LegacyIcon.GLOBE, false, true);
    }

    @Override
    public void initGui() {
        manager = Shindo.getInstance().getConnectionTweakerManager();
        BooleanSetting warpSetting = null;
        List<Setting> filteredSettings = Collections.emptyList();
        CompToggleButton localOptimizerToggle = null;
        CompToggleButton localWarpToggle = null;
        BooleanSetting optimizerSetting = null;
        if (manager != null) {
            optimizerSetting = SettingRegistry.getBooleanSetting(manager, "optimizerEnabled");
            warpSetting = SettingRegistry.getBooleanSetting(manager, "warpProxyEnabled");
            filteredSettings = filterSettings(SettingRegistry.getSettings(manager));
            localOptimizerToggle = optimizerSetting != null ? new CompToggleButton(optimizerSetting) : null;
            localWarpToggle = warpSetting != null ? new CompToggleButton(warpSetting) : null;
            settingsPanel.clear();
        } else {
            settingsPanel.clear();
        }

        moduleContext.setManager(manager);
        moduleContext.setWarpSetting(warpSetting);
        moduleContext.setOptimizerSetting(optimizerSetting);
        moduleContext.setOptimizerToggle(localOptimizerToggle);
        moduleContext.setWarpToggle(localWarpToggle);
        moduleContext.setCachedSettings(filteredSettings);
        moduleContext.setSnapshot(manager != null ? manager.getProfileSnapshot() : null);
        modules.forEach(module -> module.init(moduleContext));

        activeSection = NetworkSection.TWEAKER;
    }

    @Override
    public void initCategory() {
        List<Setting> filtered = manager != null ? filterSettings(SettingRegistry.getSettings(manager)) : Collections.emptyList();
        settingsPanel.clear();
        moduleContext.setCachedSettings(filtered);
        moduleContext.setSnapshot(manager != null ? manager.getProfileSnapshot() : null);
        modules.forEach(module -> module.onSectionActivated(moduleContext));
    }

    private List<Setting> filterSettings(List<Setting> settings) {
        if (settings == null || settings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Setting> filtered = new ArrayList<>(settings.size());
        for (Setting setting : settings) {

            if (setting == null) {
                continue;
            }

            if (setting == moduleContext.getWarpSetting()) {
                continue;
            }

            if (setting == moduleContext.getOptimizerSetting()) {
                continue;
            }

            if (setting instanceof CategorySetting && isOverviewCategory((CategorySetting) setting)) {
                continue;
            }

            if (setting instanceof CategorySetting && isRoutingCategory((CategorySetting) setting)) {
                continue;
            }
            filtered.add(setting);
        }
        return filtered;
    }

    private boolean isRoutingCategory(CategorySetting setting) {
        if (setting == null) {
            return false;
        }
        TranslateText translate = setting.getTranslate();
        if (translate == TranslateText.NETWORK_CATEGORY_ROUTING) {
            return true;
        }
        String key = setting.getNameKey();
        if (key != null && key.equalsIgnoreCase("routing")) {
            return true;
        }
        String name = setting.getName();
        return name != null && name.equalsIgnoreCase(TranslateText.NETWORK_CATEGORY_ROUTING.getText());
    }

    private boolean isOverviewCategory(CategorySetting setting) {
        if (setting == null) {
            return false;
        }
        TranslateText translate = setting.getTranslate();
        if (translate == TranslateText.NETWORK_CATEGORY_OVERVIEW) {
            return true;
        }
        String key = setting.getNameKey();
        if (key != null && key.equalsIgnoreCase("overview")) {
            return true;
        }
        String name = setting.getName();
        return name != null && name.equalsIgnoreCase(TranslateText.NETWORK_CATEGORY_OVERVIEW.getText());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        if (manager == null) {
            return;
        }

        ProfileSnapshot snapshot = manager.getProfileSnapshot();
        moduleContext.setSnapshot(snapshot);

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor accent = colorManager.getCurrentColor();

        float viewportX = getX();
        float viewportY = getY();
        float viewportWidth = getWidth();
        float viewportHeight = getHeight();

        navigationChips.clear();

        float tabHeight = drawSectionTabs(nvg, palette, accent, viewportX, viewportY, viewportWidth, mouseX, mouseY);
        float contentTop = viewportY + tabHeight + 12F;
        float contentHeight = Math.max(0F, viewportHeight - (contentTop - viewportY));
        if (contentHeight <= 0F) {
            return;
        }

        NetworkModule module = getActiveModule();
        if (module != null) {
            module.draw(moduleContext, nvg, palette, accent, mouseX, mouseY, partialTicks, contentTop, contentHeight);
        }
    }

    private float drawSectionTabs(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float viewportX, float viewportY, float viewportWidth, int mouseX, int mouseY) {

        navigationChips.clear();

        float chipGap = 10F;
        float startX = viewportX + CONTENT_PADDING - 6F;
        float maxX = viewportX + viewportWidth - CONTENT_PADDING + 6F;
        float currentX = startX;
        float currentY = viewportY + 6F;

        for (NetworkSection section : NetworkSection.values()) {

            String label = section.getLabel();
            String icon = section.getIcon();
            float chipWidth = CategoryChipRenderer.computeWidth(nvg, label, icon);

            if (currentX + chipWidth > maxX) {
                currentX = startX;
                currentY += CategoryChipRenderer.CHIP_HEIGHT + chipGap;
            }

            boolean active = section == activeSection;
            boolean hovered = MouseUtils.isInside(mouseX, mouseY, currentX, currentY, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);

            CategoryChipRenderer.drawChip(nvg, palette, accent, currentX, currentY, chipWidth, label, icon, active, hovered);

            FilterChip chip = new FilterChip(() -> {
                if (activeSection != section) {
                    activeSection = section;
                    NetworkModule activeModule = getActiveModule();
                    if (activeModule != null) {
                        activeModule.onSectionActivated(moduleContext);
                    }
                }
            });
            chip.setBounds(currentX, currentY, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);
            navigationChips.add(chip);

            currentX += chipWidth + chipGap;
        }

        return (currentY + CategoryChipRenderer.CHIP_HEIGHT) - viewportY;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        if (manager == null) {
            return;
        }

        if (mouseButton == 0) {
            for (FilterChip chip : navigationChips) {
                if (chip.contains(mouseX, mouseY)) {
                    chip.click();
                    return;
                }
            }
        }

        NetworkModule module = getActiveModule();
        if (module != null) {
            module.mouseClicked(moduleContext, mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

        if (manager == null) {
            return;
        }

        NetworkModule module = getActiveModule();
        if (module != null) {
            module.mouseReleased(moduleContext, mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (manager == null) {
            return;
        }
        NetworkModule module = getActiveModule();
        if (module != null) {
            module.keyTyped(moduleContext, typedChar, keyCode);
        }
    }

    private NetworkModule getActiveModule() {
        for (NetworkModule module : modules) {
            if (module.getSection() == activeSection) {
                return module;
            }
        }
        return null;
    }
}
