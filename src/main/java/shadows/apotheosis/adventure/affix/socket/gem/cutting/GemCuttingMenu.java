package shadows.apotheosis.adventure.affix.socket.gem.cutting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.socket.gem.GemInstance;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.cap.InternalItemHandler;
import shadows.placebo.container.FilteredSlot;
import shadows.placebo.container.PlaceboContainerMenu;

public class GemCuttingMenu extends PlaceboContainerMenu {

	public static final List<GemCuttingRecipe> RECIPES = new ArrayList<>();

	static {
		RECIPES.add(new RarityUpgrade());
	}

	protected final Player player;
	protected final ContainerLevelAccess access;
	protected final InternalItemHandler invSlots = new InternalItemHandler(3);
	protected final ResultContainer result = new ResultContainer();
	protected GemCuttingRecipe recipe;

	public GemCuttingMenu(int id, Inventory inv) {
		this(id, inv, ContainerLevelAccess.NULL);
	}

	public GemCuttingMenu(int id, Inventory inv, ContainerLevelAccess access) {
		super(Apoth.Menus.GEM_CUTTING.get(), id, inv);
		this.player = inv.player;
		this.access = access;
		this.addSlot(new UpdatingSlot(this.invSlots, 0, 8, 35, stack -> stack.getItem() == Apoth.Items.GEM_DUST.get()));
		this.addSlot(new UpdatingSlot(this.invSlots, 1, 44, 35, stack -> GemItem.getGem(stack) != null));
		this.addSlot(new UpdatingSlot(this.invSlots, 2, 93, 35, this::isValidSecondItem));
		this.addSlot(new Slot(this.result, 3, 151, 35) {
			public void onTake(Player pPlayer, ItemStack pStack) {
				super.onTake(pPlayer, pStack);
				GemCuttingMenu.this.onCraft();
			}
		});

		this.addPlayerSlots(inv, 8, 84);
		this.mover.registerRule((stack, slot) -> slot == 3, this.playerInvStart, this.hotbarStart + 9);
		this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.getItem() == Apoth.Items.GEM_DUST.get(), 0, 1);
		this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && GemItem.getGem(stack) != null && this.invSlots.getStackInSlot(1).isEmpty(), 1, 2);
		this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && isValidSecondItem(stack), 2, 3);
		this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9);
		this.registerInvShuffleRules();
	}

	@Override
	public void slotsChanged(@Nullable Container pContainer) {
		int dust = this.invSlots.getStackInSlot(0).getCount();
		ItemStack gem = this.invSlots.getStackInSlot(1);
		ItemStack catalyst = this.invSlots.getStackInSlot(2);

		for (GemCuttingRecipe r : RECIPES) {
			if (dust >= r.getDustCost() && r.matches(gem, catalyst)) {
				this.recipe = r;
				this.result.setItem(0, r.getResult(gem, catalyst));
				return;
			}
		}
		this.recipe = null;
		this.result.setItem(0, ItemStack.EMPTY);
	}

	@Override
	public void onQuickMove(ItemStack original, ItemStack remaining, Slot slot) {
		super.onQuickMove(original, remaining, slot);
		if (remaining.isEmpty() && slot.index == 3) {
			this.onCraft();
		}
	}

	protected boolean isValidSecondItem(ItemStack stack) {
		ItemStack first = this.getSlot(1).getItem();
		if (first.isEmpty()) return false;
		GemInstance inst = GemInstance.unsocketed(first);
		GemInstance otherInst = GemInstance.unsocketed(stack);
		return otherInst.isValidUnsocketed() && otherInst.gem() == inst.gem();
	}

	protected void onCraft() {
		if (this.recipe != null) {
			this.invSlots.getStackInSlot(0).shrink(this.recipe.getDustCost());
			this.recipe.decrementInputs(this.invSlots.getStackInSlot(1), this.invSlots.getStackInSlot(2));
			this.level.playSound(player, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1, 1.5F + 0.35F * (1 - 2 * this.level.random.nextFloat()));
		} else {
			AdventureModule.LOGGER.error("Took an output from the gem cutting table without a set recipe!");
			Thread.dumpStack();
		}
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return access.evaluate((level, pos) -> level.getBlockState(pos).getBlock() == Apoth.Blocks.GEM_CUTTING_TABLE.get(), true);
	}

	@Override
	public void removed(Player pPlayer) {
		super.removed(pPlayer);
		this.access.execute((level, pos) -> {
			this.clearContainer(pPlayer, new RecipeWrapper(this.invSlots));
		});
	}

	protected class UpdatingSlot extends FilteredSlot {

		public UpdatingSlot(InternalItemHandler handler, int index, int x, int y, Predicate<ItemStack> filter) {
			super(handler, index, x, y, filter);
		}

		@Override
		public void setChanged() {
			super.setChanged();
			GemCuttingMenu.this.slotsChanged(null);
		}
	}

	public static interface GemCuttingRecipe {

		int getDustCost();

		boolean matches(ItemStack gem, ItemStack catalyst);

		ItemStack getResult(ItemStack gem, ItemStack catalyst);

		void decrementInputs(ItemStack gem, ItemStack catalyst);

	}

	public static class RarityUpgrade implements GemCuttingRecipe {

		@Override
		public int getDustCost() {
			return 4;
		}

		@Override
		public boolean matches(ItemStack gem, ItemStack catalyst) {
			GemInstance g = GemInstance.unsocketed(gem);
			GemInstance c = GemInstance.unsocketed(catalyst);
			return g.isValidUnsocketed() && c.isValidUnsocketed() && g.rarity() != LootRarity.ANCIENT && g.gem() == c.gem() && g.rarity() == c.rarity();
		}

		@Override
		public ItemStack getResult(ItemStack gem, ItemStack catalyst) {
			ItemStack out = gem.copy();
			GemItem.setLootRarity(out, GemItem.getLootRarity(out).next());
			return out;
		}

		@Override
		public void decrementInputs(ItemStack gem, ItemStack catalyst) {
			gem.shrink(1);
			catalyst.shrink(1);
		}

	}

}
