package shadows.apotheosis.ench.anvil;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.Constants;
import shadows.apotheosis.ApotheosisObjects;

public class AnvilTile extends TileEntity {

	protected final Object2IntMap<Enchantment> enchantments = new Object2IntOpenHashMap<>();

	public AnvilTile() {
		super(ApotheosisObjects.ANVIL);
	}

	int unbreaking = 0;
	int splitting = 0;

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		ItemStack stack = new ItemStack(Items.ANVIL);
		EnchantmentHelper.setEnchantments(enchantments, stack);
		tag.put("enchantments", stack.getEnchantmentTagList());
		return super.write(tag);
	}

	@Override
	public void read(CompoundNBT tag) {
		super.read(tag);
		ListNBT enchants = tag.getList("enchantments", Constants.NBT.TAG_COMPOUND);
		Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(enchants);
		if (tag.contains("ub")) {
			map.put(Enchantments.UNBREAKING, tag.getInt("ub"));
		}
		if (tag.contains("splitting")) {
			map.put(ApotheosisObjects.SPLITTING, tag.getInt("splitting"));
		}
		enchantments.clear();
		enchantments.putAll(map);
	}

	@Override
	public TileEntityType<?> getType() {
		return ApotheosisObjects.ANVIL;
	}

	public Object2IntMap<Enchantment> getEnchantments() {
		return enchantments;
	}

}
