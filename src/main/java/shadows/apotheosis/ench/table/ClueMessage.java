package shadows.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import shadows.placebo.util.NetworkUtils;
import shadows.placebo.util.NetworkUtils.MessageProvider;

public class ClueMessage extends MessageProvider<ClueMessage> {

	protected final int slot;
	protected final List<EnchantmentData> clues;
	protected final boolean all;

	/**
	 * Sends a clue message to the client.
	 * @param slot
	 * @param clues The clues.
	 * @param all If this is all of the enchantments being received.
	 */
	public ClueMessage(int slot, List<EnchantmentData> clues, boolean all) {
		this.slot = slot;
		this.clues = clues;
		this.all = all;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void write(ClueMessage msg, PacketBuffer buf) {
		buf.writeByte(msg.clues.size());
		for (EnchantmentData e : msg.clues) {
			buf.writeShort(Registry.ENCHANTMENT.getId(e.enchantment));
			buf.writeByte(e.level);
		}
		buf.writeByte(msg.slot);
		buf.writeBoolean(msg.all);
	}

	@Override
	@SuppressWarnings("deprecation")
	public ClueMessage read(PacketBuffer buf) {
		int size = buf.readByte();
		List<EnchantmentData> clues = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			Enchantment ench = Registry.ENCHANTMENT.byId(buf.readShort());
			clues.add(new EnchantmentData(ench, buf.readByte()));
		}
		return new ClueMessage(buf.readByte(), clues, buf.readBoolean());
	}

	@Override
	public void handle(ClueMessage msg, Supplier<Context> ctx) {
		NetworkUtils.handlePacket(() -> () -> {
			if (Minecraft.getInstance().screen instanceof ApothEnchantScreen) {
				((ApothEnchantScreen) Minecraft.getInstance().screen).acceptClues(msg.slot, msg.clues, msg.all);
			}
		}, ctx.get());
	}

}
