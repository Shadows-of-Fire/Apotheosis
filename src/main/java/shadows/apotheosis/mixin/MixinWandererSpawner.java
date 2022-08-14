package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.world.entity.npc.WanderingTraderSpawner;

@Mixin(WanderingTraderSpawner.class)
public class MixinWandererSpawner {

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 75))
	public int replaceMaxChance(int old) {
		return 101;
	}
}
