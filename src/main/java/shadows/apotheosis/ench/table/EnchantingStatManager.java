package shadows.apotheosis.ench.table;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IRegistryDelegate;
import net.minecraftforge.server.ServerLifecycleHooks;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.ench.objects.IEnchantingBlock;
import shadows.apotheosis.util.JsonUtil;
import shadows.placebo.network.MessageHelper;
import shadows.placebo.network.MessageProvider;
import shadows.placebo.network.PacketDistro;

public class EnchantingStatManager extends SimpleJsonResourceReloadListener {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final EnchantingStatManager INSTANCE = new EnchantingStatManager();
	private final Map<IRegistryDelegate<Block>, Stats> stats = new HashMap<>();

	private float absoluteMaxEterna = 50;

	protected EnchantingStatManager() {
		super(GSON, "enchanting_stats");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager mgr, ProfilerFiller profile) {
		this.stats.clear();
		objects.forEach((key, ele) -> {
			try {
				if (!JsonUtil.checkAndLogEmpty(ele, key, "Enchanting Stats", EnchModule.LOGGER)) {
					JsonObject obj = (JsonObject) ele;
					Stats stats = GSON.fromJson(obj.get("stats"), Stats.class);
					if (obj.has("tag")) {
						Tag<Block> tag = SerializationTags.getInstance().getTagOrThrow(Registry.BLOCK_REGISTRY, new ResourceLocation(obj.get("tag").getAsString()), (p_151262_) -> {
							return new JsonSyntaxException("Unknown block tag '" + p_151262_ + "'");
						});
						tag.getValues().forEach(b -> this.stats.put(b.delegate, stats));
					} else {
						Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(obj.get("block").getAsString()));
						this.stats.put(b.delegate, stats);
					}
				}
			} catch (Exception e) {
				EnchModule.LOGGER.error("Failed to read enchantment stat file {}.", key);
				e.printStackTrace();
			}
		});
		EnchModule.LOGGER.info("Registered {} blocks with enchanting stats.", this.stats.size());
		if (ServerLifecycleHooks.getCurrentServer() != null) Apotheosis.CHANNEL.send(PacketDistributor.ALL.noArg(), new StatSyncMessage(this.stats));
		this.absoluteMaxEterna = this.computeAbsoluteMaxEterna();
	}

	/**
	 * Retrieves the Eterna value for a specific block.
	 * This can be provided by a stat file, or {@link BlockState#getEnchantPowerBonus}
	 * 1F of Eterna = 2 Levels in the enchanting table.
	 */
	public static float getEterna(BlockState state, Level world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).eterna;
		return state.getEnchantPowerBonus(world, pos);
	}

	/**
	 * Retrieves the Max Eterna value for a specific block.
	 * This can be provided by a stat file, or {@link IEnchantingBlock#getMaxEnchantingPower}
	 * 1F of Eterna = 2 Levels in the enchanting table.
	 */
	public static float getMaxEterna(BlockState state, Level world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).maxEterna;
		if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getMaxEnchantingPower(state, world, pos);
		return 15;
	}

	/**
	 * Retrieves the Quanta value for a specific block.
	 * This can be provided by a stat file, or {@link IEnchantingBlock#getQuantaBonus}
	 * 1F of Quanta = 1% of Quanta in the enchanting table.
	 */
	public static float getQuanta(BlockState state, Level world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).quanta;
		else if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getQuantaBonus(state, world, pos);
		return 0;
	}

	/**
	 * Retrieves the Arcana value for a specific block.
	 * This can be provided by a stat file, or {@link IEnchantingBlock#getArcanaBonus}
	 * 1F of Arcana = 1% of Arcana in the enchanting table.
	 */
	public static float getArcana(BlockState state, Level world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).arcana;
		else if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getArcanaBonus(state, world, pos);
		return 0;
	}

	/**
	 * Retrieves the Quanta Rectification value for a specific block.
	 * See {@link IEnchantingBlock#getQuantaRectification}
	 */
	public static float getQuantaRectification(BlockState state, Level world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).rectification;
		if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getQuantaRectification(state, world, pos);
		return 0;
	}

	/**
	 * Retrieves the number of bonus clues this block provides.
	 * See {@link IEnchantingBlock#getBonusClues}
	 */
	public static int getBonusClues(BlockState state, Level world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).clues;
		if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getBonusClues(state, world, pos);
		return 0;
	}

	/**
	 * This returns the highest possible eterna value, based on the definitions for all stat providers.
	 */
	public static float getAbsoluteMaxEterna() {
		return INSTANCE.absoluteMaxEterna;
	}

	private float computeAbsoluteMaxEterna() {
		return stats.values().stream().max(Comparator.comparingDouble(s -> s.maxEterna)).get().maxEterna;
	}

	public static void dispatch(Player player) {
		PacketDistro.sendTo(Apotheosis.CHANNEL, new StatSyncMessage(INSTANCE.stats), player);
	}

	/**
	 * Enchanting Stats.
	 * Max Eterna is the highest amount of eterna this object may contribute to.
	 * Eterna is the eterna provided (1F == 2 levels)
	 * Quanta is the quanta provided (1F == 1%)
	 * Arcana is the arcana provided (1F == 1%)
	 */
	public static class Stats {
		final float maxEterna, eterna, quanta, arcana, rectification;
		final int clues;

		public Stats(float maxEterna, float eterna, float quanta, float arcana, float rectification, int clues) {
			this.maxEterna = maxEterna;
			this.eterna = eterna;
			this.quanta = quanta;
			this.arcana = arcana;
			this.rectification = rectification;
			this.clues = clues;
		}

		public void write(FriendlyByteBuf buf) {
			buf.writeFloat(this.maxEterna);
			buf.writeFloat(this.eterna);
			buf.writeFloat(this.quanta);
			buf.writeFloat(this.arcana);
			buf.writeFloat(this.rectification);
			buf.writeByte(this.clues);
		}

		public static Stats read(FriendlyByteBuf buf) {
			return new Stats(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readByte());
		}
	}

	public static class StatSyncMessage implements MessageProvider<StatSyncMessage> {

		final Map<IRegistryDelegate<Block>, Stats> stats;

		private StatSyncMessage(Map<IRegistryDelegate<Block>, Stats> stats) {
			this.stats = stats;
		}

		public StatSyncMessage() {
			this.stats = new HashMap<>();
		}

		@Override
		public void write(StatSyncMessage msg, FriendlyByteBuf buf) {
			buf.writeShort(msg.stats.size());
			for (Map.Entry<IRegistryDelegate<Block>, Stats> e : msg.stats.entrySet()) {
				buf.writeInt(((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getID(e.getKey().get()));
				e.getValue().write(buf);
			}
		}

		@Override
		public StatSyncMessage read(FriendlyByteBuf buf) {
			int size = buf.readShort();
			StatSyncMessage pkt = new StatSyncMessage();
			for (int i = 0; i < size; i++) {
				Block b = ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getValue(buf.readInt());
				pkt.stats.put(b.delegate, Stats.read(buf));
			}
			return pkt;
		}

		@Override
		public void handle(StatSyncMessage msg, Supplier<Context> ctx) {
			MessageHelper.handlePacket(() -> () -> {
				INSTANCE.stats.clear();
				INSTANCE.stats.putAll(msg.stats);
			}, ctx);
		}

	}

}