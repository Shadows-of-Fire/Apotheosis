package dev.shadowsoffire.apotheosis.ench.table;

import java.util.Optional;
import java.util.function.Supplier;

import dev.shadowsoffire.apotheosis.ench.table.ApothEnchantmentMenu.TableStats;
import dev.shadowsoffire.placebo.network.MessageHelper;
import dev.shadowsoffire.placebo.network.MessageProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;

public class StatsMessage {

    protected final TableStats stats;

    public StatsMessage(TableStats stats) {
        this.stats = stats;
    }

    public static class Provider implements MessageProvider<StatsMessage> {

        @Override
        public Class<?> getMsgClass() {
            return StatsMessage.class;
        }

        @Override
        public void write(StatsMessage msg, FriendlyByteBuf buf) {
            msg.stats.write(buf);
        }

        @Override
        public StatsMessage read(FriendlyByteBuf buf) {
            return new StatsMessage(TableStats.read(buf));
        }

        @Override
        public void handle(StatsMessage msg, Supplier<Context> ctx) {
            MessageHelper.handlePacket(() -> {
                if (Minecraft.getInstance().screen instanceof ApothEnchantScreen es) {
                    es.menu.stats = msg.stats;
                }
            }, ctx);
        }

        @Override
        public Optional<NetworkDirection> getNetworkDirection() {
            return Optional.of(NetworkDirection.PLAY_TO_CLIENT);
        }
    }

}
