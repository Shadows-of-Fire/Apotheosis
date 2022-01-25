package shadows.apotheosis.spawn.spawner;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.apotheosis.advancements.AdvancementTriggers;
import shadows.apotheosis.spawn.SpawnerModule;
import shadows.apotheosis.spawn.modifiers.SpawnerModifier;
import shadows.placebo.util.IReplacementBlock;

public class ApothSpawnerBlock extends SpawnerBlock implements IReplacementBlock {

	public ApothSpawnerBlock() {
		super(BlockBehaviour.Properties.of(Material.STONE).strength(5.0F).sound(SoundType.METAL).noOcclusion());
		this.setRegistryName("minecraft", "spawner");
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
		ItemStack s = new ItemStack(this);
		BlockEntity te = world.getBlockEntity(pos);
		if (te != null) s.getOrCreateTag().put("BlockEntityTag", te.saveWithoutMetadata());
		return s;
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		BlockEntity te = world.getBlockEntity(pos);
		if (te != null) te.load(stack.getOrCreateTagElement("BlockEntityTag"));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new ApothSpawnerTile(pPos, pState);
	}

	@Override
	public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, BlockEntity te, ItemStack stack) {
		if (SpawnerModule.spawnerSilkLevel != -1 && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) >= SpawnerModule.spawnerSilkLevel) {
			ItemStack s = new ItemStack(this);
			if (te != null) s.getOrCreateTag().put("BlockEntityTag", te.saveWithoutMetadata());
			popResource(world, pos, s);
			player.getMainHandItem().hurtAndBreak(SpawnerModule.spawnerSilkDamage, player, pl -> pl.broadcastBreakEvent(EquipmentSlot.MAINHAND));
			player.awardStat(Stats.BLOCK_MINED.get(this));
			player.causeFoodExhaustion(0.035F);
		} else super.playerDestroy(world, player, pos, state, te, stack);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		BlockEntity te = world.getBlockEntity(pos);
		ItemStack stack = player.getItemInHand(hand);
		ItemStack otherStack = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
		if (te instanceof ApothSpawnerTile tile) {
			SpawnerModifier match = SpawnerModifier.findMatch(tile, stack, otherStack);
			if (match.apply(tile)) {
				if (world.isClientSide) return InteractionResult.SUCCESS;
				if (!player.isCreative()) {
					stack.shrink(1);
					if (match.consumesOffhand()) otherStack.shrink(1);
				}
				AdvancementTriggers.SPAWNER_MODIFIER.trigger((ServerPlayer) player, tile, match);
				world.sendBlockUpdated(pos, state, state, 3);
				return InteractionResult.CONSUME;

			}
		}
		return InteractionResult.PASS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (stack.hasTag() && stack.getTag().contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
			CompoundTag tag = stack.getTag().getCompound("BlockEntityTag");
			if (tag.contains("SpawnData")) tooltip.add(this.grayTranslated("info.spw.entity", tag.getCompound("SpawnData").getCompound("entity").getString("id")));
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

	private Component grayTranslated(String s, Object... args) {
		return new TranslatableComponent(s, args).withStyle(ChatFormatting.GRAY);
	}

	@Override
	public Item asItem() {
		return Items.SPAWNER;
	}

	@Override
	public int getExpDrop(BlockState state, LevelReader world, BlockPos pos, int fortune, int silktouch) {
		return silktouch == 0 ? super.getExpDrop(state, world, pos, fortune, silktouch) : 0;
	}

	@Override
	public void _setDefaultState(BlockState state) {
		this.registerDefaultState(state);
	}

	protected StateDefinition<Block, BlockState> container;

	@Override
	public void setStateContainer(StateDefinition<Block, BlockState> container) {
		this.container = container;
	}

	@Override
	public StateDefinition<Block, BlockState> getStateDefinition() {
		return this.container == null ? super.getStateDefinition() : this.container;
	}

}