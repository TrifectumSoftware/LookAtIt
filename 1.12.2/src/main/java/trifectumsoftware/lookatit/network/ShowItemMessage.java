package trifectumsoftware.lookatit.network;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import trifectumsoftware.lookatit.ShowItemMod;

public class ShowItemMessage implements IMessage {
    private ItemStack stack;

    public ShowItemMessage() {}

    public ShowItemMessage(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, stack);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stack = ByteBufUtils.readItemStack(buf);
    }

    public static class Handler implements IMessageHandler<ShowItemMessage, IMessage> {
        private static final Map<UUID, Long> lastSend = new HashMap<UUID, Long>();
        private static final long COOLDOWN = 5000L;

        @Override
        public IMessage onMessage(ShowItemMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player == null || message.stack == null) return null;
            long now = System.currentTimeMillis();
            UUID id = player.getGameProfile().getId();
            Long last = lastSend.get(id);
            if (last != null && now - last < COOLDOWN) return null;
            lastSend.put(id, now);

            MinecraftServer server = player.getServer();
            if (server == null) return null;

            NBTTagCompound tag = new NBTTagCompound();
            message.stack.writeToNBT(tag);

            ITextComponent itemComp = new TextComponentString("[" + message.stack.getDisplayName() + "]");
            itemComp.getStyle().setColor(trifectumsoftware.lookatit.config.LookAtItConfig.color());
            itemComp.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new TextComponentString(tag.toString())));

            ITextComponent full = new TextComponentString("<" + player.getDisplayNameString() + ">   ");
            full.appendSibling(itemComp);

            server.getPlayerList().sendMessage(full);
            return null;
        }
    }
}
