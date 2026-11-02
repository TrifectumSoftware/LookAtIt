package trifectumsoftware.lookatit.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import trifectumsoftware.lookatit.ShowItemMod;
import trifectumsoftware.lookatit.config.LookAtItConfig;
import trifectumsoftware.lookatit.network.ShowItemMessage;

@SideOnly(Side.CLIENT)
public class ClientHandler {
    public static KeyBinding KEY_BINDING;
    private boolean wasDown = false;

    public static void init() {
        KEY_BINDING = new KeyBinding("key.lookatit", Keyboard.KEY_T, "key.categories.misc");
        ClientRegistry.registerKeyBinding(KEY_BINDING);
        FMLCommonHandler.instance().bus().register(new ClientHandler());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || !(mc.currentScreen instanceof GuiContainer)) {
            wasDown = false;
            return;
        }

        boolean down = Keyboard.isKeyDown(KEY_BINDING.getKeyCode())
                && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));

        if (down && !wasDown) {
            wasDown = true;
            GuiContainer gui = (GuiContainer) mc.currentScreen;
            Slot slot = gui.getSlotUnderMouse();
            if (slot == null) return;
            ItemStack stack = slot.getStack();
            if (stack == null || stack.isEmpty()) return;
            ShowItemMod.NETWORK.sendToServer(new ShowItemMessage(stack));
        } else if (!down) {
            wasDown = false;
        }
    }
}
