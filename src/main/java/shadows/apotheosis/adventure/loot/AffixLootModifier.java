package shadows.apotheosis.adventure.loot;

import java.util.List;
import java.util.function.Predicate;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureConfig;

public class AffixLootModifier extends LootModifier {

	public static final Predicate<LootContext> IS_CHEST = c -> c.getQueriedLootTableId().getPath().startsWith("chests/");

	protected AffixLootModifier(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		if (!Apotheosis.enableAdventure) return generatedLoot;
		if (IS_CHEST.test(context) && context.getRandom().nextFloat() <= AdventureConfig.affixChestChance) {
			float luck = context.getLuck();
			ItemStack affixItem = LootController.createRandomLootItem(context.getRandom(), Math.min(1000, (int) (25 * luck)), luck);
			affixItem.getTag().putBoolean("apoth_rchest", true);
			generatedLoot.add(affixItem);
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
