package dev.shadowsoffire.apotheosis.data;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.Apoth.BuiltInRegs;
import dev.shadowsoffire.apotheosis.Apoth.LootCategories;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.effect.DamageReductionAffix.DamageType;
import dev.shadowsoffire.apotheosis.affix.effect.MobEffectAffix.Target;
import dev.shadowsoffire.apotheosis.compat.twilight.FortificationBonus;
import dev.shadowsoffire.apotheosis.compat.twilight.OreMagnetBonus;
import dev.shadowsoffire.apotheosis.compat.twilight.TreasureGoblinBonus;
import dev.shadowsoffire.apotheosis.loot.conditions.MatchesBlockCondition;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.AttributeBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.DamageReductionBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.DurabilityBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.EnchantmentBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.EnchantmentBonus.Mode;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.MobEffectBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.MultiAttrBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.AllStatsBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.BloodyArrowBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.DropTransformBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.FrozenDropsBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.LeechBlockBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.MageSlayerBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.OmneticBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.RadialBonus;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;

public class GemProvider extends DynamicRegistryProvider<Gem> {

    public static final int DEFAULT_WEIGHT = 100;
    public static final float DEFAULT_QUALITY = 0.1F;

    public static final GemClass ARMOR = new GemClass("armor", LootCategories.HELMET, LootCategories.CHESTPLATE, LootCategories.LEGGINGS, LootCategories.BOOTS);
    public static final GemClass LIGHT_WEAPON = new GemClass("light_weapon", LootCategories.MELEE_WEAPON, LootCategories.TRIDENT);
    public static final GemClass CORE_ARMOR = new GemClass("core_armor", LootCategories.CHESTPLATE, LootCategories.LEGGINGS);
    public static final GemClass RANGED_WEAPON = new GemClass("ranged_weapon", LootCategories.BOW, LootCategories.TRIDENT);
    public static final GemClass LOWER_ARMOR = new GemClass("lower_armor", LootCategories.LEGGINGS, LootCategories.BOOTS);
    public static final GemClass WEAPONS = new GemClass("weapons", LootCategories.MELEE_WEAPON, LootCategories.TRIDENT, LootCategories.BOW);
    public static final GemClass WEAPON_OR_TOOL = new GemClass("weapon_or_tool", LootCategories.MELEE_WEAPON, LootCategories.TRIDENT, LootCategories.BOW, LootCategories.BREAKER);
    public static final GemClass NON_TRIDENT_WEAPONS = new GemClass("weapons", LootCategories.MELEE_WEAPON, LootCategories.BOW);
    public static final GemClass TOOLS = new GemClass("tools", LootCategories.BREAKER, LootCategories.SHEARS);
    public static final GemClass ANYTHING = new GemClass("anything", new AnyHolderSet<>(BuiltInRegs.LOOT_CATEGORY.asLookup()));

    public static final Holder<MobEffect> TW_FROSTED = DeferredHolder.create(Registries.MOB_EFFECT, ResourceLocation.parse("twilightforest:frosted"));

    public GemProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, GemRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Gems";
    }

    @Override
    public void generate() {
        HolderLookup.Provider registries = this.lookupProvider.join();
        RegistryLookup<Enchantment> enchants = registries.lookup(Registries.ENCHANTMENT).get();

        addGem("core/ballast", c -> c
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(Attributes.ATTACK_DAMAGE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 1)
                .value(Purity.CHIPPED, 2)
                .value(Purity.FLAWED, 3.5F)
                .value(Purity.NORMAL, 5)
                .value(Purity.FLAWLESS, 7)
                .value(Purity.PERFECT, 10))
            .bonus(TOOLS, DurabilityBonus.builder()
                .value(Purity.CRACKED, 0.10F)
                .value(Purity.CHIPPED, 0.15F)
                .value(Purity.FLAWED, 0.25F)
                .value(Purity.NORMAL, 0.35F)
                .value(Purity.FLAWLESS, 0.45F)
                .value(Purity.PERFECT, 0.60F))
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.KNOCKBACK_RESISTANCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.1)
                .value(Purity.CHIPPED, 0.2)
                .value(Purity.FLAWED, 0.3)
                .value(Purity.NORMAL, 0.4)
                .value(Purity.FLAWLESS, 0.5)
                .value(Purity.PERFECT, 0.7)));

        addGem("core/brawlers", c -> c
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(Attributes.ATTACK_SPEED)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.20)
                .value(Purity.NORMAL, 0.30)
                .value(Purity.FLAWLESS, 0.35)
                .value(Purity.PERFECT, 0.50))
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.MAX_HEALTH)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 1)
                .value(Purity.CHIPPED, 2)
                .value(Purity.FLAWED, 4)
                .value(Purity.NORMAL, 6)
                .value(Purity.FLAWLESS, 8)
                .value(Purity.PERFECT, 12))
            .bonus(LootCategories.SHIELD, AttributeBonus.builder()
                .attr(Attributes.MAX_HEALTH)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.10)
                .value(Purity.FLAWED, 0.15)
                .value(Purity.NORMAL, 0.20)
                .value(Purity.FLAWLESS, 0.25)
                .value(Purity.PERFECT, 0.30)));

        addGem("core/breach", c -> c
            .unique()
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(ALObjects.Attributes.ARMOR_PIERCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 2)
                .value(Purity.CHIPPED, 3)
                .value(Purity.FLAWED, 5)
                .value(Purity.NORMAL, 7)
                .value(Purity.FLAWLESS, 9)
                .value(Purity.PERFECT, 12))
            .bonus(TOOLS, AttributeBonus.builder()
                .attr(Attributes.BLOCK_INTERACTION_RANGE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.5)
                .value(Purity.CHIPPED, 1)
                .value(Purity.FLAWED, 1.5)
                .value(Purity.NORMAL, 2)
                .value(Purity.FLAWLESS, 2.5)
                .value(Purity.PERFECT, 3))
            .bonus(LootCategories.BOW, AttributeBonus.builder()
                .attr(ALObjects.Attributes.PROT_PIERCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 4)
                .value(Purity.CHIPPED, 5)
                .value(Purity.FLAWED, 7)
                .value(Purity.NORMAL, 8)
                .value(Purity.FLAWLESS, 10)
                .value(Purity.PERFECT, 15)));

        addGem("core/combatant", c -> c
            .unique()
            .bonus(RANGED_WEAPON, AttributeBonus.builder()
                .attr(ALObjects.Attributes.ARROW_DAMAGE)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.20)
                .value(Purity.NORMAL, 0.30)
                .value(Purity.FLAWLESS, 0.40)
                .value(Purity.PERFECT, 0.55))
            .bonus(CORE_ARMOR, DamageReductionBonus.builder()
                .damageType(DamageType.PHYSICAL)
                .value(Purity.CRACKED, 0.05F)
                .value(Purity.CHIPPED, 0.075F)
                .value(Purity.FLAWED, 0.125F)
                .value(Purity.NORMAL, 0.175F)
                .value(Purity.FLAWLESS, 0.225F)
                .value(Purity.PERFECT, 0.275F))
            .bonus(LootCategories.MELEE_WEAPON, DurabilityBonus.builder()
                .value(Purity.CRACKED, 0.05F)
                .value(Purity.CHIPPED, 0.10F)
                .value(Purity.FLAWED, 0.15F)
                .value(Purity.NORMAL, 0.225F)
                .value(Purity.FLAWLESS, 0.30F)
                .value(Purity.PERFECT, 0.40F)));

        addGem("core/guardian", c -> c
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.ARMOR)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.5)
                .value(Purity.CHIPPED, 1)
                .value(Purity.FLAWED, 2.5)
                .value(Purity.NORMAL, 4)
                .value(Purity.FLAWLESS, 6)
                .value(Purity.PERFECT, 8))
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(ALObjects.Attributes.LIFE_STEAL)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.10)
                .value(Purity.FLAWED, 0.15)
                .value(Purity.NORMAL, 0.20)
                .value(Purity.FLAWLESS, 0.25)
                .value(Purity.PERFECT, 0.30))
            .bonus(LootCategories.SHIELD, AttributeBonus.builder()
                .attr(Attributes.ARMOR)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.10)
                .value(Purity.FLAWED, 0.20)
                .value(Purity.NORMAL, 0.25)
                .value(Purity.FLAWLESS, 0.30)
                .value(Purity.PERFECT, 0.45)));

        addGem("core/lightning", c -> c
            .bonus(LootCategories.BOW, AttributeBonus.builder()
                .attr(ALObjects.Attributes.ARROW_VELOCITY)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.25)
                .value(Purity.NORMAL, 0.35)
                .value(Purity.FLAWLESS, 0.425)
                .value(Purity.PERFECT, 0.55))
            .bonus(TOOLS, AttributeBonus.builder()
                .attr(ALObjects.Attributes.MINING_SPEED)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.25)
                .value(Purity.NORMAL, 0.35)
                .value(Purity.FLAWLESS, 0.425)
                .value(Purity.PERFECT, 0.50))
            .bonus(LOWER_ARMOR, AttributeBonus.builder()
                .attr(Attributes.MOVEMENT_SPEED)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.225)
                .value(Purity.FLAWED, 0.375)
                .value(Purity.NORMAL, 0.45)
                .value(Purity.FLAWLESS, 0.55)
                .value(Purity.PERFECT, 0.70)));

        addGem("core/lunar", c -> c
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(ALObjects.Attributes.COLD_DAMAGE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 1)
                .value(Purity.CHIPPED, 1.5)
                .value(Purity.FLAWED, 2.5)
                .value(Purity.NORMAL, 4)
                .value(Purity.FLAWLESS, 6)
                .value(Purity.PERFECT, 8))
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.GRAVITY)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, -0.10)
                .value(Purity.CHIPPED, -0.25)
                .value(Purity.FLAWED, -0.45)
                .value(Purity.NORMAL, -0.65)
                .value(Purity.FLAWLESS, -0.85)
                .value(Purity.PERFECT, -1.04))
            .bonus(LootCategories.BOOTS, AttributeBonus.builder()
                .attr(NeoForgeMod.SWIM_SPEED)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.20)
                .value(Purity.FLAWED, 0.30)
                .value(Purity.NORMAL, 0.45)
                .value(Purity.FLAWLESS, 0.60)
                .value(Purity.PERFECT, 0.80)));

        addGem("core/samurai", c -> c
            .unique()
            .bonus(WEAPONS, AttributeBonus.builder()
                .attr(ALObjects.Attributes.CRIT_CHANCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.25)
                .value(Purity.NORMAL, 0.35)
                .value(Purity.FLAWLESS, 0.4)
                .value(Purity.PERFECT, 0.5))
            .bonus(LOWER_ARMOR, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and")
                .modifier(b -> b
                    .attr(Attributes.ARMOR_TOUGHNESS)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.CRACKED, 0.10F)
                    .value(Purity.CHIPPED, 0.15F)
                    .value(Purity.FLAWED, 0.175F)
                    .value(Purity.NORMAL, 0.225F)
                    .value(Purity.FLAWLESS, 0.275F)
                    .value(Purity.PERFECT, 0.35F))
                .modifier(b -> b
                    .attr(Attributes.MOVEMENT_SPEED)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.CRACKED, -0.025F)
                    .value(Purity.CHIPPED, -0.05F)
                    .value(Purity.FLAWED, -0.075F)
                    .value(Purity.NORMAL, -0.10F)
                    .value(Purity.FLAWLESS, -0.125F)
                    .value(Purity.PERFECT, -0.15F)))
            .bonus(LootCategories.HELMET, AttributeBonus.builder()
                .attr(ALObjects.Attributes.ARROW_VELOCITY)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.075)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.25)
                .value(Purity.NORMAL, 0.375)
                .value(Purity.FLAWLESS, 0.425)
                .value(Purity.PERFECT, 0.50))
            .bonus(LootCategories.SHIELD, AttributeBonus.builder()
                .attr(ALObjects.Attributes.DODGE_CHANCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.01)
                .value(Purity.CHIPPED, 0.02)
                .value(Purity.FLAWED, 0.04)
                .value(Purity.NORMAL, 0.06)
                .value(Purity.FLAWLESS, 0.08)
                .value(Purity.PERFECT, 0.10)));

        addGem("core/slipstream", c -> c
            .unique()
            .bonus(LootCategories.BOW, AttributeBonus.builder()
                .attr(ALObjects.Attributes.DRAW_SPEED)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.25)
                .value(Purity.FLAWED, 0.35)
                .value(Purity.NORMAL, 0.45)
                .value(Purity.FLAWLESS, 0.5)
                .value(Purity.PERFECT, 0.60))
            .bonus(TOOLS, AttributeBonus.builder()
                .attr(ALObjects.Attributes.MINING_SPEED)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.225)
                .value(Purity.NORMAL, 0.30)
                .value(Purity.FLAWLESS, 0.375)
                .value(Purity.PERFECT, 0.45))
            .bonus(LootCategories.BOOTS, AttributeBonus.builder()
                .attr(ALObjects.Attributes.DODGE_CHANCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.025)
                .value(Purity.CHIPPED, 0.05)
                .value(Purity.FLAWED, 0.075)
                .value(Purity.NORMAL, 0.10)
                .value(Purity.FLAWLESS, 0.125)
                .value(Purity.PERFECT, 0.15)));

        addGem("core/solar", c -> c
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(ALObjects.Attributes.FIRE_DAMAGE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 1)
                .value(Purity.CHIPPED, 1.5)
                .value(Purity.FLAWED, 2.5)
                .value(Purity.NORMAL, 4)
                .value(Purity.FLAWLESS, 6)
                .value(Purity.PERFECT, 8))
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.GRAVITY)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.25)
                .value(Purity.FLAWED, 0.45)
                .value(Purity.NORMAL, 0.65)
                .value(Purity.FLAWLESS, 0.85)
                .value(Purity.PERFECT, 1.04))
            .bonus(LootCategories.BOOTS, AttributeBonus.builder()
                .attr(Attributes.STEP_HEIGHT)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.25)
                .value(Purity.CHIPPED, 0.5)
                .value(Purity.FLAWED, 1)
                .value(Purity.NORMAL, 1.5)
                .value(Purity.FLAWLESS, 2)
                .value(Purity.PERFECT, 3)));

        addGem("core/splendor", c -> c
            .bonus(ARMOR, AttributeBonus.builder()
                .attr(Attributes.LUCK)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.5)
                .value(Purity.CHIPPED, 1.25)
                .value(Purity.FLAWED, 2)
                .value(Purity.NORMAL, 2.25)
                .value(Purity.FLAWLESS, 3.5)
                .value(Purity.PERFECT, 5))
            .bonus(WEAPON_OR_TOOL, AttributeBonus.builder()
                .attr(ALObjects.Attributes.EXPERIENCE_GAINED)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.075)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.225)
                .value(Purity.NORMAL, 0.3)
                .value(Purity.FLAWLESS, 0.40)
                .value(Purity.PERFECT, 0.60)));

        addGem("core/tyrannical", c -> c
            .unique()
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(Attributes.ATTACK_KNOCKBACK)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.5)
                .value(Purity.CHIPPED, 1)
                .value(Purity.FLAWED, 1.5)
                .value(Purity.NORMAL, 2)
                .value(Purity.FLAWLESS, 3)
                .value(Purity.PERFECT, 3.5))
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.ARMOR_TOUGHNESS)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.5)
                .value(Purity.CHIPPED, 1)
                .value(Purity.FLAWED, 2)
                .value(Purity.NORMAL, 3)
                .value(Purity.FLAWLESS, 4)
                .value(Purity.PERFECT, 6))
            .bonus(LootCategories.SHIELD, AttributeBonus.builder()
                .attr(Attributes.ARMOR_TOUGHNESS)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.225)
                .value(Purity.NORMAL, 0.30)
                .value(Purity.FLAWLESS, 0.375)
                .value(Purity.PERFECT, 0.5))
            .bonus(LootCategories.BOOTS, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and")
                .modifier(b -> b
                    .attr(Attributes.MAX_HEALTH)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.CRACKED, 0.05F)
                    .value(Purity.CHIPPED, 0.075F)
                    .value(Purity.FLAWED, 0.125F)
                    .value(Purity.NORMAL, 0.175F)
                    .value(Purity.FLAWLESS, 0.225F)
                    .value(Purity.PERFECT, 0.40F))
                .modifier(b -> b
                    .attr(ALObjects.Attributes.LIFE_STEAL)
                    .op(Operation.ADD_VALUE)
                    .value(Purity.CRACKED, -0.025F)
                    .value(Purity.CHIPPED, -0.05F)
                    .value(Purity.FLAWED, -0.075F)
                    .value(Purity.NORMAL, -0.10F)
                    .value(Purity.FLAWLESS, -0.125F)
                    .value(Purity.PERFECT, -0.20F)))
            .bonus(LootCategories.BOW, MobEffectBonus.builder()
                .effect(ALObjects.MobEffects.BLEEDING)
                .target(Target.ARROW_TARGET)
                .stacking()
                .limit(4)
                .value(Purity.FLAWLESS, 160, 0, 40)
                .value(Purity.PERFECT, 160, 1, 40)));

        addGem("core/warlord", c -> c
            .bonus(NON_TRIDENT_WEAPONS, AttributeBonus.builder()
                .attr(ALObjects.Attributes.CRIT_DAMAGE)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.20)
                .value(Purity.FLAWED, 0.35)
                .value(Purity.NORMAL, 0.45)
                .value(Purity.FLAWLESS, 0.55)
                .value(Purity.PERFECT, 0.70))
            .bonus(LootCategories.CHESTPLATE, AttributeBonus.builder()
                .attr(Attributes.MAX_HEALTH)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.10)
                .value(Purity.FLAWED, 0.15)
                .value(Purity.NORMAL, 0.20)
                .value(Purity.FLAWLESS, 0.25)
                .value(Purity.PERFECT, 0.35))
            .bonus(LootCategories.HELMET, AttributeBonus.builder()
                .attr(Attributes.ATTACK_DAMAGE)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.10)
                .value(Purity.FLAWED, 0.125)
                .value(Purity.NORMAL, 0.15)
                .value(Purity.FLAWLESS, 0.175)
                .value(Purity.PERFECT, 0.225))
            .bonus(LootCategories.TRIDENT, MobEffectBonus.builder()
                .effect(MobEffects.DAMAGE_BOOST)
                .target(Target.ARROW_SELF)
                .stacking()
                .limit(5)
                .value(Purity.FLAWLESS, 200, 0, 40)
                .value(Purity.PERFECT, 200, 1, 40)));

        addGem("overworld/earth", TieredWeights.forTiersAbove(WorldTier.FRONTIER, 50, 2F), c -> c
            .unique()
            .minPurity(Purity.FLAWED)
            .contstraints(Constraints.forDimension(Level.OVERWORLD))
            .bonus(LIGHT_WEAPON, EnchantmentBonus.builder()
                .enchantment(enchants.getOrThrow(Enchantments.SHARPNESS))
                .mode(Mode.EXISTING)
                .value(Purity.FLAWED, 1)
                .value(Purity.NORMAL, 2)
                .value(Purity.FLAWLESS, 3)
                .value(Purity.PERFECT, 4))
            .bonus(CORE_ARMOR, EnchantmentBonus.builder()
                .enchantment(enchants.getOrThrow(Enchantments.PROTECTION))
                .mode(Mode.EXISTING)
                .value(Purity.FLAWED, 1)
                .value(Purity.NORMAL, 2)
                .value(Purity.FLAWLESS, 3)
                .value(Purity.PERFECT, 4))
            .bonus(TOOLS, EnchantmentBonus.builder()
                .enchantment(enchants.getOrThrow(Enchantments.FORTUNE))
                .mode(Mode.EXISTING)
                .value(Purity.FLAWED, 1)
                .value(Purity.NORMAL, 2)
                .value(Purity.FLAWLESS, 3)
                .value(Purity.PERFECT, 4)));

        addGem("overworld/royalty", TieredWeights.forTiersAbove(WorldTier.FRONTIER, 50, 2F), c -> c
            .unique()
            .minPurity(Purity.FLAWED)
            .contstraints(Constraints.forDimension(Level.OVERWORLD))
            .bonus(LootCategories.HELMET, AllStatsBonus.builder()
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.FLAWED, 0.05F)
                .value(Purity.NORMAL, 0.075F)
                .value(Purity.FLAWLESS, 0.10F)
                .value(Purity.PERFECT, 0.15F)
                .attributes(
                    Attributes.MAX_HEALTH,
                    Attributes.KNOCKBACK_RESISTANCE,
                    Attributes.MOVEMENT_SPEED,
                    Attributes.ATTACK_DAMAGE,
                    Attributes.ATTACK_KNOCKBACK,
                    Attributes.ATTACK_SPEED,
                    Attributes.ARMOR,
                    Attributes.ARMOR_TOUGHNESS,
                    Attributes.LUCK,
                    Attributes.STEP_HEIGHT,
                    Attributes.BLOCK_INTERACTION_RANGE,
                    Attributes.ENTITY_INTERACTION_RANGE,
                    ALObjects.Attributes.ARMOR_PIERCE,
                    ALObjects.Attributes.ARMOR_SHRED,
                    ALObjects.Attributes.ARROW_DAMAGE,
                    ALObjects.Attributes.ARROW_VELOCITY,
                    ALObjects.Attributes.COLD_DAMAGE,
                    ALObjects.Attributes.CRIT_CHANCE,
                    ALObjects.Attributes.CRIT_DAMAGE,
                    ALObjects.Attributes.CURRENT_HP_DAMAGE,
                    ALObjects.Attributes.DODGE_CHANCE,
                    ALObjects.Attributes.EXPERIENCE_GAINED,
                    ALObjects.Attributes.FIRE_DAMAGE,
                    ALObjects.Attributes.GHOST_HEALTH,
                    ALObjects.Attributes.HEALING_RECEIVED,
                    ALObjects.Attributes.LIFE_STEAL,
                    ALObjects.Attributes.MINING_SPEED,
                    ALObjects.Attributes.OVERHEAL,
                    ALObjects.Attributes.PROT_PIERCE,
                    ALObjects.Attributes.PROT_SHRED,
                    NeoForgeMod.SWIM_SPEED))
            .bonus(LootCategories.BREAKER, DropTransformBonus.builder()
                .condition(new MatchesBlockCondition(BuiltInRegistries.BLOCK.getOrCreateTag(Tags.Blocks.ORES_COPPER)))
                .inputs(Ingredient.of(Tags.Items.RAW_MATERIALS_COPPER))
                .desc("gem.apotheosis:overworld/royalty.bonus.pickaxe")
                .output(new ItemStack(Items.RAW_GOLD))
                .value(Purity.FLAWED, 0.15F)
                .value(Purity.NORMAL, 0.20F)
                .value(Purity.FLAWLESS, 0.25F)
                .value(Purity.PERFECT, 0.40F))
            .bonus(LootCategories.BOW, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and")
                .modifier(b -> b
                    .attr(ALObjects.Attributes.PROT_SHRED)
                    .op(Operation.ADD_VALUE)
                    .value(Purity.FLAWED, 0.25F)
                    .value(Purity.NORMAL, 0.30F)
                    .value(Purity.FLAWLESS, 0.35F)
                    .value(Purity.PERFECT, 0.40F))
                .modifier(b -> b
                    .attr(ALObjects.Attributes.DRAW_SPEED)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.FLAWED, -0.35F)
                    .value(Purity.NORMAL, -0.45F)
                    .value(Purity.FLAWLESS, -0.55F)
                    .value(Purity.PERFECT, -0.65F)))
            .bonus(LootCategories.SHIELD, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and_but")
                .modifier(b -> b
                    .attr(Attributes.ARMOR)
                    .op(Operation.ADD_MULTIPLIED_BASE)
                    .value(Purity.FLAWED, 0.15F)
                    .value(Purity.NORMAL, 0.25F)
                    .value(Purity.FLAWLESS, 0.35F)
                    .value(Purity.PERFECT, 0.50F))
                .modifier(b -> b
                    .attr(Attributes.ARMOR_TOUGHNESS)
                    .op(Operation.ADD_MULTIPLIED_BASE)
                    .value(Purity.FLAWED, 0.075F)
                    .value(Purity.NORMAL, 0.125F)
                    .value(Purity.FLAWLESS, 0.225F)
                    .value(Purity.PERFECT, 0.30F))
                .modifier(b -> b
                    .attr(Attributes.MOVEMENT_SPEED)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.FLAWED, -0.25F)
                    .value(Purity.NORMAL, -0.30F)
                    .value(Purity.FLAWLESS, -0.35F)
                    .value(Purity.PERFECT, -0.40F))));

        addGem("overworld/verdant_ruin", TieredWeights.forTiersAbove(WorldTier.FRONTIER, 50, 2F), c -> c
            .unique()
            .minPurity(Purity.FLAWED)
            .contstraints(Constraints.forDimension(Level.OVERWORLD))
            .bonus(WEAPONS, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and")
                .modifier(b -> b
                    .attr(ALObjects.Attributes.ARMOR_SHRED)
                    .op(Operation.ADD_VALUE)
                    .value(Purity.FLAWED, 0.075F)
                    .value(Purity.NORMAL, 0.15F)
                    .value(Purity.FLAWLESS, 0.25F)
                    .value(Purity.PERFECT, 0.40F))
                .modifier(b -> b
                    .attr(Attributes.ARMOR)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.FLAWED, -0.025F)
                    .value(Purity.NORMAL, -0.05F)
                    .value(Purity.FLAWLESS, -0.075F)
                    .value(Purity.PERFECT, -0.10F)))
            .bonus(LootCategories.CHESTPLATE, DamageReductionBonus.builder()
                .damageType(DamageType.MAGIC)
                .value(Purity.FLAWED, 0.125F)
                .value(Purity.NORMAL, 0.15F)
                .value(Purity.FLAWLESS, 0.175F)
                .value(Purity.PERFECT, 0.20F))
            .bonus(LootCategories.BOOTS, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and")
                .modifier(b -> b
                    .attr(Attributes.KNOCKBACK_RESISTANCE)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.FLAWED, 0.05F)
                    .value(Purity.NORMAL, 0.10F)
                    .value(Purity.FLAWLESS, 0.15F)
                    .value(Purity.PERFECT, 0.20F))
                .modifier(b -> b
                    .attr(Attributes.ATTACK_KNOCKBACK)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.FLAWED, -0.075F)
                    .value(Purity.NORMAL, -0.10F)
                    .value(Purity.FLAWLESS, -0.125F)
                    .value(Purity.PERFECT, -0.15F)))
            .bonus(LootCategories.BREAKER, RadialBonus.builder()
                .value(Purity.FLAWED, 3, 2, 0, 1)
                .value(Purity.NORMAL, 3, 3, 0, 0)
                .value(Purity.FLAWLESS, 5, 3, 0, 0)
                .value(Purity.PERFECT, 5, 5, 0, 0)));

        addGem("the_nether/blood_lord", TieredWeights.forTiersAbove(WorldTier.ASCENT, 50, 2F), c -> c
            .unique()
            .minPurity(Purity.FLAWED)
            .contstraints(Constraints.forDimension(Level.NETHER))
            .bonus(LIGHT_WEAPON, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and")
                .modifier(b -> b
                    .attr(ALObjects.Attributes.LIFE_STEAL)
                    .op(Operation.ADD_VALUE)
                    .value(Purity.FLAWED, 0.10F)
                    .value(Purity.NORMAL, 0.125F)
                    .value(Purity.FLAWLESS, 0.15F)
                    .value(Purity.PERFECT, 0.15F))
                .modifier(b -> b
                    .attr(ALObjects.Attributes.LIFE_STEAL)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.FLAWED, 0.05F)
                    .value(Purity.NORMAL, 0.10F)
                    .value(Purity.FLAWLESS, 0.20F)
                    .value(Purity.PERFECT, 0.30F)))
            .bonus(LootCategories.HELMET, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and")
                .modifier(b -> b
                    .attr(Attributes.ATTACK_DAMAGE)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.FLAWED, 0.25F)
                    .value(Purity.NORMAL, 0.35F)
                    .value(Purity.FLAWLESS, 0.45F)
                    .value(Purity.PERFECT, 0.60F))
                .modifier(b -> b
                    .attr(Attributes.MAX_HEALTH)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.FLAWED, -0.30F)
                    .value(Purity.NORMAL, -0.40F)
                    .value(Purity.FLAWLESS, -0.50F)
                    .value(Purity.PERFECT, -0.65F)))
            .bonus(LootCategories.CHESTPLATE, AttributeBonus.builder()
                .attr(ALObjects.Attributes.HEALING_RECEIVED)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.FLAWED, 0.20)
                .value(Purity.NORMAL, 0.30)
                .value(Purity.FLAWLESS, 0.40)
                .value(Purity.PERFECT, 0.50))
            .bonus(new BloodyArrowBonus(Map.of(
                Purity.FLAWED, new BloodyArrowBonus.Data(0.20F, 1.60F, 450),
                Purity.NORMAL, new BloodyArrowBonus.Data(0.30F, 1.90F, 450),
                Purity.FLAWLESS, new BloodyArrowBonus.Data(0.40F, 2.20F, 450),
                Purity.PERFECT, new BloodyArrowBonus.Data(0.50F, 2.50F, 450))))
            .bonus(new LeechBlockBonus(Map.of(
                Purity.FLAWED, new LeechBlockBonus.Data(0.25F, 450),
                Purity.NORMAL, new LeechBlockBonus.Data(0.40F, 450),
                Purity.FLAWLESS, new LeechBlockBonus.Data(0.55F, 450),
                Purity.PERFECT, new LeechBlockBonus.Data(0.65F, 450)))));

        addGem("the_nether/inferno", TieredWeights.forTiersAbove(WorldTier.ASCENT, 50, 2F), c -> c
            .unique()
            .minPurity(Purity.FLAWED)
            .contstraints(Constraints.forDimension(Level.NETHER))
            .bonus(LIGHT_WEAPON, MobEffectBonus.builder()
                .effect(ALObjects.MobEffects.DETONATION)
                .target(Target.ATTACK_TARGET)
                .value(Purity.FLAWLESS, 80, 0, 1200)
                .value(Purity.PERFECT, 80, 1, 1200))
            .bonus(LootCategories.CHESTPLATE, EnchantmentBonus.builder()
                .enchantment(standaloneHolder(registries, Ench.Enchantments.BERSERKERS_FURY))
                .mode(Mode.SINGLE)
                .value(Purity.FLAWED, 1)
                .value(Purity.NORMAL, 1)
                .value(Purity.FLAWLESS, 2)
                .value(Purity.PERFECT, 2))
            .bonus(LootCategories.BREAKER, EnchantmentBonus.builder()
                .enchantment(enchants.getOrThrow(Enchantments.EFFICIENCY))
                .mode(Mode.SINGLE)
                .value(Purity.FLAWED, 1)
                .value(Purity.NORMAL, 2)
                .value(Purity.FLAWLESS, 3)
                .value(Purity.PERFECT, 4))
            .bonus(LootCategories.HELMET, AttributeBonus.builder()
                .attr(ALObjects.Attributes.FIRE_DAMAGE)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.FLAWED, 0.50)
                .value(Purity.NORMAL, 0.625)
                .value(Purity.FLAWLESS, 0.75)
                .value(Purity.PERFECT, 0.90)));

        addGem("the_nether/molten_breach", TieredWeights.forTiersAbove(WorldTier.ASCENT, 50, 2F), c -> c
            .unique()
            .minPurity(Purity.FLAWED)
            .contstraints(Constraints.forDimension(Level.NETHER))
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(ALObjects.Attributes.PROT_PIERCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.FLAWED, 5.5)
                .value(Purity.NORMAL, 8)
                .value(Purity.FLAWLESS, 9.5)
                .value(Purity.PERFECT, 12))
            .bonus(LootCategories.BOW, AttributeBonus.builder()
                .attr(ALObjects.Attributes.ARMOR_PIERCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.FLAWED, 4)
                .value(Purity.NORMAL, 5.5)
                .value(Purity.FLAWLESS, 8)
                .value(Purity.PERFECT, 10))
            .bonus(LootCategories.SHIELD, AttributeBonus.builder()
                .attr(Attributes.ENTITY_INTERACTION_RANGE)
                .op(Operation.ADD_VALUE)
                .value(Purity.FLAWED, 0.5)
                .value(Purity.NORMAL, 0.75)
                .value(Purity.FLAWLESS, 1.25)
                .value(Purity.PERFECT, 1.75))
            .bonus(LootCategories.CHESTPLATE, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and")
                .modifier(b -> b
                    .attr(Attributes.ATTACK_DAMAGE)
                    .op(Operation.ADD_MULTIPLIED_BASE)
                    .value(Purity.FLAWED, 0.05F)
                    .value(Purity.NORMAL, 0.10F)
                    .value(Purity.FLAWLESS, 0.125F)
                    .value(Purity.PERFECT, 0.175F))
                .modifier(b -> b
                    .attr(ALObjects.Attributes.HEALING_RECEIVED)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.FLAWED, -0.05F)
                    .value(Purity.NORMAL, -0.15F)
                    .value(Purity.FLAWLESS, -0.25F)
                    .value(Purity.PERFECT, -0.35F)))
            .bonus(LootCategories.LEGGINGS, AttributeBonus.builder()
                .attr(Attributes.SCALE)
                .op(Operation.ADD_VALUE)
                .value(Purity.FLAWED, 0.075)
                .value(Purity.NORMAL, 0.125)
                .value(Purity.FLAWLESS, 0.15)
                .value(Purity.PERFECT, 0.25))
            .bonus(TOOLS, OmneticBonus.builder()
                .value(Purity.FLAWED, "diamond", Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE, Items.DIAMOND_SWORD, Items.DIAMOND_HOE)
                .value(Purity.NORMAL, "diamond", Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE, Items.DIAMOND_SWORD, Items.DIAMOND_HOE)
                .value(Purity.FLAWLESS, "netherite", Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_PICKAXE, Items.NETHERITE_SWORD, Items.NETHERITE_HOE)
                .value(Purity.PERFECT, "netherite", Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_PICKAXE, Items.NETHERITE_SWORD, Items.NETHERITE_HOE)));

        addGem("the_end/endersurge", TieredWeights.forTiersAbove(WorldTier.SUMMIT, 50, 2F), c -> c
            .unique()
            .minPurity(Purity.FLAWLESS)
            .contstraints(Constraints.forDimension(Level.END))
            .bonus(ANYTHING, EnchantmentBonus.builder()
                .enchantment(enchants.getOrThrow(Enchantments.SHARPNESS))
                .mode(Mode.GLOBAL)
                .value(Purity.FLAWLESS, 1)
                .value(Purity.PERFECT, 2)));

        addGem("the_end/mageslayer", TieredWeights.forTiersAbove(WorldTier.SUMMIT, 50, 2F), c -> c
            .unique()
            .minPurity(Purity.NORMAL)
            .contstraints(Constraints.forDimension(Level.END))
            .bonus(LIGHT_WEAPON, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and")
                .modifier(b -> b
                    .attr(ALObjects.Attributes.PROT_SHRED)
                    .op(Operation.ADD_VALUE)
                    .value(Purity.NORMAL, 0.30F)
                    .value(Purity.FLAWLESS, 0.55F)
                    .value(Purity.PERFECT, 0.75F))
                .modifier(b -> b
                    .attr(ALObjects.Attributes.ARMOR_SHRED)
                    .op(Operation.ADD_MULTIPLIED_BASE)
                    .value(Purity.NORMAL, -0.20F)
                    .value(Purity.FLAWLESS, -0.35F)
                    .value(Purity.PERFECT, -0.55F)))
            .bonus(new MageSlayerBonus(
                new GemClass(LootCategories.HELMET), Map.of(
                    Purity.NORMAL, 0.15F,
                    Purity.FLAWLESS, 0.225F,
                    Purity.PERFECT, 0.35F)))
            .bonus(LootCategories.SHIELD, MobEffectBonus.builder()
                .effect(MobEffects.DAMAGE_RESISTANCE)
                .target(Target.BLOCK_SELF)
                .value(Purity.NORMAL, 200, 0, 400)
                .value(Purity.FLAWLESS, 300, 0, 400)
                .value(Purity.PERFECT, 300, 1, 400)));

        addConditionally("twilightforest", "twilight/queen", TieredWeights.forTiersAbove(WorldTier.SUMMIT, 50, 2F), c -> c
            .unique()
            .minPurity(Purity.FLAWED)
            .contstraints(Constraints.forDimension(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("twilightforest:twilight_forest"))))
            .bonus(LootCategories.CHESTPLATE, FortificationBonus.builder()
                .value(Purity.FLAWED, 0.05F, 6000)
                .value(Purity.NORMAL, 0.10F, 5400)
                .value(Purity.FLAWLESS, 0.125F, 5100)
                .value(Purity.PERFECT, 0.15F, 4800))
            .bonus(LootCategories.HELMET, AttributeBonus.builder()
                .attr(ALObjects.Attributes.COLD_DAMAGE)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.FLAWED, 0.50)
                .value(Purity.NORMAL, 0.625)
                .value(Purity.FLAWLESS, 0.75)
                .value(Purity.PERFECT, 0.90))
            .bonus(new FrozenDropsBonus(
                new GemClass(LootCategories.MELEE_WEAPON), Map.of(
                    Purity.NORMAL, 0.666F,
                    Purity.FLAWLESS, 1.35F,
                    Purity.PERFECT, 2.25F)))
            .bonus(RANGED_WEAPON, MobEffectBonus.builder()
                .effect(TW_FROSTED)
                .target(Target.ARROW_TARGET)
                .value(Purity.FLAWLESS, 180, 1, 500)
                .value(Purity.PERFECT, 240, 2, 500)));

        addConditionally("twilightforest", "twilight/forest", TieredWeights.forTiersAbove(WorldTier.SUMMIT, 50, 2F), c -> c
            .unique()
            .minPurity(Purity.FLAWED)
            .contstraints(Constraints.forDimension(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("twilightforest:twilight_forest"))))
            .bonus(WEAPONS, TreasureGoblinBonus.builder()
                .value(Purity.FLAWED, 0.005F, 4800)
                .value(Purity.NORMAL, 0.0075F, 4800)
                .value(Purity.FLAWLESS, 0.01F, 4800)
                .value(Purity.PERFECT, 0.015F, 4800))
            .bonus(LootCategories.SHIELD, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and")
                .modifier(b -> b
                    .attr(ALObjects.Attributes.HEALING_RECEIVED)
                    .op(Operation.ADD_MULTIPLIED_BASE)
                    .value(Purity.FLAWED, 0.10F)
                    .value(Purity.NORMAL, 0.20F)
                    .value(Purity.FLAWLESS, 0.30F)
                    .value(Purity.PERFECT, 0.45F))
                .modifier(b -> b
                    .attr(Attributes.ATTACK_SPEED)
                    .op(Operation.ADD_MULTIPLIED_BASE)
                    .value(Purity.FLAWED, -0.05F)
                    .value(Purity.NORMAL, -0.10F)
                    .value(Purity.FLAWLESS, -0.15F)
                    .value(Purity.PERFECT, -0.25F)))
            .bonus(new OreMagnetBonus(
                new GemClass(LootCategories.BREAKER), Map.of(
                    Purity.FLAWED, 24,
                    Purity.NORMAL, 20,
                    Purity.FLAWLESS, 16,
                    Purity.PERFECT, 10)))
            .bonus(LootCategories.CHESTPLATE, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.but_and")
                .modifier(b -> b
                    .attr(ALObjects.Attributes.ARMOR_SHRED)
                    .op(Operation.ADD_MULTIPLIED_BASE)
                    .value(Purity.FLAWED, 0.35F)
                    .value(Purity.NORMAL, 0.50F)
                    .value(Purity.FLAWLESS, 0.75F)
                    .value(Purity.PERFECT, 1F))
                .modifier(b -> b
                    .attr(ALObjects.Attributes.ARMOR_PIERCE)
                    .op(Operation.ADD_MULTIPLIED_BASE)
                    .value(Purity.FLAWED, -0.15F)
                    .value(Purity.NORMAL, -0.225F)
                    .value(Purity.FLAWLESS, -0.325F)
                    .value(Purity.PERFECT, -0.40F))
                .modifier(b -> b
                    .attr(ALObjects.Attributes.PROT_PIERCE)
                    .op(Operation.ADD_MULTIPLIED_BASE)
                    .value(Purity.FLAWED, -0.05F)
                    .value(Purity.NORMAL, -0.125F)
                    .value(Purity.FLAWLESS, -0.225F)
                    .value(Purity.PERFECT, -0.20F))));
    }

    private void addGem(String name, UnaryOperator<Gem.Builder> config) {
        addGem(name, TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY), config);
    }

    private void addGem(String name, TieredWeights weights, UnaryOperator<Gem.Builder> config) {
        var builder = new Gem.Builder(weights);
        config.apply(builder);
        this.add(Apotheosis.loc(name), builder.build());
    }

    private void addConditionally(String modid, String name, TieredWeights weights, UnaryOperator<Gem.Builder> config) {
        var builder = new Gem.Builder(weights);
        config.apply(builder);
        this.addConditionally(Apotheosis.loc(name), builder.build(), new ModLoadedCondition(modid));
    }

    /**
     * Creates a standalone holder that can be serialized in datagen by stealing the {@link UniversalOwner} from the registry lookup.
     */
    private static <T> Holder.Reference<T> standaloneHolder(HolderLookup.Provider registries, ResourceKey<T> key) {
        return ApothMiscUtil.standaloneHolder(registries, key);
    }

}
