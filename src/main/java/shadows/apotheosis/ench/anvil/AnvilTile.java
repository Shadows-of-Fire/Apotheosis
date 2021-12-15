package shadows.apotheosis.ench.anvil;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import shadows.apotheosis.ApotheosisObjects;

public class AnvilTile extends BlockEntity {

	protected final Object2IntMap<Enchantment> enchantments = new Object2IntOpenHashMap<>();

	public AnvilTile() {
		super(ApotheosisObjects.ANVIL);
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		ItemStack stack = new ItemStack(Items.ANVIL);
		EnchantmentHelper.setEnchantments(this.enchantments, stack);
		tag.put("enchantments", stack.getEnchantmentTags());
		return super.save(tag);
	}

	@Override
	public void load(BlockState state, CompoundTag tag) {
		super.load(state, tag);
		ListTag enchants = tag.getList("enchantments", Constants.NBT.TAG_COMPOUND);
		Map<Enchantment, Integer> map = EnchantmentHelper.deserializeEnchantments(enchants);
		if (tag.getInt("ub") > 0) {
			map.put(Enchantments.UNBREAKING, tag.getInt("ub"));
		}
		if (tag.getInt("splitting") > 0) {
			map.put(ApotheosisObjects.SPLITTING, tag.getInt("splitting"));
		}
		this.enchantments.clear();
		this.enchantments.putAll(map);
	}

	@Override
	public BlockEntityType<?> getType() {
		return ApotheosisObjects.ANVIL;
	}

	public Object2IntMap<Enchantment> getEnchantments() {
		return this.enchantments;
	}

}