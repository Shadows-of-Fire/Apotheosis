package dev.shadowsoffire.apotheosis.socket.gem.storage;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingMenu;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.shadowsoffire.placebo.menu.BlockEntityMenu;
import dev.shadowsoffire.placebo.payloads.ButtonClickPayload.IButtonContainer;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class GemCaseMenu extends BlockEntityMenu<GemCaseTile> implements IButtonContainer {

    public static final int INPUT_SLOT = 0;
    public static final int FILTER_SLOT = 1;
    public static final int FIRST_GEM_SLOT = 2;
    public static final int FIRST_UPGRADE_MAT_SLOT = 8;

    protected SimpleContainer ioInv = new SimpleContainer(2);
    protected SimpleContainer upgradeMatInv = new SimpleContainer(6){
        @Override
        public void setChanged() {
            super.setChanged();
            GemCaseMenu.this.onChanged();
        }
    };
    protected Runnable notifier = null;

    @Nullable
    protected Gem selectedGem = null;

    public GemCaseMenu(int id, Inventory inv, BlockPos pos) {
        super(Apoth.Menus.GEM_CASE, id, inv, pos);
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
        this.clearContainer(player, this.upgradeMatInv);
    }

    void initCommon(Inventory inv) {
        this.addSlot(new Slot(this.ioInv, 0, 142, 99){
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
                if (!GemCaseMenu.this.level.isClientSide && !this.getItem().isEmpty()) {
                    GemCaseMenu.this.tile.depositGem(this.getItem());
                }
                if (!this.getItem().isEmpty() && GemCaseMenu.this.level.isClientSide) {
                    inv.player.level().playSound(inv.player, GemCaseMenu.this.pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.NEUTRAL, 0.5F, 0.7F);
                }
                GemCaseMenu.this.ioInv.setItem(0, ItemStack.EMPTY);
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
                GemCaseMenu.this.onChanged();
            }
        });

        for (Purity p : Purity.ALL_PURITIES) {
            this.addSlot(new GemCaseSlot(this, p, 21 + p.ordinal() * 18, 91));
        }

        for (int i = 0; i < this.upgradeMatInv.getContainerSize(); i++) {
            this.addSlot(new Slot(this.upgradeMatInv, i, -45 + 18 * (i % 2), 37 + 18 * (i / 2)){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return GemCaseMenu.this.isValidUpgradeMaterial(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 64;
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    GemCaseMenu.this.onChanged();
                }
            });
        }

        this.addPlayerSlots(inv, 8, 148);

        this.mover.registerRule((stack, slot) -> slot == FILTER_SLOT, this.playerInvStart, this.slots.size());
        this.mover.registerRule((stack, slot) -> slot >= FIRST_GEM_SLOT && slot < FIRST_UPGRADE_MAT_SLOT, this.playerInvStart, this.slots.size());
        this.mover.registerRule((stack, slot) -> slot >= FIRST_UPGRADE_MAT_SLOT && slot < FIRST_UPGRADE_MAT_SLOT + 6, this.playerInvStart, this.slots.size());
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.is(Apoth.Items.GEM), INPUT_SLOT, INPUT_SLOT + 1);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && isValidUpgradeMaterial(stack), FIRST_UPGRADE_MAT_SLOT, FIRST_UPGRADE_MAT_SLOT + 6);
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

    @Nullable
    public GemUpgradeMatch getUpgradeMatch(Purity purity) {
        if (this.selectedGem == null) return null;
        return this.tile.getUpgradeMatch(GemRegistry.INSTANCE.holder(this.selectedGem), purity, upgradeMatInv);
    }

    @Override
    public void onQuickMove(ItemStack original, ItemStack remaining, Slot slot) {
        if (slot instanceof GemCaseSlot gss) {
            int amount = original.getCount() - remaining.getCount();
            this.tile.extractGem(GemRegistry.INSTANCE.holder(this.selectedGem), gss.purity, amount);
        }
        slot.setChanged();
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        Slot slot = this.getSlot(pIndex);
        if (slot instanceof GemCaseSlot) {
            this.mover.quickMoveStack(this, pPlayer, pIndex);
            return ItemStack.EMPTY; // Always abort after a single operation so we don't extract the entire inventory at once.
        }
        return this.mover.quickMoveStack(this, pPlayer, pIndex);
    }

    public boolean isValidUpgradeMaterial(ItemStack stack) {
        List<RecipeHolder<GemCuttingRecipe>> recipes = GemCuttingMenu.getRecipes(this.level);
        for (RecipeHolder<GemCuttingRecipe> rec : recipes) {
            if (rec.value() instanceof PurityUpgradeRecipe purRec) {
                if (purRec.isValidLeftItem(null, stack) || purRec.isValidRightItem(null, stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onButtonClick(int id) {
        // Try to upgrade the purity of the clicked gem. Do as many as possible if a shift-click is encoded in the button press.
        boolean shift = (id & 0x1000) != 0;
        Purity purity = Purity.BY_ID.apply(id & 0xFFF);

        if (this.selectedGem == null || purity == Purity.CRACKED) return;

        DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(this.selectedGem);
        int tries = shift ? 64 : 1;

        while (tries-- > 0) {
            boolean result = this.tile.upgradeGem(holder, purity, this.upgradeMatInv);
            if (!result) break;

            this.level.playSound(null, this.pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1, 1.5F + 0.35F * (1 - 2 * this.level.random.nextFloat()));
        }

    }
}
