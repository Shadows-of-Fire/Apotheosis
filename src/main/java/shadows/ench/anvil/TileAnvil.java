package shadows.ench.anvil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import shadows.ApotheosisObjects;

public class TileAnvil extends TileEntity {

	public TileAnvil() {
		super(ApotheosisObjects.ANVIL);
	}

	int unbreaking = 0;
	int splitting = 0;

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putInt("ub", unbreaking);
		tag.putInt("splitting", splitting);
		return super.write(tag);
	}

	@Override
	public void read(CompoundNBT tag) {
		super.read(tag);
		unbreaking = tag.getInt("ub");
		splitting = tag.getInt("splitting");
	}

	public void setUnbreaking(int level) {
		unbreaking = level;
	}

	public int getUnbreaking() {
		return unbreaking;
	}

	public void setSplitting(int level) {
		splitting = level;
	}

	public int getSplitting() {
		return splitting;
	}

}
