package dev.xqedii.xqediiclient.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class NbtFormatter {

    private static final TextColor COLOR_PUNCTUATION = TextColor.fromRgb(0x8c8c8c);
    private static final TextColor COLOR_KEY = TextColor.fromRgb(0xd7ba7d);
    private static final TextColor COLOR_VALUE = TextColor.fromRgb(0x5498d0);

    private static final Text HOVER_TEXT = Text.translatable("chat.copy.click").formatted(Formatting.GRAY);

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
        String trimmedLine = line.trim();
        int indentLength = line.indexOf(trimmedLine);
        if (indentLength > 0) {
            formattedLine.append(line.substring(0, indentLength));
        }

        boolean afterColon = false;

        for (int i = 0; i < trimmedLine.length(); i++) {
            char c = trimmedLine.charAt(i);

            if (c == '"') {
                int endIndex = i + 1;
                while (endIndex < trimmedLine.length()) {
                    if (trimmedLine.charAt(endIndex) == '"' && trimmedLine.charAt(endIndex - 1) != '\\') {
                        break;
                    }
                    endIndex++;
                }

                formattedLine.append(Text.literal("\"").styled(s -> s.withColor(COLOR_PUNCTUATION)));

                String content = trimmedLine.substring(i + 1, endIndex);

                if (afterColon) {
                    formattedLine.append(createClickableValueText(content));
                    afterColon = false;
                } else {
                    formattedLine.append(createClickableKeyText(content));
                }

                formattedLine.append(Text.literal("\"").styled(s -> s.withColor(COLOR_PUNCTUATION)));

                i = endIndex;
                continue;
            }

            if (c == ':' || c == ',' || c == '{' || c == '}' || c == '[' || c == ']') {
                formattedLine.append(Text.literal(String.valueOf(c)).styled(s -> s.withColor(COLOR_PUNCTUATION)));
                if (c == ':') {
                    afterColon = true;
                }
                if (c == ',' || c == '{') {
                    afterColon = false;
                }
                continue;
            }

            if (Character.isWhitespace(c)) {
                formattedLine.append(String.valueOf(c));
                continue;
            }

            if (!Character.isWhitespace(c)) {
                int endIndex = i;
                while (endIndex < trimmedLine.length() && !(":,{}[] ".indexOf(trimmedLine.charAt(endIndex)) != -1)) {
                    endIndex++;
                }
                String token = trimmedLine.substring(i, endIndex);

                if (afterColon) {
                    formattedLine.append(createClickableValueText(token));
                    afterColon = false;
                } else {
                    formattedLine.append(createClickableKeyText(token));
                }

                i = endIndex -1;
            }
        }
        return formattedLine;
    }

    private static MutableText createClickableKeyText(String text) {
        Style style = Style.EMPTY
                .withColor(COLOR_KEY)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, HOVER_TEXT));

        return Text.literal(text).setStyle(style);
    }

    private static MutableText createClickableValueText(String text) {
        Style style = Style.EMPTY
                .withColor(COLOR_VALUE)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text))
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