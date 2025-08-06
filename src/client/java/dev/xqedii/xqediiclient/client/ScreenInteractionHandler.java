package dev.xqedii.xqediiclient.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class ScreenInteractionHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger("XqediiClient");
    private static boolean nbtSelectModeActive = false;
    private static Method getSlotAtMethodCache = null;

    public static void initialize() {
        ScreenEvents.AFTER_INIT.register(ScreenInteractionHandler::onScreenOpen);
    }

    public static void setNbtSelectMode(boolean active, MinecraftClient client) {
        nbtSelectModeActive = active;
        if (active && client.player != null) {
            client.setScreen(null);
            client.player.sendMessage(Text.literal("NBT select mode enabled. Left-click an item in an inventory to get its NBT.")
                    .formatted(Formatting.AQUA), false);
        }
    }

    private static void onScreenOpen(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof HandledScreen<?> handledScreen) {
            ScreenMouseEvents.afterMouseClick(screen).register((s, mouseX, mouseY, button) -> {
                if (nbtSelectModeActive) {

                    try {
                        if (getSlotAtMethodCache == null) {
                            Class<?> currentClass = s.getClass();
                            while (currentClass != Object.class) {
                                for (Method method : currentClass.getDeclaredMethods()) {
                                    if (method.getReturnType() == Slot.class && method.getParameterCount() == 2 &&
                                            method.getParameterTypes()[0] == double.class && method.getParameterTypes()[1] == double.class) {
                                        getSlotAtMethodCache = method;
                                        getSlotAtMethodCache.setAccessible(true);
                                        break;
                                    }
                                }
                                if (getSlotAtMethodCache != null) break;
                                currentClass = currentClass.getSuperclass();
                            }
                        }
                        if (getSlotAtMethodCache == null) throw new NoSuchMethodException("Could not find a compatible method.");

                        Object result = getSlotAtMethodCache.invoke(s, mouseX, mouseY);

                        if (result instanceof Slot clickedSlot) {
                            ItemStack cursorStack = handledScreen.getScreenHandler().getCursorStack();

                            if (!cursorStack.isEmpty()) {
                                processAndDisplayNbt(client, cursorStack);

                                if (client.interactionManager != null && client.player != null) {
                                    client.interactionManager.clickSlot(
                                            handledScreen.getScreenHandler().syncId,
                                            clickedSlot.id,
                                            0,
                                            SlotActionType.PICKUP,
                                            client.player
                                    );
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to process NBT click.", e);
                    }
                }
            });
        }
    }

    private static void processAndDisplayNbt(MinecraftClient client, ItemStack stack) {
        if (client == null || client.player == null || stack.isEmpty()) return;
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler == null) return;
        RegistryWrapper.WrapperLookup registry = networkHandler.getRegistryManager();
        NbtElement nbtElement = stack.toNbt(registry);
        if (nbtElement instanceof NbtCompound nbtCompound && !nbtCompound.isEmpty()) {
            nbtSelectModeActive = false;
            NbtFormatter.sendFormattedNbt(nbtCompound, client.player);
        } else {
            client.player.sendMessage(Text.literal("This item has no NBT data.").formatted(Formatting.YELLOW), false);
        }
    }
}