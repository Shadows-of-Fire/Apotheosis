package shadows.apotheosis.ench.table;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import shadows.apotheosis.ApotheosisObjects;

public class ApothEnchantTile extends EnchantingTableTileEntity {

	protected ItemStackHandler inv = new ItemStackHandler(1) {
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return Tags.Items.GEMS_LAPIS.contains(stack.getItem());
		};
	};

	public ApothEnchantTile() {

	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		super.write(tag);
		tag.put("inventory", inv.serializeNBT());
		return tag;
	}

	@Override
	public void fromTag(BlockState state, CompoundNBT tag) {
		super.fromTag(state, tag);
		inv.deserializeNBT(tag.getCompound("inventory"));
	}

	@Override
	public TileEntityType<?> getType() {
		return ApotheosisObjects.ENCHANTING_TABLE;
	}

	LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inv);

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return invCap.cast();
		return super.getCapability(cap, side);
	}

}
