package dev.xqedii.xqediiclient.mixin;

import dev.xqedii.xqediiclient.client.ClickNbtHandler;
import dev.xqedii.xqediiclient.client.NbtFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow @Nullable protected Slot focusedSlot;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClickedMixin(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ClickNbtHandler.isWaitingForNbt) {
            ClickNbtHandler.isWaitingForNbt = false;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) {
                cir.cancel();
                return;
            }

            client.player.closeHandledScreen();

            if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
                ItemStack stack = this.focusedSlot.getStack();
                ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
                if (networkHandler == null) {
                    cir.cancel();
                    return;
                }

                RegistryWrapper.WrapperLookup registry = networkHandler.getRegistryManager();
                NbtElement nbtElement = stack.toNbt(registry);

                if (nbtElement instanceof NbtCompound nbtCompound && !nbtCompound.isEmpty()) {
                    NbtFormatter.sendFormattedNbt(nbtCompound, client.player);
                } else {
                    client.player.sendMessage(Text.literal("This item has no NBT data.").formatted(Formatting.YELLOW), false);
                }
            } else {
                client.player.sendMessage(Text.literal("You did not click on a slot with an item.").formatted(Formatting.RED), false);
            }

            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}