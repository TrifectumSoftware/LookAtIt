package trifectumsoftware.lookatit.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import trifectumsoftware.lookatit.ShowItemMod;
import trifectumsoftware.lookatit.config.LookAtItConfig;

@SideOnly(Side.CLIENT)
public class ClientHandler {
    public static KeyBinding KEY_BINDING;
    private boolean wasDown = false;

    public static void init() {
        KEY_BINDING = new KeyBinding("key.lookatit", Keyboard.KEY_T, "key.categories.misc");
        cpw.mods.fml.client.registry.ClientRegistry.registerKeyBinding(KEY_BINDING);
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(new ClientHandler());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || !(mc.currentScreen instanceof GuiContainer)) {
            wasDown = false;
            return;
        }

        boolean down = Keyboard.isKeyDown(KEY_BINDING.getKeyCode())
                && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));

        if (down && !wasDown) {
            wasDown = true;
            handle(mc, (GuiContainer) mc.currentScreen);
        } else if (!down) {
            wasDown = false;
        }
    }

    private void handle(Minecraft mc, GuiContainer gui) {
        Slot slot = null;
        String[] names = new String[]{"theSlot", "field_147006_u"};
        for (String n : names) {
            try {
                java.lang.reflect.Field f = GuiContainer.class.getDeclaredField(n);
                f.setAccessible(true);
                slot = (Slot) f.get(gui);
                if (slot != null) break;
            } catch (Exception ignored) {}
        }
        if (slot == null) {
            for (java.lang.reflect.Field f : GuiContainer.class.getDeclaredFields()) {
                if (f.getType() == Slot.class) {
                    try {
                        f.setAccessible(true);
                        Slot s = (Slot) f.get(gui);
                        if (s != null) { slot = s; break; }
                    } catch (Exception ignored) {}
                }
            }
        }
        if (slot == null) {
            System.out.println("[lookatit] no hovered slot");
            return;
        }
        ItemStack stack = slot.getStack();
        if (stack == null || stack.stackSize <= 0) {
            System.out.println("[lookatit] empty slot");
            return;
        }
        System.out.println("[lookatit] sending " + stack.getDisplayName());
        ShowItemMod.NETWORK.sendToServer(new trifectumsoftware.lookatit.network.ShowItemMessage(stack));
    }
}
