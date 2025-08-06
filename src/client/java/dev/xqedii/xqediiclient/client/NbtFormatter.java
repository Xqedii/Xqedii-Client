package dev.xqedii.xqediiclient.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;

public class NbtFormatter {

    private static final TextColor COLOR_PUNCTUATION = TextColor.fromRgb(0x8c8c8c);
    private static final TextColor COLOR_KEY = TextColor.fromRgb(0xd7ba7d);
    private static final TextColor COLOR_VALUE = TextColor.fromRgb(0x5498d0);

    private static final Text HOVER_TEXT = Text.translatable("chat.copy.click").formatted(net.minecraft.util.Formatting.GRAY);

    public static void sendFormattedNbt(NbtElement nbt, ClientPlayerEntity player) {
        if (nbt == null) return;

        String prettyNbt = prettyPrintNbt(nbt.toString());
        String[] lines = prettyNbt.split("\n");

        for (String line : lines) {
            player.sendMessage(formatLine(line), false);
        }
        player.getWorld().playSound(
                player,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT,
                SoundCategory.PLAYERS,
                1.0f,
                2.0f
        );
    }

    private static Text formatLine(String line) {
        MutableText formattedLine = Text.empty();

        int indentDepth = 0;
        while (indentDepth < line.length() && Character.isWhitespace(line.charAt(indentDepth))) {
            indentDepth++;
        }
        formattedLine.append(line.substring(0, indentDepth));

        String trimmedLine = line.trim();

        String[] tokens = trimmedLine.split("(?<=[,:{}\\[\\]\"])|(?=[\",:{}\\[\\]])");

        boolean afterColon = false;

        for (String token : tokens) {
            if (token.isBlank()) {
                formattedLine.append(token);
                continue;
            }

            if (token.equals(":") || token.equals(",") || token.equals("{") || token.equals("}") || token.equals("[") || token.equals("]") || token.equals("\"")) {
                formattedLine.append(Text.literal(token).styled(s -> s.withColor(COLOR_PUNCTUATION)));
                if (token.equals(":")) {
                    afterColon = true;
                }
            }
            else if (afterColon) {
                formattedLine.append(createClickableValueText(token, COLOR_VALUE));
            }
            else {
                formattedLine.append(Text.literal(token).styled(s -> s.withColor(COLOR_KEY)));
            }
        }

        return formattedLine;
    }

    private static MutableText createClickableValueText(String text, TextColor color) {
        String copyText = text;
        if (copyText.matches(".*[bslfdBSLFD]$")) {
            copyText = copyText.substring(0, copyText.length() - 1);
        }

        Style style = Style.EMPTY
                .withColor(color)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, HOVER_TEXT));

        return Text.literal(text).setStyle(style);
    }

    private static String prettyPrintNbt(String rawNbt) {
        StringBuilder pretty = new StringBuilder();
        int indentLevel = 0;
        boolean inString = false;
        boolean escaped = false;

        for (char c : rawNbt.toCharArray()) {
            if (escaped) {
                pretty.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\') {
                pretty.append(c);
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
            }

            if (!inString) {
                if (c == '{' || c == '[') {
                    pretty.append(c).append('\n');
                    indentLevel++;
                    pretty.append("  ".repeat(indentLevel));
                } else if (c == '}' || c == ']') {
                    pretty.append('\n');
                    indentLevel--;
                    pretty.append("  ".repeat(indentLevel));
                    pretty.append(c);
                } else if (c == ',') {
                    pretty.append(c).append('\n');
                    pretty.append("  ".repeat(indentLevel));
                } else if (c == ':') {
                    pretty.append(c).append(' ');
                } else if (!Character.isWhitespace(c)) {
                    pretty.append(c);
                }
            } else {
                pretty.append(c);
            }
        }
        return pretty.toString();
    }
}