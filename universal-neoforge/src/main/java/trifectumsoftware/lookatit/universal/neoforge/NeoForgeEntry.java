package trifectumsoftware.lookatit.universal.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import trifectumsoftware.lookatit.universal.UniversalInstaller;

@Mod("lookatit_universal")
public class NeoForgeEntry {
    public NeoForgeEntry(IEventBus modBus) {
        String version = UniversalInstaller.forgeVersion();
        if (version.startsWith("1.21.1")) {
            UniversalInstaller.install("lookatit-1.21.1-NeoForge.jar");
        } else if (version.startsWith("26.1")) {
            UniversalInstaller.install("lookatit-26.1.2-NeoForge.jar");
        } else if (version.startsWith("26.2")) {
            UniversalInstaller.install("lookatit-26.2-NeoForge.jar");
        } else if (version.startsWith("26.3")) {
            UniversalInstaller.install("lookatit-26.3-NeoForge.jar");
        }
    }
}
