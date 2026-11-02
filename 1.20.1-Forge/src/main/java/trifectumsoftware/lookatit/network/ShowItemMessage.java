package trifectumsoftware.lookatit.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import trifectumsoftware.lookatit.ShowItemMod;

public record ShowItemMessage(ItemStack stack) {
    public static void encode(ShowItemMessage msg, FriendlyByteBuf buf) {
        buf.writeItem(msg.stack);
    }

    public static ShowItemMessage decode(FriendlyByteBuf buf) {
        return new ShowItemMessage(buf.readItem());
    }

    public static void handle(ShowItemMessage msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || msg.stack().isEmpty()) return;
            long now = System.currentTimeMillis();
            UUID id = player.getUUID();
            Long last = LAST.get(id);
            if (last != null && now - last < 5000L) return;
            LAST.put(id, now);

            net.minecraft.network.chat.MutableComponent itemComp =
                    Component.literal("[" + msg.stack().getHoverName().getString() + "]");
            itemComp.withStyle(s -> s.withColor(trifectumsoftware.lookatit.config.LookAtItConfig.color())
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(msg.stack()))));

            net.minecraft.network.chat.MutableComponent full =
                    Component.literal("<" + player.getName().getString() + ">   ").append(itemComp);

            player.getServer().getPlayerList().broadcastSystemMessage(full, false);
        });
        context.setPacketHandled(true);
    }

    private static final Map<UUID, Long> LAST = new HashMap<>();
}
