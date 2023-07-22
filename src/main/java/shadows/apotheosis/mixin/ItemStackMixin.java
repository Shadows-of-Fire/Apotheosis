package shadows.apotheosis.mixin;

import java.util.List;
import java.util.stream.DoubleStream;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import shadows.apotheosis.adventure.affix.AffixHelper;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
	public void apoth_affixItemName(CallbackInfoReturnable<Component> ci) {
		ItemStack ths = (ItemStack) (Object) this;
		CompoundTag afxData = ths.getTagElement(AffixHelper.AFFIX_DATA);
		if (afxData != null && afxData.contains(AffixHelper.NAME, 8)) {
			try {
				Component component = AffixHelper.getName(ths);
				if (component.getContents() instanceof TranslatableContents tContents) {
					int idx = tContents.getKey().equals("misc.apotheosis.affix_name.four") ? 2 : 1;
					tContents.getArgs()[idx] = ci.getReturnValue();
					ci.setReturnValue(component);
				} else afxData.remove(AffixHelper.NAME);
			} catch (Exception exception) {
				afxData.remove(AffixHelper.NAME);
			}
		}
	}

	// Injects just before ItemStack.TooltipPart.MODIFIERS is written to the tooltip to remember where to rewind to.
	@Inject(method = "getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;", at = @At(value = "INVOKE", ordinal = 3, target = "net/minecraft/world/item/ItemStack.shouldShowInTooltip(ILnet/minecraft/world/item/ItemStack$TooltipPart;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void apoth_tooltipMarker(@Nullable Player pPlayer, TooltipFlag pIsAdvanced, CallbackInfoReturnable<List<Component>> cir, List<Component> list) {
		list.add(Component.literal("APOTH_REMOVE_MARKER"));
	}

	// Injects just after ItemStack.TooltipPart.MODIFIERS is written to the tooltip to remember where to rewind to.
	@Inject(method = "getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;", at = @At(value = "INVOKE", ordinal = 1, target = "net/minecraft/world/item/ItemStack.hasTag()Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void apoth_tooltipMarker2(@Nullable Player pPlayer, TooltipFlag pIsAdvanced, CallbackInfoReturnable<List<Component>> cir, List<Component> list) {
		list.add(Component.literal("APOTH_REMOVE_MARKER_2"));
	}

	/**
	 * Injects before the first call to {@link ItemStack#getDamageValue()} inside of {@link ItemStack#hurt(int, RandomSource, ServerPlayer)} to reduce durability damage.
	 * Modifies the pAmount parameter, reducing it by the result of randomly rolling each point of damage against the block chance.
	 */
	@ModifyVariable(at = @At(value = "INVOKE", target = "net/minecraft/world/item/ItemStack.getDamageValue()I"), method = "hurt", argsOnly = true, ordinal = 0)
	public int swapDura(int amount, int amountCopy, RandomSource pRandom, @Nullable ServerPlayer pUser) {
		int blocked = 0;
		DoubleStream chances = AffixHelper.getAffixes((ItemStack) (Object) this).values().stream().mapToDouble(inst -> inst.getDurabilityBonusPercentage(pUser));
		double chance = chances.reduce(0, (res, ele) -> res + (1 - res) * ele);
		int delta = 1;
		if (chance < 0) {
			delta = -1;
			chance = -chance;
		}

		if (chance > 0) {
			for (int i = 0; i < amount; i++) {
				if (pRandom.nextFloat() <= chance) blocked += delta;
			}
		}
		return amount - blocked;
	}
}
