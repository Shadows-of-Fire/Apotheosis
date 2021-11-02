package shadows.apotheosis.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.IItemProvider;
import net.minecraft.world.World;
import shadows.apotheosis.util.INBTSensitiveFallingBlock;

@Mixin(FallingBlockEntity.class)
public abstract class MixinFallingBlockEntity extends Entity {

	public MixinFallingBlockEntity(EntityType<?> pType, World pLevel) {
		super(pType, pLevel);
		// TODO Auto-generated constructor stub
	}

	@Nullable
	@Override
	public ItemEntity spawnAtLocation(IItemProvider pItem) {
		if (pItem instanceof INBTSensitiveFallingBlock) {
			return ths().spawnAtLocation(((INBTSensitiveFallingBlock) pItem).toStack(ths().getBlockState(), ths().blockData), 0F);
		}
		return ths().spawnAtLocation(pItem, 0);
	}

	private FallingBlockEntity ths() {
		return ((FallingBlockEntity) (Object) this);
	}

}
