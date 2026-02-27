package igrova.enemy.effects.mixin;

import igrova.enemy.effects.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

	@Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
			at = @At(value = "RETURN"))
	private void onRender(AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		// Собираем активные эффекты (длительность > 0 или -1)
		List<StatusEffectInstance> activeEffects = new ArrayList<>();
		for (StatusEffectInstance effect : player.getStatusEffects()) {
			int duration = effect.getDuration();
			if (duration > 0 || duration == -1) {
				activeEffects.add(effect);
			}
		}
		if (activeEffects.isEmpty()) return;

		// Проверка невидимости с учётом брони
		if (player.isInvisible()) {
			boolean hasArmor = false;
			for (ItemStack stack : player.getArmorItems()) {
				if (!stack.isEmpty()) {
					hasArmor = true;
					break;
				}
			}
			if (!hasArmor) return;
		}

		// Получаем конфиг
		ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

		// Фильтруем по видимости
		List<StatusEffectInstance> visibleEffects = new ArrayList<>();
		for (StatusEffectInstance effect : activeEffects) {
			Identifier effectId = Registry.STATUS_EFFECT.getId(effect.getEffectType());
			if (effectId == null) continue;
			String key = effectId.toString();
			if (config.effectVisibility.getOrDefault(key, true)) {
				visibleEffects.add(effect);
			}
		}
		if (visibleEffects.isEmpty()) return;

		// Рендерим только visibleEffects
		matrices.push();
		double height = player.getHeight() + 1.0;
		matrices.translate(0, height, 0);
		matrices.multiply(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
		float scale = 0.025f;
		matrices.scale(-scale, -scale, scale);

		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		List<Text> lines = new ArrayList<>();
		for (StatusEffectInstance effect : visibleEffects) {
			Identifier effectId = Registry.STATUS_EFFECT.getId(effect.getEffectType());
			if (effectId == null) continue;
			String key = effectId.toString();
			int color = config.effectColors.getOrDefault(key, effect.getEffectType().getColor());

			String name = effect.getEffectType().getName().getString();
			int amp = effect.getAmplifier() + 1;
			String durationStr;
			int duration = effect.getDuration();
			if (duration == -1) {
				durationStr = "∞";
			} else {
				durationStr = (duration / 20) + "s";
			}
			String lineText = name + " " + amp + "(" + durationStr + ")";
			LiteralText text = new LiteralText(lineText);
			text.setStyle(text.getStyle().withColor(TextColor.fromRgb(color)));
			lines.add(text);
		}

		int lineHeight = textRenderer.fontHeight;
		int totalHeight = lines.size() * lineHeight;
		int startY = -totalHeight / 2;

		for (int i = 0; i < lines.size(); i++) {
			Text line = lines.get(i);
			float x = -textRenderer.getWidth(line) / 2.0f;
			float y = startY + i * lineHeight;
			textRenderer.draw(line, x, y, 0xFFFFFF, false, matrices.peek().getModel(), vertexConsumers, false, 0, light);
		}

		matrices.pop();
	}
}