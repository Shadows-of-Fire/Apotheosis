package shadows.apotheosis.deadly.affix.recipe;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.LootRarity;

public class AffixShardingRecipe extends SoulfireCookingRecipe {

	public static final IRecipeSerializer<AffixShardingRecipe> SERIALIZER = new SpecialRecipeSerializer<>(AffixShardingRecipe::new);

	public AffixShardingRecipe(ResourceLocation id) {
		super(new ResourceLocation(Apotheosis.MODID, "affix_sharding"), "", Ingredient.EMPTY, ItemStack.EMPTY, 0, 200);
	}

	@Override
	protected boolean matches(ItemStack stack) {
		return AffixHelper.getRarity(stack) != null;
	}

	@Override
	public ItemStack getCraftingResult(IInventory inv) {
		ItemStack stack = inv.getStackInSlot(0);
		LootRarity rarity = AffixHelper.getRarity(stack);
		if (rarity == null) return stack;
		return new ItemStack(DeadlyModule.RARITY_SHARDS.get(rarity), 1 + ThreadLocalRandom.current().nextInt(3));
	}

}
