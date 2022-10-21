package shadows.apotheosis.adventure.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.AdventureConfig.LootPatternMatcher;

public class AffixLootModifier extends LootModifier {

	public static final Codec<AffixLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, AffixLootModifier::new));

	protected AffixLootModifier(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		if (!Apotheosis.enableAdventure) return generatedLoot;
		for (LootPatternMatcher m : AdventureConfig.AFFIX_ITEM_LOOT_RULES) {
			if (m.matches(context.getQueriedLootTableId())) {
				if (context.getRandom().nextFloat() <= m.chance()) {
					float luck = context.getLuck();
					ItemStack affixItem = LootController.createRandomLootItem(context.getRandom(), null, luck, context.getLevel());
					if (affixItem.isEmpty()) break;
					affixItem.getTag().putBoolean("apoth_rchest", true);
					generatedLoot.add(affixItem);
				}
				break;
			}
		}
		return generatedLoot;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}
