package trifectumsoftware.lookatit.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import trifectumsoftware.lookatit.ShowItemMod;
import trifectumsoftware.lookatit.config.LookAtItConfig;

@SideOnly(Side.CLIENT)
public class ChatIconRenderer {
    public static void init() {
        MinecraftForge.EVENT_BUS.register(new ChatIconRenderer());
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (!LookAtItConfig.showIcon) return;
        if (event.getType() != RenderGameOverlayEvent.ElementType.CHAT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.ingameGUI == null) return;
        GuiNewChat chat = mc.ingameGUI.getChatGUI();
        if (chat == null) return;

        List<ChatLine> lines;
        try {
            java.lang.reflect.Field fLines = GuiNewChat.class.getDeclaredField("drawnChatLines");
            fLines.setAccessible(true);
            lines = (List<ChatLine>) fLines.get(chat);
        } catch (Exception e) {
            return;
        }
        if (lines == null || lines.isEmpty()) return;

        FontRenderer font = mc.fontRenderer;
        float scale = chat.getChatScale();
        int baseY = event.getResolution().getScaledHeight() - 48 + 8;

        GL11.glPushMatrix();
        GL11.glTranslatef(2.0F, (float) baseY, 0.0F);
        GL11.glScalef(scale, scale, 1.0F);

        int max = Math.min(lines.size(), 20);
        for (int i = 0; i < max; i++) {
            ChatLine line = lines.get(i);
            if (line == null) continue;
            int age = mc.ingameGUI.getUpdateCounter() - line.getUpdatedCounter();
            if (age >= 200 && !chat.getChatOpen()) continue;
            drawLineIcon(mc, font, line.getChatComponent(), -i * 9 - 8);
        }

        GL11.glPopMatrix();
    }

    private void drawLineIcon(Minecraft mc, FontRenderer font, ITextComponent component, int y) {
        List<ITextComponent> parts = new ArrayList<ITextComponent>();
        flatten(component, parts);
        StringBuilder before = new StringBuilder();
        for (ITextComponent part : parts) {
            if (part == null) continue;
            Style style = part.getStyle();
            if (style != null) {
                HoverEvent hover = style.getHoverEvent();
                if (hover != null && hover.getAction() == HoverEvent.Action.SHOW_ITEM) {
                    String nbt = hover.getValue().getUnformattedText();
                    try {
                        NBTBase base = JsonToNBT.getTagFromJson(nbt);
                        if (base instanceof NBTTagCompound) {
                            ItemStack stack = new ItemStack((NBTTagCompound) base);
                            if (!stack.isEmpty()) {
                                int x = font.getStringWidth(before.toString());
                                drawIcon(mc, font, stack, x - 11, y);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    return;
                }
            }
            before.append(part.getUnformattedComponentText());
        }
    }

    private void flatten(ITextComponent component, List<ITextComponent> out) {
        if (component == null) return;
        out.add(component);
        for (ITextComponent sibling : component.getSiblings()) {
            flatten(sibling, out);
        }
    }

    private void drawIcon(Minecraft mc, FontRenderer font, ItemStack stack, int x, int y) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        GL11.glScalef(0.5F, 0.5F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.renderItemAndEffectIntoGUI(stack, 0, 0);
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();
    }
}
