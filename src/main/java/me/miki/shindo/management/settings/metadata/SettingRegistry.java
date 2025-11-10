package me.miki.shindo.management.settings.metadata;

import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.SettingCategoryProvider;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.impl.CategorySetting;
import me.miki.shindo.management.settings.impl.CellGridSetting;
import me.miki.shindo.management.settings.impl.ColorSetting;
import me.miki.shindo.management.settings.impl.ComboSetting;
import me.miki.shindo.management.settings.impl.ImageSetting;
import me.miki.shindo.management.settings.impl.KeybindSetting;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.impl.SoundSetting;
import me.miki.shindo.management.settings.impl.TextSetting;
import me.miki.shindo.management.settings.impl.combo.Option;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SettingRegistry {

    private static final Set<String> PROPERTY_CACHE = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<OwnerFieldKey, Setting> PROPERTY_BINDINGS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ConfigOwner, LinkedHashMap<String, CategorySetting>> CATEGORY_BINDINGS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ConfigOwner, CopyOnWriteArrayList<Setting>> OWNER_BINDINGS = new ConcurrentHashMap<>();

    private SettingRegistry() {
    }

    private static void registerOwnerSetting(ConfigOwner owner, Setting setting) {
        if (owner == null || setting == null) {
            return;
        }
        CopyOnWriteArrayList<Setting> settings = OWNER_BINDINGS.computeIfAbsent(owner, key -> new CopyOnWriteArrayList<>());
        if (!settings.contains(setting)) {
            settings.add(setting);
        }
    }

    public static void applyMetadata(ConfigOwner owner) {
        Set<Setting> processed = new HashSet<>();
        processPropertyFields(owner, processed);
        processSettingFields(owner, processed);
    }

    private static void processSettingFields(ConfigOwner owner, Set<Setting> processed) {
        Class<?> type = owner.getClass();

        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (!Setting.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Setting setting = (Setting) field.get(owner);
                    if (setting == null || processed.contains(setting)) {
                        continue;
                    }

                    Property property = field.getAnnotation(Property.class);
                    if (property != null) {
                        ensureCategory(owner, property.category(), processed);
                    }

                    SettingMetadata metadata = new SettingMetadata(field.getName());
                    if (property != null) {
                        if (!property.key().isEmpty()) {
                            metadata.setKeyOverride(property.key());
                        }
                        metadata.setCategory(property.category());
                        metadata.setDescription(property.description());
                        metadata.setHidden(property.hidden());
                        if (!Double.isNaN(property.min())) {
                            metadata.setMin(property.min());
                        }
                        if (!Double.isNaN(property.max())) {
                            metadata.setMax(property.max());
                        }
                        if (!Double.isNaN(property.step())) {
                            metadata.setStep(property.step());
                        }
                    }

                    setting.applyMetadata(metadata);
                    processed.add(setting);
                    registerOwnerSetting(owner, setting);
                } catch (IllegalAccessException ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    private static void processPropertyFields(ConfigOwner owner, Set<Setting> processed) {
        Class<?> type = owner.getClass();

        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                Property property = field.getAnnotation(Property.class);
                if (property == null) {
                    continue;
                }

                String cacheKey = owner.getClass().getName() + '#' + field.getName();
                if (!PROPERTY_CACHE.add(cacheKey)) {
                    continue;
                }

                Setting setting = createSettingFromProperty(owner, field, property, processed);
                if (setting != null) {
                    PROPERTY_BINDINGS.put(new OwnerFieldKey(owner, field.getName()), setting);
                    processed.add(setting);
                    registerOwnerSetting(owner, setting);
                }
            }
            type = type.getSuperclass();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Setting> T getSetting(ConfigOwner owner, String fieldName, Class<T> type) {
        Setting setting = PROPERTY_BINDINGS.get(new OwnerFieldKey(owner, fieldName));
        if (setting == null) {
            return null;
        }
        if (!type.isInstance(setting)) {
            throw new IllegalArgumentException("Property '" + fieldName + "' on " + owner.getClass().getSimpleName() + " is not of type " + type.getSimpleName());
        }
        return (T) setting;
    }

    public static BooleanSetting getBooleanSetting(ConfigOwner owner, String fieldName) {
        return getSetting(owner, fieldName, BooleanSetting.class);
    }

    public static NumberSetting getNumberSetting(ConfigOwner owner, String fieldName) {
        return getSetting(owner, fieldName, NumberSetting.class);
    }

    public static TextSetting getTextSetting(ConfigOwner owner, String fieldName) {
        return getSetting(owner, fieldName, TextSetting.class);
    }

    public static ColorSetting getColorSetting(ConfigOwner owner, String fieldName) {
        return getSetting(owner, fieldName, ColorSetting.class);
    }

    public static KeybindSetting getKeybindSetting(ConfigOwner owner, String fieldName) {
        return getSetting(owner, fieldName, KeybindSetting.class);
    }

    public static ComboSetting getComboSetting(ConfigOwner owner, String fieldName) {
        return getSetting(owner, fieldName, ComboSetting.class);
    }

    public static List<Setting> getSettings(ConfigOwner owner) {
        CopyOnWriteArrayList<Setting> settings = OWNER_BINDINGS.get(owner);
        if (settings == null || settings.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(settings);
    }

    private static CategorySetting ensureCategory(ConfigOwner owner, String rawCategory, Set<Setting> processed) {
        if (rawCategory == null) {
            return null;
        }
        String category = rawCategory.trim();
        if (category.isEmpty()) {
            return null;
        }
        LinkedHashMap<String, CategorySetting> categories = CATEGORY_BINDINGS.computeIfAbsent(owner, key -> new LinkedHashMap<>());
        CategorySetting categorySetting = categories.get(category);
        if (categorySetting == null) {
            categorySetting = createCategorySetting(owner, category);
            categories.put(category, categorySetting);
            processed.add(categorySetting);
            registerOwnerSetting(owner, categorySetting);
        }
        return categorySetting;
    }

    private static CategorySetting createCategorySetting(ConfigOwner owner, String category) {
        CategorySetting setting;
        if (owner instanceof SettingCategoryProvider) {
            SettingCategoryProvider provider = (SettingCategoryProvider) owner;
            String key = category.toLowerCase(Locale.ROOT);
            TranslateText label = provider.resolveCategoryLabel(key);
            if (label != null && label != TranslateText.NONE) {
                setting = new CategorySetting(label, owner);
            } else {
                setting = new CategorySetting(category, owner);
            }
            setting.setCollapsed(provider.isCategoryInitiallyCollapsed(key));
            return setting;
        }
        setting = new CategorySetting(category, owner);
        setting.setCollapsed(false);
        return setting;
    }

    private static Setting createSettingFromProperty(ConfigOwner owner, Field field, Property property, Set<Setting> processed) {
        try {
            boolean isStatic = Modifier.isStatic(field.getModifiers());
            Object target = isStatic ? null : owner;
            field.setAccessible(true);

            switch (property.type()) {
                case BOOLEAN:
                    return createBooleanSetting(owner, field, target, property, processed);
                case NUMBER:
                    return createNumberSetting(owner, field, target, property, processed);
                case TEXT:
                    return createTextSetting(owner, field, target, property, processed);
                case COLOR:
                    return createColorSetting(owner, field, target, property, processed);
                case KEYBIND:
                    return createKeybindSetting(owner, field, target, property, processed);
                case IMAGE:
                    return createImageSetting(owner, field, target, property, processed);
                case SOUND:
                    return createSoundSetting(owner, field, target, property, processed);
                case COMBO:
                    return createComboSetting(owner, field, target, property, processed);
                case CELL_GRID:
                    return createCellGridSetting(owner, field, target, property, processed);
                default:
                    ShindoLogger.warn("Property type " + property.type() + " is not yet supported on field " + field.getName());
                    return null;
            }
        } catch (IllegalAccessException e) {
            ShindoLogger.error("Failed to bind property field " + field.getName(), e);
            return null;
        }
    }

    private static Setting createBooleanSetting(ConfigOwner owner, Field field, Object target, Property property, Set<Setting> processed) throws IllegalAccessException {
        boolean defaultValue;
        if (!Double.isNaN(property.current())) {
            defaultValue = property.current() != 0;
        } else if (field.getType() == boolean.class) {
            defaultValue = field.getBoolean(target);
        } else {
            Object val = field.get(target);
            defaultValue = val instanceof Boolean ? (Boolean) val : false;
        }
        ensureCategory(owner, property.category(), processed);
        BoundBooleanSetting setting = translate(property)
                ? new BoundBooleanSetting(property.translate(), owner, defaultValue, field, target)
                : new BoundBooleanSetting(resolveName(property, field), owner, defaultValue, field, target);

        applyMetadata(setting, property, field);
        setting.setToggled(defaultValue);
        return setting;
    }

    private static Setting createNumberSetting(ConfigOwner owner, Field field, Object target, Property property, Set<Setting> processed) throws IllegalAccessException {
        boolean integer = isIntegerType(field.getType());
        double defaultValue = !Double.isNaN(property.current()) ? property.current() : readNumericField(field, target);
        if (Double.isNaN(defaultValue)) {
            defaultValue = integer ? 0D : 0D;
        }
        double min = !Double.isNaN(property.min()) ? property.min() : (integer ? Math.min(defaultValue, 0D) : Math.min(defaultValue, 0D));
        double max = !Double.isNaN(property.max()) ? property.max() : (integer ? Math.max(defaultValue, min + 1D) : Math.max(defaultValue, min + 1D));
        if (max < min) {
            double tmp = max;
            max = min;
            min = tmp;
        }
        defaultValue = clamp(defaultValue, min, max);

        ensureCategory(owner, property.category(), processed);
        BoundNumberSetting setting = translate(property)
                ? new BoundNumberSetting(property.translate(), owner, defaultValue, min, max, integer, field, target)
                : new BoundNumberSetting(resolveName(property, field), owner, defaultValue, min, max, integer, field, target);

        SettingMetadata metadata = applyMetadata(setting, property, field);
        if (!Double.isNaN(property.step())) {
            metadata.setStep(property.step());
        }
        setting.setValue(defaultValue);
        return setting;
    }

    private static Setting createTextSetting(ConfigOwner owner, Field field, Object target, Property property, Set<Setting> processed) throws IllegalAccessException {
        String defaultValue = !property.text().isEmpty() ? property.text() : (String) field.get(target);
        if (defaultValue == null) {
            defaultValue = "";
        }

        ensureCategory(owner, property.category(), processed);
        BoundTextSetting setting = translate(property)
                ? new BoundTextSetting(property.translate(), owner, defaultValue, field, target)
                : new BoundTextSetting(resolveName(property, field), owner, defaultValue, field, target);

        applyMetadata(setting, property, field);
        setting.setText(defaultValue);
        return setting;
    }

    private static Setting createColorSetting(ConfigOwner owner, Field field, Object target, Property property, Set<Setting> processed) throws IllegalAccessException {
        Color defaultColor;
        if (property.color() != Integer.MIN_VALUE) {
            defaultColor = new Color(property.color(), property.showAlpha());
        } else {
            defaultColor = (Color) field.get(target);
            if (defaultColor == null) {
                defaultColor = Color.WHITE;
            }
        }

        ensureCategory(owner, property.category(), processed);
        BoundColorSetting setting = translate(property)
                ? new BoundColorSetting(property.translate(), owner, defaultColor, property.showAlpha(), field, target)
                : new BoundColorSetting(resolveName(property, field), owner, defaultColor, property.showAlpha(), field, target);

        applyMetadata(setting, property, field);
        setting.setColor(defaultColor);
        return setting;
    }

    private static Setting createKeybindSetting(ConfigOwner owner, Field field, Object target, Property property, Set<Setting> processed) throws IllegalAccessException {
        int defaultKey = property.keyCode() != Integer.MIN_VALUE ? property.keyCode() : field.getInt(target);

        ensureCategory(owner, property.category(), processed);
        BoundKeybindSetting setting = translate(property)
                ? new BoundKeybindSetting(property.translate(), owner, defaultKey, field, target)
                : new BoundKeybindSetting(resolveName(property, field), owner, defaultKey, field, target);

        applyMetadata(setting, property, field);
        setting.setKeyCode(defaultKey);
        return setting;
    }

    private static Setting createImageSetting(ConfigOwner owner, Field field, Object target, Property property, Set<Setting> processed) throws IllegalAccessException {
        File defaultFile = (File) field.get(target);

        ensureCategory(owner, property.category(), processed);
        BoundImageSetting setting = translate(property)
                ? new BoundImageSetting(property.translate(), owner, defaultFile, field, target)
                : new BoundImageSetting(resolveName(property, field), owner, defaultFile, field, target);

        applyMetadata(setting, property, field);
        setting.setImage(defaultFile);
        return setting;
    }

    private static Setting createSoundSetting(ConfigOwner owner, Field field, Object target, Property property, Set<Setting> processed) throws IllegalAccessException {
        File defaultFile = (File) field.get(target);

        ensureCategory(owner, property.category(), processed);
        BoundSoundSetting setting = translate(property)
                ? new BoundSoundSetting(property.translate(), owner, defaultFile, field, target)
                : new BoundSoundSetting(resolveName(property, field), owner, defaultFile, field, target);

        applyMetadata(setting, property, field);
        setting.setSound(defaultFile);
        return setting;
    }

    private static Setting createComboSetting(ConfigOwner owner, Field field, Object target, Property property, Set<Setting> processed) throws IllegalAccessException {
        Class<?> rawType = field.getType();
        if (!rawType.isEnum()) {
            ShindoLogger.warn("Property field " + field.getName() + " is marked as ENUM but does not use an enum type");
            return null;
        }

        Object[] constants = rawType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            ShindoLogger.warn("Enum property " + field.getName() + " on " + owner.getClass().getSimpleName() + " defines no constants");
            return null;
        }

        ensureCategory(owner, property.category(), processed);

        List<Option> options = new ArrayList<>(constants.length);
        LinkedHashMap<String, Enum<?>> mapping = new LinkedHashMap<>();

        for (Object constantObj : constants) {
            Enum<?> constant = (Enum<?>) constantObj;
            Option option = buildEnumOption(constant);
            options.add(option);
            mapping.put(option.getNameKey(), constant);
        }

        Enum<?> defaultValue = (Enum<?>) field.get(target);
        if (defaultValue == null) {
            defaultValue = (Enum<?>) constants[0];
        }

        String defaultKey = ensureEnumOption(mapping, defaultValue);
        BoundEnumSetting setting = translate(property)
                ? new BoundEnumSetting(property.translate(), owner, defaultKey, options, mapping, field, target)
                : new BoundEnumSetting(resolveName(property, field), owner, defaultKey, options, mapping, field, target);

        applyMetadata(setting, property, field);
        setting.initialize();
        return setting;
    }

    private static Setting createCellGridSetting(ConfigOwner owner, Field field, Object target, Property property, Set<Setting> processed) throws IllegalAccessException {
        boolean[][] defaultGrid = copyGrid((boolean[][]) field.get(target));

        ensureCategory(owner, property.category(), processed);
        BoundCellGridSetting setting = translate(property)
                ? new BoundCellGridSetting(property.translate(), owner, defaultGrid, field, target)
                : new BoundCellGridSetting(resolveName(property, field), owner, defaultGrid, field, target);

        applyMetadata(setting, property, field);
        setting.initialize();
        return setting;
    }

    private static SettingMetadata applyMetadata(Setting setting, Property property, Field field) {
        SettingMetadata metadata = new SettingMetadata(field.getName());
        metadata.setCategory(property.category());
        metadata.setDescription(property.description());
        metadata.setHidden(property.hidden());
        if (!property.key().isEmpty()) {
            metadata.setKeyOverride(property.key());
        }
        if (!Double.isNaN(property.min())) {
            metadata.setMin(property.min());
        }
        if (!Double.isNaN(property.max())) {
            metadata.setMax(property.max());
        }
        if (!Double.isNaN(property.step())) {
            metadata.setStep(property.step());
        }
        setting.applyMetadata(metadata);
        return metadata;
    }

    private static Option buildEnumOption(Enum<?> constant) {
        if (constant instanceof PropertyEnum) {
            PropertyEnum propertyEnum = (PropertyEnum) constant;
            TranslateText translate = propertyEnum.getTranslate();
            if (translate != TranslateText.NONE) {
                return new Option(translate);
            }
            return new Option(propertyEnum.getDisplayName());
        }

        String display = constant.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        if (!display.isEmpty()) {
            display = Character.toUpperCase(display.charAt(0)) + display.substring(1);
        }
        return new Option(display.isEmpty() ? constant.name() : display);
    }

    private static String ensureEnumOption(LinkedHashMap<String, Enum<?>> mapping, Enum<?> value) {
        for (Map.Entry<String, Enum<?>> entry : mapping.entrySet()) {
            if (entry.getValue() == value) {
                return entry.getKey();
            }
        }
        return mapping.isEmpty() ? "" : mapping.keySet().iterator().next();
    }

    private static boolean[][] copyGrid(boolean[][] source) {
        if (source == null) {
            return null;
        }
        boolean[][] copy = new boolean[source.length][];
        for (int i = 0; i < source.length; i++) {
            boolean[] row = source[i];
            copy[i] = row != null ? row.clone() : null;
        }
        return copy;
    }

    private static boolean translate(Property property) {
        return property.translate() != TranslateText.NONE;
    }

    private static String resolveName(Property property, Field field) {
        if (!property.name().isEmpty()) {
            return property.name();
        }
        return field.getName();
    }

    private static boolean isIntegerType(Class<?> type) {
        return type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == short.class || type == Short.class
                || type == byte.class || type == Byte.class;
    }

    private static double readNumericField(Field field, Object target) throws IllegalAccessException {
        Class<?> type = field.getType();
        if (type == double.class) {
            return field.getDouble(target);
        } else if (type == Double.class) {
            Object value = field.get(target);
            return value instanceof Double ? (Double) value : Double.NaN;
        } else if (type == float.class) {
            return field.getFloat(target);
        } else if (type == Float.class) {
            Object value = field.get(target);
            return value instanceof Float ? (Float) value : Double.NaN;
        } else if (type == long.class) {
            return field.getLong(target);
        } else if (type == Long.class) {
            Object value = field.get(target);
            return value instanceof Long ? (Long) value : Double.NaN;
        } else if (type == int.class) {
            return field.getInt(target);
        } else if (type == Integer.class) {
            Object value = field.get(target);
            return value instanceof Integer ? (Integer) value : Double.NaN;
        } else if (type == short.class) {
            return field.getShort(target);
        } else if (type == Short.class) {
            Object value = field.get(target);
            return value instanceof Short ? (Short) value : Double.NaN;
        } else if (type == byte.class) {
            return field.getByte(target);
        } else if (type == Byte.class) {
            Object value = field.get(target);
            return value instanceof Byte ? (Byte) value : Double.NaN;
        }
        return Double.NaN;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private abstract static class SettingBinding {
        final Field field;
        final Object target;

        SettingBinding(Field field, Object target) {
            this.field = field;
            this.target = target;
        }

        void handleException(Exception e) {
            ShindoLogger.error("Failed to update property field " + field.getName(), e);
        }
    }

    private static class BoundBooleanSetting extends BooleanSetting {
        private final SettingBinding binding;

        BoundBooleanSetting(TranslateText text, ConfigOwner parent, boolean defaultValue, Field field, Object target) {
            super(text, parent, defaultValue);
            this.binding = new SettingBinding(field, target) {};
        }

        BoundBooleanSetting(String name, ConfigOwner parent, boolean defaultValue, Field field, Object target) {
            super(name, parent, defaultValue);
            this.binding = new SettingBinding(field, target) {};
        }

        @Override
        public void setToggled(boolean toggle) {
            super.setToggled(toggle);
            apply(toggle);
        }

        @Override
        public void reset() {
            super.reset();
            apply(isToggled());
        }

        private void apply(boolean value) {
            try {
                Class<?> type = binding.field.getType();
                if (type == boolean.class) {
                    binding.field.setBoolean(binding.target, value);
                } else {
                    binding.field.set(binding.target, value);
                }
            } catch (IllegalAccessException e) {
                binding.handleException(e);
            }
        }
    }

    private static class BoundNumberSetting extends NumberSetting {
        private final SettingBinding binding;
        private final boolean integer;

        BoundNumberSetting(TranslateText text, ConfigOwner parent, double defaultValue, double min, double max, boolean integer, Field field, Object target) {
            super(text, parent, defaultValue, min, max, integer);
            this.binding = new SettingBinding(field, target) {};
            this.integer = integer;
        }

        BoundNumberSetting(String name, ConfigOwner parent, double defaultValue, double min, double max, boolean integer, Field field, Object target) {
            super(name, parent, defaultValue, min, max, integer);
            this.binding = new SettingBinding(field, target) {};
            this.integer = integer;
        }

        @Override
        public void setValue(double value) {
            super.setValue(value);
            apply(getValue());
        }

        @Override
        public void reset() {
            super.reset();
            apply(getValue());
        }

        private void apply(double value) {
            try {
                if (integer) {
                    long rounded = Math.round(value);
                    Class<?> type = binding.field.getType();
                    if (type == int.class) {
                        binding.field.setInt(binding.target, (int) rounded);
                    } else if (type == Integer.class) {
                        binding.field.set(binding.target, (int) rounded);
                    } else if (type == long.class) {
                        binding.field.setLong(binding.target, rounded);
                    } else if (type == Long.class) {
                        binding.field.set(binding.target, rounded);
                    } else if (type == short.class) {
                        binding.field.setShort(binding.target, (short) rounded);
                    } else if (type == Short.class) {
                        binding.field.set(binding.target, (short) rounded);
                    } else if (type == byte.class) {
                        binding.field.setByte(binding.target, (byte) rounded);
                    } else if (type == Byte.class) {
                        binding.field.set(binding.target, (byte) rounded);
                    }
                } else {
                    Class<?> type = binding.field.getType();
                    if (type == double.class) {
                        binding.field.setDouble(binding.target, value);
                    } else if (type == Double.class) {
                        binding.field.set(binding.target, value);
                    } else if (type == float.class) {
                        binding.field.setFloat(binding.target, (float) value);
                    } else if (type == Float.class) {
                        binding.field.set(binding.target, (float) value);
                    }
                }
            } catch (IllegalAccessException e) {
                binding.handleException(e);
            }
        }
    }

    private static class BoundTextSetting extends TextSetting {
        private final SettingBinding binding;

        BoundTextSetting(TranslateText text, ConfigOwner parent, String defaultText, Field field, Object target) {
            super(text, parent, defaultText);
            this.binding = new SettingBinding(field, target) {};
        }

        BoundTextSetting(String name, ConfigOwner parent, String defaultText, Field field, Object target) {
            super(name, parent, defaultText);
            this.binding = new SettingBinding(field, target) {};
        }

        @Override
        public void setText(String text) {
            super.setText(text);
            apply(text);
        }

        @Override
        public void reset() {
            super.reset();
            apply(getText());
        }

        private void apply(String text) {
            try {
                binding.field.set(binding.target, text);
            } catch (IllegalAccessException e) {
                binding.handleException(e);
            }
        }
    }

    private static class BoundColorSetting extends ColorSetting {
        private final SettingBinding binding;

        BoundColorSetting(TranslateText text, ConfigOwner parent, Color color, boolean showAlpha, Field field, Object target) {
            super(text, parent, color, showAlpha);
            this.binding = new SettingBinding(field, target) {};
        }

        BoundColorSetting(String name, ConfigOwner parent, Color color, boolean showAlpha, Field field, Object target) {
            super(name, parent, color, showAlpha);
            this.binding = new SettingBinding(field, target) {};
        }

        @Override
        public void setColor(Color color) {
            super.setColor(color);
            apply();
        }

        @Override
        public void setHue(float hue) {
            super.setHue(hue);
            apply();
        }

        @Override
        public void setSaturation(float saturation) {
            super.setSaturation(saturation);
            apply();
        }

        @Override
        public void setBrightness(float brightness) {
            super.setBrightness(brightness);
            apply();
        }

        @Override
        public void setAlpha(int alpha) {
            super.setAlpha(alpha);
            apply();
        }

        @Override
        public void reset() {
            super.reset();
            apply();
        }

        private void apply() {
            try {
                binding.field.set(binding.target, getColor());
            } catch (IllegalAccessException e) {
                binding.handleException(e);
            }
        }
    }

    private static class BoundKeybindSetting extends KeybindSetting {
        private final SettingBinding binding;

        BoundKeybindSetting(TranslateText text, ConfigOwner parent, int keyCode, Field field, Object target) {
            super(text, parent, keyCode);
            this.binding = new SettingBinding(field, target) {};
        }

        BoundKeybindSetting(String name, ConfigOwner parent, int keyCode, Field field, Object target) {
            super(name, parent, keyCode);
            this.binding = new SettingBinding(field, target) {};
        }

        @Override
        public void setKeyCode(int keyCode) {
            super.setKeyCode(keyCode);
            try {
                Class<?> type = binding.field.getType();
                if (type == int.class) {
                    binding.field.setInt(binding.target, keyCode);
                } else {
                    binding.field.set(binding.target, keyCode);
                }
            } catch (IllegalAccessException e) {
                binding.handleException(e);
            }
        }
    }

    private static class BoundImageSetting extends ImageSetting {
        private final SettingBinding binding;

        BoundImageSetting(TranslateText text, ConfigOwner parent, File defaultFile, Field field, Object target) {
            super(text, parent);
            this.binding = new SettingBinding(field, target) {};
            super.setImage(defaultFile);
        }

        BoundImageSetting(String name, ConfigOwner parent, File defaultFile, Field field, Object target) {
            super(name, parent);
            this.binding = new SettingBinding(field, target) {};
            super.setImage(defaultFile);
        }

        @Override
        public void setImage(File image) {
            super.setImage(image);
            apply(image);
        }

        @Override
        public void reset() {
            super.reset();
            apply(getImage());
        }

        private void apply(File image) {
            try {
                binding.field.set(binding.target, image);
            } catch (IllegalAccessException e) {
                binding.handleException(e);
            }
        }
    }

    private static class BoundSoundSetting extends SoundSetting {
        private final SettingBinding binding;

        BoundSoundSetting(TranslateText text, ConfigOwner parent, File defaultFile, Field field, Object target) {
            super(text, parent);
            this.binding = new SettingBinding(field, target) {};
            super.setSound(defaultFile);
        }

        BoundSoundSetting(String name, ConfigOwner parent, File defaultFile, Field field, Object target) {
            super(name, parent);
            this.binding = new SettingBinding(field, target) {};
            super.setSound(defaultFile);
        }

        @Override
        public void setSound(File sound) {
            super.setSound(sound);
            apply(sound);
        }

        @Override
        public void reset() {
            super.reset();
            apply(getSound());
        }

        private void apply(File sound) {
            try {
                binding.field.set(binding.target, sound);
            } catch (IllegalAccessException e) {
                binding.handleException(e);
            }
        }
    }

    private static class BoundEnumSetting extends ComboSetting {
        private final SettingBinding binding;
        private final Map<String, Enum<?>> mapping;

        BoundEnumSetting(TranslateText text, ConfigOwner parent, String defaultKey, List<Option> options, Map<String, Enum<?>> mapping, Field field, Object target) {
            super(text, parent, defaultKey, options);
            this.binding = new SettingBinding(field, target) {};
            this.mapping = mapping;
        }

        BoundEnumSetting(String name, ConfigOwner parent, String defaultKey, List<Option> options, Map<String, Enum<?>> mapping, Field field, Object target) {
            super(name, parent, defaultKey, options);
            this.binding = new SettingBinding(field, target) {};
            this.mapping = mapping;
        }

        void initialize() {
            apply(getOption());
        }

        @Override
        public void setOption(Option option) {
            super.setOption(option);
            apply(option);
        }

        @Override
        public void reset() {
            super.reset();
            apply(getOption());
        }

        private void apply(Option option) {
            Enum<?> value = option != null ? mapping.get(option.getNameKey()) : null;
            if (value == null && !mapping.isEmpty()) {
                value = mapping.values().iterator().next();
            }
            if (value == null) {
                return;
            }
            try {
                binding.field.set(binding.target, value);
            } catch (IllegalAccessException e) {
                binding.handleException(e);
            }
        }
    }

    private static class BoundCellGridSetting extends CellGridSetting {
        private final SettingBinding binding;

        BoundCellGridSetting(TranslateText text, ConfigOwner parent, boolean[][] defaultGrid, Field field, Object target) {
            super(text, parent, copyGrid(defaultGrid));
            this.binding = new SettingBinding(field, target) {};
        }

        BoundCellGridSetting(String name, ConfigOwner parent, boolean[][] defaultGrid, Field field, Object target) {
            super(name, parent, copyGrid(defaultGrid));
            this.binding = new SettingBinding(field, target) {};
        }

        void initialize() {
            apply(getCells());
        }

        @Override
        public void setCells(boolean[][] cells) {
            super.setCells(copyGrid(cells));
            apply(getCells());
        }

        @Override
        public void reset() {
            super.reset();
            apply(getCells());
        }

        private void apply(boolean[][] cells) {
            try {
                binding.field.set(binding.target, copyGrid(cells));
            } catch (IllegalAccessException e) {
                binding.handleException(e);
            }
        }
    }

    private static final class OwnerFieldKey {
        private final ConfigOwner owner;
        private final String fieldName;

        private OwnerFieldKey(ConfigOwner owner, String fieldName) {
            this.owner = owner;
            this.fieldName = fieldName;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            OwnerFieldKey other = (OwnerFieldKey) obj;
            return owner == other.owner && fieldName.equals(other.fieldName);
        }

        @Override
        public int hashCode() {
            int result = System.identityHashCode(owner);
            result = 31 * result + fieldName.hashCode();
            return result;
        }
    }
}
