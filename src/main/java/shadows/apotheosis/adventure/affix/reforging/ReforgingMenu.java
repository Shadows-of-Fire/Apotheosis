package shadows.apotheosis.adventure.affix.reforging;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootController;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.cap.InternalItemHandler;
import shadows.placebo.container.BlockEntityContainer;
import shadows.placebo.container.ContainerUtil;

public class ReforgingMenu extends BlockEntityContainer<ReforgingTableTile> {

    public static final String REFORGE_SEED = "apoth_reforge_seed";

    protected final Player player;
    protected InternalItemHandler itemInv = new InternalItemHandler(1);
    protected final RandomSource random = new XoroshiroRandomSource(0);
    protected final int[] seed = new int[2];
    protected final int[] costs = new int[3];
    protected DataSlot needsReset = DataSlot.standalone();

    public ReforgingMenu(int id, Inventory inv, BlockPos pos) {
        super(Apoth.Menus.REFORGING.get(), id, inv, pos);
        this.player = inv.player;
        this.addSlot(new UpdatingSlot(this.itemInv, 0, 25, 24, stack -> !LootCategory.forItem(stack).isNone()){
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public int getMaxStackSize(ItemStack pStack) {
                return 1;
            }
        });
        this.addSlot(new UpdatingSlot(this.tile.inv, 0, 15, 45, this.tile::isValidRarityMat));
        this.addSlot(new UpdatingSlot(this.tile.inv, 1, 35, 45, stack -> stack.getItem() == Apoth.Items.GEM_DUST.get()));
        this.addPlayerSlots(inv, 8, 84);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && !LootCategory.forItem(stack).isNone(), 0, 1);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.tile.isValidRarityMat(stack), 1, 2);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.getItem() == Apoth.Items.GEM_DUST.get(), 2, 3);
        this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9);
        this.registerInvShuffleRules();

        this.updateSeed();
        this.addDataSlot(this.needsReset);
        this.addDataSlot(DataSlot.shared(this.seed, 0));
        this.addDataSlot(DataSlot.shared(this.seed, 1));
        this.addDataSlot(DataSlot.shared(this.costs, 0));
        this.addDataSlot(DataSlot.shared(this.costs, 1));
        this.addDataSlot(DataSlot.shared(this.costs, 2));
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.clearContainer(pPlayer, new RecipeWrapper(this.itemInv));
    }

    protected void updateSeed() {
        int seed = this.player.getPersistentData().getInt(REFORGE_SEED);
        if (seed == 0) {
            seed = this.player.random.nextInt();
            this.player.getPersistentData().putInt(REFORGE_SEED, seed);
        }
        this.seed[0] = ContainerUtil.split(seed, false);
        this.seed[1] = ContainerUtil.split(seed, true);
    }

    public int getSeed() {
        return ContainerUtil.merge(this.seed[0], this.seed[1], true);
    }

    @Override
    public boolean clickMenuButton(Player player, int slot) {
        if (slot >= 0 && slot < 3) {

            ItemStack input = this.getSlot(0).getItem();
            LootRarity rarity = this.getRarity();
            ReforgingRecipe recipe = this.tile.getRecipeFor(rarity);
            if (recipe == null || input.isEmpty() || this.needsReset()) return false;

            int dust = this.getDustCount();
            int dustCost = this.getDustCost(slot);
            int mats = this.getMatCount();
            int matCost = this.getMatCost(slot);
            int levels = this.player.experienceLevel;
            int levelCost = this.getLevelCost(slot);

            if ((dust < dustCost || mats < matCost || levels < levelCost) && !player.isCreative()) return false;

            if (!player.level.isClientSide) {
                ItemStack[] choices = new ItemStack[3];

                RandomSource rand = this.random;
                for (int i = 0; i < 3; i++) {
                    rand.setSeed(this.getSeed() ^ ForgeRegistries.ITEMS.getKey(input.getItem()).hashCode() + i);
                    choices[i] = LootController.createLootItem(input.copy(), rarity, rand);
                }

                ItemStack out = choices[slot];
                this.getSlot(0).set(out);
                if (!player.isCreative()) {
                    this.getSlot(1).getItem().shrink(matCost);
                    this.getSlot(2).getItem().shrink(dustCost);
                }
                player.giveExperienceLevels(-3 * ++slot);
                player.getPersistentData().putInt(REFORGE_SEED, player.random.nextInt());
                this.updateSeed();
                this.needsReset.set(1);
            }

            player.playSound(SoundEvents.EVOKER_CAST_SPELL, 0.99F, this.level.random.nextFloat() * 0.25F + 1F);
            player.playSound(SoundEvents.AMETHYST_CLUSTER_STEP, 0.34F, this.level.random.nextFloat() * 0.2F + 0.8F);
            player.playSound(SoundEvents.SMITHING_TABLE_USE, 0.45F, this.level.random.nextFloat() * 0.5F + 0.75F);
            return true;
        }
        return super.clickMenuButton(player, slot);
    }

    public int getMatCount() {
        return this.getSlot(1).getItem().getCount();
    }

    public int getDustCount() {
        return this.getSlot(2).getItem().getCount();
    }

    @Nullable
    public LootRarity getRarity() {
        ItemStack s = this.getSlot(1).getItem();
        if (s.isEmpty()) return null;
        return LootRarity.getMaterialRarity(s);
    }

    public int getDustCost(int slot) {
        return this.costs[0] * ++slot;
    }

    public int getMatCost(int slot) {
        return this.costs[1] * ++slot;
    }

    public int getLevelCost(int slot) {
        return this.costs[2] * ++slot;
    }

    public boolean needsReset() {
        return this.needsReset.get() != 0;
    }

    @Override
    public void slotsChanged(Container pContainer) {
        LootRarity rarity = this.getRarity();
        if (rarity != null) {
            ReforgingRecipe recipe = this.tile.getRecipeFor(rarity);
            if (recipe != null) {
                this.costs[0] = recipe.dustCost();
                this.costs[1] = recipe.matCost();
                this.costs[2] = recipe.levelCost();
            }
        }
        if (ReforgingMenu.this.needsReset()) {
            ReforgingMenu.this.needsReset.set(0);
        }
        super.slotsChanged(pContainer);
        this.tile.setChanged();
    }

}
