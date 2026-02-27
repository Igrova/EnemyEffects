package igrova.enemy.effects;

import igrova.enemy.effects.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;

public class EnemyEffectsMod implements ModInitializer {
	public static final String MOD_ID = "igrova.enemy.effects";

	@Override
	public void onInitialize() {
		// Регистрируем конфиг
		AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
	}
}