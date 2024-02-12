package shadows.apotheosis.adventure.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import shadows.apotheosis.adventure.affix.effect.RadialAffix;
import shadows.placebo.network.MessageHelper;
import shadows.placebo.network.MessageProvider;

import java.util.function.Supplier;

public class RadialStateChangeMessage implements MessageProvider<RadialStateChangeMessage> {
    @Override
    public void write(RadialStateChangeMessage radialStateChangeMessage, FriendlyByteBuf friendlyByteBuf) {

    }

    @Override
    public RadialStateChangeMessage read(FriendlyByteBuf friendlyByteBuf) {
        return new RadialStateChangeMessage();
    }

    @Override
    public void handle(RadialStateChangeMessage radialStateChangeMessage, Supplier<NetworkEvent.Context> ctx) {
        MessageHelper.handlePacket(() -> () -> {
            Player player = ctx.get().getSender();
            if (player == null) return;
            RadialAffix.toggleRadialState(player);
        }, ctx);
    }
}
