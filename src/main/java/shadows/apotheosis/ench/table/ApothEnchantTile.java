package shadows.apotheosis.ench.table;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import shadows.apotheosis.ApotheosisObjects;

public class ApothEnchantTile extends EnchantmentTableBlockEntity {

	protected ItemStackHandler inv = new ItemStackHandler(1) {
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return stack.is(Tags.Items.ENCHANTING_FUELS);
		}
	};

	public ApothEnchantTile(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		super.save(tag);
		tag.put("inventory", this.inv.serializeNBT());
		return tag;
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.inv.deserializeNBT(tag.getCompound("inventory"));
	}

	@Override
	public BlockEntityType<?> getType() {
		return ApotheosisObjects.ENCHANTING_TABLE;
	}

	LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> this.inv);

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return this.invCap.cast();
		return super.getCapability(cap, side);
	}

}