package shadows.apotheosis.ench.table;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IRegistryDelegate;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.ench.objects.IEnchantingBlock;
import shadows.placebo.util.NetworkUtils;
import shadows.placebo.util.NetworkUtils.MessageProvider;

public class EnchantingStatManager extends JsonReloadListener {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final EnchantingStatManager INSTANCE = new EnchantingStatManager();

	private final Map<IRegistryDelegate<Block>, Stats> stats = new HashMap<>();

	protected EnchantingStatManager() {
		super(GSON, "enchanting_stats");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, IResourceManager mgr, IProfiler profile) {
		this.stats.clear();
		objects.forEach((key, ele) -> {
			try {
				JsonObject obj = (JsonObject) ele;
				Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(obj.get("block").getAsString()));
				Stats stats = GSON.fromJson(obj.get("stats"), Stats.class);
				this.stats.put(b.delegate, stats);
			} catch (Exception e) {
				EnchModule.LOGGER.error("Failed to read enchantment stat file {}.", key);
				e.printStackTrace();
			}
		});
		EnchModule.LOGGER.info("Registered {} blocks with enchanting stats.", this.stats.size());
		if (ServerLifecycleHooks.getCurrentServer() != null) Apotheosis.CHANNEL.send(PacketDistributor.ALL.noArg(), new StatSyncMessage(this.stats));
	}

	public static float getEterna(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).eterna;
		return state.getEnchantPowerBonus(world, pos);
	}

	public static float getMaxEterna(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).maxEterna;
		if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getMaxEnchantingPower(state, world, pos);
		return 15;
	}

	public static float getQuanta(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).quanta;
		else if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getQuantaBonus(state, world, pos);
		return 0;
	}

	public static float getArcana(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (INSTANCE.stats.containsKey(block.delegate)) return INSTANCE.stats.get(block.delegate).arcana;
		else if (block instanceof IEnchantingBlock) return ((IEnchantingBlock) block).getArcanaBonus(state, world, pos);
		return 0;
	}

	public static float getAbsoluteMaxEterna() {
		return INSTANCE.stats.values().stream().max(Comparator.comparingDouble(s -> s.maxEterna)).get().maxEterna;
	}

	public static void dispatch(PlayerEntity player) {
		NetworkUtils.sendTo(Apotheosis.CHANNEL, new StatSyncMessage(INSTANCE.stats), player);
	}

	public static class Stats {
		final float maxEterna, eterna, quanta, arcana;

		public Stats(float maxEterna, float eterna, float quanta, float arcana) {
			this.maxEterna = maxEterna;
			this.eterna = eterna;
			this.quanta = quanta;
			this.arcana = arcana;
		}
	}

	public static class StatSyncMessage extends MessageProvider<StatSyncMessage> {

		final Map<IRegistryDelegate<Block>, Stats> stats;

		private StatSyncMessage(Map<IRegistryDelegate<Block>, Stats> stats) {
			this.stats = stats;
		}

		public StatSyncMessage() {
			this.stats = new HashMap<>();
		}

		@Override
		public void write(StatSyncMessage msg, PacketBuffer buf) {
			buf.writeShort(msg.stats.size());
			for (Map.Entry<IRegistryDelegate<Block>, Stats> e : msg.stats.entrySet()) {
				buf.writeInt(((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getID(e.getKey().get()));
				Stats stat = e.getValue();
				buf.writeFloat(stat.maxEterna);
				buf.writeFloat(stat.eterna);
				buf.writeFloat(stat.quanta);
				buf.writeFloat(stat.arcana);
			}
		}

		@Override
		public StatSyncMessage read(PacketBuffer buf) {
			int size = buf.readShort();
			StatSyncMessage pkt = new StatSyncMessage();
			for (int i = 0; i < size; i++) {
				Block b = ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getValue(buf.readInt());
				Stats stats = new Stats(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
				pkt.stats.put(b.delegate, stats);
			}
			return pkt;
		}

		@Override
		public void handle(StatSyncMessage msg, Supplier<Context> ctx) {
			NetworkUtils.handlePacket(() -> () -> {
				INSTANCE.stats.clear();
				INSTANCE.stats.putAll(msg.stats);
			}, ctx.get());
		}

	}

}