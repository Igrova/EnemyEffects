package igrova.enemy.effects.client;

import igrova.enemy.effects.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EffectsConfigScreen {
    public static Screen create(Screen parent) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new LiteralText("Enemy Effects Config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // === Категория цветов ===
        ConfigCategory colorCategory = builder.getOrCreateCategory(new LiteralText("Effect Colors"));

        // === Категория видимости ===
        ConfigCategory visibilityCategory = builder.getOrCreateCategory(new LiteralText("Effect Visibility"));

        for (Identifier effectId : Registry.STATUS_EFFECT.getIds()) {
            StatusEffect effect = Registry.STATUS_EFFECT.get(effectId);
            if (effect == null) continue;

            String effectName = effectId.toString();
            int nativeColor = effect.getColor();

            // ----- Поле цвета (в первой категории) -----
            int currentColor = config.effectColors.getOrDefault(effectName, nativeColor);
            colorCategory.addEntry(entryBuilder.startStrField(
                            new LiteralText(effectName),
                            String.format("#%06X", currentColor)
                    )
                    .setDefaultValue(String.format("#%06X", nativeColor))
                    .setTooltip(new LiteralText("Enter color in #RRGGBB format (native: " + String.format("#%06X", nativeColor) + ")"))
                    .setSaveConsumer(newValue -> {
                        try {
                            String hex = newValue.startsWith("#") ? newValue.substring(1) : newValue;
                            int color = Integer.parseInt(hex, 16);
                            config.effectColors.put(effectName, color);
                        } catch (NumberFormatException e) {
                            config.effectColors.put(effectName, nativeColor);
                        }
                    })
                    .build());

            // ----- Чекбокс видимости (во второй категории) -----
            // Если в мапе нет значения, считаем true (показывать)
            boolean currentVisible = config.effectVisibility.getOrDefault(effectName, true);
            visibilityCategory.addEntry(entryBuilder.startBooleanToggle(
                            new LiteralText(effectName),
                            currentVisible
                    )
                    .setDefaultValue(true) // по умолчанию показывать
                    .setTooltip(new LiteralText("Toggle visibility of this effect above head"))
                    .setSaveConsumer(newValue -> {
                        // Сохраняем только если значение отличается от true? Можно всегда сохранять.
                        // Но если true и ключа нет в мапе, можно не сохранять для чистоты.
                        if (!newValue) {
                            config.effectVisibility.put(effectName, false);
                        } else {
                            config.effectVisibility.remove(effectName); // удаляем, чтобы использовалось значение по умолчанию (true)
                        }
                    })
                    .build());
        }

        builder.setSavingRunnable(() -> {
            AutoConfig.getConfigHolder(ModConfig.class).setConfig(config);
            AutoConfig.getConfigHolder(ModConfig.class).save();
        });

        return builder.build();
    }
}