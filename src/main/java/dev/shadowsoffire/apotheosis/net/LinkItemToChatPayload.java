package dev.shadowsoffire.apotheosis.net;

import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.util.ItemLinking;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LinkItemToChatPayload(int containerId, int slot, Item intendedItem) implements CustomPacketPayload {

    public static final Type<LinkItemToChatPayload> TYPE = new Type<>(Apotheosis.loc("link_item_to_chat"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LinkItemToChatPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, LinkItemToChatPayload::containerId,
        ByteBufCodecs.INT, LinkItemToChatPayload::slot,
        ByteBufCodecs.registry(Registries.ITEM), LinkItemToChatPayload::intendedItem,
        LinkItemToChatPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Provider implements PayloadProvider<LinkItemToChatPayload> {

        @Override
        public Type<LinkItemToChatPayload> getType() {
            return TYPE;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, LinkItemToChatPayload> getCodec() {
            return CODEC;
        }

        @Override
        public void handle(LinkItemToChatPayload msg, IPayloadContext ctx) {
            Player player = ctx.player();
            if (ItemLinking.isOnCooldown(player.getUUID(), player.level().getGameTime())) {
                return;
            }

            if (!AdventureConfig.enableItemLinking) {
                player.sendSystemMessage(Apotheosis.lang("message", "item_linking_disabled"));
                return;
            }

            AbstractContainerMenu menu = player.containerMenu;
            if (menu.containerId == msg.containerId && menu.slots.size() > msg.slot) {
                Slot slot = menu.getSlot(msg.slot);
                ItemStack stack = slot.getItem();

                int count = stack.getCount();

                // Stacks with a count higher than 99 cannot be serialized, so we have to clamp them.
                if (count > 99) {
                    stack = stack.copyWithCount(99);
                }

                if (stack.getItem() == msg.intendedItem) {
                    Component comp = stack.getDisplayName();
                    if (count > 1) {
                        comp = Apotheosis.lang("chat", "link_item_with_count", String.valueOf(count), comp);
                    }
                    PlayerChatMessage chatMsg = PlayerChatMessage.system("").withUnsignedContent(comp);
                    player.getServer().getPlayerList().broadcastChatMessage(chatMsg, (ServerPlayer) player, ChatType.bind(ChatType.CHAT, player));
                    ItemLinking.startCooldown(player.getUUID(), player.level().getGameTime());
                }
            }
        }

        @Override
        public List<ConnectionProtocol> getSupportedProtocols() {
            return List.of(ConnectionProtocol.PLAY);
        }

        @Override
        public Optional<PacketFlow> getFlow() {
            return Optional.of(PacketFlow.SERVERBOUND);
        }

        @Override
        public String getVersion() {
            return "1";
        }

    }

}
