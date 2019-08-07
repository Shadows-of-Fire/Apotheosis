package shadows.ench.objects;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.ApotheosisObjects;

public class BlockHellBookshelf extends Block {

	public static final IntegerProperty INFUSION = IntegerProperty.create("infusion", 0, 30);

	public BlockHellBookshelf() {
		super(Block.Properties.create(Material.ROCK, MaterialColor.BLACK).hardnessAndResistance(2, 10).sound(SoundType.STONE));
	}

	@Override
	public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) {
		if (state.getBlock() == this) return 2 + state.get(INFUSION) * 0.2F;
		return 2;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader world, List<ITextComponent> info, ITooltipFlag flag) {
		info.add(new TranslationTextComponent("info.apotheosis.hellshelf", String.valueOf(2 + Math.min(15, EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.HELL_INFUSION, stack)) * 0.2F).substring(0, 3)));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(INFUSION);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		ItemStack stack = ctx.getItem();
		return getDefaultState().with(INFUSION, Math.min(15, EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.HELL_INFUSION, stack)));
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		ItemStack stack = new ItemStack(this);
		if (state.get(INFUSION) > 0) EnchantmentHelper.setEnchantments(ImmutableMap.of(ApotheosisObjects.HELL_INFUSION, state.get(INFUSION)), stack);
		return Arrays.asList(stack);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		ItemStack stack = new ItemStack(this);
		if (state.get(INFUSION) > 0) EnchantmentHelper.setEnchantments(ImmutableMap.of(ApotheosisObjects.HELL_INFUSION, state.get(INFUSION)), stack);
		return stack;
	}

}
