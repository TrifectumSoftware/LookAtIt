package trifectumsoftware.lookatit.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;
import net.minecraft.util.EnumChatFormatting;

public class LookAtItConfig {
    public static String textColor = "aqua";
    public static boolean showIcon = true;

    private static File file() {
        return new File("config", "lookatit.properties");
    }

    public static void load() {
        Properties properties = new Properties();
        File file = file();
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                properties.load(in);
            } catch (IOException ignored) {
            }
        }
        textColor = properties.getProperty("textColor", "aqua");
        showIcon = Boolean.parseBoolean(properties.getProperty("showIcon", "true"));
        properties.setProperty("textColor", textColor);
        properties.setProperty("showIcon", String.valueOf(showIcon));
        file.getParentFile().mkdirs();
        try (OutputStream out = new FileOutputStream(file)) {
            properties.store(out, "lookatit: textColor = Minecraft formatting name or code (e.g. aqua, red, &b); showIcon = true/false");
        } catch (IOException ignored) {
        }
    }

    public static EnumChatFormatting color() {
        String value = textColor == null ? "aqua" : textColor.trim();
        if (value.length() > 1 && (value.charAt(0) == '\u00a7' || value.charAt(0) == '&')) {
            String name = nameForCode(Character.toLowerCase(value.charAt(1)));
            if (name != null) {
                EnumChatFormatting formatting = EnumChatFormatting.getValueByName(name);
                if (formatting != null) return formatting;
            }
        }
        try {
            return EnumChatFormatting.valueOf(value.toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (Exception e) {
            return EnumChatFormatting.AQUA;
        }
    }

    private static String nameForCode(char code) {
        switch (code) {
            case '0': return "black";
            case '1': return "dark_blue";
            case '2': return "dark_green";
            case '3': return "dark_aqua";
            case '4': return "dark_red";
            case '5': return "dark_purple";
            case '6': return "gold";
            case '7': return "gray";
            case '8': return "dark_gray";
            case '9': return "blue";
            case 'a': return "green";
            case 'b': return "aqua";
            case 'c': return "red";
            case 'd': return "light_purple";
            case 'e': return "yellow";
            case 'f': return "white";
            case 'k': return "obfuscated";
            case 'l': return "bold";
            case 'm': return "strikethrough";
            case 'n': return "underline";
            case 'o': return "italic";
            case 'r': return "reset";
            default: return null;
        }
    }
}
