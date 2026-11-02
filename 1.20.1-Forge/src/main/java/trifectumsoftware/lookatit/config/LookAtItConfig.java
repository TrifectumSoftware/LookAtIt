package trifectumsoftware.lookatit.config;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import net.minecraft.ChatFormatting;
import net.minecraftforge.fml.loading.FMLPaths;

public class LookAtItConfig {
    public static String textColor = "aqua";
    public static boolean showIcon = true;

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("lookatit.properties");
    }

    public static void load() {
        Properties properties = new Properties();
        Path file = file();
        try {
            if (Files.exists(file)) {
                try (InputStream in = Files.newInputStream(file)) {
                    properties.load(in);
                }
            }
            textColor = properties.getProperty("textColor", "aqua");
            showIcon = Boolean.parseBoolean(properties.getProperty("showIcon", "true"));
            properties.setProperty("textColor", textColor);
            properties.setProperty("showIcon", String.valueOf(showIcon));
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                properties.store(out, "lookatit: textColor = Minecraft formatting name or code (e.g. aqua, red, &b); showIcon = true/false");
            }
        } catch (Exception ignored) {
        }
    }

    public static ChatFormatting color() {
        String value = textColor == null ? "aqua" : textColor.trim();
        if (value.length() > 1 && (value.charAt(0) == '\u00a7' || value.charAt(0) == '&')) {
            ChatFormatting formatting = ChatFormatting.getByCode(Character.toLowerCase(value.charAt(1)));
            if (formatting != null) return formatting;
        }
        try {
            return ChatFormatting.valueOf(value.toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (Exception e) {
            return ChatFormatting.AQUA;
        }
    }
}
