package trifectumsoftware.lookatit.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import trifectumsoftware.lookatit.ShowItemMod;
import trifectumsoftware.lookatit.config.LookAtItConfig;
import trifectumsoftware.lookatit.network.ShowItemMessage;

@Mod.EventBusSubscriber(modid = ShowItemMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientHandler {
    public static final KeyMapping KEY = new KeyMapping("key.lookatit", GLFW.GLFW_KEY_T, "key.categories.misc");
    private static boolean wasDown = false;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY);
    }

    @Mod.EventBusSubscriber(modid = ShowItemMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
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
                ShowItemMod.CHANNEL.sendToServer(new ShowItemMessage(stack));
            } else if (!down) {
                wasDown = false;
            }
        }

        private static boolean isCtrlDown(long window) {
            return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                    || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        }
    }
}
