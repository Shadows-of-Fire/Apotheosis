package shadows.apotheosis.deadly.affix;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.affix.impl.generic.EnchantabilityAffix;
import shadows.apotheosis.deadly.affix.impl.heavy.CleaveAffix;
import shadows.apotheosis.deadly.affix.impl.heavy.ExecuteAffix;
import shadows.apotheosis.deadly.affix.impl.heavy.PiercingAffix;
import shadows.apotheosis.deadly.affix.impl.melee.DamageChainAffix;
import shadows.apotheosis.deadly.affix.impl.melee.LootPinataAffix;
import shadows.apotheosis.deadly.affix.impl.ranged.*;
import shadows.apotheosis.deadly.affix.impl.shield.DisengageAffix;
import shadows.apotheosis.deadly.affix.impl.shield.EldritchBlockAffix;
import shadows.apotheosis.deadly.affix.impl.shield.SpikedAffix;
import shadows.apotheosis.deadly.affix.impl.tool.OmniToolAffix;
import shadows.apotheosis.deadly.affix.impl.tool.RadiusMiningAffix;
import shadows.apotheosis.deadly.affix.impl.tool.TorchPlacementAffix;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.placebo.config.Configuration;

import java.io.File;

public class Affixes {

    public static void register(Register<Affix> e) {
        IForgeRegistry<Affix> reg = e.getRegistry();
        Affix.config = new Configuration(new File(Apotheosis.configDir, "affixes.cfg"));
        reg.registerAll(
                //Formatter::off

                //generic
                new EnchantabilityAffix(LootRarity.COMMON, 5, 30, 3).setRegistryName("enchantability"),

                //melee
                new AttributeAffix.Builder(LootRarity.COMMON).with(Apoth.Attributes.CRIT_DAMAGE, AttributeModifier.Operation.MULTIPLY_TOTAL,0.1F, 1.5F).types(t-> t == LootCategory.SWORD || t == LootCategory.HEAVY_WEAPON).setPrefix(true).weighted(2).build("crit_damage"),
                new AttributeAffix.Builder(LootRarity.COMMON).with(Apoth.Attributes.LIFE_STEAL, AttributeModifier.Operation.MULTIPLY_TOTAL, 0.05F, 0.75F).types(t-> t== LootCategory.SWORD || t==LootCategory.HEAVY_WEAPON).setPrefix(true).weighted(3).build("life_steal"),

                //common?
//                new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.MAX_HEALTH, AttributeModifier.Operation.ADDITION, (level -> 0.5F + Math.round(level * 3) / 2F)).types(LootCategory::isDefensive).build("common_max_hp"),
//                new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ARMOR, AttributeModifier.Operation.ADDITION, 0.5F, 2).types(LootCategory::isDefensive).build("common_armor"),
//                new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADDITION, 0.5F, 2).build("common_dmg"),
//                new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL, 0.05F, 0.15F).build("common_mvspd"),
//                new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ATTACK_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL, 0.1F, 0.25F).build("common_aspd"),
//                new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ATTACK_KNOCKBACK, AttributeModifier.Operation.ADDITION, 0.25F, 0.5F).build("common_kb"),
//                new AttributeAffix.Builder(LootRarity.COMMON).with(ForgeMod.REACH_DISTANCE, AttributeModifier.Operation.ADDITION, (level -> 0.5F + Math.round(level * 3) / 2F)).build("common_reach"),
                new AttributeAffix.Builder(LootRarity.COMMON).with(ForgeMod.REACH_DISTANCE, AttributeModifier.Operation.ADDITION, (level -> 0.5F + Math.round(level * 3) / 2F)).types(t -> t == LootCategory.BREAKER || t == LootCategory.SWORD || t == LootCategory.HEAVY_WEAPON).build("reach_distance"),

                //bow
                new DrawSpeedAffix(LootRarity.COMMON, 5).setRegistryName("draw_speed"),
                new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL, 0.25F, 0.77F).types(LootCategory::isRanged).setPrefix(true).weighted(5).build("movement_speed"),
                new SnipeDamageAffix(LootRarity.COMMON, 2, 10, 3).setRegistryName("snipe_damage"),
                new SpectralShotAffix(LootRarity.COMMON, 0.1F, 1F, 2).setRegistryName("spectral_shot"),
                new SnareHitAffix(LootRarity.COMMON, 1, 10, 1).setRegistryName("snare_hit"),
                new MagicArrowAffix(LootRarity.COMMON, 1).setRegistryName("magic_arrow"),
                new TeleportDropsAffix(LootRarity.COMMON, 1, 7, 2).setRegistryName("teleport_drops"),

                //sword
                new LootPinataAffix(LootRarity.COMMON, 0.001F, 0.02F, 2).setRegistryName("loot_pinata"),
                new DamageChainAffix(LootRarity.COMMON, 1, 10, 1).setRegistryName("damage_chain"),
                new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ATTACK_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL, 0.1F, 0.25F).types(t-> t == LootCategory.SWORD).setPrefix(true).weighted(5).build("attack_speed"),
                new AttributeAffix.Builder(LootRarity.COMMON).with(Apoth.Attributes.COLD_DAMAGE, AttributeModifier.Operation.ADDITION, lvl -> 1F + Math.round(lvl * 9F)).types(t-> t == LootCategory.SWORD).setPrefix(true).weighted(5).build("cold_damage"),
                //pc3k: no ignite on fire dmg - cant think of smart way to override affix methods to implement logic, + does this even add elemental dmg to calculations as it is now?
                new AttributeAffix.Builder(LootRarity.COMMON).with(Apoth.Attributes.FIRE_DAMAGE, AttributeModifier.Operation.ADDITION, lvl -> 1F + Math.round(lvl * 9F)).types(t-> t == LootCategory.SWORD).setPrefix(true).weighted(5).build("fire_damage"),
                new AttributeAffix.Builder(LootRarity.COMMON).with(Apoth.Attributes.CRIT_CHANCE, AttributeModifier.Operation.MULTIPLY_TOTAL,0.2F, 1F).types(t-> t == LootCategory.SWORD).weighted(2).setPrefix(true).build("crit_chance"),

                //axe / heavy?
                new PiercingAffix(LootRarity.COMMON, 1).setRegistryName("piercing"),
                new CleaveAffix(LootRarity.COMMON,  0.3F, 0.9999F, 3).setRegistryName("cleave"),
                new ExecuteAffix(LootRarity.COMMON,  0.03F, 0.2F, 5).setRegistryName("execute"),
                new AttributeAffix.Builder(LootRarity.COMMON).with(Apoth.Attributes.CRIT_CHANCE, AttributeModifier.Operation.MULTIPLY_TOTAL, 1F, 1F).weighted(1).types(t-> t == LootCategory.HEAVY_WEAPON).build("max_crit"),
                //pc3k: cant think of smart way to override onEntityDamaged to implement logic, disabled for the time being
//                new AttributeAffix.Builder(LootRarity.COMMON).with(Apoth.Attributes.CURRENT_HP_DAMAGE, AttributeModifier.Operation.MULTIPLY_TOTAL, 0.03F, 0.2F).weighted(2).types(t-> t == LootCategory.HEAVY_WEAPON).build("current_hp_damage"),
                new AttributeAffix.Builder(LootRarity.COMMON).with(Apoth.Attributes.OVERHEAL, AttributeModifier.Operation.MULTIPLY_TOTAL, 0.05F, 0.5F).weighted(4).types(t-> t == LootCategory.HEAVY_WEAPON).build("overheal"),

                //tools
                new TorchPlacementAffix(LootRarity.COMMON, 0, 5, 4).setRegistryName("torch_placement"),
                new OmniToolAffix(LootRarity.COMMON, 2).setRegistryName("omnitool"),
                new RadiusMiningAffix(LootRarity.COMMON, 1, 3, 2).setRegistryName("radius_mining"),

                //armor
                new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.MAX_HEALTH, AttributeModifier.Operation.ADDITION, (level -> 0.5F + Math.round(level * 3) / 2F)).types(LootCategory::isDefensive).weighted(5).build("max_health"),
                new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ARMOR, AttributeModifier.Operation.ADDITION, (l -> 0.5F + Math.round(l * 5) / 2F)).types(LootCategory::isDefensive).setPrefix(true).weighted(5).build("armor"),
                new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.ARMOR_TOUGHNESS, AttributeModifier.Operation.ADDITION, (l -> 0.5F + Math.round(l * 5) / 2F)).types(LootCategory::isDefensive).setPrefix(true).weighted(5).build("armor_toughness"),

                //shield
                new AttributeAffix.Builder(LootRarity.COMMON).with(Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL, 0.1F, 0.65F).types(t-> t == LootCategory.SHIELD).setPrefix(true).weighted(5).build("shield_speed"),
                new DisengageAffix(LootRarity.COMMON, 3).setRegistryName("disengage"),
                new SpikedAffix(LootRarity.COMMON, 0.4F, 1.5F, 2).setRegistryName("spiked_shield"),
                new EldritchBlockAffix(LootRarity.COMMON, 1).setRegistryName("eldritch_block")

                //pc3k: hated that one personally, not willing to implement xD
//                register(reg, ArrowCatcherAffix::new, "arrow_catcher", 1);

//              register(reg, ShieldDamageAffix::new, "shield_damage", 3);
                //Formatter::on
        );

        if (Affix.config.hasChanged()) Affix.config.save();

    }

    //pc3k: to be re-added later, this affix building stuff is a mess right now :<

//    static void register(IForgeRegistry<Affix> reg, Int2ObjectFunction<Affix> factory, String name, int weight) {
//        weight = Affix.config.getInt("Weight", name, weight, 0, Integer.MAX_VALUE, "The weight of this affix, relative to others that may apply to the same item.");
//        reg.register(factory.apply(weight).setRegistryName(name));
//    }

}