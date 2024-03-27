package dev.shadowsoffire.apotheosis.adventure.affix.socket.gem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.tabs.ITabFiller;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class GemItem extends Item implements ITabFiller {

    public static final String HAS_REFRESHED = "has_refreshed";
    public static final String UUID_ARRAY = "uuids";
    public static final String GEM = "gem";

    public GemItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        GemInstance inst = GemInstance.unsocketed(pStack);
        if (!inst.isValidUnsocketed()) {
            tooltip.add(Component.literal("Errored gem with no bonus!").withStyle(ChatFormatting.GRAY));
            return;
        }
        inst.gem().get().addInformation(pStack, inst.rarity().get(), tooltip::add);
    }

    @Override
    public Component getName(ItemStack pStack) {
        GemInstance inst = GemInstance.unsocketed(pStack);
        if (!inst.isValidUnsocketed()) return super.getName(pStack);
        MutableComponent comp = Component.translatable(this.getDescriptionId(pStack));
        comp = Component.translatable("item.apotheosis.gem." + inst.rarity().getId(), comp);
        return comp.withStyle(Style.EMPTY.withColor(inst.rarity().get().getColor()));
    }

    @Override
    public String getDescriptionId(ItemStack pStack) {
        DynamicHolder<Gem> gem = getGem(pStack);
        if (!gem.isBound()) return super.getDescriptionId();
        return super.getDescriptionId(pStack) + "." + gem.getId();
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        GemInstance inst = GemInstance.unsocketed(pStack);
        if (!inst.isValidUnsocketed()) return super.isFoil(pStack);
        return inst.isMaxRarity();
    }

    @Override
    public boolean canBeHurtBy(DamageSource src) {
        return super.canBeHurtBy(src) && !src.is(DamageTypes.FALLING_ANVIL);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, CreativeModeTab.Output out) {
        GemRegistry.INSTANCE.getValues().stream().sorted(Comparator.comparing(Gem::getId)).forEach(gem -> {
            RarityRegistry.INSTANCE.getOrderedRarities().stream().map(DynamicHolder::get).forEach(rarity -> {
                if (gem.clamp(rarity) == rarity) {
                    ItemStack stack = new ItemStack(this);
                    setGem(stack, gem);
                    AffixHelper.setRarity(stack, rarity);
                    out.accept(stack);
                }
            });
        });
    }

    @Override
    @Nullable
    public String getCreatorModId(ItemStack stack) {
        GemInstance inst = GemInstance.unsocketed(stack);
        if (inst.isValidUnsocketed()) {
            return inst.gem().getId().getNamespace();
        }
        return super.getCreatorModId(stack);
    }

    /**
     * Retrieves cached attribute modifier UUID(s) from a gem itemstack.<br>
     * This method simply invokes {@link #getUUIDs(CompoundTag, int)} with the root tag
     * and the {@linkplain Gem#getNumberOfUUIDs() Gem's requested UUID count}.
     *
     * @param gem The gem stack
     * @returns The stored UUID(s), creating them if they do not exist.
     */
    public static List<UUID> getUUIDs(ItemStack gemStack) {
        DynamicHolder<Gem> gem = getGem(gemStack);
        if (!gem.isBound()) return Collections.emptyList();
        return getOrCreateUUIDs(gemStack.getOrCreateTag(), gem.get().getNumberOfUUIDs());
    }

    /**
     * Retrieves cached attribute modifier UUID(s) from an itemstack.
     *
     * @param gem The gem stack
     * @returns The stored UUID(s), creating them if they do not exist.
     */
    public static List<UUID> getOrCreateUUIDs(CompoundTag tag, int numUUIDs) {
        if (numUUIDs == 0) return Collections.emptyList();
        if (tag.contains(UUID_ARRAY)) {
            ListTag list = tag.getList(UUID_ARRAY, Tag.TAG_INT_ARRAY);
            List<UUID> ret = new ArrayList<>(list.size());
            for (Tag t : list) {
                ret.add(NbtUtils.loadUUID(t));
            }
            if (ret.size() < numUUIDs) return generateAndSave(ret, numUUIDs, tag);
            return ret;
        }
        return generateAndSave(new ArrayList<>(numUUIDs), numUUIDs, tag);
    }

    private static List<UUID> generateAndSave(List<UUID> base, int amount, CompoundTag tag) {
        int needed = amount - base.size();
        for (int i = 0; i < needed; i++) {
            base.add(UUID.randomUUID());
        }
        ListTag list = new ListTag();
        for (UUID id : base) {
            list.add(NbtUtils.createUUID(id));
        }
        tag.put(UUID_ARRAY, list);
        return base;
    }

    /**
     * Sets the ID of the gem stored in this gem stack.
     *
     * @param gemStack The gem stack
     * @param gem      The Gem to store
     */
    public static void setGem(ItemStack gemStack, Gem gem) {
        gemStack.getOrCreateTag().putString(GEM, gem.getId().toString());
    }

    /**
     * Retrieves the underlying Gem instance of this gem stack.
     *
     * @param gem The gem stack
     * @returns A {@link DynamicHolder} targetting the gem, which may be unbound if the gem is missing or invalid.
     */
    public static DynamicHolder<Gem> getGem(ItemStack gem) {
        if (gem.getItem() != Items.GEM.get() || !gem.hasTag()) return GemRegistry.INSTANCE.emptyHolder();
        var tag = gem.getTag();
        return GemRegistry.INSTANCE.holder(new ResourceLocation(tag.getString(GEM)));
    }
}
