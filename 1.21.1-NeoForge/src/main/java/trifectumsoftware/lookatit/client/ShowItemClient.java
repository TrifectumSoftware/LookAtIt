package trifectumsoftware.lookatit.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import trifectumsoftware.lookatit.ShowItemMod;
import trifectumsoftware.lookatit.config.LookAtItConfig;

@EventBusSubscriber(modid = ShowItemMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ShowItemClient {
    private static final KeyMapping KEY = new KeyMapping("key.lookatit", InputConstants.Type.KEYSYM, InputConstants.KEY_T, "key.categories.misc");
    private static boolean wasDown = false;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY);
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ShowItemClient::tick);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ChatIconRenderer::onRender);
    }

    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen == null || !(mc.screen instanceof AbstractContainerScreen<?> gui)) {
            wasDown = false;
            return;
        }
        long window = mc.getWindow().getWindow();
        int keyCode = InputConstants.getKey(KEY.saveString()).getValue();
        boolean down = InputConstants.isKeyDown(window, keyCode) && isCtrlDown(window);
        if (down && !wasDown) {
            wasDown = true;
            Slot slot = gui.getSlotUnderMouse();
            if (slot == null) return;
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) return;
            PacketDistributor.sendToServer(new ShowItemMod.ShowItemPayload(stack));
        } else if (!down) {
            wasDown = false;
        }
    }

    private static boolean isCtrlDown(long window) {
        return org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS
                || org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }
}
