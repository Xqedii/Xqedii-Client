package dev.xqedii.xqediiclient.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.entity.player.PlayerEntity;

public class CustomGuiScreen extends Screen {
    private static final float SAFE_AREA_PERCENTAGE = 0.90f;
    private static final Identifier GUI_LOGO = Identifier.of("xqediiclient", "textures/ui/logo.png");
    private static final Identifier GUI_NBT_ICON = Identifier.of("xqediiclient", "textures/ui/nbt.png");
    private static final int NBT_BOX_COLOR = 0xFF1D1D1D;
    private static final int MAIN_CONTENT_COLOR = 0xFF161616;
    private static final int BUTTON_COLOR = 0xFFDEAC25;
    private static final int BUTTON_HOVER_COLOR = 0xFFF0D656;
    private static final int TEXT_COLOR_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_COLOR_BUTTON = 0xFFFFFFFF;
    private static final float TITLE_SCALE = 2.5f;
    private static final float BUTTON_TEXT_SCALE = 1.7f;
    private static final float ANIMATION_TIME_SECONDS = 0.10f;
    private final float[] buttonHoverProgress = new float[3];
    private float button1Left, button1Top, button1Width, button1Height;
    private float button2Left, button2Top, button2Width, button2Height;
    private float button3Left, button3Top, button3Width, button3Height;
    private float totalGameScale;
    private long lastRenderTime;

    public CustomGuiScreen() {
        super(Text.empty());
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < buttonHoverProgress.length; i++) {
            buttonHoverProgress[i] = 0.0f;
        }
        this.lastRenderTime = System.nanoTime();
    }

    private void playClickSound() {
        if (client != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(
                    SoundEvent.of(Identifier.of("minecraft", "ui.hud.bubble_pop")), 2.0f, 2.0f
            ));
        }
    }

    private int lerpColor(int from, int to, float progress) {
        float R1 = (from >> 16) & 0xFF;
        float G1 = (from >> 8) & 0xFF;
        float B1 = from & 0xFF;
        float R2 = (to >> 16) & 0xFF;
        float G2 = (to >> 8) & 0xFF;
        float B2 = to & 0xFF;
        int R = (int) MathHelper.lerp(progress, R1, R2);
        int G = (int) MathHelper.lerp(progress, G1, G2);
        int B = (int) MathHelper.lerp(progress, B1, B2);
        return 0xFF000000 | (R << 16) | (G << 8) | B;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        long currentTime = System.nanoTime();
        float elapsedSeconds = (currentTime - this.lastRenderTime) / 1_000_000_000.0f;
        this.lastRenderTime = currentTime;
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
        float mainContentHeight = finalPhysicalSidebarHeight;
        drawAntiAliasedRoundedRect(context, mainContentLeft, mainContentTop, mainContentWidth, mainContentHeight, finalPhysicalSidebarRadius, MAIN_CONTENT_COLOR);
        Text titleText = Text.of("NBT Tools");
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
        float finalPhysicalButtonHeight = (baseButtonHeight / baseRatioWidth) * finalPhysicalWidth;
        float buttonPadding = mainContentGap;
        float currentButtonY = titleY + (textRenderer.fontHeight * dynamicTitleScale) + mainContentGap;
        this.button1Width = mainContentWidth - (buttonPadding * 2);
        this.button1Left = mainContentLeft + buttonPadding;
        this.button1Top = currentButtonY;
        this.button1Height = finalPhysicalButtonHeight;
        currentButtonY += finalPhysicalButtonHeight + mainContentGap;
        this.button2Width = mainContentWidth - (buttonPadding * 2);
        this.button2Left = mainContentLeft + buttonPadding;
        this.button2Top = currentButtonY;
        this.button2Height = finalPhysicalButtonHeight;
        currentButtonY += finalPhysicalButtonHeight + mainContentGap;
        this.button3Width = mainContentWidth - (buttonPadding * 2);
        this.button3Left = mainContentLeft + buttonPadding;
        this.button3Top = currentButtonY;
        this.button3Height = finalPhysicalButtonHeight;
        float physicalMouseX = (float) (mouseX * this.totalGameScale);
        float physicalMouseY = (float) (mouseY * this.totalGameScale);
        float animationChange = elapsedSeconds / ANIMATION_TIME_SECONDS;
        boolean isHoveringBtn1 = isMouseOver(physicalMouseX, physicalMouseY, button1Left, button1Top, button1Width, button1Height);
        buttonHoverProgress[0] = MathHelper.clamp(buttonHoverProgress[0] + (isHoveringBtn1 ? animationChange : -animationChange), 0.0f, 1.0f);
        boolean isHoveringBtn2 = isMouseOver(physicalMouseX, physicalMouseY, button2Left, button2Top, button2Width, button2Height);
        buttonHoverProgress[1] = MathHelper.clamp(buttonHoverProgress[1] + (isHoveringBtn2 ? animationChange : -animationChange), 0.0f, 1.0f);
        boolean isHoveringBtn3 = isMouseOver(physicalMouseX, physicalMouseY, button3Left, button3Top, button3Width, button3Height);
        buttonHoverProgress[2] = MathHelper.clamp(buttonHoverProgress[2] + (isHoveringBtn3 ? animationChange : -animationChange), 0.0f, 1.0f);
        float buttonRadius = finalPhysicalSidebarRadius / 1.5f;
        float dynamicButtonTextScale = BUTTON_TEXT_SCALE * finalContentScale;
        drawButton(context, textRenderer, "Main Hand NBT", button1Left, button1Top, button1Width, button1Height, buttonRadius, dynamicButtonTextScale, 0);
        drawButton(context, textRenderer, "Targeted Entity NBT", button2Left, button2Top, button2Width, button2Height, buttonRadius, dynamicButtonTextScale, 1);
        drawButton(context, textRenderer, "GUI Item NBT", button3Left, button3Top, button3Width, button3Height, buttonRadius, dynamicButtonTextScale, 2);
        matrices.pop();
    }

    private void drawButton(DrawContext context, TextRenderer textRenderer, String text, float x, float y, float width, float height, float radius, float textScale, int buttonIndex) {
        int finalButtonColor = lerpColor(BUTTON_COLOR, BUTTON_HOVER_COLOR, buttonHoverProgress[buttonIndex]);
        drawAntiAliasedRoundedRect(context, x, y, width, height, radius, finalButtonColor);
        Text buttonText = Text.of(text);
        float buttonTextWidth = textRenderer.getWidth(buttonText) * textScale;
        float buttonTextX = x + (width - buttonTextWidth) / 2.0f;
        float buttonTextY = y + (height - (textRenderer.fontHeight * textScale)) / 2.0f;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(buttonTextX, buttonTextY, 0);
        matrices.scale(textScale, textScale, 1);
        context.drawText(textRenderer, buttonText, 0, 0, TEXT_COLOR_BUTTON, true);
        matrices.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double physicalMouseX = mouseX * this.totalGameScale;
        double physicalMouseY = mouseY * this.totalGameScale;
        if (button == 0) {
            if (isMouseOver(physicalMouseX, physicalMouseY, button1Left, button1Top, button1Width, button1Height)) {
                playClickSound();
                handleCopyMainHandNbt();
                return true;
            }
            if (isMouseOver(physicalMouseX, physicalMouseY, button2Left, button2Top, button2Width, button2Height)) {
                playClickSound();
                handleGetEntityNbt();
                return true;
            }
            if (isMouseOver(physicalMouseX, physicalMouseY, button3Left, button3Top, button3Width, button3Height)) {
                playClickSound();
                handleGetGuiItemNbt();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOver(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void handleCopyMainHandNbt() {
        if (client == null || client.player == null) return;
        client.setScreen(null);
        ItemStack stack = client.player.getMainHandStack();
        if (stack.isEmpty()) {
            client.player.sendMessage(Text.literal("You are not holding any object in your hand.").formatted(Formatting.RED), false);
            return;
        }
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler == null) return;
        RegistryWrapper.WrapperLookup registry = networkHandler.getRegistryManager();
        NbtElement nbtElement = stack.toNbt(registry);
        if (nbtElement instanceof NbtCompound nbtCompound && !nbtCompound.isEmpty()) {
            NbtFormatter.sendFormattedNbt(nbtCompound, client.player);
        } else {
            client.player.sendMessage(Text.literal("This item has no NBT data.").formatted(Formatting.YELLOW), false);
        }
    }

    private void handleGetEntityNbt() {
        if (client == null || client.player == null) return;
        client.setScreen(null);

        Entity targetedEntity = client.targetedEntity;
        if (targetedEntity == null) {
            client.player.sendMessage(Text.literal("You are not looking at any entity.").formatted(Formatting.RED), false);
            return;
        }

        NbtCompound nbt = new NbtCompound();
        targetedEntity.writeNbt(nbt);

        if (targetedEntity instanceof PlayerEntity playerEntity) {
            GameProfile gameProfile = playerEntity.getGameProfile();

            NbtCompound skinInfo = new NbtCompound();

            skinInfo.putString("Name", gameProfile.getName());
            skinInfo.putString("UUID", gameProfile.getId().toString());

            for (Property textureProperty : gameProfile.getProperties().get("textures")) {
                skinInfo.putString("Value", textureProperty.value());
                skinInfo.putString("Signature", textureProperty.signature());
            }

            if (skinInfo.isEmpty()) {
                skinInfo.putString("Info", "No texture data found in GameProfile.");
            }

            nbt.put("XqediiClient_SkinInfo", skinInfo);
        }
        if (!nbt.isEmpty()) {
            NbtFormatter.sendFormattedNbt(nbt, client.player);
        } else {
            client.player.sendMessage(Text.literal("This entity has no NBT data.").formatted(Formatting.YELLOW), false);
        }
    }

    private void handleGetGuiItemNbt() {
        if (client == null || client.player == null) return;

        ScreenInteractionHandler.setNbtSelectMode(true, client);
    }


    private void drawAntiAliasedRoundedRect(DrawContext context, float x, float y, float width, float height, float radius, int color) {
        if (width <= 0 || height <= 0) return;
        float x2 = x + width;
        float y2 = y + height;
        radius = Math.min(Math.min(width, height) / 2.0f, radius);
        context.fill((int) (x + radius), (int) y, (int) (x2 - radius), (int) y2, color);
        context.fill((int) x, (int) (y + radius), (int) (x2), (int) (y2 - radius), color);
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
                        int finalAlpha = (int) (a * alphaFactor);
                        int finalColor = (finalAlpha << 24) | (r << 16) | (g << 8) | b;
                        float drawX = 0, drawY = 0;
                        switch (quadrant) {
                            case 0:
                                drawX = centerX - x_offset;
                                drawY = centerY - y_offset;
                                break;
                            case 1:
                                drawX = centerX + x_offset - 1;
                                drawY = centerY - y_offset;
                                break;
                            case 2:
                                drawX = centerX - x_offset;
                                drawY = centerY + y_offset - 1;
                                break;
                            case 3:
                                drawX = centerX + x_offset - 1;
                                drawY = centerY + y_offset - 1;
                                break;
                        }
                        context.fill((int) drawX, (int) drawY, (int) drawX + 1, (int) drawY + 1, finalColor);
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