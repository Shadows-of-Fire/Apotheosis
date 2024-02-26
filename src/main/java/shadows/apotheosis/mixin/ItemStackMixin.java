package shadows.apotheosis.mixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
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
import shadows.apotheosis.ench.asm.EnchHooks;

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
                    int idx = "misc.apotheosis.affix_name.four".equals(tContents.getKey()) ? 2 : 1;
                    tContents.getArgs()[idx] = ci.getReturnValue();
                    ci.setReturnValue(component);
                }
                else afxData.remove(AffixHelper.NAME);
            }
            catch (Exception exception) {
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
     * Injects before the first call to {@link ItemStack#getDamageValue()} inside of {@link ItemStack#hurt(int, RandomSource, ServerPlayer)} to reduce durability
     * damage.
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

    @Unique
    private static final MutableComponent apotheosis$SPACE = Component.literal(" ");

    @Unique
    private static void appendModifiedEnchTooltip(List<Component> tooltip, Enchantment ench, int realLevel, int nbtLevel) {
        MutableComponent mc = ench.getFullname(realLevel).copy();
        mc.getSiblings().clear();
        Component nbtLevelComp = Component.translatable("enchantment.level." + nbtLevel);
        Component realLevelComp = Component.translatable("enchantment.level." + realLevel);
        if (realLevel != 1 || EnchHooks.getMaxLevel(ench) != 1) mc.append(apotheosis$SPACE).append(realLevelComp);

        int diff = realLevel - nbtLevel;
        char sign = diff > 0 ? '+' : '-';
        Component diffComp = Component.translatable("(%s " + sign + " %s)", nbtLevelComp, Component.translatable("enchantment.level." + Math.abs(diff))).withStyle(ChatFormatting.DARK_GRAY);
        mc.append(apotheosis$SPACE).append(diffComp);
        if (realLevel == 0) {
            mc.withStyle(ChatFormatting.DARK_GRAY);
        }
        tooltip.add(mc);
    }

    @Unique
    private static void foreachUniqueEnchantmentTag(ListTag tagEnchants, Consumer<CompoundTag> handleEnchantmentTag) {
        int tagSize = tagEnchants.size();
        List<CompoundTag> uniqueEnchantmentsReversed = new ArrayList<>(tagSize);
        Set<ResourceLocation> seenEnchantmentIds = new HashSet<>();

        for (int i = tagSize - 1; i >= 0 ; --i) {
            CompoundTag compoundTag = tagEnchants.getCompound(i);
            ResourceLocation enchantmentId = EnchantmentHelper.getEnchantmentId(compoundTag);
            if (seenEnchantmentIds.add(enchantmentId)) {
                uniqueEnchantmentsReversed.add(compoundTag);
            }
        }

        for (int i = uniqueEnchantmentsReversed.size() - 1; i >= 0 ; --i) {
            handleEnchantmentTag.accept(uniqueEnchantmentsReversed.get(i));
        }
    }

    /**
     * Rewrites the enchantment tooltip lines to include the effective level, as well as the (NBT + bonus) calculation.
     */
    @SuppressWarnings("deprecation")
    @Redirect(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;appendEnchantmentNames(Ljava/util/List;Lnet/minecraft/nbt/ListTag;)V"))
    public void apoth_enchTooltipRewrite(List<Component> tooltip, ListTag tagEnchants) {
        ItemStack ths = (ItemStack) (Object) this;
        Map<Enchantment, Integer> realLevels = new HashMap<>(ths.getAllEnchantments());
        foreachUniqueEnchantmentTag(tagEnchants, compoundtag -> {
            ForgeRegistries.ENCHANTMENTS.getDelegate(EnchantmentHelper.getEnchantmentId(compoundtag)).ifPresent(holder -> {
                Enchantment ench = holder.get();
                int nbtLevel = EnchantmentHelper.getEnchantmentLevel(compoundtag);
                Integer realLevel = realLevels.remove(ench);
                if (realLevel == null || nbtLevel == realLevel) {
                    // Default logic when levels are the same
                    tooltip.add(ench.getFullname(EnchantmentHelper.getEnchantmentLevel(compoundtag)));
                }
                else {
                    // Show the change vs nbt level
                    appendModifiedEnchTooltip(tooltip, ench, realLevel, nbtLevel);
                }
            });
        });
        // Show the tooltip for any modified enchantments not present in NBT.
        for (Map.Entry<Enchantment, Integer> real : realLevels.entrySet()) {
            if (real.getValue() > 0) appendModifiedEnchTooltip(tooltip, real.getKey(), real.getValue(), 0);
        }
    }
}
