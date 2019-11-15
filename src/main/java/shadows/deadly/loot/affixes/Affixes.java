package shadows.deadly.loot.affixes;

import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import shadows.Apotheosis;
import shadows.deadly.loot.LootEntry;
import shadows.deadly.loot.LootManager;
import shadows.deadly.loot.attributes.CustomAttributes;

@EventBusSubscriber(modid = Apotheosis.MODID)
@ObjectHolder(Apotheosis.MODID)
public class Affixes {

	public static final Affix FIRE_DAMAGE = null;
	public static final Affix LUNAR_DAMAGE = null;
	public static final Affix SOLAR_DAMAGE = null;
	public static final Affix COLD_DAMAGE = null;
	public static final Affix QUARTZ_FUSED = null;

	@SubscribeEvent
	public static void register(Register<Affix> e) {
		//Formatter::off
		e.getRegistry().registerAll(
			new AttributeAffix(CustomAttributes.FIRE_DAMAGE, 0.5F, 4.0F, 0, 3).setRegistryName("fire_damage"),
			new AttributeAffix(CustomAttributes.LUNAR_DAMAGE, 0.5F, 6.0F, 0, 1).setRegistryName("lunar_damage"),
			new AttributeAffix(CustomAttributes.SOLAR_DAMAGE, 0.5F, 6.0F, 0, 1).setRegistryName("solar_damage"),
			new AttributeAffix(CustomAttributes.COLD_DAMAGE, 0.5F, 4.0F, 0, 3).setRegistryName("cold_damage"),
			new EnchantmentAffix(Enchantments.SHARPNESS, 15, 2).setRegistryName("quartz_fused")
		);
		//Formatter::on
	}

	public static void init() {
		LootManager.registerAffix(LootEntry.Type.WEAPON, FIRE_DAMAGE, true);
		LootManager.registerAffix(LootEntry.Type.WEAPON, LUNAR_DAMAGE, false);
		LootManager.registerAffix(LootEntry.Type.WEAPON, SOLAR_DAMAGE, false);
		LootManager.registerAffix(LootEntry.Type.WEAPON, COLD_DAMAGE, true);
		LootManager.registerTransEnch(LootEntry.Type.WEAPON, QUARTZ_FUSED);
		LootManager.registerEntry(new LootEntry(new ItemStack(Items.DIAMOND_SWORD), LootEntry.Type.WEAPON, 3));
	}

}
