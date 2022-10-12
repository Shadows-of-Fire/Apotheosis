package shadows.apotheosis.adventure.affix.reforging;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootController;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.container.BlockEntityContainer;
import shadows.placebo.container.ContainerUtil;

public class ReforgingMenu extends BlockEntityContainer<ReforgingTableTile> {

	protected final Player player;
	protected SimpleContainer itemInv = new SimpleContainer(1) {
		@Override
		public void setChanged() {
			super.setChanged();
			ReforgingMenu.this.slotsChanged(this);
			if (ReforgingMenu.this.needsReset()) ReforgingMenu.this.needsReset.set(0);
		}
	};
	protected final Random random = new Random();
	protected final int[] seed = new int[2];
	protected DataSlot needsReset = DataSlot.standalone();

	public ReforgingMenu(int id, Inventory inv, BlockPos pos) {
		super(Apoth.Menus.REFORGING, id, inv, pos);
		this.player = inv.player;
		this.addSlot(new Slot(this.itemInv, 0, 25, 24));
		this.addSlot(new SlotItemHandler(this.tile.inv, 0, 15, 45));
		this.addSlot(new SlotItemHandler(this.tile.inv, 1, 35, 45));
		this.addPlayerSlots(inv, 8, 84);
		this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && LootCategory.forItem(stack) != LootCategory.NONE, 0, 1);
		this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && isRarityMat(stack), 1, 2);
		this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.getItem() == Apoth.Items.GEM_DUST, 2, 3);
		this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9);
		this.registerInvShuffleRules();

		updateSeed();
		this.addDataSlot(needsReset);
		this.addDataSlot(DataSlot.shared(seed, 0));
		this.addDataSlot(DataSlot.shared(seed, 1));
	}

	@Override
	public void removed(Player pPlayer) {
		super.removed(pPlayer);
		this.clearContainer(pPlayer, itemInv);
	}

	protected void updateSeed() {
		int seed = player.getEnchantmentSeed();
		this.seed[0] = ContainerUtil.split(seed, false);
		this.seed[1] = ContainerUtil.split(seed, true);
	}

	public int getSeed() {
		return ContainerUtil.merge(seed[0], seed[1], true);
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id >= 0 && id < 3) {

			ItemStack input = this.getSlot(0).getItem();
			LootRarity rarity = this.getRarity();
			if (rarity == null || input.isEmpty() || this.needsReset()) return false;

			int dust = this.getDustCount();
			int mats = this.getMatCount();
			int cost = (id + 1) * 2;
			int levels = this.player.experienceLevel;
			int levelCost = this.getLevelCost(id, rarity);

			if ((dust < cost || mats < cost || levels < levelCost) && !player.isCreative()) return false;

			if (!player.level.isClientSide) {
				ItemStack[] choices = new ItemStack[3];

				Random rand = this.random;
				for (int i = 0; i < 3; i++) {
					rand.setSeed(this.getSeed() ^ input.getItem().getRegistryName().hashCode() + i);
					choices[i] = LootController.createLootItem(input.copy(), rarity, rand);
				}

				ItemStack out = choices[id];
				this.getSlot(0).set(out);
				if (!player.isCreative()) {
					this.getSlot(1).getItem().shrink(cost);
					this.getSlot(2).getItem().shrink(cost);
				}
				player.onEnchantmentPerformed(out, cost);
				updateSeed();
				this.needsReset.set(1);
			}

			player.playSound(SoundEvents.EVOKER_CAST_SPELL, 0.99F, this.level.random.nextFloat() * 0.25F + 1F);
			player.playSound(SoundEvents.AMETHYST_CLUSTER_STEP, 0.34F, this.level.random.nextFloat() * 0.2F + 0.8F);
			player.playSound(SoundEvents.SMITHING_TABLE_USE, 0.45F, this.level.random.nextFloat() * 0.5F + 0.75F);
			return true;
		}
		return super.clickMenuButton(player, id);
	}

	public static boolean isRarityMat(ItemStack stack) {
		return AdventureModule.RARITY_MATERIALS.containsValue(stack.getItem().delegate);
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
		return AdventureModule.RARITY_MATERIALS.inverse().get(s.getItem().delegate);
	}

	public int getDustCost(int slot, LootRarity rarity) {
		return (1 + rarity.ordinal()) + Math.max(1, rarity.ordinal() / 2) * slot;
	}

	public int getMatCost(int slot, LootRarity rarity) {
		return (1 + rarity.ordinal()) + Math.max(1, rarity.ordinal() / 2) * slot;
	}

	public int getLevelCost(int slot, LootRarity rarity) {
		return (1 + rarity.ordinal()) * ++slot * 5;
	}

	public boolean needsReset() {
		return this.needsReset.get() != 0;
	}

}
