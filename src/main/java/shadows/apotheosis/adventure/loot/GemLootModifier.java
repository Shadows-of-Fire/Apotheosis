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
import shadows.apotheosis.adventure.affix.socket.GemManager;

public class GemLootModifier extends LootModifier {

	public static final Predicate<LootContext> IS_CHEST = c -> c.getQueriedLootTableId().getPath().startsWith("chests/");

	protected GemLootModifier(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		if (!Apotheosis.enableAdventure) return generatedLoot;
		if (IS_CHEST.test(context) && context.getRandom().nextFloat() <= AdventureConfig.gemChestChance) {
			float luck = context.getLuck();
			ItemStack gem = GemManager.getRandomGemStack(context.getRandom(), luck);
			generatedLoot.add(gem);
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
