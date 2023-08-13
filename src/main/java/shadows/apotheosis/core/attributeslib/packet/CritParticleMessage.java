package shadows.apotheosis.core.attributeslib.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent.Context;
import shadows.apotheosis.core.attributeslib.client.AttributesLibClient;
import shadows.placebo.network.MessageHelper;
import shadows.placebo.network.MessageProvider;

public class CritParticleMessage {

    protected final int entityId;

    public CritParticleMessage(Entity entity) {
        this(entity.getId());
    }

    public CritParticleMessage(int entityId) {
        this.entityId = entityId;
    }

    public static class Provider implements MessageProvider<CritParticleMessage> {

        @Override
        public Class<CritParticleMessage> getMsgClass() {
            return CritParticleMessage.class;
        }

        @Override
        public void write(CritParticleMessage msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public CritParticleMessage read(FriendlyByteBuf buf) {
            return new CritParticleMessage(buf.readInt());
        }

        @Override
        public void handle(CritParticleMessage msg, Supplier<Context> ctx) {
            MessageHelper.handlePacket(() -> () -> {
                if (FMLEnvironment.dist.isClient()) {
                    AttributesLibClient.apothCrit(msg.entityId);
                }
            }, ctx);
        }

    }

}
