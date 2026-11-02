package trifectumsoftware.lookatit;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import trifectumsoftware.lookatit.client.ChatIconRenderer;
import trifectumsoftware.lookatit.client.ClientHandler;
import trifectumsoftware.lookatit.config.LookAtItConfig;
import trifectumsoftware.lookatit.network.ShowItemMessage;

@Mod(ShowItemMod.MODID)
public class ShowItemMod {
    public static final String MODID = "lookatit";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> "1",
            (s) -> true,
            (s) -> true
    );

    public ShowItemMod() {
        LookAtItConfig.load();
        IEventBus bus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(0, ShowItemMessage.class,
                ShowItemMessage::encode, ShowItemMessage::decode, ShowItemMessage::handle);
    }
}
