package shadows.util;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import shadows.placebo.util.NetworkUtils;
import shadows.placebo.util.NetworkUtils.MessageProvider;

public class ParticleMessage extends MessageProvider<ParticleMessage> {

	ParticleType<?> type;
	double x, y, z, velX, velY, velZ;
	int count;

	public ParticleMessage() {
	}

	public ParticleMessage(ParticleType<?> type, double x, double y, double z, double velX, double velY, double velZ, int count) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.z = z;
		this.velX = velX;
		this.velY = velY;
		this.velZ = velZ;
		this.count = count;
	}

	@Override
	public Class<ParticleMessage> getMsgClass() {
		return ParticleMessage.class;
	}

	@Override
	public ParticleMessage read(PacketBuffer buf) {
		type = ((ForgeRegistry<ParticleType<?>>) ForgeRegistries.PARTICLE_TYPES).getValue(buf.readInt());
		x = buf.readDouble();
		y = buf.readDouble();
		z = buf.readDouble();
		velX = buf.readDouble();
		velY = buf.readDouble();
		velZ = buf.readDouble();
		count = buf.readInt();
		return new ParticleMessage(type, x, y, z, velX, velY, velZ, count);
	}

	@Override
	public void write(ParticleMessage msg, PacketBuffer buf) {
		buf.writeInt(((ForgeRegistry<ParticleType<?>>) ForgeRegistries.PARTICLE_TYPES).getID(type));
		buf.writeDouble(x).writeDouble(y).writeDouble(z);
		buf.writeDouble(velX).writeDouble(velY).writeDouble(velZ);
		buf.writeInt(count);
	}

	@Override
	public void handle(ParticleMessage msg, Supplier<Context> ctx) {
		NetworkUtils.enqueueClient(() -> {
			for (int i = 0; i < msg.count; i++)
				Minecraft.getInstance().world.addParticle((IParticleData) msg.type, msg.x, msg.y, msg.z, msg.velX, msg.velY, msg.velZ);
		});
	}

}
