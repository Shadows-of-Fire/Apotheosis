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
		this(id, inv, DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().level), BlockPos.ZERO);
	}

	@Override
	public void slotsChanged(IInventory inventory) {
		if (!this.world.isClientSide) {
			ServerPlayerEntity serverplayerentity = (ServerPlayerEntity) this.player;
			ItemStack itemstack = ItemStack.EMPTY;
			Optional<FletchingRecipe> optional = this.player.getServer().getRecipeManager().getRecipeFor(VillageModule.FLETCHING, this.craftMatrix, this.world);
			if (optional.isPresent()) {
				FletchingRecipe icraftingrecipe = optional.get();
				itemstack = icraftingrecipe.assemble(this.craftMatrix);
			}

			this.craftResult.setItem(0, itemstack);
			serverplayerentity.connection.send(new SSetSlotPacket(this.containerId, 0, itemstack));
		}
	}

	@Override
	public void removed(PlayerEntity playerIn) {
		super.removed(playerIn);
		this.clearContainer(playerIn, this.world, this.craftMatrix);
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return this.world.getBlockState(this.pos).getBlock() == Blocks.FLETCHING_TABLE && player.distanceToSqr(this.pos.getX(), this.pos.getY(), this.pos.getZ()) < 64;
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index == 0) {
				itemstack1.getItem().onCraftedBy(itemstack1, this.world, playerIn);
				if (!this.moveItemStackTo(itemstack1, 4, 40, true)) { return ItemStack.EMPTY; }
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (index >= 4 && index < 31) {
				if (!this.moveItemStackTo(itemstack1, 31, 40, false)) { return ItemStack.EMPTY; }
			} else if (index >= 31 && index < 40) {
				if (!this.moveItemStackTo(itemstack1, 4, 31, false)) { return ItemStack.EMPTY; }
			} else if (!this.moveItemStackTo(itemstack1, 4, 40, false)) { return ItemStack.EMPTY; }

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) { return ItemStack.EMPTY; }

			ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);
			if (index == 0) {
				playerIn.drop(itemstack2, false);
			}
		}

		return itemstack;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
		return slotIn.container != this.craftResult && super.canTakeItemForPickAll(stack, slotIn);
	}

	protected class FletchingResultSlot extends CraftingResultSlot {

		protected FletchingResultSlot(PlayerEntity player, CraftingInventory inv, IInventory result, int slot, int x, int y) {
			super(player, inv, result, slot, x, y);
		}

		@Override
		public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
			this.checkTakeAchievements(stack);
			net.minecraftforge.common.ForgeHooks.setCraftingPlayer(thePlayer);
			NonNullList<ItemStack> nonnulllist = thePlayer.level.getRecipeManager().getRemainingItemsFor(VillageModule.FLETCHING, FletchingContainer.this.craftMatrix, thePlayer.level);
			net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);
			for (int i = 0; i < nonnulllist.size(); ++i) {
				ItemStack itemstack = FletchingContainer.this.craftMatrix.getItem(i);
				ItemStack itemstack1 = nonnulllist.get(i);
				if (!itemstack.isEmpty()) {
					FletchingContainer.this.craftMatrix.removeItem(i, 1);
					itemstack = FletchingContainer.this.craftMatrix.getItem(i);
				}

				if (!itemstack1.isEmpty()) {
					if (itemstack.isEmpty()) {
						FletchingContainer.this.craftMatrix.setItem(i, itemstack1);
					} else if (ItemStack.isSame(itemstack, itemstack1) && ItemStack.tagMatches(itemstack, itemstack1)) {
						itemstack1.grow(itemstack.getCount());
						FletchingContainer.this.craftMatrix.setItem(i, itemstack1);
					} else if (!FletchingContainer.this.player.inventory.add(itemstack1)) {
						FletchingContainer.this.player.drop(itemstack1, false);
					}
				}
			}

			return stack;
		}

	}
}