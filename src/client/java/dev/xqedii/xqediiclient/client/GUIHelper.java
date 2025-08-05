package dev.xqedii.xqediiclient.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class GUIHelper {
    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        context.drawTexture(RenderLayer::getGuiTextured,
                texture,
                x,
                y,
                0f,
                0f,
                width,
                height,
                width,
                height);
    }

    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, int size) {
        drawTexture(context, texture, x, y, size, size);
    }
}
