package shadows.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.network.NetworkEvent.Context;
import shadows.placebo.network.MessageHelper;
import shadows.placebo.network.MessageProvider;

public class ClueMessage implements MessageProvider<ClueMessage> {

	protected final int slot;
	protected final List<EnchantmentInstance> clues;
	protected final boolean all;

	/**
	 * Sends a clue message to the client.
	 * @param slot 
	 * @param clues The clues.
	 * @param all If this is all of the enchantments being received.
	 */
	public ClueMessage(int slot, List<EnchantmentInstance> clues, boolean all) {
		this.slot = slot;
		this.clues = clues;
		this.all = all;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void write(ClueMessage msg, FriendlyByteBuf buf) {
		buf.writeByte(msg.clues.size());
		for (EnchantmentInstance e : msg.clues) {
			buf.writeShort(Registry.ENCHANTMENT.getId(e.enchantment));
			buf.writeByte(e.level);
		}
		buf.writeByte(msg.slot);
		buf.writeBoolean(msg.all);
	}

	@Override
	@SuppressWarnings("deprecation")
	public ClueMessage read(FriendlyByteBuf buf) {
		int size = buf.readByte();
		List<EnchantmentInstance> clues = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			Enchantment ench = Registry.ENCHANTMENT.byIdOrThrow(buf.readShort());
			clues.add(new EnchantmentInstance(ench, buf.readByte()));
		}
		return new ClueMessage(buf.readByte(), clues, buf.readBoolean());
	}

	@Override
	public void handle(ClueMessage msg, Supplier<Context> ctx) {
		MessageHelper.handlePacket(() -> () -> {
			if (Minecraft.getInstance().screen instanceof ApothEnchantScreen es) {
				es.acceptClues(msg.slot, msg.clues, msg.all);
			}
		}, ctx);
	}

}
