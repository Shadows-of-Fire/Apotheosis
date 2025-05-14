package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.base.Predicates;
import com.llamalad7.mixinextras.sugar.Local;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.util.IFestiveMarker;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

@Mixin(value = ItemStack.class, priority = 500, remap = false)
public abstract class ItemStackMixin implements IFestiveMarker {

    @Unique
    private boolean apoth_isFestiveMarked = false;

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    public void apoth_affixItemName(CallbackInfoReturnable<Component> cir) {
        ItemStack ths = (ItemStack) (Object) this;
        Component afxName = AffixHelper.getModifiedStackName(ths, cir.getReturnValue());
        if (afxName != null) {
            cir.setReturnValue(afxName);
        }

        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(ths);
        if (rarity.isBound()) {
            Component recolored = cir.getReturnValue().copy().withStyle(s -> s.withColor(rarity.get().color()));
            cir.setReturnValue(recolored);
        }
    }

    /**
     * Allows for the injection of item right-click behavior after evaluation of any existing item right-click behavior.
     * <p>
     * The existing {@link UseItemOnBlockEvent} only allows for injecting behavior before evaluating the item, which breaks user expectations.
     */
    @Inject(method = "useOn", at = @At("RETURN"), cancellable = true)
    public void apoth_useItemOnBlockPost(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir, @Local UseItemOnBlockEvent event) {
        if (!cir.getReturnValue().consumesAction() && !event.isCanceled()) {
            ItemStack s = (ItemStack) (Object) this;
            InteractionResult socketRes = SocketHelper.getGems(s).onItemUse(ctx);
            if (socketRes != null) {
                cir.setReturnValue(socketRes);
                return;
            }

            InteractionResult afxRes = AffixHelper.streamAffixes(s).map(afx -> afx.onItemUse(ctx)).filter(Predicates.notNull()).findFirst().orElse(null);
            if (afxRes != null) {
                cir.setReturnValue(afxRes);
                return;
            }
        }
    }

    @Shadow
    public abstract boolean isEmpty();

    @Override
    public boolean isMarked() {
        return !this.isEmpty() && this.apoth_isFestiveMarked;
    }

    @Override
    public void setMarked(boolean marked) {
        this.apoth_isFestiveMarked = marked;
    }

    @Inject(method = "copy", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    public void apoth_copyFestiveMarker(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack copy = cir.getReturnValue();
        if (this.isMarked()) {
            ((IFestiveMarker) (Object) copy).setMarked(true);
        }
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    public void apoth_tryTickMalice(Level level, Entity entity, int inventorySlot, boolean isCurrentItem, CallbackInfo ci) {
        ItemStack ths = (ItemStack) (Object) this;
        if (!level.isClientSide && ths.has(Apoth.Components.MALICE_MARKER) && entity instanceof Player player) {
            AffixHelper.applyMalice(player, ths);
            ths.remove(Components.MALICE_MARKER);
        }
    }
}
