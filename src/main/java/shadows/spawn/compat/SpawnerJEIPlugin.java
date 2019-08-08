package shadows.spawn.compat;
/*
import com.google.common.collect.ImmutableSet;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.Apotheosis;
import shadows.spawn.SpawnerModifiers;
import shadows.spawn.compat.SpawnerWrapper.SpawnerInverseWrapper;

@JeiPlugin
public class SpawnerJEIPlugin implements IModPlugin {

	public static final String SPAWNER = "spawner_modification";

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		ItemStack egg = new ItemStack(SpawnEggItem.getEgg(EntityType.WITCH));
		//Formatter::off
		reg.addRecipes(ImmutableSet.of(
				new SpawnerWrapper(SpawnerModifiers.MIN_DELAY, "MinSpawnDelay", "jei.spw.editmindelay"),
				new SpawnerWrapper(SpawnerModifiers.MAX_DELAY, "MaxSpawnDelay", "jei.spw.editmaxdelay"),
				new SpawnerWrapper(SpawnerModifiers.SPAWN_COUNT, "SpawnCount", "jei.spw.editspawncount"),
				new SpawnerWrapper(SpawnerModifiers.NEARBY_ENTITIES, "MaxNearbyEntities", "jei.spw.editnearby"),
				new SpawnerWrapper(SpawnerModifiers.PLAYER_DISTANCE, "RequiredPlayerRange", "jei.spw.editplayer"),
				new SpawnerWrapper(SpawnerModifiers.SPAWN_RANGE, "SpawnRange", "jei.spw.editspawn"),
				new SpawnerWrapper(SpawnerModifiers.CONDITIONS, "ignore_conditions", true, "jei.spw.ignoreconditions"),
				new SpawnerWrapper(SpawnerModifiers.PLAYERS, "ignore_players", true, "jei.spw.ignoreplayers"),
				new SpawnerWrapper(SpawnerModifiers.CAP, "ignore_cap", true, "jei.spw.ignorecap"),
				new SpawnerWrapper(SpawnerModifiers.REDSTONE, "redstone_control", true, "jei.spw.redstone"),
				new SpawnerWrapper(egg, new ResourceLocation("witch"), "jei.spw.changeentity"),
				new SpawnerInverseWrapper()
				), getPluginUid());
		//Formatter::on
		reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM, "jei.spw.instructions");
		for (Item i : ForgeRegistries.ITEMS) {
			if (i instanceof SpawnEggItem) reg.addIngredientInfo(new ItemStack(i), VanillaTypes.ITEM, "jei.spw.capturing");
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
		reg.addRecipeCatalyst(new ItemStack(Blocks.SPAWNER), getPluginUid());
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration reg) {
		reg.addRecipeCategories(new SpawnerCategory(reg.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, SPAWNER);
	}

}
*/