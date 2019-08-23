package shadows.village.fletching;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import shadows.ApotheosisObjects;
import shadows.village.VillagerModule;

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
		this.addSlot(new CraftingResultSlot(inv.player, this.craftMatrix, this.craftResult, 0, 124, 35));

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

	public FletchingContainer(int id, PlayerInventory inv) {
		this(id, inv, DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().world), BlockPos.ZERO);
	}

	@Override
	public void onCraftMatrixChanged(IInventory inventory) {
		if (!world.isRemote) {
			ServerPlayerEntity serverplayerentity = (ServerPlayerEntity) player;
			ItemStack itemstack = ItemStack.EMPTY;
			Optional<FletchingRecipe> optional = player.getServer().getRecipeManager().getRecipe(VillagerModule.FLETCHING, craftMatrix, world);
			if (optional.isPresent()) {
				FletchingRecipe icraftingrecipe = optional.get();
				itemstack = icraftingrecipe.getCraftingResult(craftMatrix);
			}

			craftResult.setInventorySlotContents(0, itemstack);
			serverplayerentity.connection.sendPacket(new SSetSlotPacket(windowId, 0, itemstack));
		}
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		this.clearContainer(playerIn, world, this.craftMatrix);
	}

	@Override
	public boolean canInteractWith(PlayerEntity player) {
		return world.getBlockState(pos).getBlock() == Blocks.FLETCHING_TABLE && player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < 64;
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index == 0) {
				itemstack1.getItem().onCreated(itemstack1, world, playerIn);
				if (!this.mergeItemStack(itemstack1, 10 - 7, 46 - 7, true)) { return ItemStack.EMPTY; }
				slot.onSlotChange(itemstack1, itemstack);
			} else if (index >= 10 - 7 && index < 37 - 7) {
				if (!this.mergeItemStack(itemstack1, 37 - 7, 46 - 7, false)) { return ItemStack.EMPTY; }
			} else if (index >= 37 - 7 && index < 46 - 7) {
				if (!this.mergeItemStack(itemstack1, 10 - 7, 37 - 7, false)) { return ItemStack.EMPTY; }
			} else if (!this.mergeItemStack(itemstack1, 10 - 7, 46 - 7, false)) { return ItemStack.EMPTY; }

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
}