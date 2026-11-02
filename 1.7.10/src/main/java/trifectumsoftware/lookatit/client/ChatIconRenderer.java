package trifectumsoftware.lookatit.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;
import trifectumsoftware.lookatit.ShowItemMod;
import trifectumsoftware.lookatit.config.LookAtItConfig;

@SideOnly(Side.CLIENT)
public class ChatIconRenderer {
    public static void init() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ChatIconRenderer());
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (!LookAtItConfig.showIcon) return;
        if (event.type != RenderGameOverlayEvent.ElementType.CHAT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.ingameGUI == null) return;
        GuiNewChat chat = mc.ingameGUI.getChatGUI();
        if (chat == null) return;

        List<ChatLine> lines;
        int scrollPos;
        try {
            java.lang.reflect.Field fLines = GuiNewChat.class.getDeclaredField("chatLines");
            fLines.setAccessible(true);
            lines = (List<ChatLine>) fLines.get(chat);
            java.lang.reflect.Field fScroll = GuiNewChat.class.getDeclaredField("scrollPos");
            fScroll.setAccessible(true);
            scrollPos = fScroll.getInt(chat);
        } catch (Exception e) {
            return;
        }
        if (lines == null || lines.isEmpty()) return;

        FontRenderer font = mc.fontRendererObj;
        float scale = chat.getChatScale();
        int baseY = event.resolution.getScaledHeight() - 48 + 20;

        GL11.glPushMatrix();
        GL11.glTranslatef(2.0F, (float) baseY, 0.0F);
        GL11.glScalef(scale, scale, 1.0F);

        int max = Math.min(lines.size() - scrollPos, 20);
        for (int i = 0; i < max; i++) {
            ChatLine line = lines.get(scrollPos + i);
            if (line == null) continue;
            int age = mc.ingameGUI.getUpdateCounter() - line.getUpdatedCounter();
            if (age >= 200 && !chat.getChatOpen()) continue;
            int y = -i * 9 - 8;
            drawLineIcon(mc, font, line.getChatComponent(), y);
        }

        GL11.glPopMatrix();
    }

    private void drawLineIcon(Minecraft mc, FontRenderer font, IChatComponent component, int y) {
        List<IChatComponent> parts = new ArrayList<IChatComponent>();
        flatten(component, parts);
        StringBuilder before = new StringBuilder();
        for (IChatComponent part : parts) {
            if (part == null) continue;
            ChatStyle style = part.getChatStyle();
            if (style != null) {
                net.minecraft.event.HoverEvent hover = style.getChatHoverEvent();
                if (hover != null && hover.getAction() == net.minecraft.event.HoverEvent.Action.SHOW_ITEM) {
                    String nbt = hover.getValue().getUnformattedText();
                    try {
                        NBTBase base = JsonToNBT.func_150315_a(nbt);
                        if (base instanceof NBTTagCompound) {
                            ItemStack stack = ItemStack.loadItemStackFromNBT((NBTTagCompound) base);
                            if (stack != null) {
                                int x = font.getStringWidth(before.toString());
                                drawIcon(mc, font, stack, x - 11, y);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    return;
                }
            }
            before.append(part.getUnformattedTextForChat());
        }
    }

    private void flatten(IChatComponent component, List<IChatComponent> out) {
        if (component == null) return;
        out.add(component);
        for (Object sibling : component.getSiblings()) {
            flatten((IChatComponent) sibling, out);
        }
    }

    private void drawIcon(Minecraft mc, FontRenderer font, ItemStack stack, int x, int y) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        GL11.glScalef(0.5F, 0.5F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        RenderItem.getInstance().renderItemAndEffectIntoGUI(font, mc.getTextureManager(), stack, 0, 0);
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();
    }
}
