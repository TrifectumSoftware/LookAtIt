package trifectumsoftware.lookatit.universal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public final class UniversalInstaller {
    private UniversalInstaller() {
    }

    public static void install(String fileName) {
        try {
            File mods = new File("mods");
            File target = new File(mods, fileName);
            if (target.isFile() && target.length() > 0) {
                return;
            }
            if (!mods.isDirectory() && !mods.mkdirs()) {
                System.out.println("[lookatit] Could not create mods directory");
                return;
            }
            InputStream in = UniversalInstaller.class.getResourceAsStream("/mods/" + fileName);
            if (in == null) {
                System.out.println("[lookatit] Bundled jar not found: " + fileName);
                return;
            }
            try (InputStream source = in; OutputStream out = new FileOutputStream(target)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = source.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            System.out.println("[lookatit] Installed " + fileName + " into " + target.getAbsolutePath()
                    + " - restart Minecraft to use it.");
        } catch (Throwable t) {
            System.out.println("[lookatit] Universal install failed: " + t);
            t.printStackTrace();
        }
    }

    public static String forgeVersion() {
        try {
            Class<?> loader = Class.forName("net.minecraftforge.fml.common.Loader");
            java.lang.reflect.Field field = loader.getField("MC_VERSION");
            Object value = field.get(null);
            if (value != null) return String.valueOf(value);
        } catch (Throwable ignored) {
        }
        try {
            Class<?> constants = Class.forName("net.minecraft.SharedConstants");
            java.lang.reflect.Field field = constants.getField("VERSION_STRING");
            Object value = field.get(null);
            if (value != null) return String.valueOf(value);
        } catch (Throwable ignored) {
        }
        try {
            Class<?> constants = Class.forName("net.minecraft.SharedConstants");
            Object version = constants.getMethod("getCurrentVersion").invoke(null);
            if (version != null) {
                try {
                    return String.valueOf(version.getClass().getMethod("name").invoke(version));
                } catch (Throwable ignored) {
                    return String.valueOf(version.getClass().getMethod("id").invoke(version));
                }
            }
        } catch (Throwable ignored) {
        }
        return "unknown";
    }
}
