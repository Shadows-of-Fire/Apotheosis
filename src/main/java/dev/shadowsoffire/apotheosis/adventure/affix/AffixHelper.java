package dev.shadowsoffire.apotheosis.adventure.affix;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.CachedObject;
import dev.shadowsoffire.placebo.util.CachedObject.CachedObjectSource;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

public class AffixHelper {

    public static final ResourceLocation AFFIX_CACHED_OBJECT = Apotheosis.loc("affixes");

    public static final String DISPLAY = "display";
    public static final String LORE = "Lore";

    public static final String AFFIX_DATA = "affix_data";
    public static final String AFFIXES = "affixes";
    public static final String RARITY = "rarity";
    public static final String NAME = "name";

    // Used to encode the loot category of the shooting item on arrows.
    public static final String CATEGORY = "category";

    /**
     * Adds this specific affix to the Item's NBT tag.
     */
    public static void applyAffix(ItemStack stack, AffixInstance affix) {
        var affixes = new HashMap<>(getAffixes(stack));
        affixes.put(affix.affix(), affix);
        setAffixes(stack, affixes);
    }

    public static void setAffixes(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes) {
        CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
        CompoundTag affixesTag = new CompoundTag();
        for (AffixInstance inst : affixes.values()) {
            affixesTag.putFloat(inst.affix().getId().toString(), inst.level());
        }
        afxData.put(AFFIXES, affixesTag);
    }

    public static void setName(ItemStack stack, Component name) {
        CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
        afxData.putString(NAME, Component.Serializer.toJson(name));
    }

    @Nullable
    public static Component getName(ItemStack stack) {
        if (!stack.hasTag()) return null;
        CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
        if (afxData == null) return null;
        return Component.Serializer.fromJson(afxData.getString(NAME));
    }

    /**
     * Gets the affixes of an item. Changes to this map will not write-back to the affixes on the itemstack.
     * <p>
     * Due to potential reloads, it is possible for an affix instance to become unbound but still remain cached.
     *
     * @param stack The stack being queried.
     * @return An immutable map of all affixes on the stack, or an empty map if none were found.
     * @apiNote Prefer using {@link #streamAffixes(ItemStack)} where applicable, since invalid instances will be pre-filtered.
     */
    public static Map<DynamicHolder<? extends Affix>, AffixInstance> getAffixes(ItemStack stack) {
        if (AffixRegistry.INSTANCE.getValues().isEmpty()) return Collections.emptyMap(); // Don't enter getAffixesImpl if the affixes haven't loaded yet.
        return CachedObjectSource.getOrCreate(stack, AFFIX_CACHED_OBJECT, AffixHelper::getAffixesImpl, CachedObject.hashSubkey(AFFIX_DATA));
    }

    public static Map<DynamicHolder<? extends Affix>, AffixInstance> getAffixesImpl(ItemStack stack) {
        Map<DynamicHolder<? extends Affix>, AffixInstance> map = new HashMap<>();
        if (stack.isEmpty()) return Collections.emptyMap();
        SocketHelper.loadSocketAffix(stack, map);
        CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
        if (afxData != null && afxData.contains(AFFIXES)) {
            CompoundTag affixes = afxData.getCompound(AFFIXES);
            DynamicHolder<LootRarity> rarity = getRarity(afxData);
            if (!rarity.isBound()) rarity = RarityRegistry.getMinRarity();
            LootCategory cat = LootCategory.forItem(stack);
            for (String key : affixes.getAllKeys()) {
                DynamicHolder<Affix> affix = AffixRegistry.INSTANCE.holder(new ResourceLocation(key));
                if (!affix.isBound() || !affix.get().canApplyTo(stack, cat, rarity.get())) continue;
                float lvl = affixes.getFloat(key);
                map.put(affix, new AffixInstance(affix, stack, rarity, lvl));
            }
        }
        return Collections.unmodifiableMap(map);
    }

    public static Stream<AffixInstance> streamAffixes(ItemStack stack) {
        return getAffixes(stack).values().stream().filter(AffixInstance::isValid);
    }

    public static boolean hasAffixes(ItemStack stack) {
        return stack.hasTag() && !stack.getTag().getCompound(AFFIX_DATA).getCompound(AFFIXES).isEmpty();
    }

    public static void addLore(ItemStack stack, Component lore) {
        CompoundTag display = stack.getOrCreateTagElement(DISPLAY);
        ListTag tag = display.getList(LORE, 8);
        tag.add(StringTag.valueOf(Component.Serializer.toJson(lore)));
        display.put(LORE, tag);
    }

    public static void setRarity(ItemStack stack, LootRarity rarity) {
        Component comp = Component.translatable("%s", Component.literal("")).withStyle(Style.EMPTY.withColor(rarity.getColor()));
        CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
        afxData.putString(NAME, Component.Serializer.toJson(comp));
        // if (!stack.getOrCreateTagElement(DISPLAY).contains(LORE)) AffixHelper.addLore(stack,
        // Component.translatable("info.apotheosis.affix_item").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false)));
        afxData.putString(RARITY, RarityRegistry.INSTANCE.getKey(rarity).toString());
    }

    public static void copyFrom(ItemStack stack, Entity entity) {
        if (stack.hasTag() && stack.getTagElement(AFFIX_DATA) != null) {
            CompoundTag afxData = stack.getTagElement(AFFIX_DATA).copy();
            afxData.putString(CATEGORY, LootCategory.forItem(stack).getName());
            entity.getPersistentData().put(AFFIX_DATA, afxData);
        }
    }

    @Nullable
    public static LootCategory getShooterCategory(Entity entity) {
        CompoundTag afxData = entity.getPersistentData().getCompound(AFFIX_DATA);
        if (afxData != null && afxData.contains(CATEGORY)) {
            return LootCategory.byId(afxData.getString(CATEGORY));
        }
        return null;
    }

    public static Map<DynamicHolder<? extends Affix>, AffixInstance> getAffixes(AbstractArrow arrow) {
        Map<DynamicHolder<? extends Affix>, AffixInstance> map = new HashMap<>();
        CompoundTag afxData = arrow.getPersistentData().getCompound(AFFIX_DATA);
        SocketHelper.loadSocketAffix(arrow, map);
        if (afxData != null && afxData.contains(AFFIXES)) {
            CompoundTag affixes = afxData.getCompound(AFFIXES);
            DynamicHolder<LootRarity> rarity = getRarity(afxData);
            if (!rarity.isBound()) rarity = RarityRegistry.getMinRarity();
            for (String key : affixes.getAllKeys()) {
                DynamicHolder<Affix> affix = AffixRegistry.INSTANCE.holder(new ResourceLocation(key));
                if (!affix.isBound()) continue;
                float lvl = affixes.getFloat(key);
                map.put(affix, new AffixInstance(affix, ItemStack.EMPTY, rarity, lvl));
            }
        }
        return map;
    }

    public static Stream<AffixInstance> streamAffixes(AbstractArrow arrow) {
        return getAffixes(arrow).values().stream();
    }

    /**
     * May be unbound
     */
    public static DynamicHolder<LootRarity> getRarity(ItemStack stack) {
        if (!stack.hasTag()) return RarityRegistry.INSTANCE.emptyHolder();
        CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
        return getRarity(afxData);
    }

    /**
     * May be unbound
     */
    public static DynamicHolder<LootRarity> getRarity(@Nullable CompoundTag afxData) {
        if (afxData != null) {
            try {
                return RarityRegistry.byLegacyId(afxData.getString(RARITY));
            }
            catch (IllegalArgumentException e) {
                afxData.remove(RARITY);
                return RarityRegistry.byLegacyId("empty");
            }
        }
        return RarityRegistry.INSTANCE.emptyHolder();
    }

    public static Collection<DynamicHolder<Affix>> byType(AffixType type) {
        return AffixRegistry.INSTANCE.getTypeMap().get(type);
    }

    public static StepFunction step(float min, int steps, float step) {
        return new StepFunction(min, steps, step);
    }

}
