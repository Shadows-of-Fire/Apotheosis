package shadows.apotheosis.adventure.loot;

import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.AdventureConfig.LootPatternMatcher;

public class AffixLootModifier extends LootModifier {

	protected AffixLootModifier(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
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

	public static class Serializer extends GlobalLootModifierSerializer<AffixLootModifier> {

		@Override
		public AffixLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
			return new AffixLootModifier(conditions);
		}

		@Override
		public JsonObject write(AffixLootModifier instance) {
			return this.makeConditions(instance.conditions);
		}

	}

}
