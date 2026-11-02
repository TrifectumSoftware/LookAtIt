package trifectumsoftware.lookatit.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import trifectumsoftware.lookatit.ShowItemMod;

public class ShowItemMessage implements IMessage {
    private ItemStack stack;

    public ShowItemMessage() {}

    public ShowItemMessage(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void toBytes(io.netty.buffer.ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, stack);
    }

    @Override
    public void fromBytes(io.netty.buffer.ByteBuf buf) {
        stack = ByteBufUtils.readItemStack(buf);
    }

    public static class Handler implements IMessageHandler<ShowItemMessage, IMessage> {
        private static final Map<UUID, Long> lastSend = new HashMap<UUID, Long>();
        private static final long COOLDOWN = 100L * 50L;

        @Override
        public IMessage onMessage(ShowItemMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || message.stack == null) return null;
            long now = System.currentTimeMillis();
            UUID id = player.getGameProfile().getId();
            Long last = lastSend.get(id);
            if (last != null && now - last < COOLDOWN) return null;
            lastSend.put(id, now);

            MinecraftServer server = MinecraftServer.getServer();
            if (server == null) return null;

            NBTTagCompound tag = new NBTTagCompound();
            message.stack.writeToNBT(tag);

            IChatComponent itemComp = new ChatComponentText("[" + message.stack.getDisplayName() + "]");
            itemComp.getChatStyle().setColor(trifectumsoftware.lookatit.config.LookAtItConfig.color());
            itemComp.getChatStyle().setChatHoverEvent(new net.minecraft.event.HoverEvent(net.minecraft.event.HoverEvent.Action.SHOW_ITEM, new ChatComponentText(tag.toString())));

            IChatComponent full = new ChatComponentText("<" + player.getCommandSenderName() + ">   ");
            full.appendSibling(itemComp);

            server.getConfigurationManager().sendChatMsg(full);
            return null;
        }
    }
}
