package trifectumsoftware.lookatit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import trifectumsoftware.lookatit.client.ChatIconRenderer;
import trifectumsoftware.lookatit.client.ShowItemClient;
import trifectumsoftware.lookatit.config.LookAtItConfig;

@Mod(ShowItemMod.MODID)
public class ShowItemMod {
    public static final String MODID = "lookatit";
    public static final ResourceLocation SHOW_ITEM_ID = ResourceLocation.fromNamespaceAndPath(MODID, "show_item");

    public static final CustomPacketPayload.Type<ShowItemPayload> TYPE = new CustomPacketPayload.Type<>(SHOW_ITEM_ID);
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ShowItemPayload> STREAM_CODEC =
            StreamCodec.composite(ItemStack.STREAM_CODEC, ShowItemPayload::stack, ShowItemPayload::new);

    public record ShowItemPayload(ItemStack stack) implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public ShowItemMod(IEventBus modBus) {
        LookAtItConfig.load();
        modBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(TYPE, STREAM_CODEC, this::handle);
    }

    private void handle(ShowItemPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null || payload.stack().isEmpty()) return;
            long now = System.currentTimeMillis();
            UUID id = player.getUUID();
            Long last = LAST.get(id);
            if (last != null && now - last < 5000L) return;
            LAST.put(id, now);

            net.minecraft.network.chat.MutableComponent itemComp =
                    net.minecraft.network.chat.Component.literal("[" + payload.stack().getHoverName().getString() + "]");
            itemComp.withStyle(s -> s.withColor(trifectumsoftware.lookatit.config.LookAtItConfig.color())
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                            net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM,
                            new net.minecraft.network.chat.HoverEvent.ItemStackInfo(payload.stack()))));

            net.minecraft.network.chat.MutableComponent full =
                    net.minecraft.network.chat.Component.literal("<" + player.getName().getString() + ">   ").append(itemComp);

            player.getServer().getPlayerList().broadcastSystemMessage(full, false);
        });
    }

    private static final Map<UUID, Long> LAST = new HashMap<>();
}
