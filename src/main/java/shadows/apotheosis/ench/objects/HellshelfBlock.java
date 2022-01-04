package shadows.apotheosis.ench.objects;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import shadows.apotheosis.ApotheosisObjects;

public class HellshelfBlock extends Block implements IEnchantingBlock {

	public static final IntegerProperty INFUSION = IntegerProperty.create("infusion", 0, 5);

	public HellshelfBlock() {
		super(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(2, 10).sound(SoundType.STONE));
	}

	@Override
	public float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos) {
		return 1.5F + state.getValue(INFUSION) * 0.1F;
	}

	@Override
	public float getQuantaBonus(BlockState state, LevelReader world, BlockPos pos) {
		return 0.15F + state.getValue(INFUSION) * 0.01F;
	}

	@Override
	public float getMaxEnchantingPower(BlockState state, LevelReader world, BlockPos pos) {
		return 22.5F + state.getValue(INFUSION) * 1.5F;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(INFUSION);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		ItemStack stack = ctx.getItemInHand();
		return this.defaultBlockState().setValue(INFUSION, Math.min(5, EnchantmentHelper.getItemEnchantmentLevel(ApotheosisObjects.HELL_INFUSION, stack)));
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		ItemStack stack = new ItemStack(this);
		if (state.getValue(INFUSION) > 0) EnchantmentHelper.setEnchantments(ImmutableMap.of(ApotheosisObjects.HELL_INFUSION, state.getValue(INFUSION)), stack);
		return Arrays.asList(stack);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
		ItemStack stack = new ItemStack(this);
		if (state.getValue(INFUSION) > 0) EnchantmentHelper.setEnchantments(ImmutableMap.of(ApotheosisObjects.HELL_INFUSION, state.getValue(INFUSION)), stack);
		return stack;
	}

}