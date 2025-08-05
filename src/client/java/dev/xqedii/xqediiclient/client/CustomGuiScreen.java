package dev.xqedii.xqediiclient.client.gui;

import dev.xqedii.xqediiclient.client.GUIHelper;
import dev.xqedii.xqediiclient.client.NbtFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class CustomGuiScreen extends Screen {
    private static final float SAFE_AREA_PERCENTAGE = 0.90f;
    private static final Identifier GUI_LOGO = Identifier.of("xqediiclient", "textures/ui/logo.png");
    private static final Identifier GUI_NBT_ICON = Identifier.of("xqediiclient", "textures/ui/nbt.png");

    private static final int NBT_BOX_COLOR = 0xFF1D1D1D;
    private static final int MAIN_CONTENT_COLOR = 0xFF161616;
    private static final int BUTTON_COLOR = 0xFFDEAC25;
    private static final int TEXT_COLOR_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_COLOR_BUTTON = 0xFFFFFFFF;

    private static final float TITLE_SCALE = 2.5f;
    private static final float BUTTON_TEXT_SCALE = 1.7f;

    private float buttonLeft, buttonTop, buttonWidth, finalPhysicalButtonHeight;
    private float totalGameScale;

    public CustomGuiScreen() {
        super(Text.empty());
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        TextRenderer textRenderer = client.textRenderer;

        float framebufferWidth = window.getFramebufferWidth();
        float framebufferHeight = window.getFramebufferHeight();
        float designWidth = 1366.0f;
        float designHeight = 682.0f;
        float safeAreaWidth = framebufferWidth * SAFE_AREA_PERCENTAGE;
        float safeAreaHeight = framebufferHeight * SAFE_AREA_PERCENTAGE;
        float scaleX = safeAreaWidth / designWidth;
        float scaleY = safeAreaHeight / designHeight;
        float finalContentScale = Math.min(1.0f, Math.min(scaleX, scaleY));
        float finalPhysicalWidth = designWidth * finalContentScale;
        float finalPhysicalHeight = designHeight * finalContentScale;

        float baseRatioWidth = 385.0f;
        float finalPhysicalRadius = (10.0f / baseRatioWidth) * finalPhysicalWidth;
        float finalPhysicalSidebarPadding = (9.0f / baseRatioWidth) * finalPhysicalWidth;
        float finalPhysicalSidebarWidth = (23.0f / baseRatioWidth) * finalPhysicalWidth;
        float finalPhysicalSidebarRadius = (6.0f / baseRatioWidth) * finalPhysicalWidth;
        float finalPhysicalLogoPadding = (3.0f / baseRatioWidth) * finalPhysicalWidth;
        float finalPhysicalLeft = (framebufferWidth - finalPhysicalWidth) / 2.0f;
        float finalPhysicalTop = (framebufferHeight - finalPhysicalHeight) / 2.0f;

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        this.totalGameScale = framebufferWidth / window.getScaledWidth();
        matrices.scale(1.0f / totalGameScale, 1.0f / totalGameScale, 1.0f);

        drawAntiAliasedRoundedRect(context, finalPhysicalLeft, finalPhysicalTop, finalPhysicalWidth, finalPhysicalHeight, finalPhysicalRadius, 0xFF0D0D0D);
        float finalPhysicalSidebarLeft = finalPhysicalLeft + finalPhysicalSidebarPadding;
        float finalPhysicalSidebarTop = finalPhysicalTop + finalPhysicalSidebarPadding;
        float finalPhysicalSidebarHeight = finalPhysicalHeight - (finalPhysicalSidebarPadding * 2);
        drawAntiAliasedRoundedRect(context, finalPhysicalSidebarLeft, finalPhysicalSidebarTop, finalPhysicalSidebarWidth, finalPhysicalSidebarHeight, finalPhysicalSidebarRadius, MAIN_CONTENT_COLOR);

        float logoContainerWidth = finalPhysicalSidebarWidth;
        float iconX = finalPhysicalSidebarLeft + finalPhysicalLogoPadding;
        float iconY = finalPhysicalSidebarTop + finalPhysicalLogoPadding;
        float iconSize = logoContainerWidth - (finalPhysicalLogoPadding * 2);
        if (iconSize > 0) GUIHelper.drawTexture(context, GUI_LOGO, (int) iconX, (int) iconY, (int) iconSize);
        float gap = finalPhysicalLogoPadding * 2;
        float nbtBoxLeft = finalPhysicalSidebarLeft + finalPhysicalLogoPadding;
        float nbtBoxTop = finalPhysicalSidebarTop + logoContainerWidth + gap;
        float nbtBoxWidth = finalPhysicalSidebarWidth - (finalPhysicalLogoPadding * 2);
        drawAntiAliasedRoundedRect(context, nbtBoxLeft, nbtBoxTop, nbtBoxWidth, nbtBoxWidth, finalPhysicalSidebarRadius, NBT_BOX_COLOR);
        float nbtIconX = nbtBoxLeft + finalPhysicalLogoPadding;
        float nbtIconY = nbtBoxTop + finalPhysicalLogoPadding;
        float nbtIconSize = nbtBoxWidth - (finalPhysicalLogoPadding * 2);
        if (nbtIconSize > 0) GUIHelper.drawTexture(context, GUI_NBT_ICON, (int) nbtIconX, (int) nbtIconY, (int) nbtIconSize);

        float mainContentGap = finalPhysicalSidebarPadding;
        float mainContentLeft = finalPhysicalSidebarLeft + finalPhysicalSidebarWidth + mainContentGap;
        float mainContentTop = finalPhysicalSidebarTop;
        float remainingWidth = finalPhysicalWidth - (finalPhysicalSidebarPadding * 3) - finalPhysicalSidebarWidth;
        float mainContentWidth = remainingWidth / 2.0f;
        float mainContentHeight = finalPhysicalSidebarHeight / 2.0f;
        drawAntiAliasedRoundedRect(context, mainContentLeft, mainContentTop, mainContentWidth, mainContentHeight, finalPhysicalSidebarRadius, MAIN_CONTENT_COLOR);

        Text titleText = Text.of("Copy NBT Tags");
        float dynamicTitleScale = TITLE_SCALE * finalContentScale;
        float titleTextWidth = textRenderer.getWidth(titleText) * dynamicTitleScale;
        float titleX = mainContentLeft + (mainContentWidth - titleTextWidth) / 2.0f;
        float titleY = mainContentTop + mainContentGap;
        matrices.push();
        matrices.translate(titleX, titleY, 0);
        matrices.scale(dynamicTitleScale, dynamicTitleScale, 1);
        context.drawText(textRenderer, titleText, 0, 0, TEXT_COLOR_PRIMARY, true);
        matrices.pop();

        float baseButtonHeight = 16.0f;
        this.finalPhysicalButtonHeight = (baseButtonHeight / baseRatioWidth) * finalPhysicalWidth;
        float buttonPadding = mainContentGap;
        this.buttonWidth = mainContentWidth - (buttonPadding * 2);
        this.buttonLeft = mainContentLeft + buttonPadding;
        this.buttonTop = titleY + (textRenderer.fontHeight * dynamicTitleScale) + mainContentGap;
        float buttonRadius = finalPhysicalSidebarRadius / 1.5f;
        drawAntiAliasedRoundedRect(context, buttonLeft, buttonTop, buttonWidth, finalPhysicalButtonHeight, buttonRadius, BUTTON_COLOR);

        Text buttonText = Text.of("Copy NBT");
        float dynamicButtonTextScale = BUTTON_TEXT_SCALE * finalContentScale;
        float buttonTextWidth = textRenderer.getWidth(buttonText) * dynamicButtonTextScale;
        float buttonTextX = buttonLeft + (buttonWidth - buttonTextWidth) / 2.0f;
        float buttonTextY = buttonTop + (finalPhysicalButtonHeight - (textRenderer.fontHeight * dynamicButtonTextScale)) / 2.0f;
        matrices.push();
        matrices.translate(buttonTextX, buttonTextY, 0);
        matrices.scale(dynamicButtonTextScale, dynamicButtonTextScale, 1);
        context.drawText(textRenderer, buttonText, 0, 0, TEXT_COLOR_BUTTON, true);
        matrices.pop();

        matrices.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double physicalMouseX = mouseX * this.totalGameScale;
        double physicalMouseY = mouseY * this.totalGameScale;

        if (button == 0 &&
                physicalMouseX >= this.buttonLeft &&
                physicalMouseX <= this.buttonLeft + this.buttonWidth &&
                physicalMouseY >= this.buttonTop &&
                physicalMouseY <= this.buttonTop + this.finalPhysicalButtonHeight) {
            handleCopyNbt();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleCopyNbt() {
        if (client == null || client.player == null) return;
        client.setScreen(null);

        ItemStack stack = client.player.getMainHandStack();
        if (stack.isEmpty()) {
            client.player.sendMessage(Text.literal("You are not holding any object in your hand.").formatted(Formatting.RED), false);
            return;
        }

        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler == null) {
            client.player.sendMessage(Text.literal("No connection to the server, unable to transfer NBT data.").formatted(Formatting.RED), false);
            return;
        }

        RegistryWrapper.WrapperLookup registry = networkHandler.getRegistryManager();
        NbtElement nbtElement = stack.toNbt(registry);

        if (nbtElement instanceof NbtCompound nbtCompound && !nbtCompound.isEmpty()) {
            NbtFormatter.sendFormattedNbt(nbtCompound, client.player);
        } else {
            client.player.sendMessage(Text.literal("This item have no NBT data.").formatted(Formatting.YELLOW), false);
        }
    }

    private void drawAntiAliasedRoundedRect(DrawContext context, float x, float y, float width, float height, float radius, int color) {
        if (width <= 0 || height <= 0) return;

        float x2 = x + width;
        float y2 = y + height;
        radius = Math.min(Math.min(width, height) / 2.0f, radius);

        context.fill((int)(x + radius), (int)y, (int)(x2 - radius), (int)y2, color);
        context.fill((int)x, (int)(y + radius), (int)(x2), (int)(y2 - radius), color);

        drawOptimizedAntiAliasedQuarterCircle(context, x + radius, y + radius, radius, 0, color);
        drawOptimizedAntiAliasedQuarterCircle(context, x2 - radius, y + radius, radius, 1, color);
        drawOptimizedAntiAliasedQuarterCircle(context, x + radius, y2 - radius, radius, 2, color);
        drawOptimizedAntiAliasedQuarterCircle(context, x2 - radius, y2 - radius, radius, 3, color);
    }

    private void drawOptimizedAntiAliasedQuarterCircle(DrawContext context, float centerX, float centerY, float radius, int quadrant, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;
        if (a == 0) a = 255;

        float radiusWithFeatherSq = (radius + 1.0f) * (radius + 1.0f);

        for (int y_offset = 0; y_offset <= Math.ceil(radius) + 1; y_offset++) {
            float y_offset_sq = y_offset * y_offset;

            for (int x_offset = 0; x_offset <= Math.ceil(radius) + 1; x_offset++) {
                float distSq = x_offset * x_offset + y_offset_sq;

                if (distSq <= radiusWithFeatherSq) {
                    double dist = Math.sqrt(distSq);
                    double alphaFactor = MathHelper.clamp(1.0 - (dist - (radius - 0.5f)), 0.0, 1.0);

                    if (alphaFactor > 0) {
                        int finalAlpha = (int)(a * alphaFactor);
                        int finalColor = (finalAlpha << 24) | (r << 16) | (g << 8) | b;

                        float drawX = 0, drawY = 0;
                        switch (quadrant) {
                            case 0: drawX = centerX - x_offset; drawY = centerY - y_offset; break;
                            case 1: drawX = centerX + x_offset - 1; drawY = centerY - y_offset; break;
                            case 2: drawX = centerX - x_offset; drawY = centerY + y_offset - 1; break;
                            case 3: drawX = centerX + x_offset - 1; drawY = centerY + y_offset - 1; break;
                        }
                        context.fill((int)drawX, (int)drawY, (int)drawX + 1, (int)drawY + 1, finalColor);
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}