package trifectumsoftware.lookatit;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import trifectumsoftware.lookatit.client.ChatIconRenderer;
import trifectumsoftware.lookatit.client.ShowItemClient;
import trifectumsoftware.lookatit.config.LookAtItConfig;

public class ShowItemMod implements ModInitializer {
    public static final String MODID = "lookatit";
    public static final ResourceLocation SHOW_ITEM_ID = new ResourceLocation(MODID, "show_item");

    @Override
    public void onInitialize() {
        LookAtItConfig.load();
        ServerPlayNetworking.registerGlobalReceiver(SHOW_ITEM_ID, (server, player, handler, buf, responseSender) -> {
            net.minecraft.world.item.ItemStack stack = buf.readItem();
            server.execute(() -> handle(server, player, stack));
        });
    }

    private static void handle(net.minecraft.server.MinecraftServer server, ServerPlayer player, net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) return;
        long now = System.currentTimeMillis();
        java.util.UUID id = player.getUUID();
        Long last = LAST.get(id);
        if (last != null && now - last < 5000L) return;
        LAST.put(id, now);

        net.minecraft.network.chat.MutableComponent itemComp =
                net.minecraft.network.chat.Component.literal("[" + stack.getHoverName().getString() + "]");
        itemComp.withStyle(s -> s.withColor(trifectumsoftware.lookatit.config.LookAtItConfig.color())
                .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM, new net.minecraft.network.chat.HoverEvent.ItemStackInfo(stack))));

        net.minecraft.network.chat.MutableComponent full =
                net.minecraft.network.chat.Component.literal("<" + player.getName().getString() + ">   ").append(itemComp);

        server.getPlayerList().broadcastSystemMessage(full, false);
    }

    private static final java.util.Map<java.util.UUID, Long> LAST = new java.util.HashMap<>();
}
