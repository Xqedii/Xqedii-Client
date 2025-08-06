package dev.xqedii.xqediiclient.client;

import dev.xqedii.xqediiclient.client.CustomGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class XqediiclientClient implements ClientModInitializer {

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        ScreenInteractionHandler.initialize();
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xqediiclient.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.category.xqediiclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.wasPressed()) {
                client.setScreen(new CustomGuiScreen());
            }
        });
    }
}