package dev.shadowsoffire.apotheosis.socket.gem.safe;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.placebo.menu.BlockEntityMenu;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GemSafeMenu extends BlockEntityMenu<GemSafeTile> {

    public static final int INPUT_SLOT = 0;
    public static final int FILTER_SLOT = 1;

    protected SimpleContainer ioInv = new SimpleContainer(2);
    protected Runnable notifier = null;

    @Nullable
    protected Gem selectedGem = null;

    protected List<GemSafeSlot> gemSlots = new ArrayList<>();

    public GemSafeMenu(int id, Inventory inv, BlockPos pos) {
        super(Apoth.Menus.GEM_SAFE, id, inv, pos);
        this.tile.addListener(this);
        this.initCommon(inv);
    }

    public void setSelectedGem(DynamicHolder<Gem> gem) {
        this.selectedGem = gem.isBound() ? gem.get() : null;
        this.onChanged();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!this.level.isClientSide) this.tile.removeListener(this);
        this.clearContainer(player, this.ioInv);
    }

    void initCommon(Inventory inv) {
        this.addSlot(new Slot(this.ioInv, 0, 142, 106){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Apoth.Items.GEM);
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                if (!GemSafeMenu.this.level.isClientSide && !this.getItem().isEmpty()) {
                    GemSafeMenu.this.tile.depositGem(this.getItem());
                }
                if (!this.getItem().isEmpty() && GemSafeMenu.this.level.isClientSide) {
                    inv.player.level().playSound(inv.player, GemSafeMenu.this.pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL, 0.5F, 0.7F);
                }
                GemSafeMenu.this.ioInv.setItem(0, ItemStack.EMPTY);
            }
        });
        this.addSlot(new Slot(this.ioInv, 1, 142, 18){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !LootCategory.forItem(stack).isNone();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void setChanged() {
                GemSafeMenu.this.onChanged();
            }
        });

        this.gemSlots.clear();
        for (Purity p : Purity.ALL_PURITIES) {
            GemSafeSlot slot = new GemSafeSlot(this, p, 21 + p.ordinal() * 18, 94);
            this.gemSlots.add(slot);
            this.addSlot(slot);
        }

        this.addPlayerSlots(inv, 8, 148);

        this.mover.registerRule((stack, slot) -> slot == FILTER_SLOT, this.playerInvStart, this.slots.size());
        this.mover.registerRule((stack, slot) -> slot > 1 && slot < 8, this.playerInvStart, this.slots.size());
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.is(Apoth.Items.GEM), INPUT_SLOT, INPUT_SLOT + 1);
        this.mover.registerRule((stack, slot) -> !LootCategory.forItem(stack).isNone(), FILTER_SLOT, FILTER_SLOT + 1);
        this.registerInvShuffleRules();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(this.pos.getX(), this.pos.getY(), this.pos.getZ()) < 16 * 16 && this.tile != null && !this.tile.isRemoved();
    }

    public void setNotifier(Runnable r) {
        this.notifier = r;
    }

    public void onChanged() {
        if (this.notifier != null) this.notifier.run();
    }

    public int getGemCount(Gem gem) {
        int sum = 0;
        for (Purity p : Purity.ALL_PURITIES) {
            sum += this.tile.getCount(gem, p);
        }
        return sum;
    }

    public int getGemCount(Gem gem, Purity p) {
        return this.tile.getCount(gem, p);
    }

    public ItemStack extractGem(Purity p, int count) {
        if (this.selectedGem == null) return ItemStack.EMPTY;
        DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(this.selectedGem);
        return this.tile.extractGem(holder, p, count);
    }

    @Override
    public void onQuickMove(ItemStack original, ItemStack remaining, Slot slot) {
        if (slot instanceof GemSafeSlot gss) {
            int amount = original.getCount() - remaining.getCount();
            this.tile.extractGem(GemRegistry.INSTANCE.holder(this.selectedGem), gss.purity, amount);
        }
        slot.setChanged();
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        Slot slot = this.getSlot(pIndex);
        if (slot instanceof GemSafeSlot) {
            this.mover.quickMoveStack(this, pPlayer, pIndex);
            return ItemStack.EMPTY; // Always abort after a single operation so we don't extract the entire inventory at once.
        }
        return this.mover.quickMoveStack(this, pPlayer, pIndex);
    }
}
