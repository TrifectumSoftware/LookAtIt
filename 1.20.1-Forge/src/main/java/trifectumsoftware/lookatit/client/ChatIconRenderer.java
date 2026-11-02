package trifectumsoftware.lookatit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import trifectumsoftware.lookatit.ShowItemMod;
import trifectumsoftware.lookatit.config.LookAtItConfig;

@Mod.EventBusSubscriber(modid = ShowItemMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChatIconRenderer {
    @SubscribeEvent
    public static void onRender(RenderGuiOverlayEvent.Post event) {
        if (!LookAtItConfig.showIcon) return;
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.CHAT_PANEL.id())) return;
        Minecraft mc = Minecraft.getInstance();
        ChatComponent chat = mc.gui.getChat();

        List<GuiMessage.Line> lines;
        int scroll;
        try {
            java.lang.reflect.Field fLines = ChatComponent.class.getDeclaredField("trimmedMessages");
            fLines.setAccessible(true);
            lines = (List<GuiMessage.Line>) fLines.get(chat);
            java.lang.reflect.Field fScroll = ChatComponent.class.getDeclaredField("chatScrollbarPos");
            fScroll.setAccessible(true);
            scroll = fScroll.getInt(chat);
        } catch (Exception e) {
            return;
        }
        if (lines == null || lines.isEmpty()) return;

        GuiGraphics gg = event.getGuiGraphics();
        float scale = (float) chat.getScale();
        int linesPerPage = chat.getLinesPerPage();
        int lineHeight = (int) (9.0 * (mc.options.chatLineSpacing().get() + 1.0));
        double spacing = mc.options.chatLineSpacing().get();
        int offset = (int) Math.round(-8.0 * (spacing + 1.0) + 4.0 * spacing);
        int baseY = Mth.floor((float) (gg.guiHeight() - 40) / scale);

        PoseStack pose = gg.pose();
        pose.pushPose();
        pose.scale(scale, scale, 1.0F);
        pose.translate(4.0F, 0.0F, 0.0F);

        int max = Math.min(lines.size() - scroll, linesPerPage);
        for (int i = 0; i < max; i++) {
            GuiMessage.Line line = lines.get(scroll + i);
            if (line == null) continue;
            int y = baseY - i * lineHeight + offset;
            drawLineIcon(mc, gg, line.content(), y);
        }

        pose.popPose();
    }

    private static void drawLineIcon(Minecraft mc, GuiGraphics gg, FormattedCharSequence sequence, int y) {
        StringBuilder before = new StringBuilder();
        boolean[] done = new boolean[]{false};
        sequence.accept((index, style, codepoint) -> {
            if (done[0]) return false;
            HoverEvent hover = style.getHoverEvent();
            if (hover != null && hover.getAction() == HoverEvent.Action.SHOW_ITEM) {
                HoverEvent.ItemStackInfo info = hover.getValue(HoverEvent.Action.SHOW_ITEM);
                ItemStack stack = info != null ? info.getItemStack() : ItemStack.EMPTY;
                if (!stack.isEmpty()) {
                    drawIcon(gg, stack, mc.font.width(before.toString()) - 11, y);
                    done[0] = true;
                    return false;
                }
            }
            before.appendCodePoint(codepoint);
            return true;
        });
    }

    private static void drawIcon(GuiGraphics gg, ItemStack stack, int x, int y) {
        PoseStack pose = gg.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(0.5F, 0.5F, 1.0F);
        gg.renderItem(stack, 0, 0);
        pose.popPose();
    }
}
