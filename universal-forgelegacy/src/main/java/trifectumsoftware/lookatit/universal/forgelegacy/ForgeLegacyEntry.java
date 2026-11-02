package trifectumsoftware.lookatit.universal.forgelegacy;

import trifectumsoftware.lookatit.universal.UniversalInstaller;

@net.minecraftforge.fml.common.Mod(modid = "lookatit_universal", name = "lookatit installer", version = "1.0.0")
public class ForgeLegacyEntry {
    public ForgeLegacyEntry() {
        String version = UniversalInstaller.forgeVersion();
        if (version.startsWith("1.8.9")) {
            UniversalInstaller.install("lookatit-1.8.9-Forge.jar");
        } else if (version.startsWith("1.12.2")) {
            UniversalInstaller.install("lookatit-1.12.2-Forge.jar");
        } else if (version.startsWith("1.20.1")) {
            UniversalInstaller.install("lookatit-1.20.1-Forge.jar");
        }
    }
}
