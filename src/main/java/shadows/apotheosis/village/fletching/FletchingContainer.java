package shadows.apotheosis.village.fletching;

import java.util.Optional;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.village.VillageModule;

public class FletchingContainer extends Container {

	protected final CraftingInventory craftMatrix = new CraftingInventory(this, 1, 3);
	protected final CraftResultInventory craftResult = new CraftResultInventory();
	protected final World world;
	protected final BlockPos pos;
	protected final PlayerEntity player;

	public FletchingContainer(int id, PlayerInventory inv, World world, BlockPos pos) {
		super(ApotheosisObjects.FLETCHING, id);
		this.world = world;
		this.pos = pos;
		this.player = inv.player;
		this.addSlot(new FletchingResultSlot(inv.player, this.craftMatrix, this.craftResult, 0, 124, 35));

		for (int i = 0; i < 3; ++i) {
			this.addSlot(new Slot(this.craftMatrix, i, 48, 17 + i * 18));
		}

		for (int k = 0; k < 3; ++k) {
			for (int i1 = 0; i1 < 9; ++i1) {
				this.addSlot(new Slot(inv, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
			}
		}

		for (int l = 0; l < 9; ++l) {
			this.addSlot(new Slot(inv, l, 8 + l * 18, 142));
		}

	}

	@SuppressWarnings("deprecation")
	public FletchingContainer(int id, PlayerInventory inv) {
		this(id, inv, DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().world), BlockPos.ZERO);
	}

	@Override
	public void onCraftMatrixChanged(IInventory inventory) {
		if (!this.world.isRemote) {
			ServerPlayerEntity serverplayerentity = (ServerPlayerEntity) this.player;
			ItemStack itemstack = ItemStack.EMPTY;
			Optional<FletchingRecipe> optional = this.player.getServer().getRecipeManager().getRecipe(VillageModule.FLETCHING, this.craftMatrix, this.world);
			if (optional.isPresent()) {
				FletchingRecipe icraftingrecipe = optional.get();
				itemstack = icraftingrecipe.getCraftingResult(this.craftMatrix);
			}

			this.craftResult.setInventorySlotContents(0, itemstack);
			serverplayerentity.connection.sendPacket(new SSetSlotPacket(this.windowId, 0, itemstack));
		}
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		this.clearContainer(playerIn, this.world, this.craftMatrix);
	}

	@Override
	public boolean canInteractWith(PlayerEntity player) {
		return this.world.getBlockState(this.pos).getBlock() == Blocks.FLETCHING_TABLE && player.getDistanceSq(this.pos.getX(), this.pos.getY(), this.pos.getZ()) < 64;
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index == 0) {
				itemstack1.getItem().onCreated(itemstack1, this.world, playerIn);
				if (!this.mergeItemStack(itemstack1, 4, 40, true)) { return ItemStack.EMPTY; }
				slot.onSlotChange(itemstack1, itemstack);
			} else if (index >= 4 && index < 31) {
				if (!this.mergeItemStack(itemstack1, 31, 40, false)) { return ItemStack.EMPTY; }
			} else if (index >= 31 && index < 40) {
				if (!this.mergeItemStack(itemstack1, 4, 31, false)) { return ItemStack.EMPTY; }
			} else if (!this.mergeItemStack(itemstack1, 4, 40, false)) { return ItemStack.EMPTY; }

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) { return ItemStack.EMPTY; }

			ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);
			if (index == 0) {
				playerIn.dropItem(itemstack2, false);
			}
		}

		return itemstack;
	}

	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
		return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
	}

	protected class FletchingResultSlot extends CraftingResultSlot {

		protected FletchingResultSlot(PlayerEntity player, CraftingInventory inv, IInventory result, int slot, int x, int y) {
			super(player, inv, result, slot, x, y);
		}

		@Override
		public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
			this.onCrafting(stack);
			net.minecraftforge.common.ForgeHooks.setCraftingPlayer(thePlayer);
			NonNullList<ItemStack> nonnulllist = thePlayer.world.getRecipeManager().getRecipeNonNull(VillageModule.FLETCHING, FletchingContainer.this.craftMatrix, thePlayer.world);
			net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);
			for (int i = 0; i < nonnulllist.size(); ++i) {
				ItemStack itemstack = FletchingContainer.this.craftMatrix.getStackInSlot(i);
				ItemStack itemstack1 = nonnulllist.get(i);
				if (!itemstack.isEmpty()) {
					FletchingContainer.this.craftMatrix.decrStackSize(i, 1);
					itemstack = FletchingContainer.this.craftMatrix.getStackInSlot(i);
				}

				if (!itemstack1.isEmpty()) {
					if (itemstack.isEmpty()) {
						FletchingContainer.this.craftMatrix.setInventorySlotContents(i, itemstack1);
					} else if (ItemStack.areItemsEqual(itemstack, itemstack1) && ItemStack.areItemStackTagsEqual(itemstack, itemstack1)) {
						itemstack1.grow(itemstack.getCount());
						FletchingContainer.this.craftMatrix.setInventorySlotContents(i, itemstack1);
					} else if (!FletchingContainer.this.player.inventory.addItemStackToInventory(itemstack1)) {
						FletchingContainer.this.player.dropItem(itemstack1, false);
					}
				}
			}

			return stack;
		}

	}
}