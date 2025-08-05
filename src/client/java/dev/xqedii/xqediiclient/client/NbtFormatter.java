package dev.xqedii.xqediiclient.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public class NbtFormatter {

    private static final TextColor COLOR_STRUCTURE = TextColor.fromRgb(0xb4b4b4);
    private static final TextColor COLOR_KEY = TextColor.fromRgb(0xd7ba7d);
    private static final TextColor COLOR_STRING_VALUE = TextColor.fromRgb(0xd69d85);
    private static final TextColor COLOR_NUMBER_VALUE = TextColor.fromRgb(0x569cd6);

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

        int indentDepth = 0;
        while (indentDepth < line.length() && line.charAt(indentDepth) == ' ') {
            indentDepth++;
        }
        String indent = line.substring(0, indentDepth);
        formattedLine.append(Text.literal(indent));

        int colonIndex = trimmedLine.indexOf(':');
        if (colonIndex > 0) {
            String key = trimmedLine.substring(0, colonIndex);
            String value = trimmedLine.substring(colonIndex + 1).trim();

            formattedLine.append(Text.literal(key).setStyle(Style.EMPTY.withColor(COLOR_KEY)));
            formattedLine.append(Text.literal(": ").setStyle(Style.EMPTY.withColor(COLOR_STRUCTURE)));
            formattedLine.append(formatValue(value));
        } else {
            if (trimmedLine.endsWith(",")) {
                formattedLine.append(formatValue(trimmedLine.substring(0, trimmedLine.length() - 1)));
                formattedLine.append(Text.literal(",").setStyle(Style.EMPTY.withColor(COLOR_STRUCTURE)));
            } else {
                formattedLine.append(formatValue(trimmedLine));
            }
        }
        return formattedLine;
    }

    private static Text formatValue(String value) {
        value = value.trim();
        if (value.startsWith("\"") || value.startsWith("'")) {
            return Text.literal(value).setStyle(Style.EMPTY.withColor(COLOR_STRING_VALUE));
        }
        if (Character.isDigit(value.charAt(0)) || (value.length() > 1 && value.charAt(0) == '-' && Character.isDigit(value.charAt(1)))) {
            return Text.literal(value).setStyle(Style.EMPTY.withColor(COLOR_NUMBER_VALUE));
        }
        if (value.equals("true") || value.equals("false")) {
            return Text.literal(value).setStyle(Style.EMPTY.withColor(COLOR_NUMBER_VALUE));
        }
        return Text.literal(value).setStyle(Style.EMPTY.withColor(COLOR_STRUCTURE));
    }

    private static String prettyPrintNbt(String rawNbt) {
        StringBuilder pretty = new StringBuilder();
        int indentLevel = 0;
        boolean inString = false;

        for (char c : rawNbt.toCharArray()) {
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