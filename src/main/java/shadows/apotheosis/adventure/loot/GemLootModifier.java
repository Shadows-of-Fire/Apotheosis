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
import shadows.apotheosis.adventure.affix.socket.GemManager;

public class GemLootModifier extends LootModifier {

	protected GemLootModifier(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		if (!Apotheosis.enableAdventure) return generatedLoot;
		for (LootPatternMatcher m : AdventureConfig.AFFIX_ITEM_LOOT_RULES) {
			if (m.matches(context.getQueriedLootTableId())) {
				if (context.getRandom().nextFloat() <= m.chance()) {
					float luck = context.getLuck();
					ItemStack gem = GemManager.getRandomGemStack(context.getRandom(), luck, context.getLevel());
					generatedLoot.add(gem);
				}
				break;
			}
		}
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<GemLootModifier> {

		@Override
		public GemLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
			return new GemLootModifier(conditions);
		}

		@Override
		public JsonObject write(GemLootModifier instance) {
			return this.makeConditions(instance.conditions);
		}

	}

}
