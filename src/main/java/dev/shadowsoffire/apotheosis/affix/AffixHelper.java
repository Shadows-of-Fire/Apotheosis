package dev.shadowsoffire.apotheosis.affix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingMenu;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mixin.ItemStackMixin;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.CachedObject;
import dev.shadowsoffire.placebo.util.CachedObject.CachedObjectSource;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class AffixHelper {

    public static final ResourceLocation AFFIX_CACHED_OBJECT = Apotheosis.loc("affixes");

    // Used to encode the shooting weapon on arrows.
    public static final String SOURCE_WEAPON = "apoth.source_weapon";

    /**
     * Adds this specific affix to the Item's NBT tag.
     */
    public static void applyAffix(ItemStack stack, AffixInstance inst) {
        ItemAffixes.Builder builder = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY).toBuilder();
        builder.put(inst.affix(), inst.level());
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
     * Called from {@link ItemStackMixin} to update the {@link ItemStack#getHoverName() hover name} of an itemstack based on {@link Components#AFFIX_NAME}.
     * <p>
     * This method retrieves the current affix name, and if present, makes a deep copy of the contents to insert the hover name in as an argument.
     * <p>
     * Failure to make a deep copy of the contents will lead to the component being different on the client and server, causing desyncs in container menus.
     *
     * @param stack       The item stack
     * @param currentName The current return value from {@link ItemStack#getHoverName()}
     * @return The updated name, or null if the component was absent or malformed
     */
    @Nullable
    public static Component getModifiedStackName(ItemStack stack, Component currentName) {
        if (stack.has(Components.AFFIX_NAME)) {
            if (FMLEnvironment.dist.isClient()) {
                Component hidden = ClientAccess.getHiddenAffixName(currentName);
                if (hidden != null) {
                    return hidden;
                }
            }

            try {
                Component component = AffixHelper.getName(stack);
                TranslatableContents contents = copyContents(component);
                int idx = "misc.apotheosis.affix_name.four".equals(contents.getKey()) ? 2 : 1;
                contents.getArgs()[idx] = currentName;
                var ret = MutableComponent.create(contents).withStyle(component.getStyle());
                for (Component sibling : component.getSiblings()) {
                    ret.append(sibling);
                }
                return ret;
            }
            catch (Exception exception) {
                stack.remove(Components.AFFIX_NAME);
            }
        }
        return null;
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
        if (AffixRegistry.INSTANCE.getValues().isEmpty()) {
            return Collections.emptyMap(); // Don't enter getAffixesImpl if the affixes haven't loaded yet.
        }
        return CachedObjectSource.getOrCreate(stack, AFFIX_CACHED_OBJECT, AffixHelper::getAffixesImpl, CachedObject.hashComponents(Components.AFFIXES, Components.RARITY));
    }

    public static Map<DynamicHolder<Affix>, AffixInstance> getAffixesImpl(ItemStack stack) {
        if (stack.isEmpty()) {
            return Collections.emptyMap();
        }
        DynamicHolder<LootRarity> rarity = getRarity(stack);
        if (!rarity.isBound()) {
            return Collections.emptyMap();
        }
        Map<DynamicHolder<Affix>, AffixInstance> map = new HashMap<>();
        ItemAffixes affixes = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY);
        if (!affixes.isEmpty()) {
            LootCategory cat = LootCategory.forItem(stack);
            for (DynamicHolder<Affix> affix : affixes.keySet()) {
                if (!affix.isBound() || !affix.get().canApplyTo(stack, cat, rarity.get())) {
                    continue;
                }
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

    /**
     * Applies the effect of the Sigil of Malice to the given item stack.
     * <p>
     * The sigil increases the effective level of one affix on the item to 1.5F, and removes another affix at random (based on the reforge seed).
     * 
     * @param stack The input stack. The stack is modified in place.
     * @apiNote This cannot be run reliably on the client, as the reforge seed is not guaranteed to be present.
     */
    public static void applyMalice(Player player, ItemStack stack) {
        ItemAffixes affixes = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY);
        if (affixes.isEmpty() || affixes.size() < 2) {
            return;
        }

        int seed = player.getPersistentData().getInt(ReforgingMenu.REFORGE_SEED);
        RandomSource rand = new XoroshiroRandomSource(seed);

        ItemAffixes.Builder builder = affixes.toBuilder();
        List<DynamicHolder<Affix>> afxList = new ArrayList<>(affixes.keySet());

        // TODO: Should we filter out affixes that are level-independent?

        // Choose two distinct indices
        int size = afxList.size();
        int firstIndex = rand.nextInt(size);
        int secondIndex;
        do {
            secondIndex = rand.nextInt(size);
        }
        while (secondIndex == firstIndex);

        DynamicHolder<Affix> buffed = afxList.get(firstIndex);
        DynamicHolder<Affix> removed = afxList.get(secondIndex);

        builder.upgrade(buffed, 1.5F);
        float oldLevel = builder.getLevel(removed);
        builder.remove(removed);

        setAffixes(stack, builder.build());
        stack.set(Components.TOUCHED_BY_MALICE, true);
        player.getPersistentData().putInt(ReforgingMenu.REFORGE_SEED, player.getRandom().nextInt());

        AttributeTooltipContext ctx = AttributeTooltipContext.of(player, TooltipContext.of(player.level()), ApothicAttributes.getTooltipFlag());

        AffixInstance buff = new AffixInstance(buffed, 1.5F, getRarity(stack), stack);
        AffixInstance rem = new AffixInstance(removed, oldLevel, getRarity(stack), stack);

        MutableComponent buffedName = Component.translatable("[%s]", buff.getName(true));
        buffedName.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, buff.getAugmentingText(ctx))));

        MutableComponent removedName = Component.translatable("[%s]", rem.getName(true));
        removedName.setStyle(Style.EMPTY.withColor(ChatFormatting.RED).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, rem.getAugmentingText(ctx))));

        Component msg = Apotheosis.lang("text", "malice_notice", buffedName, removedName);
        player.sendSystemMessage(msg);
    }

    /**
     * Applies the effect of the Sigil of Supremacy to the given item stack.
     * <p>
     * The sigil increases the effective level of all affixes on the item to 1.5F.
     * 
     * @param stack The input stack. The stack is modified in place.
     */
    public static void applySupremacy(ItemStack stack) {
        ItemAffixes affixes = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY);
        if (affixes.isEmpty()) {
            return;
        }

        ItemAffixes.Builder builder = affixes.toBuilder();
        List<DynamicHolder<Affix>> afxList = new ArrayList<>(affixes.keySet());

        for (DynamicHolder<Affix> affix : afxList) {
            builder.upgrade(affix, 1.5F);
        }

        setAffixes(stack, builder.build());
    }

    @Deprecated
    public static StepFunction step(float min, int steps, float step) {
        return new StepFunction(min, steps, step);
    }

    private static TranslatableContents copyContents(Component comp) {
        TranslatableContents tContents = (TranslatableContents) comp.getContents();
        Object[] args = tContents.getArgs();
        Object[] clone = Arrays.copyOf(args, args.length);
        return new TranslatableContents(tContents.getKey(), tContents.getFallback(), clone);
    }

    private static class ClientAccess {

        @Nullable
        private static Component getHiddenAffixName(Component currentName) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && WorldTier.isTutorialActive(player)) {
                return Apotheosis.lang("text", "unidentified", currentName);
            }
            return null;
        }
    }

}
