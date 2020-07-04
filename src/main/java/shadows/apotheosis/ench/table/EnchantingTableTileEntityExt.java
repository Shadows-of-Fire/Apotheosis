package shadows.apotheosis.ench.table;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.items.ItemStackHandler;
import shadows.apotheosis.ApotheosisObjects;

public class EnchantingTableTileEntityExt extends EnchantingTableTileEntity {

	protected ItemStackHandler inv = new ItemStackHandler(1);

	public EnchantingTableTileEntityExt() {

	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		super.write(tag);
		tag.put("inventory", inv.serializeNBT());
		return tag;
	}

	@Override
	public void read(CompoundNBT tag) {
		super.read(tag);
		inv.deserializeNBT(tag.getCompound("inventory"));
	}

	@Override
	public TileEntityType<?> getType() {
		return ApotheosisObjects.ENCHANTING_TABLE;
	}

}
