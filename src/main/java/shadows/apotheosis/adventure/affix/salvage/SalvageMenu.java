package shadows.apotheosis.adventure.affix.salvage;

import java.util.Random;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public class SalvageMenu extends PlaceboContainerMenu {

	protected final Player player;
	protected final ContainerLevelAccess access;
	protected final InternalItemHandler inputSlots = new InternalItemHandler(1);
	protected Runnable updateButtons;

	public SalvageMenu(int id, Inventory inv) {
		this(id, inv, ContainerLevelAccess.NULL);
	}

	public SalvageMenu(int id, Inventory inv, ContainerLevelAccess access) {
		super(Apoth.Menus.SALVAGE, id, inv);
		this.player = inv.player;
		this.access = access;
		this.addSlot(new FilteredSlot(this.inputSlots, 0, 13, 36) {
			@Override
			public void setChanged() {
				super.setChanged();
				if (SalvageMenu.this.updateButtons != null) SalvageMenu.this.updateButtons.run();
			}
		});

		this.addPlayerSlots(inv, 8, 84);
		this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && AffixHelper.getRarity(stack) != null, 0, 1);
		this.mover.registerRule((stack, slot) -> slot == 0, this.playerInvStart, this.hotbarStart + 9);
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
						if (SalvageMenu.this.updateButtons != null) SalvageMenu.this.updateButtons.run();
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
					if (SalvageMenu.this.updateButtons != null) SalvageMenu.this.updateButtons.run();
				}

			});
		}
	}

	public void setButtonUpdater(Runnable r) {
		this.updateButtons = r;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return this.access.evaluate((level, pos) -> level.getBlockState(pos).getBlock() == Apoth.Blocks.SALVAGE_TABLE, true);
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
			ItemStack stack = this.getSlot(0).getItem();
			if (!stack.isEmpty()) {
				LootRarity rarity = AffixHelper.getRarity(stack);

				if (rarity != null) {
					ItemStack mat = new ItemStack(AdventureModule.RARITY_MATERIALS.get(rarity).get(), getSalvageCount(stack, player.random));
					this.getSlot(0).set(mat);
				}
			}
		}

		if (id > 0 && id <= 4) {
			switch (id) {
			case 1:
				salvageAll(LootRarity.COMMON);
				break;
			case 2:
				salvageAll(LootRarity.UNCOMMON);
				break;
			case 3:
				salvageAll(LootRarity.RARE);
				break;
			case 4:
				salvageAll(LootRarity.EPIC);
				break;
			}
		}

		if (id <= 4) {
			player.level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.99F, this.level.random.nextFloat() * 0.25F + 1F);
			player.level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.BLOCKS, 0.34F, this.level.random.nextFloat() * 0.2F + 0.8F);
			player.level.playSound(null, player.blockPosition(), SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.45F, this.level.random.nextFloat() * 0.5F + 0.75F);
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

	protected void salvageAll(LootRarity target) {
		for (int i = 1; i < this.slots.size(); i++) {
			Slot s = this.getSlot(i);
			ItemStack stack = s.getItem();
			LootRarity rarity = stack.hasTag() ? AffixHelper.getRarity(stack) : null;
			if (rarity == target) {
				ItemStack mat = new ItemStack(AdventureModule.RARITY_MATERIALS.get(rarity).get(), getSalvageCount(stack, player.random));
				giveItem(this.player, mat);
				stack.shrink(1);
			}
		}
	}

	protected int getSalvageCount(ItemStack stack, Random rand) {
		if (stack.isDamageableItem()) {
			int max = stack.getMaxDamage();
			int damage = stack.getDamageValue();
			float durability = max - damage;
			float ratio = durability / max;
			if (ratio >= 0.75F) {
				return 1 + rand.nextInt(3);
			} else if (ratio >= 0.5F) return 1 + rand.nextInt(2);
		}
		return 1;
	}

}
