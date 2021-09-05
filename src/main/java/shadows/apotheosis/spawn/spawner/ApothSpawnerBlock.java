package shadows.apotheosis.spawn.spawner;

import java.util.List;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.material.Material;
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
import net.minecraft.item.SpawnEggItem;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import shadows.apotheosis.advancements.AdvancementTriggers;
import shadows.apotheosis.spawn.SpawnerModifiers;
import shadows.apotheosis.spawn.SpawnerModule;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
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
			player.causeFoodExhaustion(0.005F);
		} else super.playerDestroy(world, player, pos, state, te, stack);
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn) {
		return new ApothSpawnerTile();
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		TileEntity te = world.getBlockEntity(pos);
		if (te instanceof ApothSpawnerTile) {
			ApothSpawnerTile tile = (ApothSpawnerTile) te;
			boolean inverse = SpawnerModifiers.INVERSE.getIngredient().test(player.getItemInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND));
			for (SpawnerModifier sm : SpawnerModifiers.MODIFIERS.values()) {
				if (sm.canModify(tile, stack, inverse)) {
					if (world.isClientSide) return ActionResultType.SUCCESS;
					if (sm.modify(tile, stack, inverse)) {
						if (!player.isCreative() && !(stack.getItem() instanceof SpawnEggItem)) stack.shrink(1);
						AdvancementTriggers.SPAWNER_MODIFIER.trigger((ServerPlayerEntity) player, tile, sm);
						return ActionResultType.SUCCESS;
					}
				}
			}
		}
		return ActionResultType.PASS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTag() && stack.getTag().contains("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) {
			CompoundNBT tag = stack.getTag().getCompound("BlockEntityTag");
			if (tag.contains("SpawnData")) tooltip.add(this.grayTranslated("info.spw.entity", tag.getCompound("SpawnData").getString("id")));
			if (tag.contains("MinSpawnDelay")) tooltip.add(this.grayTranslated("waila.spw.mindelay", tag.getShort("MinSpawnDelay")));
			if (tag.contains("MaxSpawnDelay")) tooltip.add(this.grayTranslated("waila.spw.maxdelay", tag.getShort("MaxSpawnDelay")));
			if (tag.contains("SpawnCount")) tooltip.add(this.grayTranslated("waila.spw.spawncount", tag.getShort("SpawnCount")));
			if (tag.contains("MaxNearbyEntities")) tooltip.add(this.grayTranslated("waila.spw.maxnearby", tag.getShort("MaxNearbyEntities")));
			if (tag.contains("RequiredPlayerRange")) tooltip.add(this.grayTranslated("waila.spw.playerrange", tag.getShort("RequiredPlayerRange")));
			if (tag.contains("SpawnRange")) tooltip.add(this.grayTranslated("waila.spw.spawnrange", tag.getShort("SpawnRange")));
			if (tag.getBoolean("ignore_players")) tooltip.add(this.grayTranslated("waila.spw.ignoreplayers"));
			if (tag.getBoolean("ignore_conditions")) tooltip.add(this.grayTranslated("waila.spw.ignoreconditions"));
			if (tag.getBoolean("ignore_cap")) tooltip.add(this.grayTranslated("waila.spw.ignorecap"));
			if (tag.getBoolean("redstone_control")) tooltip.add(this.grayTranslated("waila.spw.redstone"));
		}
	}

	private ITextComponent grayTranslated(String s, Object... args) {
		return new TranslationTextComponent(s, args).withStyle(TextFormatting.GRAY);
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