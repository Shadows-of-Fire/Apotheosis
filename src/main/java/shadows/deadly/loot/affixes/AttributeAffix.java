package shadows.deadly.loot.affixes;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.RandomValueRange;
import shadows.deadly.gen.BossItem.EquipmentType;
import shadows.deadly.loot.Affix;
import shadows.deadly.loot.AffixModifier;

public class AttributeAffix extends Affix {

	protected final IAttribute attr;
	protected final RandomValueRange range;
	protected final int op;

	public AttributeAffix(IAttribute attr, RandomValueRange range, int op, String key, boolean prefix, int weight) {
		super(key, prefix, weight);
		this.attr = attr;
		this.range = range;
		this.op = op;
	}

	public AttributeAffix(IAttribute attr, float min, float max, int op, String key, boolean prefix, int weight) {
		this(attr, new RandomValueRange(min, max), op, key, prefix, weight);
	}

	@Override
	public void apply(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
		EntityEquipmentSlot type = EquipmentType.getTypeFor(stack).getSlot(stack);
		AttributeModifier modif = new AttributeModifier(key, range.generateFloat(rand), op);
		stack.addAttributeModifier(attr.getName(), modif, type);
	}

}
