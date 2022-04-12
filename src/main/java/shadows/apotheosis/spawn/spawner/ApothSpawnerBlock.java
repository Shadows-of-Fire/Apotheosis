package shadows.apotheosis.spawn.spawner;

import java.util.List;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import shadows.apotheosis.advancements.AdvancementTriggers;
import shadows.apotheosis.spawn.SpawnerModule;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
import shadows.apotheosis.spawn.modifiers.SpawnerStats;
import shadows.placebo.util.IReplacementBlock;

public class ApothSpawnerBlock extends SpawnerBlock implements IReplacementBlock {

	public ApothSpawnerBlock() {
		super(AbstractBlock.Properties.of(Material.STONE).strength(5.0F).sound(SoundType.METAL).noOcclusion());
		this.setRegistryName("minecraft", "spawner");
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		ItemStack s = new ItemStack(this);
		TileEntity te = world.getBlockEntity(pos);
		if (te != null) te.save(s.getOrCreateTagElement("BlockEntityTag"));
		return s;
	}

	@Override
	public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		TileEntity te = world.getBlockEntity(pos);
		if (te != null) {
			te.load(state, stack.getOrCreateTagElement("BlockEntityTag"));
			te.setPosition(pos);
		}
	}

	@Override
	public void playerDestroy(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te, ItemStack stack) {
		if (SpawnerModule.spawnerSilkLevel != -1 && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) >= SpawnerModule.spawnerSilkLevel) {
			ItemStack s = new ItemStack(this);
			if (te != null) te.save(s.getOrCreateTagElement("BlockEntityTag"));
			popResource(world, pos, s);
			player.getMainHandItem().hurtAndBreak(SpawnerModule.spawnerSilkDamage, player, pl -> pl.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
			player.awardStat(Stats.BLOCK_MINED.get(this));
			player.causeFoodExhaustion(0.035F);
		} else super.playerDestroy(world, player, pos, state, te, stack);
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn) {
		return new ApothSpawnerTile();
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		TileEntity te = world.getBlockEntity(pos);
		ItemStack stack = player.getItemInHand(hand);
		ItemStack otherStack = player.getItemInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
		if (te instanceof ApothSpawnerTile) {
			ApothSpawnerTile tile = (ApothSpawnerTile) te;
			SpawnerModifier match = SpawnerModifier.findMatch(tile, stack, otherStack);
			if (match != null && match.apply(tile)) {
				if (world.isClientSide) return ActionResultType.SUCCESS;
				if (!player.isCreative()) {
					stack.shrink(1);
					if (match.consumesOffhand()) otherStack.shrink(1);
				}
				AdvancementTriggers.SPAWNER_MODIFIER.trigger((ServerPlayerEntity) player, tile, match);
				world.sendBlockUpdated(pos, state, state, 3);
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.PASS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTag() && stack.getTag().contains("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) {
			if (Screen.hasShiftDown()) {
				CompoundNBT tag = stack.getTag().getCompound("BlockEntityTag");
				if (tag.contains("SpawnData")) {
					String name = tag.getCompound("SpawnData").getCompound("entity").getString("id");
					String key = "entity." + name.replace(':', '.');
					tooltip.add(concat(new TranslationTextComponent("misc.apotheosis.entity"), I18n.exists(key) ? I18n.get(key) : name));
				}
				if (tag.contains("MinSpawnDelay")) tooltip.add(concat(SpawnerStats.MIN_DELAY.name(), tag.getShort("MinSpawnDelay")));
				if (tag.contains("MaxSpawnDelay")) tooltip.add(concat(SpawnerStats.MAX_DELAY.name(), tag.getShort("MaxSpawnDelay")));
				if (tag.contains("SpawnCount")) tooltip.add(concat(SpawnerStats.SPAWN_COUNT.name(), tag.getShort("SpawnCount")));
				if (tag.contains("MaxNearbyEntities")) tooltip.add(concat(SpawnerStats.MAX_NEARBY_ENTITIES.name(), tag.getShort("MaxNearbyEntities")));
				if (tag.contains("RequiredPlayerRange")) tooltip.add(concat(SpawnerStats.REQ_PLAYER_RANGE.name(), tag.getShort("RequiredPlayerRange")));
				if (tag.contains("SpawnRange")) tooltip.add(concat(SpawnerStats.SPAWN_RANGE.name(), tag.getShort("SpawnRange")));
				if (tag.getBoolean("ignore_players")) tooltip.add(SpawnerStats.IGNORE_PLAYERS.name().withStyle(TextFormatting.DARK_GREEN));
				if (tag.getBoolean("ignore_conditions")) tooltip.add(SpawnerStats.IGNORE_CONDITIONS.name().withStyle(TextFormatting.DARK_GREEN));
				if (tag.getBoolean("redstone_control")) tooltip.add(SpawnerStats.REDSTONE_CONTROL.name().withStyle(TextFormatting.DARK_GREEN));
				if (tag.getBoolean("ignore_light")) tooltip.add(SpawnerStats.IGNORE_LIGHT.name().withStyle(TextFormatting.DARK_GREEN));
				if (tag.getBoolean("no_ai")) tooltip.add(SpawnerStats.NO_AI.name().withStyle(TextFormatting.DARK_GREEN));
			} else {
				tooltip.add(new TranslationTextComponent("misc.apotheosis.shift_stats").withStyle(TextFormatting.GRAY));
			}
		}
	}

	public static ITextComponent concat(Object... args) {
		return new TranslationTextComponent("misc.apotheosis.value_concat", args[0], new StringTextComponent(args[1].toString()).withStyle(TextFormatting.GRAY)).withStyle(TextFormatting.GREEN);
	}

	@Override
	public Item asItem() {
		return Items.SPAWNER;
	}

	@Override
	public int getExpDrop(BlockState state, IWorldReader world, BlockPos pos, int fortune, int silktouch) {
		return silktouch == 0 ? super.getExpDrop(state, world, pos, fortune, silktouch) : 0;
	}

	@Override
	public void _setDefaultState(BlockState state) {
		this.registerDefaultState(state);
	}

	protected StateContainer<Block, BlockState> container;

	@Override
	public void setStateContainer(StateContainer<Block, BlockState> container) {
		this.container = container;
	}

	@Override
	public StateContainer<Block, BlockState> getStateDefinition() {
		return this.container == null ? super.getStateDefinition() : this.container;
	}

}