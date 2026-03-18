package dev.shadowsoffire.apotheosis.affix.augmenting;

import java.util.Comparator;
import java.util.List;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.Apoth.Menus;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.ItemAffixes;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.net.RerollResultPayload;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.menu.BlockEntityMenu;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class AugmentingMenu extends BlockEntityMenu<AugmentingTableTile> {

    public static final int UPGRADE = 0;
    public static final int REROLL = 1;

    protected final Player player;
    protected InternalItemHandler itemInv = new InternalItemHandler(1);

    public AugmentingMenu(int id, Inventory inv, BlockPos pos) {
        super(Menus.AUGMENTING, id, inv, pos);
        this.player = inv.player;

        this.addSlot(new UpdatingSlot(this.itemInv, 0, 16, 16, AffixHelper::hasAffixes){
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public int getMaxStackSize(ItemStack pStack) {
                return 1;
            }
        });

        this.addSlot(new UpdatingSlot(this.tile.inv, 0, 16, 41, stack -> stack.is(Items.SIGIL_OF_ENHANCEMENT)));

        this.addPlayerSlots(inv, 8, 140);

        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && AffixHelper.hasAffixes(stack), 0, 1);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.is(Items.SIGIL_OF_ENHANCEMENT), 1, 2);
        this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9, true);
        this.registerInvShuffleRules();
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.clearContainer(pPlayer, this.itemInv);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        int selected = id >> 1;

        ItemStack mainItem = this.getMainItem();

        if (mainItem.isEmpty()) {
            return false;
        }

        List<AffixInstance> affixes = computeItemAffixes(mainItem);

        if (affixes.isEmpty() || selected >= affixes.size()) {
            return false;
        }

        switch (id & 0b1) {
            case UPGRADE -> {
                AffixInstance inst = affixes.get(selected);
                if (!canAugment(inst)) {
                    return false;
                }

                ItemStack sigils = this.getSigils();
                if (!this.player.isCreative()) {
                    if (!hasUpgradeCost()) {
                        return false;
                    }
                    else {
                        sigils.shrink(AdventureConfig.upgradeSigilCost);
                        EnchantmentUtils.chargeExperience(player, EnchantmentUtils.getTotalExperienceForLevel(AdventureConfig.upgradeLevelCost));
                    }
                }

                AffixHelper.applyAffix(mainItem, inst.withNewLevel(Math.min(inst.level() + 0.25F, Affix.STANDARD_MAX_LEVEL)));
                this.slots.get(0).set(mainItem);
                player.level().playSound(null, this.pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1F, player.level().random.nextFloat() * 0.25F + 1F);
                player.level().playSound(null, this.pos, SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 0.34F, player.level().random.nextFloat() * 0.2F + 0.8F);
                player.level().playSound(null, this.pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS, 0.45F, player.level().random.nextFloat() * 0.75F + 0.5F);
                return true;
            }
            case REROLL -> {
                AffixInstance inst = affixes.get(selected);
                List<DynamicHolder<Affix>> alternatives = computeAlternatives(player, mainItem, inst);
                if (alternatives.isEmpty()) {
                    return false;
                }

                ItemStack sigils = this.getSigils();
                if (!this.player.isCreative()) {
                    if (!hasRerollCost()) {
                        return false;
                    }
                    else {
                        sigils.shrink(AdventureConfig.rerollSigilCost);
                        EnchantmentUtils.chargeExperience(player, EnchantmentUtils.getTotalExperienceForLevel(AdventureConfig.rerollLevelCost));
                    }
                }

                ItemAffixes.Builder builder = mainItem.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY).toBuilder();
                builder.remove(inst.affix());

                GenContext ctx = GenContext.forPlayer(player);
                List<WeightedEntry.Wrapper<Affix>> weighted = getWeightedAffixes(alternatives, ctx);
                DynamicHolder<Affix> newAffix = WeightedRandom.getRandomItem(player.getRandom(), weighted).map(Wrapper::data).map(AffixRegistry.INSTANCE::holder).orElse(null);

                // We need to fallback to a random affix if all the weights were zero.
                if (newAffix == null) {
                    newAffix = alternatives.get(player.getRandom().nextInt(alternatives.size()));
                }

                builder.upgrade(newAffix, player.getRandom().nextFloat());

                AffixHelper.setAffixes(mainItem, builder.build());
                this.slots.get(0).set(mainItem);
                player.level().playSound(null, this.pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1F, player.level().random.nextFloat() * 0.25F + 1F);
                player.level().playSound(null, this.pos, SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 0.34F, player.level().random.nextFloat() * 0.2F + 0.8F);
                player.level().playSound(null, this.pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS, 0.45F, player.level().random.nextFloat() * 0.75F + 0.5F);
                this.broadcastChanges();
                PacketDistributor.sendToPlayer((ServerPlayer) this.player, new RerollResultPayload(newAffix));
                this.tile.setChanged();
                return true;
            }
        }

        return false;
    }

    public ItemStack getMainItem() {
        return this.slots.get(0).getItem();
    }

    public ItemStack getSigils() {
        return this.slots.get(1).getItem();
    }

    public boolean hasUpgradeCost() {
        return this.getSigils().getCount() >= AdventureConfig.upgradeSigilCost && this.player.experienceLevel >= AdventureConfig.upgradeLevelCost;
    }

    public boolean hasRerollCost() {
        return this.getSigils().getCount() >= AdventureConfig.rerollSigilCost && this.player.experienceLevel >= AdventureConfig.rerollLevelCost;
    }

    /**
     * Returns a sorted list of the item affixes on the given stack.
     */
    public static List<AffixInstance> computeItemAffixes(ItemStack stack) {
        if (!stack.has(Components.AFFIXES)) {
            return List.of();
        }

        return AffixHelper.streamAffixes(stack).sorted(Comparator.comparing(inst -> inst.affix().getId())).toList();
    }

    /**
     * Returns true if the given affix instance can be upgraded in the augmenting table.
     */
    public static boolean canAugment(AffixInstance inst) {
        return !inst.isLevelIndependent() && inst.level() < Affix.STANDARD_MAX_LEVEL;
    }

    protected static List<DynamicHolder<Affix>> computeAlternatives(Player player, ItemStack stack, AffixInstance selected) {
        return LootController.getAlternativeAffixes(player, stack, selected.getRarity(), selected.affix()).toList();
    }

    protected static List<WeightedEntry.Wrapper<Affix>> getWeightedAffixes(List<DynamicHolder<Affix>> affixes, GenContext ctx) {
        return affixes.stream().map(a -> a.get().<Affix>wrap(ctx.tier(), ctx.luck())).toList();
    }

}
