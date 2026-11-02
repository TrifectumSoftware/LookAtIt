package trifectumsoftware.lookatit.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import trifectumsoftware.lookatit.ShowItemMod;
import trifectumsoftware.lookatit.config.LookAtItConfig;

public class ShowItemClient {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ShowItemMod.MODID, "lookatit"));
    private static final KeyMapping KEY = new KeyMapping("key.lookatit", InputConstants.Type.KEYSYM, InputConstants.KEY_T, CATEGORY);
    private static boolean wasDown = false;

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY);
    }

    public static void setup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ShowItemClient::tick);
        NeoForge.EVENT_BUS.addListener(ChatIconRenderer::onRender);
    }

    private static void tick(ClientTickEvent.Post event) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui == null) return;
            net.minecraft.client.gui.screens.Screen currentScreen = mc.gui.screen();
            if (mc.player == null || currentScreen == null || !(currentScreen instanceof AbstractContainerScreen<?> gui)) {
                wasDown = false;
                return;
            }
            int keyCode = InputConstants.getKey(KEY.saveString()).getValue();
            boolean down = InputConstants.isKeyDown(mc.getWindow(), keyCode)
                    && (InputConstants.isKeyDown(mc.getWindow(), InputConstants.KEY_LCONTROL)
                    || InputConstants.isKeyDown(mc.getWindow(), InputConstants.KEY_RCONTROL));
            if (down && !wasDown) {
                wasDown = true;
                Slot slot = null;
                try {
                    java.lang.reflect.Field f = AbstractContainerScreen.class.getDeclaredField("hoveredSlot");
                    f.setAccessible(true);
                    slot = (Slot) f.get(gui);
                } catch (Exception ignored) {}
                if (slot == null) return;
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) return;
                mc.getConnection().send(new ServerboundCustomPayloadPacket(new ShowItemMod.ShowItemPayload(stack)));
            } else if (!down) {
                wasDown = false;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
