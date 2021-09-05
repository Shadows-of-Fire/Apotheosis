package shadows.apotheosis.ench.library;

import java.util.List;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import shadows.apotheosis.ApotheosisObjects;
import shadows.placebo.net.MessageButtonClick.IButtonContainer;

public class EnchLibraryContainer extends Container implements IButtonContainer {

	protected World world;
	protected BlockPos pos;
	protected EnchLibraryTile tile = null;
	protected Inventory ioInv = new Inventory(2);
	protected Runnable notifier = null;

	public EnchLibraryContainer(int id, PlayerInventory inv, World world, BlockPos pos) {
		super(ApotheosisObjects.ENCH_LIB_CON, id);
		this.world = world;
		this.pos = pos;
		TileEntity te = world.getBlockEntity(pos);
		if (te instanceof EnchLibraryTile) this.tile = (EnchLibraryTile) te;
		if (world.isClientSide) this.tile.addListener(this);
		this.initCommon(inv);
	}

	@SuppressWarnings("deprecation")
	public EnchLibraryContainer(int id, PlayerInventory inv, BlockPos pos) {
		this(id, inv, DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().level), pos);
	}

	@Override
	public void removed(PlayerEntity player) {
		super.removed(player);
		if (!this.world.isClientSide) this.tile.removeListener(this);
		this.clearContainer(player, this.world, this.ioInv);
	}

	void initCommon(PlayerInventory inv) {
		this.addSlot(new Slot(this.ioInv, 0, 148, 47) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.getItem() == Items.ENCHANTED_BOOK;
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}

			@Override
			public void setChanged() {
				super.setChanged();
				if (!EnchLibraryContainer.this.world.isClientSide && !this.getItem().isEmpty()) {
					EnchLibraryContainer.this.tile.depositBook(this.getItem());
				}
				if (!this.getItem().isEmpty()) inv.player.level.playSound(inv.player, EnchLibraryContainer.this.pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.NEUTRAL, 0.5F, 0.8F);
				EnchLibraryContainer.this.ioInv.setItem(0, ItemStack.EMPTY);
			}
		});
		this.addSlot(new Slot(this.ioInv, 1, 148, 98) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.getItem() == Items.ENCHANTED_BOOK;
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 159 + i * 18));
			}
		}
		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(inv, k, 8 + k * 18, 217));
		}
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return player.distanceToSqr(this.pos.getX(), this.pos.getY(), this.pos.getZ()) < 16 * 16 && this.tile != null && !this.tile.isRemoved();
	}

	public int getNumStoredEnchants() {
		return (int) this.tile.getPointsMap().values().stream().filter(s -> s > 0).count();
	}

	public List<Object2ShortMap.Entry<Enchantment>> getPointsForDisplay() {
		return this.tile.getPointsMap().object2ShortEntrySet().stream().filter(s -> s.getShortValue() > 0).collect(Collectors.toList());
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index == 0) {
				if (!this.moveItemStackTo(itemstack1, 2, 38, true)) { return ItemStack.EMPTY; }
			} else if (index == 1) {
				if (!this.moveItemStackTo(itemstack1, 2, 38, true)) { return ItemStack.EMPTY; }
			} else if (itemstack1.getItem() == Items.LAPIS_LAZULI) {
				if (!this.moveItemStackTo(itemstack1, 1, 2, true)) { return ItemStack.EMPTY; }
			} else {
				if (this.slots.get(0).hasItem() || !this.slots.get(0).mayPlace(itemstack1)) { return ItemStack.EMPTY; }

				ItemStack itemstack2 = itemstack1.copy();
				itemstack2.setCount(1);
				itemstack1.shrink(1);
				this.slots.get(0).set(itemstack2);
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) { return ItemStack.EMPTY; }

			slot.onTake(player, itemstack1);
		}

		return itemstack;
	}

	public void setNotifier(Runnable r) {
		this.notifier = r;
	}

	public void onChanged() {
		if (this.notifier != null) this.notifier.run();
	}

	@Override
	public void onButtonClick(int id) {
		boolean shift = (id & 0x80000000) == 0x80000000;
		if (shift) id = id & 0x7FFFFFFF; //Look, if this ever breaks, it's not my fault someone has 2 billion enchantments.
		Enchantment ench = ((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getValue(id);
		ItemStack outSlot = this.ioInv.getItem(1);
		int curLvl = EnchantmentHelper.getEnchantments(outSlot).getOrDefault(ench, 0);
		int desired = shift ? this.tile.getMax(ench) : curLvl + 1;
		if (!this.tile.canExtract(ench, desired, curLvl)) return;
		if (outSlot.isEmpty()) outSlot = new ItemStack(Items.ENCHANTED_BOOK);
		this.tile.extractEnchant(outSlot, ench, desired);
		this.ioInv.setItem(1, outSlot);
	}
}
