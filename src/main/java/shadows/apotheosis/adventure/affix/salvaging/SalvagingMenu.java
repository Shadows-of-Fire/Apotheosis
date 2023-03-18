package shadows.apotheosis.adventure.affix.salvaging;

import com.google.common.base.Predicates;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.cap.InternalItemHandler;
import shadows.placebo.container.FilteredSlot;
import shadows.placebo.container.PlaceboContainerMenu;

public class SalvagingMenu extends PlaceboContainerMenu {

	protected final Player player;
	protected final ContainerLevelAccess access;
	protected final InternalItemHandler invSlots = new InternalItemHandler(21);
	protected Runnable updateButtons;

	public SalvagingMenu(int id, Inventory inv) {
		this(id, inv, ContainerLevelAccess.NULL);
	}

	public SalvagingMenu(int id, Inventory inv, ContainerLevelAccess access) {
		super(Apoth.Menus.SALVAGE.get(), id, inv);
		this.player = inv.player;
		this.access = access;
		for (int i = 0; i < 15; i++) {
			this.addSlot(new FilteredSlot(this.invSlots, i, 8 + i % 5 * 18, 17 + i / 5 * 18, s -> AffixHelper.getRarity(s) != null) {
				@Override
				public void setChanged() {
					super.setChanged();
					if (SalvagingMenu.this.updateButtons != null) SalvagingMenu.this.updateButtons.run();
				}

				@Override
				public int getMaxStackSize() {
					return 1;
				}

				@Override
				public int getMaxStackSize(ItemStack stack) {
					return 1;
				}
			});
		}

		for (int i = 0; i < 6; i++) {
			this.addSlot(new FilteredSlot(this.invSlots, 15 + i, 134 + i % 2 * 18, 17 + i / 2 * 18, Predicates.alwaysFalse()) {
				@Override
				public void setChanged() {
					super.setChanged();
					if (SalvagingMenu.this.updateButtons != null) SalvagingMenu.this.updateButtons.run();
				}
			});
		}

		this.addPlayerSlots(inv, 8, 84);
		this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && AffixHelper.getRarity(stack) != null, 0, 15);
		this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9);
		this.registerInvShuffleRules();
	}

	@Override
	protected void addPlayerSlots(Inventory pInv, int x, int y) {
		this.playerInvStart = this.slots.size();
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				this.addSlot(new Slot(pInv, column + row * 9 + 9, x + column * 18, y + row * 18) {
					@Override
					public void setChanged() {
						super.setChanged();
						if (SalvagingMenu.this.updateButtons != null) SalvagingMenu.this.updateButtons.run();
					}
				});
			}
		}

		this.hotbarStart = this.slots.size();
		for (int row = 0; row < 9; row++) {
			this.addSlot(new Slot(pInv, row, x + row * 18, y + 58) {

				@Override
				public void setChanged() {
					super.setChanged();
					if (SalvagingMenu.this.updateButtons != null) SalvagingMenu.this.updateButtons.run();
				}

			});
		}
	}

	public void setButtonUpdater(Runnable r) {
		this.updateButtons = r;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return this.access.evaluate((level, pos) -> level.getBlockState(pos).getBlock() == Apoth.Blocks.SALVAGING_TABLE.get(), true);
	}

	@Override
	public void removed(Player pPlayer) {
		super.removed(pPlayer);
		this.access.execute((level, pos) -> {
			this.clearContainer(pPlayer, new RecipeWrapper(this.invSlots));
		});
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id == 0) {
			salvageAll();
			player.level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.99F, this.level.random.nextFloat() * 0.25F + 1F);
			player.level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.BLOCKS, 0.34F, this.level.random.nextFloat() * 0.2F + 0.8F);
			player.level.playSound(null, player.blockPosition(), SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.45F, this.level.random.nextFloat() * 0.5F + 0.75F);
			return true;
		}
		return super.clickMenuButton(player, id);
	}

	protected void giveItem(Player player, ItemStack stack) {
		if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer) player).hasDisconnected()) {
			player.drop(stack, false);
		} else {
			Inventory inventory = player.getInventory();
			if (inventory.player instanceof ServerPlayer) {
				inventory.placeItemBackInInventory(stack);
			}
		}
	}

	protected void salvageAll() {
		for (int i = 0; i < 15; i++) {
			Slot s = this.getSlot(i);
			ItemStack stack = s.getItem();
			ItemStack out = salvageItem(stack, this.player.random);
			LootRarity rarity = AffixHelper.getRarity(stack);
			s.set(ItemStack.EMPTY);
			if (out.isEmpty()) continue;
			out = this.invSlots.insertItem(rarity.ordinal() + 15, out, false);
			if (!out.isEmpty()) giveItem(this.player, out);
		}
	}

	public static int getSalvageCount(ItemStack stack, RandomSource rand) {
		int[] counts = getSalvageCounts(stack);
		return rand.nextInt(counts[0], counts[1] + 1);
	}

	public static int[] getSalvageCounts(ItemStack stack) {
		if (stack.isDamageableItem()) {
			int maxDmg = stack.getMaxDamage();
			float durability = maxDmg - stack.getDamageValue();
			int ratio = (int) (durability / maxDmg * 100);
			int max = ratio / 20 - 1;
			return new int[] { max <= 2 ? 0 : 1, Math.max(1, max) };
		}
		return new int[] { 1, 1 };
	}

	public static ItemStack salvageItem(ItemStack stack, RandomSource rand) {
		LootRarity rarity = AffixHelper.getRarity(stack);
		if (rarity == null) return ItemStack.EMPTY;
		ItemStack mat = new ItemStack(AdventureModule.RARITY_MATERIALS.get(rarity), getSalvageCount(stack, rand));
		return mat;
	}

}
