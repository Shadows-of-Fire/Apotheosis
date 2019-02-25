package shadows.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ParticleMessage implements IMessage {

	EnumParticleTypes type;
	double x, y, z, velX, velY, velZ;
	int count;

	public ParticleMessage() {
	}

	public ParticleMessage(EnumParticleTypes type, double x, double y, double z, double velX, double velY, double velZ, int count) {
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
	public void toBytes(ByteBuf buf) {
		buf.writeInt(type.getParticleID());
		buf.writeDouble(x).writeDouble(y).writeDouble(z);
		buf.writeDouble(velX).writeDouble(velY).writeDouble(velZ);
		buf.writeInt(count);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		type = EnumParticleTypes.getParticleFromId(buf.readInt());
		x = buf.readDouble();
		y = buf.readDouble();
		z = buf.readDouble();
		velX = buf.readDouble();
		velY = buf.readDouble();
		velZ = buf.readDouble();
		count = buf.readInt();
	}

	public static class Handler implements IMessageHandler<ParticleMessage, IMessage> {

		@Override
		public IMessage onMessage(ParticleMessage msg, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				for (int i = 0; i < msg.count; i++)
					Minecraft.getMinecraft().world.spawnParticle(msg.type, msg.x, msg.y, msg.z, msg.velX, msg.velY, msg.velZ);
			});
			return null;
		}

	}

}
