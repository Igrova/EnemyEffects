package igrova.enemy.effects.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.HashMap;
import java.util.Map;

@Config(name = "enemy_effects")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public Map<String, Integer> effectColors = new HashMap<>();

    @ConfigEntry.Gui.Tooltip
    public Map<String, Boolean> effectVisibility = new HashMap<>();

    public ModConfig() {
        // Значения по умолчанию для цветов не задаём — они будут браться из родного цвета эффекта
        // Для видимости по умолчанию все эффекты видны (если ключа нет в мапе, считаем true)
    }
}