package shadows.apotheosis.adventure.loot;

import java.util.List;
import java.util.Random;

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
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.loot.LootRarity.Clamped;

public class AffixConvertLootModifier extends LootModifier {

	protected AffixConvertLootModifier(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		if (!Apotheosis.enableAdventure) return generatedLoot;
		for (LootPatternMatcher m : AdventureConfig.AFFIX_CONVERT_LOOT_RULES) {
			if (m.matches(context.getQueriedLootTableId())) {
				Clamped rarities = AdventureConfig.AFFIX_CONVERT_RARITIES.get(context.getLevel().dimension().location());
				Random rand = context.getRandom();
				float luck = context.getLuck();
				for (ItemStack s : generatedLoot) {
					if (LootCategory.forItem(s) != LootCategory.NONE && AffixHelper.getAffixes(s).isEmpty() && rand.nextFloat() <= m.chance()) {
						LootController.createLootItem(s, LootRarity.random(rand, luck, rarities), rand);
					}
				}
				break;
			}
		}
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<AffixConvertLootModifier> {

		@Override
		public AffixConvertLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
			return new AffixConvertLootModifier(conditions);
		}

		@Override
		public JsonObject write(AffixConvertLootModifier instance) {
			return this.makeConditions(instance.conditions);
		}

	}

}
