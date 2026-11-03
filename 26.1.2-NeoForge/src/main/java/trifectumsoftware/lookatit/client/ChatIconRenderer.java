package trifectumsoftware.lookatit.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import trifectumsoftware.lookatit.ShowItemMod;
import trifectumsoftware.lookatit.config.LookAtItConfig;

public class ChatIconRenderer {
    public static void onRender(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) {
        if (!LookAtItConfig.showIcon) return;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui == null) return;
            ChatComponent chat = mc.gui.getChat();
            if (chat == null) return;

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

            GuiGraphicsExtractor gg = event.getGuiGraphics();
            float scale = mc.options.chatScale().get().floatValue();
            int linesPerPage = chat.getLinesPerPage();
            double spacing = mc.options.chatLineSpacing().get();
            int lineHeight = (int) (9.0 * (spacing + 1.0));
            int offset = (int) Math.round(8.0 * (spacing + 1.0) - 4.0 * spacing);
            int baseY = Mth.floor((float) (gg.guiHeight() - 40) / scale);
            int tickCount = mc.gui.getGuiTicks();

            Matrix3x2fStack pose = gg.pose();
            pose.pushMatrix();
            try {
                pose.scale(scale, scale);
                pose.translate(4.0F, 0.0F);

                int max = Math.min(lines.size() - scroll, linesPerPage);
                for (int i = 0; i < max; i++) {
                    GuiMessage.Line line = lines.get(scroll + i);
                    if (line == null) continue;
                    if (!chat.isChatFocused() && tickCount - line.parent().addedTime() >= 200) continue;
                    int y = baseY - i * lineHeight - offset;
                    drawLineIcon(mc, gg, line.content(), y);
                }
            } finally {
                pose.popMatrix();
            }
        } catch (Throwable t) {
        }
    }

    private static void drawLineIcon(Minecraft mc, GuiGraphicsExtractor gg, FormattedCharSequence sequence, int y) {
        StringBuilder before = new StringBuilder();
        boolean[] done = new boolean[]{false};
        sequence.accept((index, style, codepoint) -> {
            if (done[0]) return false;
            HoverEvent hover = style.getHoverEvent();
            if (hover instanceof HoverEvent.ShowItem showItem) {
                ItemStack stack = showItem.item().create();
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

    private static void drawIcon(GuiGraphicsExtractor gg, ItemStack stack, int x, int y) {
        Matrix3x2fStack pose = gg.pose();
        pose.pushMatrix();
        try {
            pose.translate(x, y);
            pose.scale(0.5F, 0.5F);
            gg.item(stack, 0, 0);
        } finally {
            pose.popMatrix();
        }
    }
}
