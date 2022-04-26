package shadows.apotheosis.mixin;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.common.MinecraftForge;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.ItemUseEvent;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.asm.DeadlyHooks;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
	public void apoth_getHoverName(CallbackInfoReturnable<Component> ci) {
		ItemStack ths = (ItemStack) (Object) this;
		CompoundTag afxData = ths.getTagElement(AffixHelper.AFFIX_DATA);
		if (afxData != null && afxData.contains("Name", 8)) {
			try {
				Component component = Component.Serializer.fromJson(afxData.getString("Name"));
				if (component instanceof TranslatableComponent tComp) {
					tComp.getArgs()[0] = ci.getReturnValue();
					ci.setReturnValue(tComp);
				} else afxData.remove("Name");
			} catch (Exception exception) {
				afxData.remove("Name");
			}
		}
	}

	@Inject(method = "getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;", at = @At(shift = Shift.BEFORE, value = "JUMP", ordinal = 9, opcode = Opcodes.IFEQ), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void getTooltipLines(@Nullable Player pPlayer, TooltipFlag pIsAdvanced, CallbackInfoReturnable<List<Component>> cir, List<Component> list) {
		if (Apotheosis.enableDeadly) list.add(new TextComponent("APOTH_REMOVE_MARKER"));
	}

	@Inject(method = "onItemUse", at=@At("RETURN"), cancellable = true, remap = false)
	public void apoth_onItemUse(UseOnContext ctx, Function<UseOnContext, InteractionResult> callback, CallbackInfoReturnable<InteractionResult> cir)
	{
		ItemUseEvent event = new ItemUseEvent(ctx);
		MinecraftForge.EVENT_BUS.post(event);
		if(event.isCanceled()) cir.setReturnValue(event.getCancellationResult());
	}
}
