package trifectumsoftware.lookatit.universal.forge1710;

import trifectumsoftware.lookatit.universal.UniversalInstaller;

@cpw.mods.fml.common.Mod(modid = "lookatit_universal", name = "lookatit installer", version = "1.0.0")
public class Forge1710Entry {
    public Forge1710Entry() {
        UniversalInstaller.install("lookatit-1.7.10-Forge.jar");
    }
}
