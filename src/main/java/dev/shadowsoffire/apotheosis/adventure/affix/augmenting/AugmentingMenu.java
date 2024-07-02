package dev.shadowsoffire.apotheosis.adventure.affix.augmenting;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.shadowsoffire.apotheosis.Apoth.Affixes;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.menu.BlockEntityMenu;
import dev.shadowsoffire.placebo.network.PacketDistro;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class AugmentingMenu extends BlockEntityMenu<AugmentingTableTile> {

    public static final int UPGRADE = 0;
    public static final int REROLL = 1;

    public static final int UPGRADE_COST = 2;
    public static final int REROLL_COST = 1;

    protected final Player player;
    protected InternalItemHandler itemInv = new InternalItemHandler(1);

    public AugmentingMenu(int id, Inventory inv, BlockPos pos) {
        super(Adventure.Menus.AUGMENTING.get(), id, inv, pos);
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

        this.addSlot(new UpdatingSlot(this.tile.inv, 0, 16, 41, stack -> stack.getItem() == Items.SIGIL_OF_ENHANCEMENT.get()));

        this.addPlayerSlots(inv, 8, 140);

        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && AffixHelper.hasAffixes(stack), 0, 1);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.getItem() == Items.SIGIL_OF_ENHANCEMENT.get(), 1, 2);
        this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9, true);
        this.registerInvShuffleRules();
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.clearContainer(pPlayer, new RecipeWrapper(this.itemInv));
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
                if (inst.level() >= 1) {
                    return false;
                }

                ItemStack sigils = this.getSigils();
                if (!this.player.isCreative()) {
                    if (sigils.getCount() < UPGRADE_COST) {
                        return false;
                    }
                    else {
                        sigils.shrink(UPGRADE_COST);
                    }
                }

                AffixHelper.applyAffix(mainItem, inst.withNewLevel(inst.level() + 0.25F));
                this.slots.get(0).set(mainItem);
                player.level().playSound(null, this.pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1F, player.level().random.nextFloat() * 0.25F + 1F);
                player.level().playSound(null, this.pos, SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 0.34F, player.level().random.nextFloat() * 0.2F + 0.8F);
                player.level().playSound(null, this.pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS, 0.45F, player.level().random.nextFloat() * 0.75F + 0.5F);
                return true;
            }
            case REROLL -> {
                AffixInstance inst = affixes.get(selected);
                List<DynamicHolder<? extends Affix>> alternatives = computeAlternatives(mainItem, inst, affixes);
                if (alternatives.isEmpty()) {
                    return false;
                }

                ItemStack sigils = this.getSigils();
                if (!this.player.isCreative()) {
                    if (sigils.getCount() < REROLL_COST) {
                        return false;
                    }
                    else {
                        sigils.shrink(REROLL_COST);
                    }
                }

                Map<DynamicHolder<? extends Affix>, AffixInstance> newAffixes = new HashMap<>(AffixHelper.getAffixes(mainItem));
                newAffixes.remove(inst.affix());

                DynamicHolder<? extends Affix> newAffix = alternatives.get(player.random.nextInt(alternatives.size()));
                newAffixes.put(newAffix, new AffixInstance(newAffix, mainItem, inst.rarity(), player.random.nextFloat()));

                AffixHelper.setAffixes(mainItem, newAffixes);
                this.slots.get(0).set(mainItem);
                player.level().playSound(null, this.pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1F, player.level().random.nextFloat() * 0.25F + 1F);
                player.level().playSound(null, this.pos, SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 0.34F, player.level().random.nextFloat() * 0.2F + 0.8F);
                player.level().playSound(null, this.pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS, 0.45F, player.level().random.nextFloat() * 0.75F + 0.5F);
                this.broadcastChanges();
                PacketDistro.sendTo(Apotheosis.CHANNEL, new RerollResultMessage(newAffix), this.player);
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

    public static List<AffixInstance> computeItemAffixes(ItemStack stack) {
        Map<DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        if (affixes.isEmpty()) {
            return Collections.emptyList();
        }

        return affixes.values().stream().sorted(Comparator.comparing(inst -> inst.affix().getId())).filter(a -> !a.affix().equals(Affixes.DURABLE)).toList();
    }

    protected static List<DynamicHolder<? extends Affix>> computeAlternatives(ItemStack stack, AffixInstance selected, List<AffixInstance> affixes) {
        return LootController.getAvailableAffixes(stack, selected.rarity().get(), affixes.stream().map(AffixInstance::affix).collect(Collectors.toSet()),
            selected.affix().get().getType());
    }

}
