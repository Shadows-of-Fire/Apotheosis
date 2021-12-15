package shadows.apotheosis.spawn.compat;

import com.google.common.collect.ImmutableSet;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.spawn.SpawnerModifiers;
import shadows.apotheosis.spawn.SpawnerModule;
import shadows.apotheosis.spawn.compat.SpawnerWrapper.SpawnerInverseWrapper;

@JeiPlugin
public class SpawnerJEIPlugin implements IModPlugin {

	public static final String SPAWNER = "spawner_modification";

	@Override
	public void registerRecipes(IRecipeRegistration reg) {
		if (!Apotheosis.enableSpawner) return;
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
				new SpawnerWrapper(new ResourceLocation("witch"), "jei.spw.changeentity"),
				new SpawnerInverseWrapper()
				), this.getPluginUid());
		//Formatter::on
		reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM, new TranslatableComponent("jei.spw.instructions", ((MutableComponent) Enchantments.SILK_TOUCH.getFullname(SpawnerModule.spawnerSilkLevel)).withStyle(ChatFormatting.DARK_BLUE).getString()));
		for (Item i : ForgeRegistries.ITEMS) {
			if (i instanceof SpawnEggItem) reg.addIngredientInfo(new ItemStack(i), VanillaTypes.ITEM, new TranslatableComponent("jei.spw.capturing"));
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
		if (!Apotheosis.enableSpawner) return;
		reg.addRecipeCatalyst(new ItemStack(Blocks.SPAWNER), this.getPluginUid());
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration reg) {
		if (!Apotheosis.enableSpawner) return;
		reg.addRecipeCategories(new SpawnerCategory(reg.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Apotheosis.MODID, SPAWNER);
	}

}