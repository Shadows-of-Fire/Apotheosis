package shadows.apotheosis.adventure.affix.reforging;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootController;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.cap.InternalItemHandler;
import shadows.placebo.container.FilteredSlot;
import shadows.placebo.container.PlaceboContainerMenu;
import shadows.placebo.container.QuickMoveHandler;

public class ReforgingMenu extends PlaceboContainerMenu {

	protected final Player player;
	protected final ContainerLevelAccess access;
	protected final ResultContainer resultSlots = new ResultContainer();
	protected final InternalItemHandler inputSlots = new InternalItemHandler(4) {
		public void onContentsChanged(int slot) {
			ReforgingMenu.this.slotsChanged(null);
		}
	};
	protected final QuickMoveHandler subMover = new QuickMoveHandler();

	public ReforgingMenu(int id, Inventory inv) {
		this(id, inv, ContainerLevelAccess.NULL);
	}

	public ReforgingMenu(int id, Inventory inv, ContainerLevelAccess access) {
		super(Apoth.Menus.REFORGING, id, inv);
		this.player = inv.player;
		this.access = access;
		this.addSlot(new FilteredSlot(this.inputSlots, 0, 8, 21, ReforgingMenu::isRarityMat));
		this.addSlot(new FilteredSlot(this.inputSlots, 1, 8, 49, stack -> this.getSlot(0).getItem().getItem() == stack.getItem()));
		this.addSlot(new FilteredSlot(this.inputSlots, 2, 44, 35, stack -> stack.getItem() == Apoth.Items.GEM_DUST));
		this.addSlot(new FilteredSlot(this.inputSlots, 3, 81, 35, stack -> LootCategory.forItem(stack) != LootCategory.NONE));
		this.addSlot(new Slot(this.resultSlots, 0, 148, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public boolean mayPickup(Player player) {
				return true;
			}

			@Override
			public void onTake(Player player, ItemStack stack) {

			}
		});

		this.addPlayerSlots(inv, 8, 84);

		this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && isRarityMat(stack), 0, 2);
		this.subMover.registerRule((stack, slot) -> slot >= this.playerInvStart && isRarityMat(stack), 1, 2, true);
		this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.getItem() == Apoth.Items.GEM_DUST, 2, 3);
		this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && LootCategory.forItem(stack) != null, 3, 4);
		this.mover.registerRule((stack, slot) -> slot <= 4, this.playerInvStart, this.hotbarStart + 9);

		this.registerInvShuffleRules();
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		ItemStack item = this.getSlot(pIndex).getItem();
		ItemStack topMat = this.getSlot(0).getItem();
		if (topMat.isEmpty() && isRarityMat(item) && pIndex >= this.playerInvStart && item.getCount() > 1) {
			ItemStack copy = item.copy();
			item.setCount(item.getCount() / 2);
			copy.shrink(item.getCount());
			super.quickMoveStack(pPlayer, pIndex);
			this.getSlot(pIndex).set(copy);
			return subMover.quickMoveStack(this, pPlayer, pIndex);
		}

		return super.quickMoveStack(pPlayer, pIndex);
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return this.access.evaluate((level, pos) -> level.getBlockState(pos).getBlock() == Apoth.Blocks.REFORGING_TABLE, true);
	}

	@Override
	public void removed(Player pPlayer) {
		super.removed(pPlayer);
		this.access.execute((level, pos) -> {
			this.clearContainer(pPlayer, new RecipeWrapper(this.inputSlots));
		});
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id == 0) {
			ItemStack leftMat = this.getSlot(0).getItem();
			ItemStack rightMat = this.getSlot(1).getItem();
			ItemStack dust = this.getSlot(2).getItem();
			ItemStack item = this.getSlot(3).getItem();

			if (!leftMat.isEmpty() && leftMat.getItem() == rightMat.getItem()) {
				LootRarity rarity = AdventureModule.RARITY_MATERIALS.inverse().get(leftMat.getItem().delegate);
				if (rarity == null || dust.getCount() < rarity.ordinal() + 1) return false;
				ItemStack output = item.copy();
				output.getOrCreateTag().remove(AffixHelper.AFFIX_DATA);
				output = LootController.createLootItem(output, rarity, player.random);
				this.resultSlots.setItem(0, output);
				leftMat.shrink(1);
				rightMat.shrink(1);
				dust.shrink(rarity.ordinal() + 1);
				this.inputSlots.setStackInSlot(3, ItemStack.EMPTY);
				player.level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.99F, this.level.random.nextFloat() * 0.25F + 1F);
				player.level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.BLOCKS, 0.34F, this.level.random.nextFloat() * 0.2F + 0.8F);
				player.level.playSound(null, player.blockPosition(), SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.45F, this.level.random.nextFloat() * 0.5F + 0.75F);
			}
		}
		return super.clickMenuButton(player, id);
	}

	public static boolean isRarityMat(ItemStack stack) {
		return AdventureModule.RARITY_MATERIALS.containsValue(stack.getItem().delegate);
	}

}
