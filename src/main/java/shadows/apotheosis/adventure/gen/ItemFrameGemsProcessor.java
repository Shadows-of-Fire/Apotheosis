package shadows.apotheosis.adventure.gen;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.socket.gem.GemManager;
import shadows.placebo.json.WeightedJsonReloadListener.IDimensional;

public class ItemFrameGemsProcessor extends StructureProcessor {
	public static final Codec<ItemFrameGemsProcessor> CODEC = RecordCodecBuilder.create(instance -> instance.group(ResourceLocation.CODEC.fieldOf("loot_table").forGetter(ItemFrameGemsProcessor::getLootTable)).apply(instance, ItemFrameGemsProcessor::new));

	private final ResourceLocation lootTable;

	public ResourceLocation getLootTable() {
		return this.lootTable;
	}

	public ItemFrameGemsProcessor(ResourceLocation lootTable) {
		this.lootTable = lootTable;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return AdventureModule.ITEM_FRAME_LOOT;
	}

	@Override
	public StructureEntityInfo processEntity(LevelReader world, BlockPos seedPos, StructureEntityInfo rawEntityInfo, StructureEntityInfo entityInfo, StructurePlaceSettings placementSettings, StructureTemplate template) {
		CompoundTag entityNBT = entityInfo.nbt;

		String id = entityNBT.getString("id"); // entity type ID
		if (world instanceof ServerLevelAccessor && "minecraft:item_frame".equals(id)) {
			ServerLevel serverWorld = ((ServerLevelAccessor) world).getLevel();
			this.writeEntityNBT(serverWorld, entityInfo.blockPos, entityNBT, placementSettings);
		}

		return entityInfo;
	}

	protected void writeEntityNBT(ServerLevel world, BlockPos pos, CompoundTag nbt, StructurePlaceSettings settings) {
		// generate and set itemstack
		ItemStack stack = GemManager.createRandomGemStack(world.getRandom(), world, 0, IDimensional.matches(world));
		nbt.put("Item", stack.serializeNBT());
		nbt.putInt("TileX", pos.getX());
		nbt.putInt("TileY", pos.getY());
		nbt.putInt("TileZ", pos.getZ());
	}

	protected ItemStack generateItemStack(ServerLevel world, BlockPos pos) {
		LootContext context = new LootContext.Builder(world).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)) // positional context
				.create(LootContextParamSets.CHEST); // chest set requires positional context, has no other mandatory parameters

		LootTable table = world.getServer().getLootTables().get(this.lootTable);
		List<ItemStack> stacks = table.getRandomItems(context);
		return stacks.size() > 0 ? stacks.get(0) : ItemStack.EMPTY;
	}
}
