package dev.shadowsoffire.apotheosis.affix;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.CachedObject;
import dev.shadowsoffire.placebo.util.CachedObject.CachedObjectSource;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class AffixHelper {

    public static final ResourceLocation AFFIX_CACHED_OBJECT = Apotheosis.loc("affixes");

    // Used to encode the shooting weapon on arrows.
    public static final String SOURCE_WEAPON = "apoth.source_weapon";

    /**
     * Adds this specific affix to the Item's NBT tag.
     */
    public static void applyAffix(ItemStack stack, AffixInstance inst) {
        ItemAffixes.Builder builder = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY).toBuilder();
        builder.upgrade(inst.affix(), inst.level());
        setAffixes(stack, builder.build());
    }

    public static void setAffixes(ItemStack stack, ItemAffixes affixes) {
        stack.set(Components.AFFIXES, affixes);
    }

    public static void setName(ItemStack stack, Component name) {
        stack.set(Components.AFFIX_NAME, name.copy());
    }

    @Nullable
    public static Component getName(ItemStack stack) {
        return stack.get(Components.AFFIX_NAME);
    }

    /**
     * Gets the affixes of an item. The returned map is immutable.
     * <p>
     * Due to potential reloads, it is possible for an affix instance to become unbound but still remain cached.
     *
     * @param stack The stack being queried.
     * @return An immutable map of all affixes on the stack, or an empty map if none were found.
     * @apiNote Prefer using {@link #streamAffixes(ItemStack)} where applicable, since invalid instances will be pre-filtered.
     */
    public static Map<DynamicHolder<Affix>, AffixInstance> getAffixes(ItemStack stack) {
        if (AffixRegistry.INSTANCE.getValues().isEmpty()) return Collections.emptyMap(); // Don't enter getAffixesImpl if the affixes haven't loaded yet.
        return CachedObjectSource.getOrCreate(stack, AFFIX_CACHED_OBJECT, AffixHelper::getAffixesImpl, CachedObject.hashComponents(Components.AFFIXES, Components.RARITY));
    }

    public static Map<DynamicHolder<Affix>, AffixInstance> getAffixesImpl(ItemStack stack) {
        if (stack.isEmpty()) return Collections.emptyMap();
        DynamicHolder<LootRarity> rarity = getRarity(stack);
        if (!rarity.isBound()) {
            return Collections.emptyMap();
        }
        Map<DynamicHolder<Affix>, AffixInstance> map = new HashMap<>();
        ItemAffixes affixes = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY);
        if (!affixes.isEmpty()) {
            LootCategory cat = LootCategory.forItem(stack);
            for (DynamicHolder<Affix> affix : affixes.keySet()) {
                if (!affix.isBound() || !affix.get().canApplyTo(stack, cat, rarity.get())) continue;
                float lvl = affixes.getLevel(affix);
                map.put(affix, new AffixInstance(affix, lvl, rarity, stack));
            }
        }
        return Collections.unmodifiableMap(map);
    }

    public static Stream<AffixInstance> streamAffixes(ItemStack stack) {
        return getAffixes(stack).values().stream().filter(AffixInstance::isValid);
    }

    public static Stream<AffixInstance> streamAffixes(Projectile proj) {
        return getAffixes(proj).values().stream().filter(AffixInstance::isValid);
    }

    public static boolean hasAffixes(ItemStack stack) {
        return !getAffixes(stack).isEmpty();
    }

    public static void setRarity(ItemStack stack, LootRarity rarity) {
        stack.set(Components.RARITY, RarityRegistry.INSTANCE.holder(rarity));
    }

    /**
     * Copies the entire source weapon itemstack into the target entity iff relevant components are present.
     *
     * @param stack  The source item stack (the ranged weapon that fired the projectile).
     * @param entity The newly created projectile.
     */
    public static void copyToProjectile(ItemStack stack, Entity entity) {
        ItemAffixes affixes = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY);
        ItemContainerContents gems = stack.getOrDefault(Components.SOCKETED_GEMS, ItemContainerContents.EMPTY);
        if (!affixes.isEmpty() || gems.nonEmptyStream().findAny().isPresent()) {
            entity.getPersistentData().put(SOURCE_WEAPON, stack.save(entity.level().registryAccess()));
        }
    }

    /**
     * Retrieves the encoded source weapon from a projectile, if one was available.
     */
    public static ItemStack getSourceWeapon(Entity entity) {
        if (entity.getPersistentData().contains(SOURCE_WEAPON)) {
            return ItemStack.parseOptional(entity.level().registryAccess(), entity.getPersistentData().getCompound(SOURCE_WEAPON));
        }
        return ItemStack.EMPTY;
    }

    public static Map<DynamicHolder<Affix>, AffixInstance> getAffixes(Projectile proj) {
        ItemStack stack = getSourceWeapon(proj);
        return getAffixes(stack);
    }

    /**
     * May be unbound
     */
    public static DynamicHolder<LootRarity> getRarity(ItemStack stack) {
        return stack.getOrDefault(Components.RARITY, RarityRegistry.INSTANCE.emptyHolder());
    }

    public static Collection<DynamicHolder<Affix>> byType(AffixType type) {
        return AffixRegistry.INSTANCE.getTypeMap().get(type);
    }

    @Deprecated
    public static StepFunction step(float min, int steps, float step) {
        return new StepFunction(min, steps, step);
    }

}
