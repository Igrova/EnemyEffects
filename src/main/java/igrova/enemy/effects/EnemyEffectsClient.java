package igrova.enemy.effects;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

public class EnemyEffectsClient implements ClientModInitializer {
    private static boolean renderingEnabled = true;
    private static KeyBinding toggleKeyBinding;

    @Override
    public void onInitializeClient() {
        // Регистрируем клавишу
        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.enemy_effects.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.enemy_effects"
        ));

        // Обрабатываем нажатия
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKeyBinding.wasPressed()) {
                toggleRendering();
                if (client.player != null) {
                    client.player.sendMessage(
                            new LiteralText("§eEffect rendering " + (renderingEnabled ? "§aenabled" : "§cdisabled")),
                            true // в action bar
                    );
                }
            }
        });
    }

    public static void toggleRendering() {
        renderingEnabled = !renderingEnabled;
    }

    public static boolean isRenderingEnabled() {
        return renderingEnabled;
    }
}