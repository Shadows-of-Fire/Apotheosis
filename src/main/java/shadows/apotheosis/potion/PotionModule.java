package shadows.apotheosis.potion;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolderRegistry;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisConstruction;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.potion.compat.CuriosCompat;
import shadows.apotheosis.potion.potions.KnowledgeEffect;
import shadows.apotheosis.potion.potions.PotionSundering;
import shadows.placebo.config.Configuration;

public class PotionModule {

	public static final Logger LOG = LogManager.getLogger("Apotheosis : Potion");
	public static final ResourceLocation POTION_TEX = new ResourceLocation(Apotheosis.MODID, "textures/potions.png");

	static int knowledgeMult = 4;

	@SubscribeEvent
	public void preInit(ApotheosisConstruction e) {
		this.reload(null);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			FMLJavaModLoadingContext.get().getModEventBus().register(new PotionModuleClient());
		});
	}

	@SubscribeEvent
	public void init(FMLCommonSetupEvent e) {
		PotionBrewing.addMix(Potions.AWKWARD, Items.SHULKER_SHELL, ApotheosisObjects.RESISTANCE);
		PotionBrewing.addMix(ApotheosisObjects.RESISTANCE, Items.REDSTONE, ApotheosisObjects.LONG_RESISTANCE);
		PotionBrewing.addMix(ApotheosisObjects.RESISTANCE, Items.GLOWSTONE_DUST, ApotheosisObjects.STRONG_RESISTANCE);

		PotionBrewing.addMix(ApotheosisObjects.RESISTANCE, Items.FERMENTED_SPIDER_EYE, ApotheosisObjects.T_SUNDERING);
		PotionBrewing.addMix(ApotheosisObjects.LONG_RESISTANCE, Items.FERMENTED_SPIDER_EYE, ApotheosisObjects.LONG_SUNDERING);
		PotionBrewing.addMix(ApotheosisObjects.STRONG_RESISTANCE, Items.FERMENTED_SPIDER_EYE, ApotheosisObjects.STRONG_SUNDERING);
		PotionBrewing.addMix(ApotheosisObjects.T_SUNDERING, Items.REDSTONE, ApotheosisObjects.LONG_SUNDERING);
		PotionBrewing.addMix(ApotheosisObjects.T_SUNDERING, Items.GLOWSTONE_DUST, ApotheosisObjects.STRONG_SUNDERING);

		PotionBrewing.addMix(Potions.AWKWARD, Items.GOLDEN_APPLE, ApotheosisObjects.ABSORPTION);
		PotionBrewing.addMix(ApotheosisObjects.ABSORPTION, Items.REDSTONE, ApotheosisObjects.LONG_ABSORPTION);
		PotionBrewing.addMix(ApotheosisObjects.ABSORPTION, Items.GLOWSTONE_DUST, ApotheosisObjects.STRONG_ABSORPTION);

		PotionBrewing.addMix(Potions.AWKWARD, Items.MUSHROOM_STEW, ApotheosisObjects.HASTE);
		PotionBrewing.addMix(ApotheosisObjects.HASTE, Items.REDSTONE, ApotheosisObjects.LONG_HASTE);
		PotionBrewing.addMix(ApotheosisObjects.HASTE, Items.GLOWSTONE_DUST, ApotheosisObjects.STRONG_HASTE);

		PotionBrewing.addMix(ApotheosisObjects.HASTE, Items.FERMENTED_SPIDER_EYE, ApotheosisObjects.FATIGUE);
		PotionBrewing.addMix(ApotheosisObjects.LONG_HASTE, Items.FERMENTED_SPIDER_EYE, ApotheosisObjects.LONG_FATIGUE);
		PotionBrewing.addMix(ApotheosisObjects.STRONG_HASTE, Items.FERMENTED_SPIDER_EYE, ApotheosisObjects.STRONG_FATIGUE);
		PotionBrewing.addMix(ApotheosisObjects.FATIGUE, Items.REDSTONE, ApotheosisObjects.LONG_FATIGUE);
		PotionBrewing.addMix(ApotheosisObjects.FATIGUE, Items.GLOWSTONE_DUST, ApotheosisObjects.STRONG_FATIGUE);

		if (ApotheosisObjects.SKULL_FRAGMENT != null) PotionBrewing.addMix(Potions.AWKWARD, ApotheosisObjects.SKULL_FRAGMENT, ApotheosisObjects.WITHER);
		else PotionBrewing.addMix(Potions.AWKWARD, Items.WITHER_SKELETON_SKULL, ApotheosisObjects.WITHER);
		PotionBrewing.addMix(ApotheosisObjects.WITHER, Items.REDSTONE, ApotheosisObjects.LONG_WITHER);
		PotionBrewing.addMix(ApotheosisObjects.WITHER, Items.GLOWSTONE_DUST, ApotheosisObjects.STRONG_WITHER);

		PotionBrewing.addMix(Potions.AWKWARD, Items.EXPERIENCE_BOTTLE, ApotheosisObjects.T_KNOWLEDGE);
		PotionBrewing.addMix(ApotheosisObjects.T_KNOWLEDGE, Items.REDSTONE, ApotheosisObjects.LONG_KNOWLEDGE);
		PotionBrewing.addMix(ApotheosisObjects.T_KNOWLEDGE, Items.EXPERIENCE_BOTTLE, ApotheosisObjects.STRONG_KNOWLEDGE);

		PotionBrewing.addMix(Potions.AWKWARD, ApotheosisObjects.LUCKY_FOOT, Potions.LUCK);

		Ingredient fireRes = Apotheosis.potionIngredient(Potions.FIRE_RESISTANCE);
		Ingredient abs = Apotheosis.potionIngredient(ApotheosisObjects.STRONG_ABSORPTION);
		Ingredient res = Apotheosis.potionIngredient(ApotheosisObjects.RESISTANCE);
		Ingredient regen = Apotheosis.potionIngredient(Potions.STRONG_REGENERATION);
		Apotheosis.HELPER.addShaped(Items.ENCHANTED_GOLDEN_APPLE, 3, 3, fireRes, regen, fireRes, abs, Items.GOLDEN_APPLE, abs, res, abs, res);
		MinecraftForge.EVENT_BUS.addListener(this::drops);
		MinecraftForge.EVENT_BUS.addListener(this::xp);
		MinecraftForge.EVENT_BUS.addListener(this::reload);
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		e.getRegistry().register(new TrueInfinityEnchant().setRegistryName(Apotheosis.MODID, "true_infinity"));
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		e.getRegistry().registerAll(new LuckyFootItem().setRegistryName(Apotheosis.MODID, "lucky_foot"), new PotionCharmItem().setRegistryName(Apotheosis.MODID, "potion_charm"));
	}

	@SubscribeEvent
	public void types(Register<Potion> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new Potion("resistance", new EffectInstance(Effects.DAMAGE_RESISTANCE, 3600)).setRegistryName(Apotheosis.MODID, "resistance"),
				new Potion("resistance", new EffectInstance(Effects.DAMAGE_RESISTANCE, 9600)).setRegistryName(Apotheosis.MODID, "long_resistance"),
				new Potion("resistance", new EffectInstance(Effects.DAMAGE_RESISTANCE, 1800, 1)).setRegistryName(Apotheosis.MODID, "strong_resistance"),
				new Potion("absorption", new EffectInstance(Effects.ABSORPTION, 1200, 1)).setRegistryName(Apotheosis.MODID, "absorption"),
				new Potion("absorption", new EffectInstance(Effects.ABSORPTION, 3600, 1)).setRegistryName(Apotheosis.MODID, "long_absorption"),
				new Potion("absorption", new EffectInstance(Effects.ABSORPTION, 600, 3)).setRegistryName(Apotheosis.MODID, "strong_absorption"),
				new Potion("haste", new EffectInstance(Effects.DIG_SPEED, 3600)).setRegistryName(Apotheosis.MODID, "haste"),
				new Potion("haste", new EffectInstance(Effects.DIG_SPEED, 9600)).setRegistryName(Apotheosis.MODID, "long_haste"),
				new Potion("haste", new EffectInstance(Effects.DIG_SPEED, 1800, 1)).setRegistryName(Apotheosis.MODID, "strong_haste"),
				new Potion("fatigue", new EffectInstance(Effects.DIG_SLOWDOWN, 3600)).setRegistryName(Apotheosis.MODID, "fatigue"),
				new Potion("fatigue", new EffectInstance(Effects.DIG_SLOWDOWN, 9600)).setRegistryName(Apotheosis.MODID, "long_fatigue"),
				new Potion("fatigue", new EffectInstance(Effects.DIG_SLOWDOWN, 1800, 1)).setRegistryName(Apotheosis.MODID, "strong_fatigue"),
				new Potion("wither", new EffectInstance(Effects.WITHER, 3600)).setRegistryName(Apotheosis.MODID, "wither"),
				new Potion("wither", new EffectInstance(Effects.WITHER, 9600)).setRegistryName(Apotheosis.MODID, "long_wither"),
				new Potion("wither", new EffectInstance(Effects.WITHER, 1800, 1)).setRegistryName(Apotheosis.MODID, "strong_wither"),
				new Potion("sundering", new EffectInstance(ApotheosisObjects.SUNDERING, 3600)).setRegistryName(Apotheosis.MODID, "sundering"),
				new Potion("sundering", new EffectInstance(ApotheosisObjects.SUNDERING, 9600)).setRegistryName(Apotheosis.MODID, "long_sundering"),
				new Potion("sundering", new EffectInstance(ApotheosisObjects.SUNDERING, 1800, 1)).setRegistryName(Apotheosis.MODID, "strong_sundering"),
				new Potion("knowledge", new EffectInstance(ApotheosisObjects.P_KNOWLEDGE, 2400)).setRegistryName(Apotheosis.MODID, "knowledge"),
				new Potion("knowledge", new EffectInstance(ApotheosisObjects.P_KNOWLEDGE, 4800)).setRegistryName(Apotheosis.MODID, "long_knowledge"),
				new Potion("knowledge", new EffectInstance(ApotheosisObjects.P_KNOWLEDGE, 1200, 1)).setRegistryName(Apotheosis.MODID, "strong_knowledge"));
		//Formatter::on
	}

	@SubscribeEvent
	public void potions(Register<Effect> e) {
		e.getRegistry().register(new PotionSundering().setRegistryName(Apotheosis.MODID, "sundering"));
		e.getRegistry().register(new KnowledgeEffect().setRegistryName(Apotheosis.MODID, "knowledge"));
		ObjectHolderRegistry.applyObjectHolders(r -> r.getNamespace().equals(Apotheosis.MODID) && (r.getPath().equals("sundering") || r.getPath().equals("knowledge")));
	}

	@SubscribeEvent
	public void serializers(Register<IRecipeSerializer<?>> e) {
		e.getRegistry().register(PotionCharmRecipe.Serializer.INSTANCE.setRegistryName(ApotheosisObjects.POTION_CHARM.getRegistryName()));
	}

	@SubscribeEvent
	public void imcEvent(InterModEnqueueEvent e) {
		if (ModList.get().isLoaded("curios")) CuriosCompat.sendIMC();
	}

	public void drops(LivingDropsEvent e) {
		if (e.getEntityLiving() instanceof RabbitEntity) {
			RabbitEntity rabbit = (RabbitEntity) e.getEntityLiving();
			if (rabbit.level.random.nextFloat() < 0.03F + 0.03F * e.getLootingLevel()) {
				e.getDrops().clear();
				e.getDrops().add(new ItemEntity(rabbit.level, rabbit.getX(), rabbit.getY(), rabbit.getZ(), new ItemStack(ApotheosisObjects.LUCKY_FOOT)));
			}
		}
	}

	public void xp(LivingExperienceDropEvent e) {
		if (e.getAttackingPlayer() != null && e.getAttackingPlayer().getEffect(ApotheosisObjects.P_KNOWLEDGE) != null) {
			int level = e.getAttackingPlayer().getEffect(ApotheosisObjects.P_KNOWLEDGE).getAmplifier() + 1;
			int curXp = e.getDroppedExperience();
			int newXp = curXp + e.getOriginalExperience() * level * knowledgeMult;
			e.setDroppedExperience(newXp);
		}
	}

	public void reload(ApotheosisReloadEvent e) {
		Configuration config = new Configuration(new File(Apotheosis.configDir, "potion.cfg"));
		knowledgeMult = config.getInt("Knowledge XP Multiplier", "general", knowledgeMult, 1, Integer.MAX_VALUE, "The strength of Ancient Knowledge.  This multiplier determines how much additional xp is granted.");
		if (e == null && config.hasChanged()) config.save();
	}

}