package trifectumsoftware.lookatit.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import trifectumsoftware.lookatit.ShowItemMod;
import trifectumsoftware.lookatit.config.LookAtItConfig;

public class ShowItemClient implements ClientModInitializer {
    private static KeyMapping key;
    private static boolean wasDown = false;

    @Override
    public void onInitializeClient() {
        key = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.lookatit", GLFW.GLFW_KEY_T, "key.categories.misc"));
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register(ChatIconRenderer::onRender);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.screen == null || !(client.screen instanceof AbstractContainerScreen<?> gui)) {
                wasDown = false;
                return;
            }
            long window = client.getWindow().getWindow();
            int keyCode = InputConstants.getKey(key.saveString()).getValue();
            boolean down = InputConstants.isKeyDown(window, keyCode) && isCtrlDown(window);
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
                FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                buf.writeItem(stack);
                ClientPlayNetworking.send(ShowItemMod.SHOW_ITEM_ID, buf);
            } else if (!down) {
                wasDown = false;
            }
        });
    }

    private static boolean isCtrlDown(long window) {
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }
}
