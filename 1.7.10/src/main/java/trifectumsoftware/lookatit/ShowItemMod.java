package trifectumsoftware.lookatit;

import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import trifectumsoftware.lookatit.client.ChatIconRenderer;
import trifectumsoftware.lookatit.client.ClientHandler;
import trifectumsoftware.lookatit.config.LookAtItConfig;
import trifectumsoftware.lookatit.network.ShowItemMessage;

@Mod(modid = ShowItemMod.MODID, name = "LookAtIt", version = "1.0.0")
public class ShowItemMod {
    public static final String MODID = "lookatit";
    public static SimpleNetworkWrapper NETWORK;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LookAtItConfig.load();
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        NETWORK.registerMessage(ShowItemMessage.Handler.class, ShowItemMessage.class, 0, Side.SERVER);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            ClientHandler.init();
            ChatIconRenderer.init();
        }
    }
}
