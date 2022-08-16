package shadows.apotheosis.mixin;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import shadows.apotheosis.Apoth.Affixes;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
	public void apoth_affixItemName(CallbackInfoReturnable<Component> ci) {
		ItemStack ths = (ItemStack) (Object) this;
		CompoundTag afxData = ths.getTagElement(AffixHelper.AFFIX_DATA);
		if (afxData != null && afxData.contains(AffixHelper.NAME, 8)) {
			try {
				Component component = AffixHelper.getName(ths);
				if (component instanceof TranslatableComponent tComp) {
					tComp.getArgs()[1] = ci.getReturnValue();
					ci.setReturnValue(tComp);
				} else afxData.remove(AffixHelper.NAME);
			} catch (Exception exception) {
				afxData.remove(AffixHelper.NAME);
			}
		}
	}

	// Injects just before ItemStack.TooltipPart.MODIFIERS is written to the tooltip to remember where to rewind to.
	@Inject(method = "getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;", at = @At(value = "INVOKE", ordinal = 3, target = "shouldShowInTooltip"), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void apoth_tooltipMarker(@Nullable Player pPlayer, TooltipFlag pIsAdvanced, CallbackInfoReturnable<List<Component>> cir, List<Component> list) {
		if (Apotheosis.enableAdventure) list.add(new TextComponent("APOTH_REMOVE_MARKER"));
	}

	// Injects just before ItemStack.TooltipPart.MODIFIERS is written to the tooltip to remember where to rewind to.
	@Inject(method = "getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;", at = @At(value = "INVOKE", ordinal = 1, target = "hasTag"), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void apoth_tooltipMarker2(@Nullable Player pPlayer, TooltipFlag pIsAdvanced, CallbackInfoReturnable<List<Component>> cir, List<Component> list) {
		if (Apotheosis.enableAdventure) list.add(new TextComponent("APOTH_REMOVE_MARKER_2"));
	}

	// Actually applies the above, since mixin can't write back to params
	@ModifyVariable(at = @At(value = "INVOKE", target = "getDamageValue"), method = "hurt", argsOnly = true, ordinal = 0)
	public int swapDura(int amount, int amountCopy, Random pRandom, @Nullable ServerPlayer pUser) {
		int blocked = 0;
		AffixInstance inst = AffixHelper.getAffixes((ItemStack) (Object) this).get(Affixes.DURABLE);
		if (inst != null) {
			float chance = inst.level();
			for (int i = 0; i < amount; i++) {
				if (pRandom.nextFloat() <= chance) blocked++;
			}
		}
		return amount - blocked;
	}
}
