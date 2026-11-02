package trifectumsoftware.lookatit.universal.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import trifectumsoftware.lookatit.universal.UniversalInstaller;

public class FabricEntry implements ModInitializer {
    @Override
    public void onInitialize() {
        String version = FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        if (version.startsWith("1.20.1")) {
            UniversalInstaller.install("lookatit-1.20.1-Fabric.jar");
        }
    }
}
