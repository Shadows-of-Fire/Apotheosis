package dev.shadowsoffire.apotheosis.adventure.socket.gem.cutting;

import java.util.ArrayList;
import java.util.List;

import dev.shadowsoffire.apotheosis.advancements.AdvancementTriggers;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Blocks;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Items;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Menus;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.menu.PlaceboContainerMenu;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class GemCuttingMenu extends PlaceboContainerMenu {

    public static final int NEXT_MAT_COST = 1;
    public static final int STD_MAT_COST = 3;
    public static final int PREV_MAT_COST = 9;

    public static final List<GemCuttingRecipe> RECIPES = new ArrayList<>();

    static {
        RECIPES.add(new RarityUpgrade());
    }

    protected final Player player;
    protected final ContainerLevelAccess access;
    protected final InternalItemHandler inv = new InternalItemHandler(4){
        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? 1 : super.getSlotLimit(slot);
        };
    };

    public GemCuttingMenu(int id, Inventory playerInv) {
        this(id, playerInv, ContainerLevelAccess.NULL);
    }

    public GemCuttingMenu(int id, Inventory playerInv, ContainerLevelAccess access) {
        super(Menus.GEM_CUTTING.get(), id, playerInv);
        this.player = playerInv.player;
        this.access = access;
        this.addSlot(new UpdatingSlot(this.inv, 0, 62, 45, stack -> GemItem.getGem(stack).isBound()));
        this.addSlot(new UpdatingSlot(this.inv, 1, 90, 64, stack -> stack.getItem() == Items.GEM_DUST.get()));
        this.addSlot(new UpdatingSlot(this.inv, 2, 33, 64, this::matchesMainGem));
        this.addSlot(new UpdatingSlot(this.inv, 3, 62, 12, this::isValidMaterial));

        this.addPlayerSlots(playerInv, 8, 98);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.inv.getStackInSlot(0).isEmpty() && GemCuttingMenu.isValidMainGem(stack), 0, 1);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.getItem() == Items.GEM_DUST.get(), 1, 2);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.matchesMainGem(stack), 2, 3);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.isValidMaterial(stack), 3, 4);
        this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9);
        this.registerInvShuffleRules();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            ItemStack gem = this.inv.getStackInSlot(0);
            ItemStack left = this.inv.getStackInSlot(1);
            ItemStack bot = this.inv.getStackInSlot(2);
            ItemStack right = this.inv.getStackInSlot(3);
            for (GemCuttingRecipe r : RECIPES) {
                if (r.matches(gem, left, bot, right)) {
                    ItemStack out = r.getResult(gem, left, bot, right);
                    r.decrementInputs(gem, left, bot, right);
                    this.inv.setStackInSlot(0, out);
                    this.level.playSound(player, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1, 1.5F + 0.35F * (1 - 2 * this.level.random.nextFloat()));
                    AdvancementTriggers.GEM_CUT.trigger((ServerPlayer) player, out, AffixHelper.getRarity(out).getId());
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValidMainGem(ItemStack stack) {
        GemInstance inst = GemInstance.unsocketed(stack);
        return inst.isValidUnsocketed() && !inst.isMaxRarity();
    }

    protected boolean isValidMaterial(ItemStack stack) {
        var mainGem = GemInstance.unsocketed(this.inv.getStackInSlot(0));
        if (!mainGem.isValidUnsocketed()) return false;
        DynamicHolder<LootRarity> rarity = RarityRegistry.getMaterialRarity(stack.getItem());
        return rarity.isBound() && Math.abs(rarity.get().ordinal() - mainGem.rarity().get().ordinal()) <= 1;
    }

    protected boolean matchesMainGem(ItemStack stack) {
        var gem = GemInstance.unsocketed(stack);
        var mainGem = GemInstance.unsocketed(this.inv.getStackInSlot(0));
        return gem.isValidUnsocketed() && mainGem.isValidUnsocketed() && gem.gem() == mainGem.gem() && gem.rarity() == mainGem.rarity();
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.access.evaluate((level, pos) -> level.getBlockState(pos).getBlock() == Blocks.GEM_CUTTING_TABLE.get(), true);
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.access.execute((level, pos) -> {
            this.clearContainer(pPlayer, new RecipeWrapper(this.inv));
        });
    }

    public static interface GemCuttingRecipe {

        /**
         * Checks if this recipe matches the inputs
         *
         * @param gem   The gem in the primary slot.
         * @param left  The left input (Gem Dust).
         * @param bot   The bottom input (Second Gem).
         * @param right The right input (Rarity Materials).
         * @return If this recipe is valid for the inputs.
         */
        boolean matches(ItemStack gem, ItemStack left, ItemStack bot, ItemStack right);

        /**
         * Generates the result of this recipe.<br>
         * Calling this method when {@link #matches} return false is undefined behavior.
         *
         * @param gem   The gem in the primary slot.
         * @param left  The left input (Gem Dust).
         * @param bot   The bottom input (Second Gem).
         * @param right The right input (Rarity Materials).
         * @return A new copy of the output itemstack.
         */
        ItemStack getResult(ItemStack gem, ItemStack left, ItemStack bot, ItemStack right);

        /**
         * Reduces the count of the inputs, based on how many items should be consumed.<br>
         * Calling this method when {@link #matches} return false is undefined behavior.
         *
         * @param gem   The gem in the primary slot.
         * @param left  The left input (Gem Dust).
         * @param bot   The bottom input (Second Gem).
         * @param right The right input (Rarity Materials).
         */
        void decrementInputs(ItemStack gem, ItemStack left, ItemStack bot, ItemStack right);
    }

    public static class RarityUpgrade implements GemCuttingRecipe {

        @Override
        public boolean matches(ItemStack gem, ItemStack left, ItemStack bot, ItemStack right) {
            GemInstance g = GemInstance.unsocketed(gem);
            GemInstance g2 = GemInstance.unsocketed(bot);

            if (!g.isValidUnsocketed() || !g2.isValidUnsocketed() || g.gem() != g2.gem() || g.rarity() != g2.rarity()) return false;
            if (g.isMaxRarity()) return false;
            if (left.getItem() != Items.GEM_DUST.get() || left.getCount() < getDustCost(g.rarity().get())) return false;
            if (!RarityRegistry.isMaterial(right.getItem())) return false;

            DynamicHolder<LootRarity> matRarity = RarityRegistry.getMaterialRarity(right.getItem());
            DynamicHolder<LootRarity> gemRarity = g.rarity();
            if (matRarity == gemRarity) return right.getCount() >= STD_MAT_COST;
            else if (matRarity == RarityRegistry.next(gemRarity)) return right.getCount() >= NEXT_MAT_COST;
            else return matRarity == RarityRegistry.prev(gemRarity) && right.getCount() >= PREV_MAT_COST;
        }

        @Override
        public ItemStack getResult(ItemStack gem, ItemStack left, ItemStack bot, ItemStack right) {
            ItemStack out = gem.copy();
            AffixHelper.setRarity(out, RarityRegistry.next(AffixHelper.getRarity(out)).get());
            return out;
        }

        @Override
        public void decrementInputs(ItemStack gem, ItemStack left, ItemStack bot, ItemStack right) {
            DynamicHolder<LootRarity> matRarity = RarityRegistry.getMaterialRarity(right.getItem());
            DynamicHolder<LootRarity> gemRarity = GemInstance.unsocketed(gem).rarity();
            gem.shrink(1);
            left.shrink(getDustCost(gemRarity.get()));
            bot.shrink(1);
            right.shrink(matRarity == gemRarity ? STD_MAT_COST : matRarity == RarityRegistry.next(gemRarity) ? NEXT_MAT_COST : PREV_MAT_COST);
        }
    }

    public static int getDustCost(LootRarity gemRarity) {
        return 1 + gemRarity.ordinal() * 2;
    }
}
